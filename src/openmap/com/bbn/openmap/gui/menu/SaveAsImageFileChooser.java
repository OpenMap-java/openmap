// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/gui/menu/SaveAsImageFileChooser.java,v $
// $RCSfile: SaveAsImageFileChooser.java,v $
// $Revision: 1.1 $
// $Date: 2003/03/15 20:36:25 $
// $Author: dietrick $
// 
// **********************************************************************


package com.bbn.openmap.gui.menu;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import com.bbn.openmap.util.PaletteHelper;

/**
 * A class extended from a JFileChooser that adds fields for
 * specifying the image size.
 */
public class SaveAsImageFileChooser extends JFileChooser {

    SaveAsImageFileChooser.DimensionQueryPanel dqp = 
	new SaveAsImageFileChooser.DimensionQueryPanel();

    /**
     * Create file chooser with the image size fields filled in.
     */
    public SaveAsImageFileChooser(int width, int height) {
	super();
	dqp.setImageHeight(height);
	dqp.setImageWidth(width);
    }
    
   /**
     * Creates and returns a new <code>JDialog</code> wrapping
     * <code>this</code> centered on the <code>parent</code>
     * in the <code>parent</code>'s frame.
     * This method can be overriden to further manipulate the dialog,
     * to disable resizing, set the location, etc. Example:
     * <pre>
     *     class MyFileChooser extends SaveAsImageFileChooser {
     *         protected JDialog createDialog(Component parent) throws HeadlessException {
     *             JDialog dialog = super.createDialog(parent);
     *             dialog.setLocation(300, 200);
     *             dialog.setResizable(false);
     *             return dialog;
     *         }
     *     }
     * </pre>
     *
     * @param   parent  the parent component of the dialog;
     *			can be <code>null</code>
     * @return a new <code>JDialog</code> containing this instance
     * @exception HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     * @since 1.4
     */
    protected JDialog createDialog(Component parent) throws HeadlessException {
        Frame frame = parent instanceof Frame ? (Frame) parent
              : (Frame)SwingUtilities.getAncestorOfClass(Frame.class, parent);

	String title = getUI().getDialogTitle(this);

        JDialog dialog = new JDialog(frame, title, true);

        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this, BorderLayout.CENTER);

	JPanel imageSizePanel = PaletteHelper.createPaletteJPanel(" Set Image Size ");
	imageSizePanel.add(dqp);
	contentPane.add(imageSizePanel, BorderLayout.NORTH);

        if (JDialog.isDefaultLookAndFeelDecorated()) {
            boolean supportsWindowDecorations = 
            UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (supportsWindowDecorations) {
                dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
            }
        }

        dialog.pack();
        dialog.setLocationRelativeTo(parent);

	return dialog;
    }

    /**
     * Set the value of the image width setting from the GUI.
     */
    public void setImageWidth(int w) {
	dqp.setImageWidth(w);
    }

    /**
     * Get the value of the image width setting from the GUI.
     */
    public int getImageWidth() {
	return dqp.getImageWidth();
    }

    /**
     * Set the value of the image height setting from the GUI.
     */
    public void setImageHeight(int h) {
	dqp.setImageHeight(h);
    }
    /**
     * Get the value of the image height setting from the GUI.
     */
    public int getImageHeight() {
	return dqp.getImageHeight();
    }

    public class DimensionQueryPanel extends JPanel {

	private JTextField hfield;
	private JTextField vfield;
	private JLabel htext;
	private JLabel vtext;
	private JLabel ptext1;
	private JLabel ptext2;
	
	public DimensionQueryPanel() {
	    this(0, 0);
	}

	public DimensionQueryPanel(int width, int height) {

	    htext = new JLabel("Height: ");
	    htext.setHorizontalAlignment(SwingConstants.RIGHT);
	    vtext = new JLabel("Width: ");
	    vtext.setHorizontalAlignment(SwingConstants.RIGHT);
	    hfield = new JTextField(Integer.toString(width),5);
	    vfield = new JTextField(Integer.toString(height),5);
	    ptext1 = new JLabel(" pixels");
	    ptext2 = new JLabel(" pixels");
	    layoutPanel();
	}
	
	public void setImageWidth(int width) {
	    hfield.setText(Integer.toString(width));
	}

	public int getImageWidth() {
	    return Integer.parseInt(hfield.getText());
	}

	public void setImageHeight(int height) {
	    vfield.setText(Integer.toString(height));
	}

	public int getImageHeight() {
	    return Integer.parseInt(vfield.getText());
	}

	public void layoutPanel() {
	    GridBagLayout gb = new GridBagLayout();
	    GridBagConstraints c = new GridBagConstraints();
	    
	    setLayout(gb);
	    c.fill = GridBagConstraints.HORIZONTAL;
	    c.weightx = .25;
	    c.gridx = 0;
	    c.gridy = 0;
	    gb.setConstraints(htext,c);
	    add(htext);

	    c.gridx = GridBagConstraints.RELATIVE;
	    c.weightx = 0;
	    gb.setConstraints(hfield,c);
	    add(hfield);

	    c.weightx = .25;
	    gb.setConstraints(ptext1,c);
	    add(ptext1);

	    c.weightx = .25;
	    gb.setConstraints(vtext,c);
	    add(vtext);

	    c.weightx = 0;
	    gb.setConstraints(vfield,c);
	    add(vfield);

	    c.weightx = .25;
	    gb.setConstraints(ptext2,c);
	    add(ptext2);
	}
    }
}
