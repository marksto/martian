(defproject com.github.oliyh/martian-test "0.1.32-SNAPSHOT"
  :description "Testing tools for martian"
  :url "https://github.com/oliyh/martian"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :plugins [[lein-modules "0.3.11"]]
  :dependencies [[com.github.oliyh/martian :version]
                 [prismatic/schema-generators "0.1.4"] ;; TODO: Upgrade to "0.1.5" makes the tests fail.
                 [org.clojure/test.check "1.1.1"]
                 [org.clojure/core.async "1.8.741"]]
  :profiles {:provided {:dependencies [[org.clojure/clojure "1.12.1"]
                                       [org.clojure/clojurescript "1.10.520" :upgrade false] ;; upgrading this makes the tests fail for some reason...
                                       ]}
             :dev {:exclusions [[org.clojure/tools.reader]]
                   :resource-paths ["../test-common"]
                   :dependencies [[prismatic/schema "1.1.12"]
                                  [binaryage/devtools "1.0.3"]
                                  [com.bhauman/figwheel-main "0.2.13"]
                                  [org.clojure/tools.nrepl "0.2.13"]
                                  [org.clojure/tools.reader "1.3.5"]
                                  [cider/piggieback "0.5.2"]
                                  [com.github.oliyh/martian-httpkit :version]]
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}}
  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dist"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" martian.test-runner]
            "test" ["do" ["clean"] ["test"] ["fig:test"]]})
