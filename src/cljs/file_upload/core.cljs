(ns file-upload.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [POST]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType])
  (:import goog.History
           goog.net.IframeIo
           [goog.net.EventType :as NetEventType]))

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
  (let [class "fa fa-spinner fa-spin fa-pulse"]
    (session/put! :upload-status [:div 
                                  [:p "Uploading file... "
                                   [:span {:class class}]]])))

(defn into-list [items]
  (into [:ul]
        (for [i items]
          [:li i])))

(defn set-status [class title items]
  [:div {:class class}
   [:h4 title]
   (into-list items)])

;;; cljs-ajax upload routines
(defn handle-response-ok [resp]
  (let [rsp (js->clj resp :keywordize-keys true)
        status (set-status "alert alert-success"
                           "Upload Successful"
                           [(str "Filename: " (:filename rsp))
                            (str "Size: " (:size rsp))
                            (str "Tempfile: " (:tempfile rsp))])]
    (session/put! :upload-status status)))

(defn handle-response-error [ctx]
  (let [rsp (js->clj (:response ctx) :keywordize-keys true)
        status (set-status "alert alert-danger"
                           "Upload Failure"
                           [(str "Status: " (:status ctx) " "
                                 (:status-text ctx))
                            (str (:message rsp))])]
    (.log js/console (str "cljs-ajax error: " status))
    (session/put! :upload-status status)))

(defn cljs-ajax-upload-file [element-id]
  (let [el (.getElementById js/document element-id)
        name (.-name el)
        file (aget (.-files el) 0)
        form-data (doto
                      (js/FormData.)
                    (.append name file))]
    (POST "/upload" {:params form-data
                     :response-format :json
                     :keywords? true
                     :handler handle-response-ok
                     :error-handler handle-response-error})
    (set-upload-indicator)))

(defn cljs-ajax-upload-button []
  [:div
   [:hr]
   [:button {:class "btn btn-primary" :type "button"
             :on-click #(cljs-ajax-upload-file "upload-file")}
    "Upload using cljs-ajax  " [:span {:class "fa fa-upload"}]]])

;;; goog.net.IFrameIO routines
(defn iframe-response-ok [msg]
  (let [status (set-status "alert alert-success"
                           "Upload Successful"
                           [(str "Filename: " (:filename msg))
                            (str "Size: " (:size msg))
                            (str "Tempfile: " (:tempfile msg))])]
    (session/put! :upload-status status)))

(defn iframe-response-error [msg]
  (let [status (set-status "alert alert-danger"
                           "Upload Failure"
                           [(str "Status: " (:status msg))
                            (str (:message msg))])]
    (session/put! :upload-status status)))

(defn handle-iframe-response [json-msg]
  (let [msg (js->clj json-msg :keywordize-keys true)]
    (.log js/console (str "iframe-response: " msg))
    (cond
      (= "OK" (:status msg)) (iframe-response-ok msg)
      (= "ERROR" (:status msg)) (iframe-response-error msg)
      :else (session/put! :upload-status [:div.alert.alert-danger
                                          [:h4 "Unexpected Error"]
                                          [:ul
                                           [:li (str "Status: " (:status msg))]
                                           [:li (:message msg)]]]))))
;;; Stole this from Dmitri Sotnikov - thanks.
;;; Original code is at https://github.com/yogthos
(defn iframeio-upload-file [form-id]
  (let [el (.getElementById js/document form-id)
        iframe (IframeIo.)]
    (events/listen iframe NetEventType.COMPLETE
                   (fn [event]
                     (let [rsp (.getResponseJson iframe)
                           status ()])
                     (handle-iframe-response (.getResponseJson iframe))
                     (.dispose iframe)))
    (set-upload-indicator)
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
