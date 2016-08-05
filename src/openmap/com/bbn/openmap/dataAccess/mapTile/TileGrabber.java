
package com.bbn.openmap.dataAccess.mapTile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.bbn.openmap.dataAccess.mapTile.MapTileCoordinateTransform;
import com.bbn.openmap.dataAccess.mapTile.OSMMapTileCoordinateTransform;
import com.bbn.openmap.dataAccess.mapTile.ServerMapTileFactory;
import com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory.TilePathBuilder;
import com.bbn.openmap.dataAccess.mapTile.TMSMapTileCoordinateTransform;
import com.bbn.openmap.dataAccess.mapTile.ZoomLevelInfo;
import com.bbn.openmap.dataAccess.shape.EsriGraphic;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.coords.LatLonPoint;
import com.bbn.openmap.util.PropUtils;

/**
 * Utility class that fetches tiles from a http source. Runs as an application.
 * Prints out a usage statement that describes options.
 * 
 * @author dietrick
 */
public class TileGrabber {

	public TileGrabber() {

	}

	/**
	 * @author ddietrick
	 */
	public static class Builder {
		protected String source;
		protected String target = "";

		protected int fromZoom = 0;
		protected int toZoom = 0;

		protected int minx = 0;
		protected int miny = 0;
		protected int maxx = -1;
		protected int maxy = -1;

		protected double minlon = -180.0;
		protected double minlat = -85.0;
		protected double maxlon = 180.0;
		protected double maxlat = 85.0;

		protected boolean fill = false;
		protected boolean verbose = false;
		protected boolean extraVerbose = false;
		protected boolean tileBoundsSet = false;

		MapTileCoordinateTransform transform = new OSMMapTileCoordinateTransform();

		public Builder(String source) throws FileNotFoundException {

			if (source == null) {
				throw new FileNotFoundException("Source file invalid");
			}

			this.source = source;
		}

		public Builder targetFile(String targetFile) {
			this.target = targetFile;

			return this;
		}

		public Builder fromZoom(int zoomLevel) throws NumberFormatException {
			if (checkZoomLevel(zoomLevel)) {
				this.fromZoom = zoomLevel;
			}

			if (this.fromZoom > this.toZoom) {
				this.toZoom = this.fromZoom;
			}

			return this;
		}

		public Builder toZoom(int zoomLevel) throws NumberFormatException {
			if (checkZoomLevel(zoomLevel)) {
				this.toZoom = zoomLevel;

				if (this.fromZoom > this.toZoom) {
					this.fromZoom = this.toZoom;
				}
			}
			return this;
		}

		protected boolean checkZoomLevel(int zoomLevel) {
			if (zoomLevel < 0 || zoomLevel > 20) {
				throw new NumberFormatException("Zoom level needs to be > 0 and < 20");
			}
			return true;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder("WholeWorldTileHandler[");
			sb.append("source:").append(source).append(',');
			sb.append("target:").append(target).append(',');
			sb.append("fromZoom:").append(fromZoom).append(',');
			sb.append("toZoom:").append(toZoom).append(',');
			if (minx != 0) {
				sb.append(",").append("minx:").append(minx);
			}
			if (miny != 0) {
				sb.append(",").append("miny:").append(miny);
			}
			if (maxx >= 0) {
				sb.append(",").append("maxx:").append(maxx);
			}
			if (maxy >= 0) {
				sb.append(",").append("maxy:").append(maxy);
			}

			sb.append(']');

			return sb.toString();
		}

		protected int[] getTileBoundsForProjection(LatLonPoint ul, LatLonPoint lr, int z) {
			if (fromZoom == toZoom && tileBoundsSet) {
				ZoomLevelInfo zoomInfo = new ZoomLevelInfo();
				zoomInfo.setZoomLevel(z);
				int maxDim = zoomInfo.getEdgeTileCount();

				if (maxx == -1) {
					maxx = maxDim;
				}

				if (maxy == -1) {
					maxy = maxDim;
				}

				return new int[] { miny, minx, maxy, maxx };
			}

			return transform.getTileBoundsForProjection(ul, lr, z);
		}

		/**
		 * source:
		 * "http://192.168.99.100:32771/mapbox-studio-osm-bright/{z}/{x}/{y}.png"
		 * target: "/Users/dietrick/Downloads/st_osm_tiles"
		 * 
		 * @throws FileNotFoundException
		 * @throws IOException
		 */

		public void go() throws FileNotFoundException, IOException {

			LatLonPoint ul = new LatLonPoint.Double(maxlat, minlon);
			LatLonPoint lr = new LatLonPoint.Double(minlat, maxlon);

			ServerMapTileFactory tileServer = new ServerMapTileFactory();
			TilePathBuilder serverPathBuilder = new TilePathBuilder(source);
			TilePathBuilder localPathBuilder = new TilePathBuilder(target);

			ZoomLevelInfo zoomInfo = new ZoomLevelInfo();

			for (int z = fromZoom; z <= toZoom; z++) {

				int[] uv = getTileBoundsForProjection(ul, lr, z);

				int startX = uv[1];
				int endX = uv[3];
				int startY = uv[0];
				int endY = uv[2];

				if (verbose) {
					System.out.println("fetching tiles for zoom level " + z + " between " + startY + ", " + startX
							+ " and " + endY + ", " + endX);
				}

				zoomInfo.setZoomLevel(z);
				int maxDim = zoomInfo.getEdgeTileCount();
				for (int x = startX; x <= endX && x <= maxDim - 1; x++) {
					for (int y = startY; y <= endY && y <= maxDim - 1; y++) {

						String serverImagePath = serverPathBuilder.buildTilePath(x, y, z, ".png");
						String localFilePath = localPathBuilder.buildTilePath(x, y, z, ".png");

						if (fill) {
							File localFile = new File(localFilePath);
							if (localFile.exists()) {
								if (extraVerbose) {
									System.out.println("--- skipping " + localFilePath + ", aready got it");
								}
								continue;
							}
						}

						if (extraVerbose) {
							System.out.println("<<< fetching " + serverImagePath);
						}
						tileServer.getImageBytes(serverImagePath, localFilePath);
					}
				}
			}
		}

		public void go(String shapefile) throws FileNotFoundException, IOException {
			try {
				if (verbose) {
					System.out.println("fetching tiles covering " + shapefile);
				}
				URL shp = PropUtils.getResourceOrFileOrURL(shapefile);
				EsriGraphicList egl = EsriGraphicList.getEsriGraphicList(shp, null, null);

				fetchTilesForEsriGraphicList(egl);

			} catch (MalformedURLException murle) {
				throw new FileNotFoundException(murle.getMessage());
			}
		}

		protected void fetchTilesForEsriGraphicList(EsriGraphicList egl) throws FileNotFoundException, IOException {
			for (OMGraphic omg : egl) {
				if (omg instanceof EsriGraphicList) {
					fetchTilesForEsriGraphicList((EsriGraphicList) omg);
				} else if (omg instanceof EsriGraphic) {
					fetchTilesForEsriGraphic((EsriGraphic) omg);
				}
			}
		}

		protected void fetchTilesForEsriGraphic(EsriGraphic eg) throws FileNotFoundException, IOException {
			// miny, minx, maxy maxx
			double[] coords = eg.getExtents();
			minlat = coords[0];
			minlon = coords[1];
			maxlat = coords[2];
			maxlon = coords[3];

			go();
		}

		/**
		 * Set the starting x number of the subjar file to create. Depends on
		 * the subjar zoom to figure out what that means.
		 * 
		 * @param parseInt
		 */
		public void minx(int parseInt) {
			minx = parseInt;
			tileBoundsSet = true;
		}

		/**
		 * Set the starting y number of the subjar file to create. Depends on
		 * the subjar zoom to figure out what that means.
		 * 
		 * @param parseInt
		 */
		public void miny(int parseInt) {
			miny = parseInt;
			tileBoundsSet = true;
		}

		/**
		 * Set the ending y number of the subjar file to create. Depends on the
		 * subjar zoom to figure out what that means.
		 * 
		 * @param parseInt
		 */
		public void maxx(int parseInt) {
			maxx = parseInt;
			tileBoundsSet = true;
		}

		/**
		 * Set the ending y number of the subjar file to create. Depends on the
		 * subjar zoom to figure out what that means.
		 * 
		 * @param parseInt
		 */
		public void maxy(int parseInt) {
			maxy = parseInt;
			tileBoundsSet = true;
		}

		/**
		 * set lower longitude of fetching
		 * 
		 * @param parsed
		 */
		public void minlon(double parsed) {
			minlon = parsed;
		}

		/**
		 * set lower latitude of fetching
		 * 
		 * @param parsed
		 */
		public void minlat(double parsed) {
			minlat = parsed;
		}

		/**
		 * set upper longitude of fetching
		 * 
		 * @param parsed
		 */
		public void maxlon(double parsed) {
			maxlon = parsed;
		}

		/**
		 * set upper latitude of fetching
		 * 
		 * @param parsed
		 */
		public void maxlat(double parsed) {
			maxlat = parsed;
		}

		/**
		 * Check whether the build process will only fetch tiles that don't
		 * exist.
		 */
		public boolean isFill() {
			return fill;
		}

		/**
		 * Set whether the build process will only fetch tiles that don't exist.
		 * 
		 * @param fill
		 */
		public void setFill(boolean fill) {
			this.fill = fill;
		}
	}

	/**
	 * Takes arguments for source tile directory, target directory, and option
	 * sub-jar zoom level, and creates jars in the right place with expected
	 * tiles. Prints usage statement.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		com.bbn.openmap.util.ArgParser ap = new com.bbn.openmap.util.ArgParser("TileGrabber");

		ap.add("source", "Path to fetch tiles from, should be http://path/{z}/{x}/{y}.png form", 1);
		ap.add("target", "Path to the output directory for fetched tiles", 1);
		ap.add("fromZoom", "Starting zoom level", 1);
		ap.add("toZoom", "Ending zoom level", 1);
		ap.add("minx", "min x tile (only used if fromZoom and toZoom match)", 1);
		ap.add("miny", "min y tile (only used if fromZoom and toZoom match)", 1);
		ap.add("maxx", "max x tile (only used if fromZoom and toZoom match)", 1);
		ap.add("maxy", "max y tile (only used if fromZoom and toZoom match)", 1);

		ap.add("minlon", "min longitude", 1);
		ap.add("minlat", "min latitude", 1);
		ap.add("maxlon", "max longitude", 1);
		ap.add("maxlat", "max latitude", 1);

		ap.add("shapefile", "fetch coverage over shapes in shapefile", 1);

		ap.add("verbose", "Describe what's going on.");
		ap.add("extraVerbose", "Really describe what's going on.");
		ap.add("fill", "Only fetch tiles that don't exist.");
		ap.add("TMS", "Specify that the tile numbering scheme matches TMS (OSM is default)");

		if (!ap.parse(args)) {
			ap.printUsage();
			System.exit(0);
		}

		String[] arg = ap.getArgValues("source");
		if (arg != null) {
			try {
				Builder wwthBuilder = new Builder(arg[0]);

				arg = ap.getArgValues("target");
				if (arg != null) {
					wwthBuilder.targetFile(arg[0]);
				}

				arg = ap.getArgValues("fromZoom");
				if (arg != null) {
					wwthBuilder.fromZoom(Integer.parseInt(arg[0]));
				}

				arg = ap.getArgValues("toZoom");
				if (arg != null) {
					wwthBuilder.toZoom(Integer.parseInt(arg[0]));
				}

				arg = ap.getArgValues("minx");
				if (arg != null) {
					wwthBuilder.minx(Integer.parseInt(arg[0]));
				}

				arg = ap.getArgValues("miny");
				if (arg != null) {
					wwthBuilder.miny(Integer.parseInt(arg[0]));
				}

				arg = ap.getArgValues("maxx");
				if (arg != null) {
					wwthBuilder.maxx(Integer.parseInt(arg[0]));
				}

				arg = ap.getArgValues("maxy");
				if (arg != null) {
					wwthBuilder.maxy(Integer.parseInt(arg[0]));
				}

				arg = ap.getArgValues("minlon");
				if (arg != null) {
					wwthBuilder.minlon(Double.parseDouble(arg[0]));
				}

				arg = ap.getArgValues("minlat");
				if (arg != null) {
					wwthBuilder.minlat(Double.parseDouble(arg[0]));
				}

				arg = ap.getArgValues("maxlon");
				if (arg != null) {
					wwthBuilder.maxlon(Double.parseDouble(arg[0]));
				}

				arg = ap.getArgValues("maxlat");
				if (arg != null) {
					wwthBuilder.maxlat(Double.parseDouble(arg[0]));
				}
				arg = ap.getArgValues("verbose");
				if (arg != null) {
					wwthBuilder.verbose = true;
				}
				arg = ap.getArgValues("extraVerbose");
				if (arg != null) {
					wwthBuilder.verbose = true;
					wwthBuilder.extraVerbose = true;
				}

				arg = ap.getArgValues("fill");
				if (arg != null) {
					wwthBuilder.setFill(true);
				}

				arg = ap.getArgValues("TMS");
				if (arg != null) {
					wwthBuilder.transform = new TMSMapTileCoordinateTransform();
				}

				arg = ap.getArgValues("shapefile");
				if (arg != null) {

					wwthBuilder.go(arg[0]);

				} else {

					System.out.println(wwthBuilder.toString());
					wwthBuilder.go();
				}

			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
				System.out.println(nfe.getMessage());
			} catch (FileNotFoundException fnfe) {
				fnfe.printStackTrace();
				System.out.println(fnfe.getMessage());
			} catch (IOException ioe) {
				ioe.printStackTrace();
				System.out.println(ioe.getMessage());
			}
		} else {
			ap.bail("Need a source directory.", true);
		}

		System.exit(0);
	}
}
