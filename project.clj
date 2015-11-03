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
                 [org.clojure/data.json "0.2.6" :classifier "aot"]]

  :cljsbuild {:builds [{:source-paths ["src" "src-background-main"]
                         :compiler {:output-dir "target/extension-js/js/gen"
                                    :asset-path "js/gen"
                                    :source-map true
                                    :optimizations :simple
                                    :modules {:background {:output-to "target/extension-js/js/gen/background.js"
                                                           :entries #{"instasave.background.main"}}}}}]})
