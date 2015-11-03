(ns instasave.background
  (:require [instasave.core :as core]))

(defn run []
  (enable-console-print!)
  (println "Hello, world")
  (println (core/foo)))
