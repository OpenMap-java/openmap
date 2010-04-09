// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/SaveAsImageMenuItem.java,v $
// $RCSfile: SaveAsImageMenuItem.java,v $
// $Revision: 1.5 $
// $Date: 2004/10/14 18:05:50 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.bbn.openmap.MapBean;
import com.bbn.openmap.image.AbstractImageFormatter;
import com.bbn.openmap.util.Debug;

/**
 * A MenuItem that is capable of looking at MapBean and saving it as an Image
 */
public class SaveAsImageMenuItem extends MapHandlerMenuItem implements
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	AbstractImageFormatter formatter = null;

	public AbstractImageFormatter getFormatter() {
		return formatter;
	}

	public void setFormatter(AbstractImageFormatter formatter) {
		this.formatter = formatter;
	}

	/**
	 * @param display
	 *            A String that will be displayed when this menuitem is shown in
	 *            GUI
	 * @param in_formatter
	 *            A formatter that knows how to generate an image from MapBean.
	 */
	public SaveAsImageMenuItem(String display,
			AbstractImageFormatter in_formatter) {
		super(display);
		formatter = in_formatter;
		addActionListener(this);
	}

	public void actionPerformed(ActionEvent ae) {
		Debug.message("saveimage", "SaveAsImageMenuItem: actionPerformed");

		if (mapHandler == null) {
			Debug.output("SaveAsImageMenuItem: mapHandler = null, returning");
			return;
		}

		MapBean mb = (MapBean) mapHandler.get("com.bbn.openmap.MapBean");

		if (mb != null) {
			Debug.message("saveimage", "MapBean found, creating image");
			try {

				while (true) {
					SaveAsImageFileChooser chooser = new SaveAsImageFileChooser(
							mb.getWidth(), mb.getHeight());

					int returnVal = chooser.showSaveDialog(getParent());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String filename = chooser.getSelectedFile()
								.getAbsolutePath();
						if (formatter == null) {
							break;
						}

						filename = checkFileName(filename, formatter
								.getFormatLabel().toLowerCase());
						if (filename == null) {
							// This is the reason for the while
							// loop, the name didn't really pass
							// muster, so we'll try again.
							continue;
						}

						int imageHeight = chooser.getImageHeight();
						int imageWidth = chooser.getImageWidth();

						byte[] imageBytes = formatter.getImageFromMapBean(mb,
								imageWidth, imageHeight);
						FileOutputStream binFile = new FileOutputStream(
								filename);
						binFile.write(imageBytes);
						binFile.close();
						if (Debug.debugging("saveimage")) {
							com.bbn.openmap.proj.Projection proj = mb
									.getProjection();
							Debug.output("Created image at " + filename
									+ "where projection covers "
									+ proj.getUpperLeft() + " to "
									+ proj.getLowerRight());
						}
						break;
					} else if (returnVal == JFileChooser.CANCEL_OPTION) {
						break;
					}
				}
			} catch (IOException e) {
				Debug.error("SaveAsImageMenuItem: " + e);
			}
		}
	}

	/**
	 * A little method that checks the file path to see if it exists, and
	 * modifies it with the imageSuffix if it doesn't have one specified by the
	 * user. Asks the user if it's OK to overwrite if the file exists.
	 * 
	 * @param filePath
	 *            absolute file path to check.
	 * @param imageSuffix
	 *            suffix to append to filePath if it doesn't already have one.
	 *            This word should not contain a starting '.'.
	 * @return null if name is no good, a String to use if good.
	 */
	protected String checkFileName(String filePath, String imageSuffix) {

		String fileName = filePath.substring(filePath
				.lastIndexOf(File.separator));
		String newFilePath;

		if (fileName.indexOf('.') == -1) {
			newFilePath = filePath + "." + imageSuffix;
		} else {
			// else leave it alone, user specified suffix
			newFilePath = filePath;
		}

		File file = new File(newFilePath);
		if (file.exists()) {
			// Check to see if it is alright to overwrite.
			int choice = JOptionPane.showConfirmDialog(null, "The file "
					+ newFilePath + " exists, replace?",
					"Confirm File Replacement", JOptionPane.YES_NO_OPTION);
			if (choice != JOptionPane.YES_OPTION) {
				newFilePath = null;
			}
		}

		return newFilePath;
	}

}