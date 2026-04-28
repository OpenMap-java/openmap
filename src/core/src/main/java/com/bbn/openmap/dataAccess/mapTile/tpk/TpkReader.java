package com.bbn.openmap.dataAccess.mapTile.tpk;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.bbn.openmap.util.PropUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Simple TPK reader that loads the mapserver.json in the tpk package to get the
 * imagery path. Uses the MapServerDescription object to parse the json file.
 * 
 * @author dietrick
 */
public class TpkReader {

	public final String SERVICE_DESCRIPTION_LOCATION = "servicedescriptions/mapserver/mapserver.json";

	protected String pathToTiles;
	protected ZipFile tpkFile;

	private Logger logger = Logger.getLogger("com.bbn.openmap.dataAccess.mapTile.tpk.TpkReader");

	public TpkReader(String tpkLocation) throws IOException, NoClassDefFoundError {

		tpkFile = new ZipFile(tpkLocation);

		ZipEntry descriptionEntry = tpkFile.getEntry(SERVICE_DESCRIPTION_LOCATION);

		if (descriptionEntry != null) {
			InputStream zipStream = tpkFile.getInputStream(descriptionEntry);
			MapServerDescription mapServerDescription = loadJSON(zipStream);
			zipStream.close();

			pathToTiles = mapServerDescription.getPathToTiles();
		}

	}

	/**
	 * Get an input stream for an entry that should be there.
	 * 
	 * @param entryString entry path, relative to internal root of zip file
	 * @throws IOException
	 */
	public InputStream getStream(String entryString) throws IOException {
		if (tpkFile != null) {
			ZipEntry ze = tpkFile.getEntry(entryString);
			if (ze != null) {
				return tpkFile.getInputStream(ze);
			}
		}
		return null;
	}

	/**
	 * Returns the parent directory of the Level directories. From here you can
	 * find the Lzz directories that contains the bundle and bundlx files
	 * containing the images.
	 * 
	 * @return path to Lzz directories
	 */
	public String getPathToTiles() {
		return pathToTiles;
	}

	/**
	 * Load the JSON file describing the tpk contents.
	 * 
	 * @param urlString path to json file.
	 * @return MapServerDescription with json contents.
	 */
	public MapServerDescription loadJSON(String urlString) {
		if (urlString != null) {
			URL input;
			try {
				input = PropUtils.getResourceOrFileOrURL(urlString);

				InputStream inputStream = input.openStream();
				return loadJSON(inputStream);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoClassDefFoundError ncdfe) {
				logger.warning("The TPK reading code needs Jackson JSON package in the classpath");
			}
		}

		return null;
	}

	/**
	 * Load the JSON file describing the tpk contents, given an input stream.
	 * 
	 * @param jsonStream stream for the json file
	 * @return MapServerDescription with json contents, or null of something wh
	 */
	public MapServerDescription loadJSON(InputStream jsonStream) throws IOException {
		return new ObjectMapper().readValue(jsonStream, MapServerDescription.class);
	}

}
