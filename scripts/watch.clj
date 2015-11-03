(require '[cljs.build.api :as b])

(b/watch "src"
  {:main 'instasave.core
   :output-to "out/instasave.js"
   :output-dir "out"})
