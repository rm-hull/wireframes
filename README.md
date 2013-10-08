# Wireframes [![Build Status](https://secure.travis-ci.org/rm-hull/wireframes.png)](http://travis-ci.org/rm-hull/wireframes)

A lightweight real-time 3D rendering engine in Clojure & ClojureScript.

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/solid-torus.png)

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
  (extrude (translate 0 0 n))
  (extrude (translate 0 n 0))
  (extrude (translate n 0 0)))
```

### Drawing shapes

There are various software renderers in the ```wireframes.renderer``` namespace for
output to java images, SVG or HTML5 canvas (the availability of which combinations
depends on whether you are executing the code in Clojure or ClojureScript).

#### Clojure

For example, in Clojure, to generate a torus angled in the Y- and Z-axles, written
out to a PNG file:

```clojure
(def angle (degrees->radians 65))

(def img 
  (->img
    (concat
      (rotate angle)
      (transpose-axes :y :z)
      (rotate (/ angle 1.618))
      (transpose-axes :y :z)
      (translate 0 0 6))
    (make-torus 1 3 60 60)
    [400 400]))

(write-png img "torus-65.png")
```

Produces:

![Torus](https://raw.github.com/rm-hull/wireframes/master/doc/torus-65.png)

## References

* http://pobox.com/~kragen/sw/torus.html
* http://lists.canonical.org/pipermail/kragen-hacks/2007-February/000448.html

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
