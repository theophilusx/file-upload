(ns file-upload.handler
  (:require [clojure.java.io :as io]
            [compojure.core :refer [GET POST defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [prone.middleware :refer [wrap-exceptions]]
            [cheshire.core :refer [generate-string]]
            [environ.core :refer [env]]
            [clojure.java.io :as io]))

(defn handle-upload [{:keys [filename size tempfile]}]
  (println (str "handle-upload: Filename: " filename " size: " size
                " tempfile: " (str tempfile)))
  (cond
    (= "" filename) {:status 400
                     :headers {"Content-Type" "application/json"}
                     :body (generate-string {:status "ERROR"
                                             :message "No file parameter sent"})}
    (< size 100) {:status 400
                  :headers {"Content-Type" "application/json"}
                  :body (generate-string {:status "ERROR"
                                          :message (str "File less than 100 bytes"
                                                        " - Can't be bothered")})}
    (>= size 100) {:status 200
                   :headers {"Content-Type" "application/json"}
                   :body (generate-string {:status "OK"
                                           :filename filename
                                           :size size
                                           :tempfile (str tempfile)})}
    :else {:status 400
           :headers {"Content-Type" "application/json"}
           :body (generate-string {:status "ERROR"
                                   :message "Unexpected Error"})}))

(defroutes routes
  (GET "/" [] (slurp (io/resource "public/index.html")))
  (POST "/upload" [upload-file] (handle-upload upload-file))
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

