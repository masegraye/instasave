(defproject instasave "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :jvm-opts ^:replace ["-Xmx1g" "-server"]
  :plugins [[lein-npm "0.6.1"]
            [lein-cljsbuild "1.1.1-SNAPSHOT"]]

  :hooks [leiningen.cljsbuild]

  :npm {:dependencies [[source-map-support "0.3.2"]]}

  :source-paths ["src" "target/classes"]
  :clean-targets ["target"]
  :target-path "target"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.162" :classifier "aot"
                  :exclusion [org.clojure/data.json]]
                 [org.clojure/data.json "0.2.6" :classifier "aot"]
                 [org.clojure/core.async "0.2.371"]]

  ;; Going to experiment with multiple builds instead of module builds - might be faster...
  ;; and this will certainly be faster for background.cljs development - because we can use figwheel
  :cljsbuild {:builds #_{:background {:source-paths ["src" "src-background-main"]
                                    :compiler {:output-dir "target/extension-js/js/gen/background"
                                               :output-to "target/extension-js/js/gen/background/background.js"
                                               :source-map "target/extension-js/js/gen/background/background.js.map"
                                               :optimizations :whitespace
                                               :main "instasave.background.main"
                                               :asset-path "js/gen/background"}}

                       :gram-bomb {:source-paths ["src" "src-gram-bomb-main"]
                                   :compiler {:output-dir "target/extension-js/js/gen/gram_bomb"
                                              :output-to "target/extension-js/js/gen/gram_bomb/gram_bomb.js"
                                              :optimizations :simple}}}
              [{:source-paths ["src"
                               ;; we keep these in separate source directories because contain
                               ;; the inline invocations to their respective [module]/run methods,
                               ;; which cljs.test doesn't like. Putting the in separate directories from
                               ;; src allows us to skip their inclusion when building the CLJS test binary
                               "src-background-main"
                               "src-gram-bomb-main"]
                :compiler {:output-dir "target/extension-js/js/gen"
                           :asset-path "js/gen"
                           :source-map true
                           :optimizations :simple
                           ;; Surprisingly slow...
                           :modules {:background {:output-to "target/extension-js/js/gen/background.js"
                                                  :entries #{"instasave.background.main"}}
                                     :gram-bomb {:output-to "target/extension-js/js/gen/gram_bomb.js"
                                                 :entries #{"instasave.gram-bomb.main"}}}}}]})
