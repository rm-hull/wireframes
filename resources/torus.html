<html><head><title>A Rotating Torus, 3D-Rendered In Your Browser</title>
<script type="text/javascript">//<![CDATA[
// === basic wheel reinvention stuff ===

function $(id) { return document.getElementById(id) }

// comparison function using a key, to pass to .sort()
function keycomp(key) {
  return function(a, b) {
    var ka = key(a)
    var kb = key(b)
    if (ka < kb) return -1
    if (ka > kb) return 1
    return 0 
  }
}

// return a list transformed by a function
function map(f, list) {
  var rv = []
  for (var ii = 0; ii < list.length; ii++) rv.push(f(list[ii]))
  return rv
}

// === 3d transforms ===

// We represent transforms as a 3x4 list of lists (ahem, array of arrays):
// [[x_from_x, x_from_y, x_from_z, x_off],
//  [y_from_x, y_from_y, y_from_z, y_off],
//  [z_from_x, z_from_y, z_from_z, z_off]]
// And we only actually multiply points through them in xform.
function translate(x, y, z) {
  return [[1, 0, 0, x], [0, 1, 0, y], [0, 0, 1, z]]
}
function identity() { return translate(0, 0, 0) }
// rotation around the Z-axis
function rotate(theta) {
  var s = Math.sin(theta)
  var c = Math.cos(theta)
  return [[c, -s, 0, 0], [s, c, 0, 0], [0, 0, 1, 0]]
}
// exchange two of the X, Y, Z axes --- useful for making rotate() go around
// another axis :)
function transpose_axes(a, b) {
  var rv = identity()
  var tmp = rv[a]
  rv[a] = rv[b]
  rv[b] = tmp
  return rv
}
// you'd think we'd have a scale() function too, but I haven't needed it yet.
// concatenate two transforms --- the magic that makes it all possible
function concat(x1, x2) {
  var rv = [[0, 0, 0, 0], [0, 0, 0, 0], [0, 0, 0, 0]]
  for (var ii = 0; ii < 3; ii++) {
    rv[ii][3] = x2[ii][3]
    for (var jj = 0; jj < 3; jj++) {
      rv[ii][3] += x1[jj][3] * x2[ii][jj]
      for (var kk = 0; kk < 3; kk++) {
        rv[ii][jj] += x1[kk][jj] * x2[ii][kk]
      }
    }
  }
  return rv
}
// concatenate N transforms.  I'd insert a special case for 0 transforms,
// but amusingly this function isn't all that performance-critical.
function concat_n(xforms) {
  var rv = identity()
  for (var ii = 0; ii < xforms.length; ii++) rv = concat(rv, xforms[ii]) 
  return rv
}
// transform a point.
function xform(xform, p) {
  var result_vec = []
  for (var ii = 0; ii < 3; ii++) {
    var rv = xform[ii][3]
    for (var jj = 0; jj < 3; jj++) rv += xform[ii][jj] * p[jj]
    result_vec.push(rv)
  }
  return result_vec
}
// transform multiple points.
function xform_points(xf, points) {
  var xp = []
  for (var ii = 0; ii < points.length; ii++) {
    xp.push(xform(xf, points[ii]))
  }
  return xp
}
// perspective-transform a point --- into 2d.
function persp(p) { return [p[0] / p[2], p[1] / p[2]] }
// perspective-transform multiple points
function persp_points(points) {
  return map(persp, points)
}

// return the normal of a triangle defined by three points.
function normal(p1, p2, p3) {
  var v1 = [p1[0]-p2[0], p1[1]-p2[1], p1[2]-p2[2]]
  var v2 = [p2[0]-p3[0], p2[1]-p3[1], p2[2]-p3[2]]
  var n = [v1[1]*v2[2]-v1[2]*v2[1], 
      	   v1[2]*v2[0]-v1[0]*v2[2],
           v1[0]*v2[1]-v1[1]*v2[0]]
  var mag = Math.sqrt(n[0]*n[0] + n[1]*n[1] + n[2]*n[2])
  return [n[0]/mag, n[1]/mag, n[2]/mag]
}

// === 3d shapes ===
// We represent these as an array of three arrays
// [points, lines, polies] where each line is two indices into the points array
// and each poly is three indices into the points array

function dup(array) {
  var newarray = new Array(array.length)
  for (var ii = 0; ii < array.length; ii++) newarray[ii] = array[ii]
  return newarray
}

// transform a shape, returning a new shape
function xform_shape(xf, shape) {
  // de-alias new lines and polies
  return [xform_points(xf, shape[0]), dup(shape[1]), dup(shape[2])]
}

// add a new shape onto an old shape, mutating the old one
function augment(shape1, shape2) {
  var s1p = shape1[0]
  var off = s1p.length
  for (var ii = 0; ii < shape2[0].length; ii++) s1p.push(shape2[0][ii])
  var s2ll = shape2[1].length  // in case of aliasing
  for (var ii = 0; ii < s2ll; ii++) 
    shape1[1].push([shape2[1][ii][0] + off, shape2[1][ii][1] + off])
  var s2pl = shape2[2].length
  for (var ii = 0; ii < s2pl; ii++) {
    var tri = shape2[2][ii]
    shape1[2].push([tri[0]+off, tri[1]+off, tri[2]+off])
  }
}

// given a shape, make a more complicated shape by copying it through transform
// xf n times, and connecting the corresponding points.  This is more powerful
// than the usual kind of extrusion, and can be used to create fairly 
// interesting shapes --- a snail shell from a circle, for instance.
function extrude_shape(xf, shape, n) {
  if (n == null) n = 1
  var new_part = shape
  var old_line_base = 0 // where the lines to attach the triangles start
  for (var ii = 0; ii < n; ii++) {
    var new_part = xform_shape(xf, new_part)
    var shape_length = shape[0].length
    var new_line_base = shape[1].length  // for triangles later
    augment(shape, new_part)
    var new_part_length = new_part[0].length
    // connect corresponding points
    for (var jj = 0; jj < new_part_length; jj++) {
      shape[1].push([shape_length + jj - new_part_length, shape_length + jj])
    }
    // make triangles
    var nlines = new_part[1].length
    // var old_line_base = new_line_base - nlines
    for (var jj = 0; jj < nlines; jj++) {
      var old_line = shape[1][old_line_base + jj]
      var new_line = shape[1][new_line_base + jj]
      shape[2].push([old_line[0], old_line[1], new_line[0]])
      shape[2].push([new_line[1], new_line[0], old_line[1]])
    }
    old_line_base = new_line_base
  }
}
// a shape consisting of a single point
function point_shape(x, y, z) { return [[[x, y, z]], [], []] }
// approximate a circle in the x-y plane around the origin; radius r and n points
function circle(r, n) {
  var shape = point_shape(r, 0, 0)
  extrude_shape(rotate(Math.atan(1)*8/n), shape, n)
  return shape
}
// approximate a torus with major radius r2 and minor radius r1,
// with correspondingly n2 and n1 points around each axis
function make_torus(r1, r2, n1, n2) {
  var c = xform_shape(translate(r2, 0, 0), circle(r1, n1))
  extrude_shape(concat_n([transpose_axes(1, 2), 
    	                  rotate(Math.atan(1)*8/n2),
		          transpose_axes(1, 2)]),
		c, n2)
  return c
}

// === drawing code ===

// draw a 3d shape on a canvas
// 95% of the run time is in this function and its kids
function draw_shape(canvas, xf, shape, alpha) {
  var ctx = canvas.getContext('2d')
  var w = canvas.width
  var h = canvas.height

  // set up coordinate system so canvas is (-1, -1) to (1, 1)
  ctx.save()
  ctx.translate(w/2, h/2)
  ctx.scale(w/2, h/2)  

  // 1/3 of the time is in these two lines (when not doing polies)
  var points3d = xform_points(xf, shape[0])
  var points = persp_points(points3d)
  var lines = shape[1]
  // 2/3 of the time is in this loop (when we're not doing polies)
  if (alpha == null) {
    ctx.strokeStyle = 'grey'
    ctx.lineWidth = 1/(w/2)
    ctx.beginPath()
    var p1, p2
    for (var ii = 0; ii < lines.length; ii++) {
      p1 = points[lines[ii][0]]
      p2 = points[lines[ii][1]]
      ctx.moveTo(p1[0], p1[1])
      ctx.lineTo(p2[0], p2[1])
    }
    ctx.stroke()
  }

  // when we're doing polies, 90% of our time is spent doing polies
  if (alpha != null) {
    // Sort polygons by depth so we draw the farthest-away stuff first
    // ("painter's algorithm")
    var minusdepth = function(p) {
      return [-(points3d[p[0]][2] + points3d[p[1]][2] + points3d[p[2]][2]), p] 
    }
    var polies = map(minusdepth, shape[2])
    polies.sort(keycomp(function(p) { return p[0] }))

    // draw all the polygons
    var tri, p1, p2, p3, n, bright
    for (var ii = 0; ii < polies.length; ii++) {
      tri = polies[ii][1]
      if (alpha == '1') {
        // light surface
	n = normal(points3d[tri[0]], points3d[tri[1]], points3d[tri[2]])
	// I'm not sure how to make backface removal work with perspective: 
	// if (n[2] > 0 && alpha == '1') continue // backface removal

	// lighting from (1, -1, -1) direction
	bright = parseInt(((n[0]-n[1]-n[2]) / Math.sqrt(3) * 255))
	if (bright < 20) bright = 20
      } else {
        // lighting doesn't make sense if the object is transparent,
        // so we color by depth to have some variation in color...
        var maxd = polies[polies.length-1][0]
	var mind = polies[0][0]
	var d = polies[ii][0]
	bright = parseInt((d-mind)/(maxd-mind) * 255)
      }
      ctx.fillStyle = 'rgba(' + bright + ',' + bright + ',' + bright + 
                      ',' + alpha + ')'
      ctx.beginPath()
      p1 = points[tri[0]]
      p2 = points[tri[1]]
      p3 = points[tri[2]]
      ctx.moveTo(p1[0], p1[1])
      ctx.lineTo(p2[0], p2[1])
      ctx.lineTo(p3[0], p3[1])
      // ctx.closePath()  seems to be unnecessary
      ctx.fill()
    }
  }

  ctx.restore()
}
// clear a canvas
function cls(canvas) {
  var ctx = canvas.getContext('2d')
  ctx.fillStyle = 'black'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
}

// === drawing of particular shapes. also DOM. ===
angle = 0
function unit_cube() {
  var shape = point_shape(0, 0, 0)
  extrude_shape(translate(0,0,1), shape)
  extrude_shape(translate(0,1,0), shape)
  extrude_shape(translate(1,0,0), shape)
  return shape
}

// this was where I tested stuff as I wrote this
function make_some_junk() {
  // make a unit cube centered on the origin
  var shape = xform_shape(translate(-0.5, -0.5, -0.5), unit_cube())

  // add some circles
  augment(shape, circle(0.707, 16))
  augment(shape, xform_shape(transpose_axes(0, 2), circle(0.707, 16)))
  augment(shape, xform_shape(transpose_axes(1, 2), circle(0.707, 16)))
  augment(shape, circle(1, 15))

  // add a disc
  var big_disc = circle(2, 20)
  extrude_shape(translate(0, 0, 0.5), big_disc, 2)
  augment(shape, big_disc)
  return shape
}
var some_junk = make_some_junk()

function draw_some_junk(canvas) {
  var xf = concat_n([transpose_axes(1, 2),
                     rotate(angle),
      	     	     transpose_axes(1, 2),
		     rotate(angle*1.618),
		     translate(0, 0, 2.5)])

  draw_shape(canvas, xf, some_junk)
}
var torus = make_torus(1, 3, 12, 12)
function draw_torus(canvas) {
  var start = new Date()
  var alpha = null
  if ($('fill').checked) alpha = ($('translucent').checked ? '0.5' : 1)
  if ($('trails').checked) {
    $('canvas').getContext('2d').globalAlpha = 0.33
  } else {
    $('canvas').getContext('2d').globalAlpha = 1
  }
  draw_shape(canvas, concat_n([rotate(angle), 
  		               transpose_axes(1, 2),
                               rotate(angle / 1.618),  // to reduce periodicity
                               transpose_axes(1, 2),
                               translate(0, 0, 6)]),
             torus, alpha)
  var end = new Date()
  var ms = $('ms')
  if (ms) {
    var msvalue = ms.value + ' ' + (end.getTime() - start.getTime())
    if (msvalue.length > 25) msvalue = msvalue.substr(msvalue.length - 25)
    ms.value = msvalue
  }
}
function update() {
  if (!$('go').checked) return
  angle += 3.14159 / 30
  cls($('canvas'))
  draw_torus($('canvas'))
}
function init(ev) {
  setInterval(update, 100)
  // this doesn't work: $('fill').addEventListener('change', update, true)
  // how do you do what I want to do there?
  cls($('canvas'))
  draw_torus($('canvas'))
}
window.addEventListener('load', init, true)
// ]]>
</script>
</head><body><h1>A rotating torus</h1>
<p>I thought it would be fun one night to write a 3D engine in
JavaScript.  The next afternoon, I had this.</p>
<p>
<!-- <input id="ms" /> ms -->
<input id="go" type="checkbox" checked="checked" /><label for="go"> Go</label>
<input id="fill" type="checkbox" /><label for="fill"> Solid</label>
<input id="translucent" type="checkbox" /><label 
  for="translucent"> Translucent</label>
<input id="trails" type="checkbox" /><label for="trails"> Trails</label>
</p>
<canvas id="canvas" width="400" height="400">
</canvas>

<h2>Problems</h2>

<p>It's not perfect, for sure; I mean, heck, I spent less than a day
on it.  In case I come back to it later, I made the following list of
its biggest problems.</p>

<ul>
 <li>Because all the loops and all the math are in JavaScript, and
  because &lt;canvas&gt; drawing is painfully slow, it's painfully
  slow.  On a dual-core 1.8GHz machine, it does around 3000 polygons
  per second, with a single lighting source and flat shading.  There
  might be room in there to double its speed.  But it's still fast
  enough to be cool. (I mean, hey, that's about as fast as Mesa,
  right? :) )</li>
 <li>I haven't implemented Gouraud shading yet.</li>
 <li>I wish I could implement texture-mapping, but I'm pretty sure
  that'll be too slow.</li>
 <li>On one hand, it's great that the &lt;canvas&gt; widget does
  antialiased drawing with alpha by default.  But I wish I could turn
  it off for this &mdash; it results in dotted lines running down the
  diagonals of all the rectangles.  I could work around this to some
  extent by supporting rectangles (well, planar quadrilaterals) as a
  primitive, but that still leaves dotted dark lines running down the
  joints between them.  Supersampling antialiasing (where each pixel
  starts out as 4 or 9 pixels in a super-res image) would work.</li>
 <li>There's, like, no UI.  There are some nifty solid-modeling things
  in the code that aren't obvious from the UI.</li>
 <li>If you change the state of one of the checkboxes while "Go" is
  unchecked, nothing happens until you check "Go" again; </li>
 <li>There are still big optimization opportunities in the code; for example:
  <ul>
    <li> You could store the normals and transform them with the
     matrix instead of doing a cross-product on every polygon every frame;</li>
    <li> And in this object, there are eight times as many polygons as
     there are normals, so you could win even bigger;</li>
    <li> It's not even doing backface removal because I'm too dumb to figure out
     how to do it safely with perspective;</li>
    <li> If you had a numerical-array library like NumPy or PDL in
     your browser, you could probably make it 100 times faster,
     roughly as fast as a normal software renderer; also, the code
     would be a lot simpler; </li>
    <li> If you were doing it in 3D hardware, you could probably make
     it 100 times faster again;</li>
    <li> Maybe there's some way to do the Z-sort without invoking a
     JavaScript function for every pair of elements to be compared; </li>
    <li> There are lots of places where we might be able to cons less
     by preallocating arrays of the right size; </li>
    <li> And, going even further, there's really no reason it needs to
     cons at all when going from one frame to the next &mdash; it
     should be able to run in constant space;</li>
    <li> This is probably irrelevant except for the wire-frame, but
     unrolling the loops in 'xform' would probably make it faster; </li>
    <li> The 'extrude_shape' function may generate the same points and
     lines twice &mdash; in the torus, there are two copies of the
     beginning/end circle. </li>
 </ul></li>
 <li>The stitching logic in 'extrude_shape' is pretty tightly tied to
  how 'augment' works.</li>
 <li>Also, maybe it would be nice to be able to pull out that logic
  for use in some other kind of mesh-generation code, like something
  that graphs a height field or a parametric function.</li>
</ul>

</body></html>