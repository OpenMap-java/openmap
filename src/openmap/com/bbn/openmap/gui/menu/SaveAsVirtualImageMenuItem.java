/* SaveAsVirtualImageMenuItem class - written by Eliot T. Lebsack of the MITRE Corp. 10/16/2002. */

package com.bbn.openmap.gui.menu;

import javax.swing.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.*;
import java.util.*;
import java.io.*;

import com.bbn.openmap.*;
import com.bbn.openmap.util.Debug;
import com.bbn.openmap.image.*;
import com.bbn.openmap.proj.*;

/**
 * A MenuItem that is capable of looking at MapBean and saving it as
 * an Image of virtual size. Not really needed, the functionality
 * provided here has been added to the SaveAsImageMenuItem and the
 * SaveAsImageFileChooser.
 * 
 * @deprecated Redundant, functionality integrated into
 *             SaveAsImageMenuItem.
 */
public class SaveAsVirtualImageMenuItem extends SaveAsImageMenuItem {

    protected Iterator it = null;
    protected Object someObj = null;
    protected Layer[] visibleLayers = null;
    protected String filename;
    protected DimensionQueryWindow DW = null;

    /**
     * @param display A String that will be displayed when this
     *        menuitem is shown in GUI
     * @param in_formatter A formatter that knows how to generate an
     *        image from MapBean.
     */
    public SaveAsVirtualImageMenuItem(String display,
            AbstractImageFormatter in_formatter) {
        super(display, in_formatter);
    }

    public void actionPerformed(ActionEvent ae) {
        if (mapHandler == null) {
            return;
        }

        it = mapHandler.iterator();

        while (it.hasNext()) {
            someObj = it.next();
            if (someObj instanceof LayerHandler) {
                visibleLayers = ((LayerHandler) someObj).getMapLayers();
            }
        }

        it = mapHandler.iterator();

        while (it.hasNext()) {
            someObj = it.next();
            if (someObj instanceof MapBean) {
                JFileChooser chooser = new JFileChooser();
                int returnVal = chooser.showSaveDialog(getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    filename = chooser.getSelectedFile().getAbsolutePath();
                    Projection tp0 = ((MapBean) someObj).getProjection();

                    DW = new DimensionQueryWindow(tp0.getWidth(), tp0.getHeight());
                    DW.layoutWindow();
                }
                return;
            }
        }
        return;
    }

    public class DimensionQueryWindow {
        private JFrame f;
        private JTextField hfield;
        private JTextField vfield;
        private JLabel htext;
        private JLabel vtext;
        private JLabel ptext1;
        private JLabel ptext2;
        private JButton ok;
        private JButton cancel;

        public DimensionQueryWindow(int width, int height) {
            f = new JFrame("Output Image Size");
            htext = new JLabel("Height");
            vtext = new JLabel("Width");
            hfield = new JTextField(Integer.toString(width), 5);
            vfield = new JTextField(Integer.toString(height), 5);
            ptext1 = new JLabel("pixels");
            ptext2 = new JLabel("pixels");
            ok = new JButton("OK");
            cancel = new JButton("Cancel");
        }

        public int getWidth() {
            return Integer.parseInt(hfield.getText());
        }

        public int getHeight() {
            return Integer.parseInt(vfield.getText());
        }

        public void closeWindow() {
            f.dispose();
        }

        public void layoutWindow() {
            GridBagLayout gb = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();

            f.getContentPane().setLayout(gb);
            f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            c.fill = GridBagConstraints.HORIZONTAL;

            c.gridx = 0;
            c.gridy = 0;
            gb.setConstraints(htext, c);
            f.getContentPane().add(htext);

            c.gridx = 1;
            c.gridy = 0;
            gb.setConstraints(hfield, c);
            f.getContentPane().add(hfield);

            c.gridx = 2;
            c.gridy = 0;
            gb.setConstraints(ptext1, c);
            f.getContentPane().add(ptext1);

            c.gridx = 0;
            c.gridy = 1;
            gb.setConstraints(vtext, c);
            f.getContentPane().add(vtext);

            c.gridx = 1;
            c.gridy = 1;
            gb.setConstraints(vfield, c);
            f.getContentPane().add(vfield);

            c.gridx = 2;
            c.gridy = 1;
            gb.setConstraints(ptext2, c);
            f.getContentPane().add(ptext2);

            c.gridx = 0;
            c.gridy = 2;
            gb.setConstraints(ok, c);
            f.getContentPane().add(ok);
            ok.setActionCommand("OK");
            ok.addActionListener(new ButtonHandler());

            c.gridx = 2;
            c.gridy = 2;
            gb.setConstraints(cancel, c);
            f.getContentPane().add(cancel);
            cancel.setActionCommand("CANCEL");
            cancel.addActionListener(new ButtonHandler());

            f.pack();
            f.setVisible(true);
        }
    }

    public class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            if (ae.getActionCommand().equals("OK")) {
                try {
                    if (formatter == null) {
                        return;
                    }
                    Proj tp = null;
                    Proj tp0 = null;

                    MapBean mb = (MapBean) someObj;
                    LatLonPoint cp = new LatLonPoint(mb.getCenter());

                    double area1, area2, scaleMod;
                    int height = DW.getHeight();
                    int width = DW.getWidth();

                    tp0 = (Proj) mb.getProjection();

                    area1 = (double) tp0.getHeight() * (double) tp0.getWidth();
                    area2 = (double) height * (double) width;

                    scaleMod = Math.sqrt(area1 / area2);

                    System.out.println("scaleMod = " + scaleMod);

                    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

                    tp = (Proj) ProjectionFactory.makeProjection(mb.getProjection().getClass(),
                            cp.getLatitude(),
                            cp.getLongitude(),
                            mb.getScale() * (float) scaleMod,
                            width,
                            height);

                    tp.drawBackground((Graphics2D) bi.createGraphics(),
                            mb.getBackground());

                    for (int i = visibleLayers.length - 1; i > -1; i--) {
                        visibleLayers[i].renderDataForProjection(tp,
                                bi.createGraphics());
                    }
                    byte[] imageBytes = formatter.formatImage(bi);
                    FileOutputStream binFile = new FileOutputStream(filename);
                    binFile.write(imageBytes);
                    binFile.close();
                    DW.closeWindow();
                } catch (IOException e) {
                    Debug.error("SaveAsVirtualImageMenuItem: " + e);
                }
            } else if (ae.getActionCommand().equals("CANCEL")) {
                DW.closeWindow();
            }
        }
    }
}