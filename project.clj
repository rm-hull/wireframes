(defproject rm-hull/wireframes "0.0.1"
  :description "A lightweight 3D wireframe renderer for both Clojure and ClojureScript"
  :url "https://github.com/rm_hull/wireframes"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1913"]
                 [prismatic/dommy "0.1.2"]
                 [hiccup "1.0.4"]
                 [jayq "2.4.0"]
                 [rm-hull/monet "0.1.8"]]
  :plugins [[lein-cljsbuild "0.3.3"]
            [com.birdseye-sw/lein-dalap "0.1.0"]]
  :hooks [leiningen.dalap]
  :source-path "src"
  :cljsbuild {
    :builds [{:source-paths ["target/generated"], :id "main", :jar true}]}
  :min-lein-version "2.3.2"
  :global-vars {*warn-on-reflection* true})
