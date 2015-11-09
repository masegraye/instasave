(ns instasave.gram-bomb
  (:require [instasave.core :as core]
            [taoensso.timbre :as log]
            [cljs.core.async :refer [put!]]))

(def modal-photo-css-selector ".-cx-PRIVATE-Modal__contents .-cx-PRIVATE-Photo__image")

(def post-page-photo-css-selector ".-cx-PRIVATE-PostPage__container .-cx-PRIVATE-Photo__image")

(declare -register-platform-callbacks!)

(defn run []
  (log/debug "Staring gram-bomb")
  (-register-platform-callbacks!))

(declare -on-runtime-message)

(defn -register-platform-callbacks! []
  (.. js/chrome
      -runtime
      -onMessage
      (addListener -on-runtime-message)))

(declare -get-photo)

(defn -on-runtime-message [js-msg sender respond]
  (log/debug "Got message from runtime")
  (.. js/console (log js-msg))
  (let [msg (js->clj js-msg :keywordize-keys true)]
    (log/debug "Converted msg" msg)
    (if (= (:event-id msg)
           "get-photo-url")
      (as-> (-get-photo) $
        (hash-map :photo-url $)
        (clj->js $)
        (respond $))
      (respond nil))))

(defn -get-photo []
  (letfn [(find-in-dom [css]
            (some-> (.. js/document (querySelector css))
                    (.-src)))]
    (or (find-in-dom modal-photo-css-selector)
        (find-in-dom post-page-photo-css-selector))))
