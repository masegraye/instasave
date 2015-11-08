(ns instasave.background
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [instasave.core :as core]
            [cljs.core.async :refer [chan <! put!]]))

(def gram-bomb-file "js/gen/gram_bomb.js")

(def cljs-base-file "js/gen/cljs_base.js")

(defn run []
  (enable-console-print!)
  (println "Hello, world")
  (println (core/foo)))

(defn -inject-js [src]
  (let [c (chan)]
    (go
      (.. js/chrome
          -tabs
          (executeScript nil
                         #js {:file src :runAt "document_idle"}
                         #(put! c true))))
    c))


(defn ^:export -test-inject []
  (go
    (<! (-inject-js cljs-base-file))
    (<! (-inject-js gram-bomb-file))))
