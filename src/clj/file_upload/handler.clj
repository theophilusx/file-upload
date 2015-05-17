(ns file-upload.handler
  (:require [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [prone.middleware :refer [wrap-exceptions]]
            [cheshire.core :refer [generate-string]]
            [environ.core :refer [env]]
            [clojure.java.io :as io]))

(def home-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     [:div#app]
     (include-js "js/app.js")]]))

(defn handle-upload [req]
  (let [params (:params req)]
    {:status 200
     :headers {"Content-Type" "applicaiton/json"}
     :body (generate-string
            {:filename (get-in params [:upload-file :filename])
             :size (get-in params [:upload-file :size])
             :tempfile (str (get-in params [:upload-file :tempfile]))})}))

(defroutes routes
  (GET "/" [] home-page)
  (POST "/upload" [] handle-upload)
  (resources "/")
  (not-found "Not Found"))

(def app
  ;; We turn off CSRF protection for this recipe to keep things simple
  ;; You can use CSRF protection with Ajax/API type applications
  ;; but you must add a mechanism for the client to get the current
  ;; session anti-forgery toekn, which you then pass back in a header when
  ;; doing AJAX/API calls
  ;; We add the wrap-restful-format middleware because it just makes things
  ;; simple.
  (-> routes
      wrap-restful-format
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      wrap-exceptions))

