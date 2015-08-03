# Introduction #

The OpenMap package includes applications that demonstrate the capabilities of the toolkit.  There are many layer examples that also demonstrate how to create custom layers for displaying your own data.


# Applications #

The application classes in OpenMap are in the {{com.bbn.openmap.app}}} package.

The `com.bbn.openmap.app.Main` class was introduced in OpenMap 5.0.  It uses the openmap.properties file for configuration, with the `main.components` defining the application components.  The `openmap.layers` property defines the layers used in the application.  This is the application that gets started by the scripts in the bin directory.  You can start this application at the command line by typing:

`java -classpath ./lib/openmap.jar:./share com.bbn.openmap.app.Main -properties ./openmap.properties`

The `com.bbn.openmap.app.OpenMap` class is the legacy application that uses the `openmap.properties` file to define the components in the application.  The `openmap.components` property in `openmap.properties` lists the components that will be added to the application, and the `openmap.layers` property defines the layers in the application.

`java -classpath ./lib/openmap.jar:./share com.bbn.openmap.app.OpenMap -properties ./openmap.properties`

Both applications are the best way to start getting to know OpenMap, and the best starting point for your own application. You can add or remove components in the application using the {{openmap.properties}}} file, which contains instructions for how to modify the various properties within it.

If you want to a deeper understanding on how the OpenMap application framework should be used, the `com.bbn.openmap.app.example` package contains `SimpleMap` and `SimpleMap2`.  Both examples are heavily commented, and provide examples of how to programmatically configure OpenMap mapping components.

`java -classpath ./lib/openmap.jar:./share com.bbn.openmap.app.example.SimpleMap`

`java -classpath ./lib/openmap.jar:./share com.bbn.openmap.app.example.SimpleMap2`

# Layers #

OpenMap includes many layers that work with standard map data formats.  These layers are under the `com.bbn.openmap.layers` package.  The `openmap.properties` file contains examples of how to configure properties for some of them, and the javadocs for a layer class should contain property settings and instructions for configuring the layer.

If you want to learn to write your own layer, look at the layers in the `com.bbn.openmap.layers.learn` package:
  * `BasicLayer` shows how to make a layer where the data doesn't change depending on time or map projection.  A good first layer.
  * `ProjectionResponseLayer` shows how to make a layer that responds to the map projection to display map features for the current view.
  * `InteractionLayer` shows how to make the layer react to user input.
  * `SimpleAnimationLayer` shows how to move features around on a map.

Also, the `com.bbn.openmap.layer.DemoLayer` is an excellent tool, demonstrating almost anything you may want to do with a layer.  It is heavily commented, and studying that layer will reveal how to respond to user input, use the drawing tool to edit map features, create a Layer gui to change properties of the layer, use the MIL-STD-2525 symbology components, and display various OMGraphic map features with labels.  This layer is also part of the standard `OpenMap` and `Main` application configuration, so you can see that layer in action by running those applications.

# Servlets #

OpenMap can be used to power servlets (using glassfish or tomcat) that provide map data to other applications.  From the src directory:

  * `wmsservlet` provides a servlet that can be used to provide WMS images.  In addition to whole map images, the wmsservlet can respond to request to create TMS (Tile Map Specification) sized images on the fly.
  * `maptileservlet` provides a servlet that can handle requests for TMS (Tile Map Specification) layers, such as Leaflet, OpenLayers and Google Maps.  These tile map sets are assumed to be pre-created using mapnik, TileMill, GDAL, or OpenMap's MapTileMaker.  The OpenMap `MapTileLayer` can talk to this servlet (and any other TMS server).  Also, when the `maptileservlet` is loaded with `MapTileSet` tiles, contacting the servlet at the top-level will bring up a Leaflet map allowing you to look at the tile sets.

The `MapTileMaker` is a class that creates tile sets.  It uses a properties file to define layers and lets zoom levels be defined to dictate which layers are active for different tile scales.  The `MapTileMaker` has recently been updated to implement `EmptyTileHandler`, which means it can now be used by the `maptileservlet` to create tiles on the fly, and cache them for later use.

The `vpfservlet` doesn't serve VPF data, it provides a way to explore VPF datasets.

# Other #

You can use OpenMap map components in any java application that supports Swing components (so mobile SDKs generally don't work with OpenMap).  You can embed OpenMap into an existing application.  Look at the `BasicMapPanel` and `OverlayMapPanel` as the main component to embed into another application.

OpenMap runs as an applet. The share directory contains an omapplet.html page to get started.  There is a jnlp file to run OpenMap as a Java WebStart application.