package com.bbn.openmap.dataAccess.mapTile.mb;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MVTMapTileFactory can fetch vector tile data from a URL. It can handle
 * pbf data, too, just set compressed to false and change the rootDir
 * appropriately.
 * 
 * <pre>
 * 
 * mvtLayer.class=com.bbn.openmap.layer.image.MapTileLayer
 * mvtLayer.tileFactory=com.bbn.openmap.dataAccess.mapTile.mb.MVTMapTileFactory
 * mvtLayer.prettyName=MVT Server
 * mvtLayer.rootDir=http://sample_server/osm/water/{z}/{x}/{y}.mvt?api_key={api-key}
 * 
 * #optional - mbtiles and mvt files have compressed data, pbf data is not compressed.
 * mvtLayer.compressed=true
 * </pre>
 */

public class MVTMapTileFactory extends VectorMapTileFactory {

	public MVTMapTileFactory() {

	}

	public String getFileExt() {
		return "";
	}

	/**
	 * Fetch tile data. Uses the key as the path to the file.
	 * 
	 * @param key
	 *            the path to the tile built up from rootDir and the
	 *            TilePathBuilder source location.
	 * @param x
	 *            horizontal tile column
	 * @param y
	 *            vertical tile row
	 * @param zoomLevel
	 *            tile zoom level
	 * @return uncompressed tile data, ready for decoding
	 */
	public byte[] getTileData(Object key, int x, int y, int zoomLevel) throws Exception {

		String tilePath = (String) key;

		try {
			java.net.URL url = new java.net.URL(tilePath);
			java.net.HttpURLConnection urlc = (java.net.HttpURLConnection) url.openConnection();

			if (getLogger().isLoggable(Level.FINER)) {
				getLogger().finer("url content type: " + urlc.getContentType());
			}

			if (urlc == null || urlc.getContentType() == null) {
				if (getLogger().isLoggable(Level.FINE)) {
					getLogger().fine("unable to connect to (tile might be unavailable): " + tilePath);
				}

				// text
			} else if (urlc.getContentType().startsWith("text")) {
				java.io.BufferedReader bin = new java.io.BufferedReader(
						new java.io.InputStreamReader(urlc.getInputStream()));
				String st;
				StringBuffer message = new StringBuffer();
				while ((st = bin.readLine()) != null) {
					message.append(st);
				}

				// Debug.error(message.toString());
				// How about we toss the message out to the user
				// instead?
				getLogger().fine(message.toString());

				// image
			} else {

				InputStream in = urlc.getInputStream();
				// ------- Testing without this
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int buflen = 2048; // 2k blocks
				byte buf[] = new byte[buflen];
				int len = -1;
				while ((len = in.read(buf, 0, buflen)) != -1) {
					out.write(buf, 0, len);
				}
				out.flush();
				out.close();

				if (compressed) {
					return inflate(out.toByteArray());
				} else {
					return out.toByteArray();
				}

			} // end if image
		} catch (java.net.MalformedURLException murle) {
			getLogger().warning("URL \"" + tilePath + "\" is malformed.");
		} catch (java.io.IOException ioe) {
			getLogger().fine("Couldn't connect to " + tilePath + ", connection problem");
		}

		return null;
	}

	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(MVTMapTileFactory.class.getName());

		/**
		 * Prevent instantiation
		 */
		private LoggerHolder() {
			throw new AssertionError("The LoggerHolder should never be instantiated");
		}
	}

	/**
	 * Get the logger for this class.
	 *
	 * @return logger for this class
	 */
	private static Logger getLogger() {
		return LoggerHolder.LOGGER;
	}
	// </editor-fold>
}
