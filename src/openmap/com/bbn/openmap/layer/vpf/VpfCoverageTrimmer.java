package com.bbn.openmap.layer.vpf;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.dataAccess.shape.DbfTableModel;
import com.bbn.openmap.dataAccess.shape.EsriGraphic;
import com.bbn.openmap.dataAccess.shape.EsriGraphicList;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.PropUtils;

public class VpfCoverageTrimmer {
	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {
		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger.getLogger(VpfCoverageTrimmer.class.getName());

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

	private String sourceVpfLibLoc;
	private LibrarySelectionTable lst;
	private File outputLocation;
	private List<Rectangle2D> filters;

	private VpfCoverageTrimmer(Builder builder) {
		this.sourceVpfLibLoc = builder.vpfPath;
		this.lst = builder.lst;
		this.outputLocation = builder.outputLocation;
		this.filters = builder.filters;
	}

	public void go() throws IOException {

		for (Rectangle2D filter : filters) {
			trim(filter);
		}

		copyFilesNotDirectories(new File(sourceVpfLibLoc));
	}

	protected void copyTileDirectories(File sourceLoc) throws IOException {

		if (sourceLoc.isDirectory()) {
			for (File currentFile : sourceLoc.listFiles()) {
				String localPath = currentFile.getAbsolutePath().substring(sourceVpfLibLoc.length() + 1);

				String destFilePath = outputLocation.getAbsolutePath() + "/" + localPath;

				if (currentFile.isDirectory()) {
					File subDir = new File(destFilePath);
					subDir.mkdirs();
					copyTileDirectories(subDir);
				} else {
					File destFile = new File(destFilePath);
					if (destFile.exists()) {
						continue;
					}
					destFile.getParentFile().mkdirs();
					FileUtils.copy(currentFile, destFile, 1028);
				}

			}
		}
	}

	protected void copyFilesNotDirectories(File sourceLoc) throws IOException {
		if (sourceLoc.isDirectory()) {
			for (File currentFile : sourceLoc.listFiles()) {
				String localPath = currentFile.getAbsolutePath().substring(sourceVpfLibLoc.length() + 1);
				String destFilePath = outputLocation.getAbsolutePath() + "/" + localPath;

				if (currentFile.isDirectory()) {
					File subDir = new File(destFilePath);
					if (subDir.exists()) {
						copyFilesNotDirectories(currentFile);
					}
				} else {
					File destFile = new File(destFilePath);
					if (destFile.exists()) {
						continue;
					}
					destFile.getParentFile().mkdirs();
					FileUtils.copy(currentFile, destFile, 1028);
				}

			}
		}
	}

	protected void trim(Rectangle2D filter) throws IOException {

		double ulx = filter.getX();
		double uly = filter.getY();
		double lrx = ulx + filter.getWidth();
		double lry = uly - filter.getHeight();

		uly = Math.toDegrees(ProjMath.normalizeLatitude(Math.toRadians(uly), Math.toRadians(10.0)));
		lry = Math.toDegrees(ProjMath.normalizeLatitude(Math.toRadians(lry), Math.toRadians(10.0)));
		ulx = Math.toDegrees(ProjMath.wrapLongitude(Math.toRadians(ulx)));
		lrx = Math.toDegrees(ProjMath.wrapLongitude(Math.toRadians(lrx)));

		if (getLogger().isLoggable(Level.FINE)) {
			getLogger().fine("filtering on: " + uly + ", " + lry + ", " + lrx + ", " + ulx);
		}

		try {

			for (String libName : lst.getLibraryNames()) {
				CoverageAttributeTable cat = lst.getCAT(libName);
				List<TileDirectory> td = cat.tilesInRegion((float) uly, (float) lry, (float) lrx, (float) ulx);
				if (td != null) {
					for (TileDirectory t : td) {
						for (String covName : cat.getCoverageNames()) {
							copyTileDirectories(new File(sourceVpfLibLoc, libName + "/" + covName + "/" + t.getPath()));
						}

						copyTileDirectories(new File(sourceVpfLibLoc, libName + "/tileref"));
						copyTileDirectories(new File(sourceVpfLibLoc, libName + "/libref"));
					}
				}
			}

		} catch (FormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class Builder {
		private String vpfPath;
		private LibrarySelectionTable lst;
		private File outputLocation;
		private List<Rectangle2D> filters = new ArrayList<Rectangle2D>();

		public Builder(String vpfPath) throws FormatException {
			this.lst = new LibrarySelectionTable(vpfPath);
			this.vpfPath = vpfPath;
		}

		/**
		 * rect defined in decimal degrees, x, y is upper left corner.
		 * 
		 * @param rect
		 * @return Builder
		 */
		public Builder addFilterRect(Rectangle2D rect) {
			filters.add(rect);
			return (Builder) this;
		}

		public Builder addFilterRect(String shpFile, String column, String attribute) throws MalformedURLException, FormatException {

			URL shpURL = PropUtils.getResourceOrFileOrURL(shpFile);
			if (shpURL != null) {
				EsriGraphicList graphicList = EsriGraphicList.getEsriGraphicList(shpURL, null, null);
				DbfTableModel dbf = graphicList.getTable();
				int colIndex = dbf.findColumn(column);
				if (colIndex != -1) {
					int recordCount = 0;
					for (List<Object> record : dbf) {
						if (record.get(colIndex).equals(attribute)) {
							EsriGraphic eg = (EsriGraphic) graphicList.get(recordCount);
							// miny, minx, maxy, maxx
							double[] extents = eg.getExtents();
							Rectangle2D rect = new Rectangle2D.Double(extents[1], extents[2], extents[3] - extents[1], extents[2] - extents[0]);							
							addFilterRect(rect);
							getLogger().fine("Adding rect for " + attribute + " :" + rect);
						}
						recordCount++;
					}
				} else {
					throw new FormatException("Column " + column + " not found");
				}

			}

			return (Builder) this;
		}

		public VpfCoverageTrimmer create(String outputLocationDirectory) {

			File vpfPathDir = new File(vpfPath);
			outputLocation = new File(outputLocationDirectory, vpfPathDir.getName());

			if (filters.isEmpty()) {
				// If no filters have been added, do whole world
				filters.add(new Rectangle2D.Double(-180.0, 90.0, 360.0, 180.0));
			}

			return new VpfCoverageTrimmer(this);
		}
	}

	public static void main(String[] args) {
		String vpfPath = "/Volumes/data/vpf/dnc/dnc01";

		try {
			new VpfCoverageTrimmer.Builder(vpfPath).addFilterRect("/Volumes/data/shape/world/cntry02/cntry02.shp", "CNTRY_NAME", "Gabon").create("/Users/dietrick/Desktop").go();

		} catch (FormatException fe) {
			System.out.println(fe.getMessage());
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}

	}
}
