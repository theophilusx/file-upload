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
     [:h3 "Upload Status"]
     [:p "The file was uploaded and the server returned the following"]
     [:p status]]))

(defn error-component []
  (if-let [err (session/get :upload-error)]
    [:div
     [:h3 "Upload Error"]
     [:p "The file upload has failed. The following error was detected"]
     [:p err]]))

(defn upload-component []
  [:div
   [:form {:id "upload-form"
           :enc-type "multipart/form-data"
           :method "POST"}
    [:labe "Upload Filename: "]
    [:input {:type "file"
             :name "upload-file"
             :id "upload-file"}]]
   [:hr]])


(defn handle-response-ok [resp]
  (let [rsp (js->clj resp)]
    (.log js/console (str "Response OK: " rsp))
    (session/put! :upload-status [:ul
                                  [:li "Filename: " (get rsp "filename")]
                                  [:li "Size: " (str (get rsp "size") " bytes")]
                                  [:li "Tempfile: " (get rsp "tempfile")]])
    (session/put! :upload-error nil)))

;;; cljs-ajax upload routines
(defn handle-response-error [ctx]
  (.log js/console (str "Response ERROR: " ctx))
  (session/put! :upload-error (str ctx))
  (session/put! :upload-status nil))

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
                     :error-handler handle-response-error
                     :timeout 1000})))

(defn cljs-ajax-upload-button []
  [:div
   [:button {:type "button"
             :on-click #(cljs-ajax-upload-file "upload-file")}
    "Upload using cljs-ajax"]])

;;; goog.net.IFrameIO routines

(defn home-page []
  (fn []
    [:div [:h2 "Welcome to file-upload"]
     [:p "This provides an example of different methods to upload a "
      "file to the server fro a reagent based SPA."]
     [status-component]
     [error-component]
     [upload-component]
     [cljs-ajax-upload-button]
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
