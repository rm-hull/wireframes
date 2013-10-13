(ns wireframes.examples
  (:use [clojure.test])
  (:require [wireframes.transform :as t]
            [wireframes.shapes.primitives :as p]
            [wireframes.shapes.curved-solids :as cs]
            [wireframes.shapes.platonic-solids :as ps]
            [wireframes.shapes.patch-loader :as pl]
            [wireframes.shapes.wavefront-loader :as wl]
            [wireframes.renderer.bitmap :as b]))

(defn harness [{:keys [shape focal-length transform draw-fn filename size]}]
  (println filename)
  (time
    (is (b/write-png
        (b/->img
          (partial draw-fn focal-length transform shape)
          (or size [1000 900]))
        filename))))

(deftest ^:examples wireframes

  (harness {
    :filename "doc/gallery/wireframe-teapot.png"
    :shape (pl/load-shape "resources/newell-teapot/teapot" 16)
    :focal-length 10
    :draw-fn b/draw-wireframe
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 35))
                 (t/rotate :x (p/degrees->radians -120))
                 (t/translate 0 -1 40))})

  (harness {
    :filename "doc/gallery/wireframe-icon-a5.png"
    :shape (wl/load-shape "resources/obj_IconA5.obj")
    :focal-length 4
    :draw-fn b/draw-wireframe
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 25))
                 (t/rotate :y (p/degrees->radians 130))
                 (t/rotate :x (p/degrees->radians -50))
                 (t/translate 3 -5 60))})

  (harness {
    :filename "doc/gallery/wireframe-torus.png"
    :shape (cs/make-torus 1 3 60 60)
    :focal-length 3
    :size [400 400]
    :draw-fn b/draw-wireframe
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 65))
                 (t/rotate :y (p/degrees->radians -30))
                 (t/translate 0 0 16))})

  (harness {
    :filename "doc/gallery/wireframe-sphere.png"
    :shape (cs/make-sphere 3 30)
    :focal-length 3
    :draw-fn b/draw-wireframe
    :transform (t/concat
                 (t/rotate :x (p/degrees->radians 60))
                 (t/rotate :y (p/degrees->radians -15))
                 (t/translate 0 0 16))})

  (harness {
    :filename "doc/gallery/wireframe-wineglass.png"
    :shape (cs/make-wineglass 60)
    :focal-length 8
    :draw-fn b/draw-wireframe
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 15))
                 (t/rotate :x (p/degrees->radians -20))
                 (t/translate 0 -0.2 5.5))})

  (harness {
    :filename "doc/gallery/tetrahedron.png"
    :shape ps/tetrahedron
    :focal-length 5
    :size [300 300]
    :draw-fn b/draw-wireframe
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 55))
                 (t/rotate :y (p/degrees->radians -20))
                 (t/translate 0 0 16))})

  (harness {
    :filename "doc/gallery/icosahedron.png"
    :shape ps/icosahedron
    :focal-length 5
    :size [300 300]
    :draw-fn b/draw-wireframe
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 55))
                 (t/rotate :y (p/degrees->radians -30))
                 (t/translate 0 0 16))}))


(deftest ^:examples solids
  (harness {
    :filename "doc/gallery/solid-teapot.png"
    :shape (pl/load-shape "resources/newell-teapot/teapot" 16)
    :focal-length 10
    :draw-fn b/draw-solid
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 35))
                 (t/rotate :x (p/degrees->radians -120))
                 (t/translate 0 -1 40))})

  (harness {
    :filename "doc/gallery/solid-icon-a5.png"
    :shape (wl/load-shape "resources/obj_IconA5.obj")
    :focal-length 4
    :draw-fn b/draw-solid
    :transform (t/concat
                 (t/rotate :z (p/degrees->radians 25))
                 (t/rotate :y (p/degrees->radians 130))
                 (t/rotate :x (p/degrees->radians -50))
                 (t/translate 3 -5 60))})

  (harness {
    :filename "doc/gallery/solid-teacup.png"
    :shape (pl/load-shape "resources/newell-teapot/teacup" 16)
    :focal-length 10
    :draw-fn b/draw-solid
    :transform (t/concat
                 (t/rotate :x (p/degrees->radians -30))
                 (t/translate 0 0 25))}))

