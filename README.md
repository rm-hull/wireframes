# Wireframes [![Build Status](https://secure.travis-ci.org/rm-hull/wireframes.png)](http://travis-ci.org/rm-hull/wireframes)

A lightweight 3D rendering engine in Clojure & ClojureScript.

![Aventador](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/translucent/aventador.png)

Adapted and extended from a javascript demo (originally by Kragen Javier Sitaker, see references below) 
into a ClojureScript/Clojure library (that will render to SVG, an HTML5 Canvas or a Graphics2D object 
depending on the runtime environment).

A variety of (in-progress) generated wireframe and solid shapes can be found 
in the [gallery](https://github.com/rm-hull/wireframes/blob/master/GALLERY.md).

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

There *will be* a version hosted at [Clojars](https://clojars.org/rm-hull/wireframes). For leiningen include a dependency:

```clojure
[rm-hull/wireframes "0.0.1"]
```

For maven-based projects, add the following to your `pom.xml`:

```xml
<dependency>
  <groupId>rm-hull</groupId>
  <artifactId>wireframes</artifactId>
  <version>0.0.1</version>
</dependency>
```

## Basic Usage

### Creating shapes

There are some drawing primitives in the ```wireframes.shapes``` namespace to create
objects such as circles, cuboids, cylinders and torus shapes.

The basic mechanism for *building* shapes is **extrusion**. For example, to create a cube, 
start with a point, and extrude that point into a line along the Z-axis. Then extrude
that line along the Y-axis to create a square; extrude the square along the X-axis to
create a cube, as per the following code:

```clojure
(->
  (make-point 0 0 0)
  (extrude (translate 0 0 1) 1)
  (extrude (translate 0 1 0) 1)
  (extrude (translate 1 0 0) 1))
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
    (draw-solid
      {:focal-length 3
       :fill-color (Color. 255 255 255 0) ; transparent white
       :transform (concat
                    (rotate :z (degrees->radians 65))
                    (rotate :y (degrees->radians -30))
                    (translate 0 0 16))
       :shape (make-torus 1 3 60 60)})
    [400 400])
  "torus-65.png")
```
Produces:

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/transparent/torus.png)

### Loading common 3D shape files

The defacto/clichéd Utah teapot (or any patch/vertex 3D file) can be loaded in with the following
code sample:

```clojure
(write-png
  (->img
    (draw-solid 
      {:focal-length 10
       :fill-color (Color. 255 255 255 128) ; translucent white
       :transform (t/concat
                    (t/rotate :z (sp/degrees->radians 35))
                    (t/rotate :x (sp/degrees->radians -70))
                    (t/translate 0 -1 40))
       :shape (sl/load-shape "resources/newell-teapot/teapot")})
    [1000 900])
  "teapot.png")
```
which generates:

![Teapot](https://raw.github.com/rm-hull/wireframes/master/doc/gallery/translucent/teapot.png)

## TODO

* ~~Efficiently calculate polygons on extruded shapes~~
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
* ~~Backface removal~~
* Compute shape bounds
* gcode generation for 3D printers
* Support loading from & saving to .stl files
* ~~Support loading from Wavefront .obj files~~
* Deprecate ```:lines``` - no longer used except in platonic solids
* Improve documentation
* Examples

## Known Bugs

* Bug in shader/lighting position - affected by applied transforms?
* Improve depth criteria for priority fill/painters algorithm
* Cube (multi-dimension) extrusion is generating erroneous polygons

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
