# Wireframes [![Build Status](https://secure.travis-ci.org/rm-hull/wireframes.png)](http://travis-ci.org/rm-hull/wireframes)

A lightweight 3D rendering engine in Clojure & ClojureScript.

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/solid-torus.png)

Ported from javascript (see references below) into ClojureScript (that will
render to SVG or an HTML5 Canvas), and Clojure (that will render to a Graphics2D object
or SVG).

## Usage

### Creating shapes

There are some drawing primitives in the ```wireframes.shape``` namespace to create
objects such as circles, cuboids, cylinders and torus shapes.

The basic mechanism for building shapes is *extrusion*. For example, to create a cube, 
start with a point, and extrude that point into a line along the Z-axis. Then extrude
that line along the Y-axis to create a square; extrude the square along the X-axis to
create a cube, as per the following code:

```clojure
(->>
  (make-point 0 0 0)
  (extrude (translate 0 0 1))
  (extrude (translate 0 1 0))
  (extrude (translate 1 0 0)))
```

### Drawing shapes

There are various software renderers in the ```wireframes.renderer``` namespace for
output to java images, SVG or HTML5 canvas (the availability of which combinations
depends on whether you are executing the code in Clojure or ClojureScript).

#### Clojure

For example, in Clojure, to generate a torus angled in the Y- and Z-axles, written
out to a PNG file:

```clojure
  (write-png
    (->img
      3
      (concat
        (rotate :z (degrees->radians 65))
        (rotate :y (degrees->radians -30))
        (translate 0 0 16))
      (make-torus 1 3 60 60)
      [400 400])
    "torus-65.png")
```
Produces:

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/torus-65.png)

The defacto/clichéd Utah teapot (or any patch/vertex 3D file) can be loaded in with the following
code sample:

```clojure
(write-png
(->img
  10
  (t/concat
    (t/rotate :z (sp/degrees->radians 35))
    (t/rotate :x (sp/degrees->radians -70))
    (t/translate 0 -1 40))
  (sl/load-shape "resources/newell-teapot/teapot" 16)
  [600 600])
"teapot.png")
```
which generates:

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/wireframe-teapot.png)

## TODO

* Efficiently calculate polygons on extruded shapes
* Rewrite/rename wireframes.transform/concat - unroll loops for performance
* ~~Complete Bezier patch code~~
* ~~Rectilinear perspective mapping~~
* ~~Stitch adjacent surface panels together~~
* SVG renderer
* Canvas renderer
* ~~Simple flat shading / lighting~~
* Configurable lighting position(s)
* Colours
* Gourand shading
* Texture mapping
* Backface removal
* Support loading from .dae files
* Improve documentation
* Examples

## References

* http://lists.canonical.org/pipermail/kragen-hacks/2007-February/000448.html
* http://www.sjbaker.org/wiki/index.php?title=The_History_of_The_Teapot
* http://www.scratchapixel.com/lessons/3d-basic-lessons/lesson-11-rendering-the-teapot-bezier-surfaces/b-zier-surface/
* https://en.wikipedia.org/wiki/Rotation_matrix#Basic_rotations
* https://www.mathdisk.com/pub/safi/worksheets/Perspective_Projection

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
