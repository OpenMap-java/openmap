package com.bbn.openmap.dataAccess.mapTile.tpk;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import com.bbn.openmap.dataAccess.mapTile.StandardMapTileFactory;
import com.bbn.openmap.image.BufferedImageHelper;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.MoreMath;
import com.bbn.openmap.util.PropUtils;
import com.bbn.openmap.util.cacheHandler.CacheObject;

/**
 * MapTileFactory that tries to pull images from a TPK package. TPKs are
 * actually zip files containing a bunch of stuff.
 * 
 * @author dietrick
 *
 */
public class TpkMapTileFactory extends StandardMapTileFactory {

	public final static String TPK_FILE_PROPERTY = "tpk";

	protected String tpkLocation;
	protected TpkReader tpkReader;

	public TpkMapTileFactory() {
		logger.fine("Using TpkMapTileFactory");
		setRootDir("TPK");
	}

	/**
	 * Loads the tile from the bundle file, after consulting the tpkReader where
	 * it might be and what bundle file to check.
	 *
	 * @param key
	 *            the cache key for this tile
	 * @param x
	 *            the world x index for tile
	 * @param y
	 *            the world y index for tile
	 * @param zoomLevel
	 *            the zoom level of current projection
	 * @param proj
	 *            the current map projection
	 * @return a CacheObject that contains the tile.
	 */
	public CacheObject load(Object key, int x, int y, int zoomLevel, Projection proj) {
		if (key instanceof String) {
			if (tpkReader != null) {
				String pathToTiles = tpkReader.getPathToTiles();

				StringBuilder bundleName = new StringBuilder(pathToTiles).append("/L");
				if (zoomLevel < 10) {
					bundleName.append("0");
				}
				bundleName.append(zoomLevel).append("/");

				// Does packetsize from conf.xml make a difference here? For
				// now, assume 128
				int row = (y / 128) * 128;
				int col = (x / 128) * 128;

				String rowPart = Integer.toHexString(row);
				String colPart = Integer.toHexString(col);

				while (rowPart.length() < 4) {
					rowPart = "0" + rowPart;
				}
				while (colPart.length() < 4) {
					colPart = "0" + colPart;
				}

				bundleName.append("R").append(rowPart).append("C").append(colPart);
				String bundlxName = new StringBuilder(bundleName).append(".bundlx").toString();

				if (logger.isLoggable(Level.FINE)) {
					logger.fine("looking for tile in " + bundleName.toString());
				}

				try {
					InputStream is = tpkReader.getStream(bundlxName);
					BundleX bundlex = new BundleX(is);
					is.close();

					int rowIndex = y % 128;
					int colIndex = x % 128;

					int offset = bundlex.getOffset(colIndex, rowIndex);

					if (offset != -1) {
						InputStream bundleStream = tpkReader.getStream(bundleName.append(".bundle").toString());
						bundleStream.skip(offset);

						// int length = bbf.readInteger();
						byte[] lengthVec = new byte[4];
						bundleStream.read(lengthVec);
						int length = MoreMath.BuildIntegerLE(lengthVec, 0);

						byte[] imageBytes = new byte[length];
						read(bundleStream, imageBytes, 0, imageBytes.length);
						bundleStream.close();

						if (imageBytes != null && imageBytes.length > 2000) {
							// image found
							ImageIcon ii = new ImageIcon(imageBytes);

							try {
								BufferedImage rasterImage = preprocessImage(ii.getImage(), ii.getIconWidth(),
										ii.getIconHeight());
								OMGraphic raster = createOMGraphicFromBufferedImage(rasterImage, x, y, zoomLevel, proj);

								/*
								 * Again, create a CacheObject based on the
								 * local name if the local dir is defined.
								 */
								if (raster != null) {
									if (logger.isLoggable(Level.FINE)) {
										logger.fine("building" + key);
									}

									return new CacheObject(key, raster);
								}

							} catch (InterruptedException ie) {
								if (logger.isLoggable(Level.FINE)) {
									logger.fine("factory interrupted fetching " + key);
								}
							}

						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				logger.warning("TpkMapTileFactory: tpkReader is null");
			}

		}
		return null;
	}

	public int read(InputStream inputStream, byte b[], int off, int len) throws IOException {

		if (inputStream == null) {
			throw new IOException("Stream closed");
		}

		int gotsofar = 0;
		while (gotsofar < len) {
			int read = inputStream.read(b, off + gotsofar, len - gotsofar);
			if (read == -1) {
				if (gotsofar > 0) {
					// Hit the EOF in the middle of the loop.
					return gotsofar;
				} else {
					return read;
				}
			} else {
				gotsofar += read;
			}
		}

		return gotsofar;
	}

	protected BufferedImage preprocessImage(Image origImage, int imageWidth, int imageHeight)
			throws InterruptedException {

		return BufferedImageHelper.getBufferedImage(origImage, 0, 0, 256, 256, BufferedImage.TYPE_INT_ARGB);
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		prefix = PropUtils.getScopedPropertyPrefix(prefix);

		String tpkLoc = props.getProperty(prefix + TPK_FILE_PROPERTY);
		if (tpkLoc != null) {
			loadTpk(tpkLoc);
		}
	}

	public Properties getProperties(Properties props) {
		props = super.getProperties(props);
		String prefix = PropUtils.getScopedPropertyPrefix(this);

		props.put(prefix + TPK_FILE_PROPERTY, PropUtils.unnull(tpkLocation));

		return props;
	}

	protected void loadTpk(String tpkLoc) {
		try {

			this.tpkReader = new TpkReader(tpkLoc);
			this.tpkLocation = tpkLoc;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
