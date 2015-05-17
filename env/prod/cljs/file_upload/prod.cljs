(ns file-upload.prod
  (:require [file-upload.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
