package com.bbn.openmap.dataAccess.mapTile.tpk;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.bbn.openmap.io.BinaryBufferedFile;
import com.bbn.openmap.io.FormatException;
import com.bbn.openmap.util.MoreMath;

/**
 * Reads the tpk bundlex file and gathers the indexes for the tiles. You can ask
 * this object for the indexes for images, from 0-128, 0-128 range. These
 * indexes are column major, and modulo (%128) from the whole earth index for a
 * particular zoom level as contained in a compacted bundle file. Open file,
 * read, close, then ask for indexes.
 * 
 * @author dietrick
 */
public class BundleX {

	int[][] offsets = new int[128][128];

	public BundleX(InputStream stream) throws FormatException, IOException {
		readFile(stream);
	}

	public void readFile(InputStream stream) throws FormatException, EOFException, IOException {

		/* byte[16] header */ stream.skip(16);

		int nextFilePos = -1;

		byte[] bytevec = new byte[5];
		stream.read(bytevec);
		int currentFilePos = MoreMath.BuildIntegerLE(bytevec, 0);

		// The images are stored in row major order
		for (int x = 0; x < 128; x++) {
			for (int y = 0; y < 128; y++) {
				stream.read(bytevec);
				nextFilePos = MoreMath.BuildIntegerLE(bytevec, 0);
				int storedVal = -1;

				if (nextFilePos != currentFilePos + 4) {
					storedVal = currentFilePos;
				}

				offsets[x][y] = storedVal;

				/*
				 * if (storedVal != -1) { System.out.println("tile in " + x +
				 * ", " + y + " " + (storedVal)); }
				 */

				currentFilePos = nextFilePos;

			}
		}
	}

	/**
	 * Return offset for tile. If the tile doesn't exist in the bundle, return
	 * -1.
	 * 
	 * @param x
	 *            column of tile in bundle
	 * @param y
	 *            row of tile in bundle
	 * @return int offset into bundle file
	 */
	public int getOffset(int x, int y) {
		return offsets[x][y];
	}

	/**
	 * This class should display the tiled image from a bundle file as indexed
	 * in the bundlx file. Just for testing, and it's kinda sloppy. But helpful.
	 * 
	 * @param args
	 *            the path to a bundlx or bundle file.
	 */
	public static void main(String[] args) {
		try {

			int dotIndex = args[0].indexOf('.');
			if (dotIndex < 0) {
				System.out.println("can't figure out the proper extension (bundlx) from the given path");
				System.exit(-1);
			}

			String loc = args[0].substring(0, dotIndex);
			FileInputStream fis = new FileInputStream(new File(loc + ".bundlx"));
			BundleX bx = new BundleX(fis);
			fis.close();

			BinaryBufferedFile bbf = new BinaryBufferedFile(loc + ".bundle");

			JFrame jFrame = new JFrame(loc);
			JPanel top = new JPanel();
			JScrollPane scrollPane = new JScrollPane(top, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
			jFrame.getContentPane().add(scrollPane);

			GridBagLayout gridBag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			top.setLayout(gridBag);

			for (int x = 0; x < 128; x++) {
				for (int y = 0; y < 128; y++) {

					int i = bx.getOffset(x, y);
					if (i > 0) {

						bbf.seek(i);
						int length = bbf.readInteger();

						byte[] imageBytes = bbf.readBytes(length, false);

						ImageIcon ii = new ImageIcon(imageBytes);
						JLabel holder = new JLabel(ii);
						if (length > 2000) {
							c.gridx = x;
							c.gridy = y;

							gridBag.setConstraints(holder, c);

							top.add(holder);
						}

					}
				}
			}

			jFrame.setSize(256, 256);
			jFrame.validate();
			jFrame.setVisible(true);

		} catch (IOException ioe) {
			System.out.println("caught IOException trying to read bundlx file");
		} catch (FormatException fe) {
			System.out.println("caught FormatException trying to read bundlx file");
		}
	}

}
