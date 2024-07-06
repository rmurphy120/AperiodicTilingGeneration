<h1>AperiodicTilingGenerator</h1>

<h3>Overview</h3>
<p>This was a really fun project done over a couple of weeks inspired by Simon Tatham (https://www.chiark.greenend.org.uk/~sgtatham/quasiblog/aperiodic-tilings/). Right now can generate a random subsection of the kite/dart Penrose tiling. The code is set up so that in the future it could be easy to add other aperiodic tiling, such as rhombs Penrose tiling and hat tiling (Mentioned on the website). Has 2 modes with one having two options. The first mode displays the algorithm progressing through the entire triangle at different depths. The 2nd modes first option is to display the triangle with a random subsection taken from it. It helps visualize the optimization algorithm utilized. The other option is a fullscreen which tiles the window. It takes the previous output and scales it up to fullscreen. I consider these options to be progressions of each other, adding more complexity each time with the fullscreen being the most complex.</p>
<h3>Math diagrams</h3>
The following are a series of diagrams and equations to help with the math supporting the code (There is quite a bit of ambiguous math)
<br/>
<p>Overview of the different types of KDPenroseTri (Short for kite/dart Penrose triangles)</p>
<img src="Screenshots/Screenshot 2024-07-06 073051.png" style="width:400px;height:400px;">
<br/>
<p>Properties of rootTri</p>
<img src="Screenshots/Screenshot 2024-07-06 073107.png" style="width:400px;height:400px;">
<br/>
<p>Partition rules</p>
<img src="Screenshots/Screenshot 2024-07-06 164632.png" style="width:350px;height:400px;">
<br/>
<p>Depth calculation based on a desired density</p>
<img src="Screenshots/Screenshot 2024-07-06 073124.png" style="width:500px;height:500px;">
<br/>
<p>The next 3 images relate to each of the 3 cases in order to check if a solid triangle overlaps with a solid rectangle</p>
<img src="Screenshots/Screenshot 2024-07-06 073137.png" style="width:400px;height:300px;">
<img src="Screenshots/Screenshot 2024-07-06 073213.png" style="width:450px;height:500px;">
<img src="Screenshots/Screenshot 2024-07-06 073234.png" style="width:450px;height:450px;">
