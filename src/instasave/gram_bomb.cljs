(ns instasave.gram-bomb
  (:require [instasave.core :as core]))

(defn run []
  (enable-console-print!)
  (println "Hello from the content script context")
  (println (core/foo)))
