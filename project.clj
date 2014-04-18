(defproject rm-hull/wireframes "0.0.1-SNAPSHOT"
  :description "A lightweight 3D wireframe renderer for both Clojure and ClojureScript"
  :url "https://github.com/rm-hull/wireframes"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2202"]
                 [gloss "0.2.2"]
                 [potemkin "0.3.4"]
                 [byte-streams "0.1.10"]
                 [hiccup "1.0.5"]

                 [org.clojure/core.rrb-vector "0.0.11" :exclusions [org.clojure/clojurescript]]
                 [cljs-webgl "0.1.4-SNAPSHOT" :exclusions [org.clojure/clojurescript]]
                 [rm-hull/dommy "0.1.3-SNAPSHOT" :exclusions [org.clojure/clojurescript]]
                 [rm-hull/monet "0.1.11" :exclusions [org.clojure/clojurescript]]
                 [rm-hull/inkspot "0.0.1-SNAPSHOT" :exclusions [org.clojure/clojurescript]]
                 [rm-hull/cljs-test "0.0.8-SNAPSHOT" :exclusions [org.clojure/clojurescript]]

                 [com.taoensso/timbre "3.1.6" :scope "test"]
                 [me.raynes/fs "1.4.5" :scope "test"]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [com.birdseye-sw/lein-dalap "0.1.0"]]
  :hooks [leiningen.dalap
          leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :cljsbuild {
    :test-commands {"phantomjs"  ["phantomjs" "target/unit-test.js"]}
    :builds {
      :main {
        :source-paths ["src/cljs" "target/generated-src"]
        :jar true
        :compiler {
          :output-to "target/wireframes.js"
          :source-map "target/wireframes.map"
          :static-fns true
          ;:optimizations :advanced
          :pretty-print true }}
      :test {
        :source-paths ["src-cljs" "target/generated-src" "test"]
        :incremental? true
        :compiler {
          :output-to "target/unit-test.js"
          :source-map "target/unit-test.map"
          :static-fns true
          :optimizations :whitespace
          :pretty-print true }}
      :demo {
        :source-paths ["src-cljs" "target/generated-src" "doc/gallery/cljs-demo"]
        :incremental? true
        :compiler {
          :output-to "target/demo.js"
          :source-map "target/demo.map"
          :static-fns true
          :optimizations :whitespace
          :pretty-print true }}}}
  :test-selectors {:default (complement :examples)
                   :examples :examples }
  :min-lein-version "2.3.4"
  :global-vars {*warn-on-reflection* true}
  :repositories {"sonartype snapshots" "https://oss.sonatype.org/content/repositories/snapshots"})
