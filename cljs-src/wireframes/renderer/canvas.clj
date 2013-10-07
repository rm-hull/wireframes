(ns wireframes.renderer.canvas
  (:use [monet.canvas :only [save restore stroke-width stroke-cap stroke-style fill fill-style
                             begin-path line-to move-to stroke close-path transform]]))
