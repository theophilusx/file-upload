(ns file-upload.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [POST]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType])
  (:import goog.History
           goog.net.IframeIo
           goog.net.EventType
           [goog.events EventType]))

;; -------------------------
;; Views

(defn status-component []
  (if-let [status (session/get :upload-status)]
    [:div
     [:h3 "Status"]
     status]))

(defn upload-component []
  [:div
   [:form {:id "upload-form"
           :enc-type "multipart/form-data"
           :method "POST"}
    [:labe "Upload Filename: "]
    [:input {:type "file"
             :name "upload-file"
             :id "upload-file"}]]])

(defn set-upload-indicator []
  (session/put! :upload-status [:div 
                                [:h4 "Upload In Progress"]
                                [:p
                                 [:span {:class "fa fa-spinner fa-spin fa-pulse fa-5x"}]]
                                [:p]]))

;;; cljs-ajax upload routines
(defn handle-response-ok [resp]
  (let [rsp (js->clj resp)]
    (session/put! :upload-status [:div {:class "alert alert-success"}
                                  [:h4 "Upload Successful"]
                                  [:ul
                                   [:li "Filename: " (get rsp "filename")]
                                   [:li "Size: " (str (get rsp "size") " bytes")]
                                   [:li "Tempfile: " (get rsp "tempfile")]]])))

(defn handle-response-error [ctx]
  (let [rsp (js->clj (:response ctx))]
    (.log js/console (str "cljs-ajax error: " (:status ctx) " " (:status-text ctx)
                          " " (get rsp "message")))
    (session/put! :upload-status [:div {:class "alert alert-danger"}
                                  [:h4 "Upload Failure"]
                                  [:ul
                                   [:li (str (:status ctx) " " (:status-text ctx))]
                                   [:li (get rsp "message")]]])))

(defn cljs-ajax-upload-file [element-id]
  (let [el (.getElementById js/document element-id)
        name (.-name el)
        f (aget (.-files el) 0)
        form-data (doto
                      (js/FormData.)
                    (.append name f))]
    (POST "/upload" {:params form-data
                     :response-format :json
                     :handler handle-response-ok
                     :error-handler handle-response-error})
    (set-upload-indicator)))

(defn cljs-ajax-upload-button []
  [:div
   [:hr]
   [:button {:class "btn btn-primary" :type "button"
             :on-click #(cljs-ajax-upload-file "upload-file")}
    "Upload using cljs-ajax"]])

;;; goog.net.IFrameIO routines
(defn iframe-response-ok [json-msg]
  (let [msg (js->clj json-msg)]
    (session/put! :upload-status [:div {:class "alert alert-success"}
                                  [:h4 "Upload Success"]
                                  [:ul
                                   [:li "Filename: " (get msg "filename")]
                                   [:li "Size: " (str (get msg "size") " bytes")]
                                   [:li "Tempfile: " (get msg "tempfile")]]])))

(defn iframe-response-error [json-msg]
  (let [msg (js->clj json-msg :keywordize-keys true)]
    (.log js/console (str "iframe-response-error: " msg))
    (session/put! :upload-status [:div {:class "alert alert-danger"}
                                  [:h4 "Upload Failure"]
                                  [:ul
                                   [:li (str "Status " (:status msg))]
                                   [:li (:message msg)]]])))

;;; Stole this from Dmitri Sotnikov - thanks.
;;; Original code is at https://github.com/yogthos
(defn iframeio-upload-file [form-id]
  (let [el (.getElementById js/document form-id)
        iframe (IframeIo.)]
    (events/listen iframe goog.net.EventType.SUCCESS
                   (fn [event]
                     (.log js/console "Success event fired")
                     (iframe-response-ok (.getResponseJson iframe))))
    (events/listen iframe goog.net.EventType.ERROR
                   (fn [event]
                     (.log js/console "Error event fired")
                     (iframe-response-error (.getResponseJson iframe))))
    (events/listen iframe goog.net.EventType.COMPLETE
                   (fn [event]
                     (.log js/console "Complete event fired")))
    (.setErrorChecker iframe #(= "ERROR" (get (js->clj (.getResponseJson iframe)) "status")))
    (.sendFromForm iframe el "/upload")))

(defn iframeio-upload-button []
  [:div
   [:hr]
   [:button {:class "btn btn-primary"
             :type "button"
             :on-click #(iframeio-upload-file "upload-form")}
    "Upload Using IFrameIO " [:span {:class "fa fa-upload"}]]])

(defn home-page []
  (fn []
    [:div [:h2 "Welcome to file-upload"]
     [:p "This provides an example of different methods to upload a "
      "file to the server fro a Reagent based client."]
     [status-component]
     [upload-component]
     [cljs-ajax-upload-button]
     [iframeio-upload-button]
     [:hr]
     [:div [:a {:href "#/about"} "go to about page"]]]))

(defn about-page []
  [:div [:h2 "About file-upload"]
   [:div [:a {:href "#/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))


;; -------------------------
;; Initialize app
(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))
