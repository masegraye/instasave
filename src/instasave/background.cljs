(ns instasave.background
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [instasave.core :as core]
            [cljs.core.async :refer [chan <! put!]]
            [cemerick.url :refer [url]]
            [taoensso.timbre :as log]
            [clojure.string :as s]))

(def gram-bomb-file "js/gen/gram_bomb.js")

(def cljs-base-file "js/gen/cljs_base.js")

(def instagram-host "instagram.com")

(declare -register-platform-callbacks!)

(defn run []
  (log/debug "Background page is loaded")
  (-register-platform-callbacks!))

(declare -photo-path?)

(defn whitelisted-url [url']
  (and (not (nil? url'))
       (let [url (url url')]
         (and  (= instagram-host
                  (:host url))
               (-photo-path? (:path url))))))

(declare -on-tab-updated
         -on-page-action-clicked
         -enable-page-action!
         -disable-page-action!)

(defn -register-platform-callbacks! []
  (.. js/chrome
      -tabs
      -onUpdated
      (addListener -on-tab-updated))

  (.. js/chrome
      -pageAction
      -onClicked
      (addListener -on-page-action-clicked)))

(defn -on-tab-updated [tab-id change-info tab]
  (log/debug "Tab updated" tab-id (.-status change-info))
  (if (and (= "complete" (.-status change-info))
           (whitelisted-url (.-url tab)))
    (-enable-page-action! tab-id)
    (-disable-page-action! tab-id)))

(declare -prepare-tab!)

(declare -cmd-get-photo! -download-photo!)

(defn -on-page-action-clicked [tab]
  (go
    (<! (-prepare-tab! (.-id tab)))
    (log/debug "Tab has content script now")
    (let [photo (<! (-cmd-get-photo! (.-id tab)))]
      (log/debug "Photo details" photo)
      (when-not (= photo
                   ::not-found)
        (let [download-id (<! (-download-photo! (:photo-url photo)))]
          (log/debug "Download id" download-id))))))

(defn -inject-js
  ([src] (-inject-js src nil))
  ([src tab-id]
   (let [c (chan)]
     (go
       (.. js/chrome
           -tabs
           (executeScript tab-id
                          #js {:file src :runAt "document_idle"}
                          #(put! c true))))
     c)))

(defn -enable-page-action! [tab-id]
  (log/debug "Enabling page action for tab" tab-id)
  (.. js/chrome
      -pageAction
      (show tab-id)))

(def ^:private prepared-tabs (atom #{}))

(defn -disable-page-action! [tab-id]
  (log/debug "Disabling page action for tab" tab-id)
  (.. js/chrome
      -pageAction
      (hide tab-id))
  (swap! prepared-tabs disj tab-id))

(defn -prepare-tab! [tab-id]
  (go
    (when-not (@prepared-tabs tab-id)
      (<! (-inject-js cljs-base-file))
      (<! (-inject-js gram-bomb-file))
      (swap! prepared-tabs conj tab-id))))

(defn -photo-path? [path]
  (and (not (s/blank? path))
       (= "/p/" (subs path 0 3))))

(defn -cmd-get-photo! [tab-id]
  (let [c (chan)
        msg (-> {:event-id "get-photo-url"} clj->js)]
    (.. js/chrome
        -tabs
        (sendMessage tab-id msg #js {}
                     (fn [response]
                       (as->  (js->clj response
                                       :keywordize-keys true) $
                         (if (nil? $)
                           (put! c ::not-found)
                           (put! c $))))))
    c))

(defn -download-photo! [photo-url]
  (let [c (chan)
        filename (-> (url photo-url)
                     :path
                     (s/split #"/")
                     last)]
    (.. js/chrome
        -downloads
        (download #js {:url photo-url
                       :filename filename
                       :saveAs true}
                  (fn [download-id]
                    (->> (or download-id
                             :canceled)
                         (put! c)))))
    c))
