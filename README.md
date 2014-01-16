# Wireframes [![Build Status](https://secure.travis-ci.org/rm-hull/wireframes.png)](http://travis-ci.org/rm-hull/wireframes)

A lightweight 3D rendering engine in Clojure & ClojureScript.

![Aventador](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/translucent/aventador.png)

Adapted and extended from a javascript demo (originally by Kragen Javier Sitaker, see references below)
into a Clojure/ClojureScript library (that renders to SVG, an HTML5 Canvas or a Graphics2D object -
depending on the runtime environment, obviously).

This started out as a experiment to plot Lorenz attractors in 3D space, but it turns out to be a really
*simple* way to programmatically generate three dimensional geometric shapes - basically a programmable
CAD system - I'm pretty sure that AutoCAD could already do this (and much quicker too), but where I
would really like to go with this is:

* build up a robust and idiomatic Clojure API for generating shapes
* implement a wide variety of output renderers - potentially even a GLSL cross-compiler and
  certainly a gcode output formatter suitable for 3D printers
* maintain 100% compatibility with ClojureScript

As this is a *software* renderer, please don't expect OpenGL levels of performance (or until WebGL
and OpenGL renderers have been written).

A variety of (in-progress) Clojure-generated wireframe and solid shapes can be found
in the [gallery](https://github.com/rm-hull/wireframes/blob/master/GALLERY.md), and a ClojureScript demo of an
[animated torus tumbling in 3D space](http://programming-enchiladas.destructuring-bind.org/rm-hull/7098992).

### Pre-requisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.3.2 or above installed.

### Building

To build and install the library locally, run:

    $ lein test
    $ lein cljsbuild once
    $ lein install

To re-generate the examples in the ```doc/gallery``` directory, run:

    $ lein test :examples

### Including in your project

There is an 'alpha-quality' version hosted at [Clojars](https://clojars.org/rm-hull/wireframes).
For leiningen include a dependency:

```clojure
[rm-hull/wireframes "0.0.1-SNAPSHOT"]
```

For maven-based projects, add the following to your `pom.xml`:

```xml
<dependency>
  <groupId>rm-hull</groupId>
  <artifactId>wireframes</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Basic Usage

### Creating shapes

There are some drawing primitives in the ```wireframes.shapes``` namespace to create
objects such as lines, bezier curves, polygons, circles, cuboids, cylinders and torus shapes.

The basic mechanism for *building* shapes is **extrusion**. For example, to create a cube,
start with a square polygon in the X-Y plane, and extrude that shape into a line along the
Z-axis, as per the following code:

```clojure
(use 'wireframes.shapes.primitives)

(->
  (make-polygon
    (make-point 0 0 0)
    (make-point 0 1 0)
    (make-point 1 1 0)
    (make-point 1 0 0))
  (extrude (translate 0 0 1) 1))
```

### Drawing shapes

There are various software renderers in the ```wireframes.renderer``` namespace for
output to java images, SVG or HTML5 canvas (the availability of which combinations
depends on whether you are executing the code in Clojure or ClojureScript).

#### Clojure

For example, in Clojure, to generate a torus angled in the Y- and Z-axles, written
out to a PNG file:

```clojure
(use 'wireframes.shapes.curved-solids)
(use 'wireframes.transform)
(use 'wireframes.renderer.bitmap)
(use 'wireframes.renderer.color)

(write-png
  (->img
    (draw-solid
      {:focal-length 3
       :color-fn (wireframe :white :transparent)
       :style :transparent
       :transform (combine
                    (rotate :z (degrees->radians 65))
                    (rotate :y (degrees->radians -30))
                    (translate 0 0 16))
       :shape (make-torus 1 3 60 60)})
    [400 400])
  "torus-65.png")
```
Produces:

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/transparent/torus.png)

### Surfaces and other primitives

MATLAB-style function plots can be generated thus:

```clojure
(use 'wireframes.shapes.primitives)
(use 'wireframes.transform)
(use 'wireframes.renderer.bitmap)
(use 'wireframes.renderer.lighting)
(use 'wireframes.renderer.color)

(defn sqr [x]
  (* x x))

(defn sinc
  "Unnormalized/cardinal sine function"
  [x] (if (zero? x)
        1.0
        (/ (Math/sin x) x)))

(defn mexican-hat [x y]
  (* 15 (sinc (Math/sqrt (+ (sqr x) (sqr y ))))))

(write-png
  (->img
    (draw-solid
      {:focal-length 30
       :color-fn (comp
                   black-edge                         ; [1]
                   (positional-lighting-decorator     ; [2]
                     default-position
                     (spectral-z -6.5 15)))           ; [3]
       :style :shaded
       :transform (combine
                    (rotate :z (degrees->radians 15))
                    (rotate :x (degrees->radians 135))
                    (scale 0.05)
                    (translate 0 0 10))
       :shape (make-surface                           ; [4]
                (range -22 22 0.25)
                (range -22 22 0.25)
                mexican-hat)})                        ; [5]
    [600 600])
  "sinc3D.png")
```

Results in:

![Hat](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/shaded/sinc3D.png)

#### Program Notes
TODO

### Loading common 3D shape files

The defacto/clichéd Utah teapot (or any patch/vertex 3D file) can be loaded in with the following
code sample:

```clojure
(use 'wireframes.shapes.patch-loader)
(use 'wireframes.renderer.bitmap)
(use 'wireframes.transform)

(write-png
  (->img
    (draw-solid
      {:focal-length 10
       :fill-color Color/WHITE
       :style :translucent
       :transform (combine
                    (rotate :z (degrees->radians 35))
                    (rotate :x (degrees->radians -70))
                    (translate 0 -1 40))
       :shape (load-shape "resources/newell-teapot/teapot")})
    [1000 900])
  "teapot.png")
```
which generates:

![Teapot](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/translucent/teapot.png)

The following file formats support loading:

* Patch files in the ```wireframes.shapes.patch-loader``` namespace,
* Wavefront .obj files in the ```wireframes.shapes.wavefront-loader``` namespace,
* Stereolithography .stl files in the ```wireframes.shapes.stl-loader``` namespace

### Saving 3D shapes to STL-format files

Once generated or loaded by whatever means, a shape may be persisted in STL format with the following code sample:

```clojure
(use 'wireframes.shapes.stl-loader)
(use 'wireframes.shapes.curved-solids)

(save-shape
  (make-torus 1 3 60 60)
  "a description which will get truncated to 80 chars"
  "doc/gallery/torus.stl")
```

This specific [torus](https://github.com/rm-hull/wireframes/blob/master/doc/gallery/torus.stl), the
[teapot](https://github.com/rm-hull/wireframes/blob/master/doc/gallery/teapot.stl) and a
[wineglass](https://github.com/rm-hull/wireframes/blob/master/doc/gallery/wineglass.stl)
can then be viewed using the GitHub 3D viewer.

## TODO

* Investigate using primitive arrays (see [array](https://github.com/rm-hull/wireframes/tree/array) branch)
* Use/implement a vector library
* Geometric extension with Minkowski addition (see http://projecteuler.net/problem=228)
* ~~Efficiently calculate polygons on extruded shapes~~
* ~~Rewrite/rename wireframes.transform/concat - unroll loops for performance~~
* ~~Complete Bezier patch code~~
* ~~Rectilinear perspective mapping~~
* ~~Stitch adjacent surface panels together~~
* Renderer implementations:
  - ~~Graphics2D~~
  - ~~SVG~~
  - ~~Canvas~~
  - WebGL
  - OpenGL
* ~~Simple flat shading / lighting~~
* ~~Configurable lighting position(s)~~
* ~~Colours~~
* Gourand shading
* Texture mapping
* ~~Backface removal~~
* Z-buffer
* Polygon inflation
* ~~Compute shape bounds~~
* ~~Center shape at origin function~~
* gcode generation for 3D printers
* ~~Support loading from & saving to .stl files~~
* ~~Support loading from Wavefront .obj files~~
* ~~Deprecate ```:lines``` - no longer used except in platonic solids~~
* Improve documentation
* Examples
* ~~MATLAB style surface functions~~
* ~~Integrate [Inkspot](https://github.com/rm-hull/inkspot) & custom vertex/fragment shader~~
* Constructive Solid Geometry (CSG) boolean operators

## Known Bugs

* Bug in shader/lighting position - affected by applied transforms?
* ~~Improve depth criteria for priority fill/painters algorithm~~
* Cube (multi-dimension) extrusion is generating erroneous polygons
* ~~RRB-Vector implementation does not bundle .cljs files! See http://dev.clojure.org/jira/browse/CRRBV-1~~
* Priority fill fails with TimSort exception _“Comparison method violates its general contract!”_ if any point is NaN.

## References

* http://lists.canonical.org/pipermail/kragen-hacks/2007-February/000448.html
* http://www.sjbaker.org/wiki/index.php?title=The_History_of_The_Teapot
* http://www.scratchapixel.com/lessons/3d-basic-lessons/lesson-11-rendering-the-teapot-bezier-surfaces/b-zier-surface/
* https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations
* https://www.mathdisk.com/pub/safi/worksheets/Perspective_Projection
* http://www.cs.berkeley.edu/~jrs/mesh/
* http://www.victoriakirst.com/beziertool/
* https://en.wikipedia.org/wiki/Wavefront_.obj_file
* https://en.wikipedia.org/wiki/STL_(file_format)
* https://github.com/colah/ImplicitCAD
* http://evanw.github.io/csg.js/
* http://www.opengl.org/wiki/Calculating_a_Surface_Normal
* http://www.cs.utah.edu/~xchen/columbia/session1/lec24/html/
* http://www.3dcadbrowser.com/3dmodels.aspx?word=star%20wars
* http://derek.troywest.com/articles/by-example-gloss/
* http://doc.cgal.org/latest/Manual/packages.html
* http://stackoverflow.com/questions/3749678/expand-fill-of-convex-polygon
* http://stackoverflow.com/questions/1109536/an-algorithm-for-inflating-deflating-offsetting-buffering-polygons
* https://github.com/tsaastam/cljs-webgl-example

## License

The MIT License (MIT)

Copyright (c) 2013 Richard Hull

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/rm-hull/wireframes/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
