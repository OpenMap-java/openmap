# Introduction #

Map tiles are images stored in a directory structure, usually by zoom level, x coordinate and then named by their y coordinate (z/x/y.png).

The _com.bbn.openmap.layer.imageTile.MapTileLayer_ can be used to display map tiles, stored locally on your computer or downloaded from a server.  There are many options available for the MapTileLayer, and they can be set in the _openmap.properties_ file, or in a _tiles.omp_ file stored in the root directory of a tile set.

# Configuration Options #

The most basic configuration of a MapTileLayer simply define a **rootDir** property defining where the tiles are located and how they should be referenced.

```
class=com.bbn.openmap.layer.imageTile.MapTileLayer
prettyName=Map Tiles
rootDir=/data/tiles/MapTileSet1/{z}/{x}/{y}.png
```

Other options:

**rootDir**: The rootDir can be a URL, absolute or relative path to the tile files. It can also be a jar file.  If you use a jar file to hold the tiles, you should use a tiles.omp inside the jar to hold other properties, like another rootDir property for the relative path to the files within the jar.  The **rootDir** can also point to a TileMill mbtiles file.  If it does, you'll need to set the **tileFactory** to use the _com.bbn.openmap.dataAccess.mapTile.TileMillMapTileFactory_.  You'll also need to include the sqlitejdbc jar file in the classpath to use the TileMillMapTileFactory.

**localCacheRootDir**: If the **rootDir** is defined as a URL, you can also use a **localCacheRootDir** property to define local storage for the tiles.  If the layer will check the **localCacheRootDir** first for tiles, and then move to the **rootDir** location if needed.  This will drastically reduce the server load.

**tileFactory**:  The MapTileLayer uses a MapTileFactory to handle the fetching of tiles.  For URLs and file paths, you generally don't have to set this, the layer will figure out which one to use.  You can check out the _com.bbn.openmap.dataAccess.mapTile_ package for various MapTileFactory implementations, and look at their javadocs for more information about specific properties for those factory objects.

**cacheSize**: the number of tiles to hold in memory for the layer, defaults to 50.

**attribution**: if you aren't using your own tiles, this property can be used to display the tiles' owner's name in the lower left corner of the map.

**mapTileTransform**: The default MapTileCoordinateTransform is the _com.bbn.openmap.dataAccess.mapTile.OSMMapTileTCoordinateTransform_ class, and that defines the tile coordinates from the upper left corner of the Mercator projection, with zoom levels starting at 0 and zooming into higher levels.  If you are working with tiles defined differently, you can specify a different class to use for those tiles.  For instance, if you use GDAL to create image tiles sets you may need to use the _com.bbn.openmap.dataAccess.mapTile.TMSMapTileCoordinateTransform_.

**tileImagePreparer**: defines a class that can be used to process the images before display.  The _com.bbn.openmap.dataAccess.mapTile.GreyscaleImagePreparer_ will convert all tiles to greyscale.

**emptyTileHandler**: defines a class that determines what to do when a tile is missing.  The _com.bbn.openmap.dataAccess.mapTile.ShpFileEmptyTileHandler uses a shape file to draw a basic shape onto a temporarily used tile._

```
emptyTileHandler=com.bbn.openmap.dataAccess.mapTile.ShpFileEmptyTileHandler
shpFile=File, resource or URL to shape file for land representation.
  
 # Properties to set how the shp file contents are rendered.
 land.fillColor=hex RGB color
 land.lineColor=hex RGB color
 land.fillPattern=path to resource, file or URL of pattern to use for tile fill.
 
 # From SimpleEmptyTileHandler superclass, handling the 'water'
 # clear by default if not specified
 background.fillColor=hex RGB color
 background.lineColor=hex RGB color
 background.fillPattern=path to resource, file or URL of pattern to use for tile fill.
  
 # Zoom level to start using noCoverage attributes.  Is 0 by default if the shape file 
 # is not specified.  If the shape file is specified and this isn't the zoom level 
 # will be set to 20.
 noCoverageZoom=zoom level when you don't want empty tiles, you want no coverage tiles
 
 # How to render standard empty tiles, will be clear if not defined
 noCoverage.fillColor=hex RGB color
 noCoverage.lineColor=hex RGB color
 noCoverage.fillPattern=path to resource, file or URL of pattern to use for tile fill.
```

# Adding a MapTileLayer to the Application #

To add a MapTileLayer to the map, you can modify the _openmap.properties_ file.  First, pick short word name for your layer, any little word, your choice.  It should be unique compared to all of the other names listed in the _openmap.layers_ property.  Next, add your name to that _openmap.layers_ list where you want your layer to be listed in the layer stack (first on the list is on top of the map).  Then, add properties for your layer in the _openmap.properties_ file, using the name you picked for your layer as a scoping prefix for the properties.  For example, let's say you chose 'osm\_tiles' for your name.

```
openmap.layers=... osm_tiles ...

osm_tiles.class=com.bbn.openmap.layer.imageTile.MapTileLayer
osm_tiles.prettyName=OSM Tiles
osm_tiles.rootDir=http://c.tile.openstreetmap.org/

# Add other properties, starting with 'osm_tiles.', as needed for your configuration
```

If you want the layer to show up when the application is started, also add the **osm\_tiles** name to the _openmap.startupLayers_ list.

To add a MapTileLayer to the SimpleMap2 example, you don't really need a property prefix for the properties:

```
 // Just add this code
 MapTileLayer mapTileLayer = new MapTileLayer();
 Properties tileProperties = new Properties();
 tileProperties.setProperty("rootDir", "http://c.tile.openstreetmap.org/");
 mapTileLayer.setProperties(tileProperties);
 mapTileLayer.setVisible(true);
 mapHandler.add(mapTileLayer);
```