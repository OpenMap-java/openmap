package com.bbn.openmap.tools.svg;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.w3c.dom.svg.SVGDocument;

import com.bbn.openmap.omGraphics.OMColor;
import com.bbn.openmap.util.FileUtils;
import com.bbn.openmap.util.PropUtils;

public class SVGRasterizer {

    /**
     * The transcoder input.
     */
    protected TranscoderInput input;

    /**
     * The transcoder hints.
     */
    protected TranscodingHints hints = new TranscodingHints();

    /**
     * The image that represents the SVG document.
     */
    protected BufferedImage img;

    /**
     * Constructs a new SVGRasterizer.
     * 
     * @param uri the uri of the document to rasterize
     */
    /*
     * public SVGRasterizer(String uri) { this.input = new TranscoderInput(uri);
     * }
     */

    /**
     * Constructs a new SVGRasterizer.
     * 
     * @param url the URL of the document to rasterize
     */
    public SVGRasterizer(URL url) {
        this.input = new TranscoderInput(url.toString());
    }

    public SVGRasterizer(String filename) {
        File f = new File(filename);
        try {
            this.input = new TranscoderInput(f.toURI().toURL().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a new SVGRasterizer converter.
     * 
     * @param istream the input stream that represents the SVG document to
     *        rasterize
     */
    public SVGRasterizer(InputStream istream) {
        this.input = new TranscoderInput(istream);
    }

    /**
     * Constructs a new SVGRasterizer converter.
     * 
     * @param reader the reader that represents the SVG document.
     */
    public SVGRasterizer(Reader reader) {
        this.input = new TranscoderInput(reader);
    }

    public SVGRasterizer() {
        // this.input = new TranscoderInput(reader);
    }

    /**
     * Constructs a new SVGRasterizer converter.
     * 
     * @param document the SVG document
     */
    public SVGRasterizer(SVGDocument document) {
        this.input = new TranscoderInput(document);
    }

    /**
     * Returns the image that represents the SVG document.
     */
    public BufferedImage createBufferedImage()
            throws TranscoderException, IOException {
        Rasterizer r = new Rasterizer();
        r.setTranscodingHints(hints);
        r.transcode(input, null);
        return img;
    }

    public BufferedImage resizeBufferedImage(Dimension d)
            throws TranscoderException, IOException {
        if (input != null) {
            this.setImageDimension(d);
            Rasterizer r = new Rasterizer();
            r.setTranscodingHints(hints);
            r.transcode(input, null);
            r = null; // for garbage collection
        }
        return img;

    }

    public BufferedImage createBufferedImage(Dimension d)
            throws TranscoderException, IOException {
        if (d != null) {
            this.setImageDimension(d);
        }
        Rasterizer r = new Rasterizer();
        r.setTranscodingHints(hints);
        r.transcode(input, null);
        r = null;
        return img;
    }

    public BufferedImage createJPG(File f)
            throws TranscoderException, IOException {
        Rasterizer r = new Rasterizer();
        JPEGTranscoder jpeg = new JPEGTranscoder();
        r.setTranscodingHints(hints);

        String parentName = f.getParent();
        File parent = new File(parentName);
        parent.mkdirs();

        FileWriter fw = new FileWriter(f);
        TranscoderOutput output = new TranscoderOutput(fw);
        r.transcode(input, output);
        r.setTranscodingHints(hints);
        jpeg.transcode(input, output);
        return img;
    }

    /**
     * Sets the width of the image to rasterize.
     * 
     * @param width the image width
     */
    public void setImageWidth(float width) {
        hints.put(ImageTranscoder.KEY_WIDTH, new Float(width));
    }

    /**
     * Sets the height of the image to rasterize.
     * 
     * @param height the image height
     */
    public void setImageHeight(float height) {
        hints.put(ImageTranscoder.KEY_HEIGHT, new Float(height));
    }

    public void setImageDimension(Dimension d) {
        hints.put(ImageTranscoder.KEY_WIDTH, new Float(d.width));
        hints.put(ImageTranscoder.KEY_HEIGHT, new Float(d.height));
    }

    public void setArea(Rectangle2D area) {
        hints.put(ImageTranscoder.KEY_AOI, area);
    }

    /**
     * Sets the preferred language to use. SVG documents can provide text in
     * multiple languages, this method lets you control which language to use if
     * possible. e.g. "en" for english or "fr" for french.
     * 
     * @param language the preferred language to use
     */
    public void setLanguages(String language) {
        hints.put(ImageTranscoder.KEY_LANGUAGE, language);
    }

    /**
     * Sets the unit conversion factor to the specified value. This method lets
     * you choose how units such as 'em' are converted. e.g. 0.26458 is 96dpi
     * (the default) or 0.3528 is 72dpi.
     * 
     * @param px2mm the pixel to millimeter convertion factor.
     */
    public void setPixelToMMFactor(float px2mm) {
        hints.put(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(px2mm));
    }

    /**
     * Sets the uri of the user stylesheet. The user stylesheet can be used to
     * override styles.
     * 
     * @param uri the uri of the user stylesheet
     */
    public void setUserStyleSheetURI(String uri) {
        hints.put(ImageTranscoder.KEY_USER_STYLESHEET_URI, uri);
    }

    /**
     * Sets whether or not the XML parser used to parse SVG document should be
     * validating or not, depending on the specified parameter. For futher
     * details about how media work, see the <a
     * href="http://www.w3.org/TR/CSS2/media.html";>Media types in the CSS2
     * specification </a>.
     * 
     * @param b true means the XML parser will validate its input
     */
    public void setXMLParserValidating(boolean b) {
        hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, (b ? Boolean.TRUE : Boolean.FALSE));
    }

    /**
     * Sets the media to rasterize. The medium should be separated by comma.
     * e.g. "screen", "print" or "screen, print"
     * 
     * @param media the media to use
     */
    public void setMedia(String media) {
        hints.put(ImageTranscoder.KEY_MEDIA, media);
    }

    /**
     * Sets the alternate stylesheet to use. For futher details, you can have a
     * look at the <a href="http://www.w3.org/TR/xml-stylesheet/";>Associating
     * Style Sheets with XML documents </a>.
     * 
     * @param alternateStylesheet the alternate stylesheet to use if possible
     */
    public void setAlternateStylesheet(String alternateStylesheet) {
        hints.put(ImageTranscoder.KEY_ALTERNATE_STYLESHEET, alternateStylesheet);
    }

    /**
     * Sets the Paint to use for the background of the image.
     * 
     * @param p the paint to use for the background
     */
    public void setBackgroundColor(Paint p) {
        hints.put(ImageTranscoder.KEY_BACKGROUND_COLOR, p);
    }

    public static void main(String[] args) {
        try {
            String fileName = FileUtils.getFilePathToOpenFromUser("Locate SVG File");
            URL fileURL = PropUtils.getResourceOrFileOrURL(fileName);

            SVGRasterizer svgr = new SVGRasterizer(fileURL);
            svgr.setBackgroundColor(OMColor.clear);
            BufferedImage bi = svgr.createBufferedImage();

            JFrame frame = new JFrame();
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent e) {
                    System.exit(0);
                }
            });
            frame.getContentPane().add(new JLabel(new ImageIcon(bi)));
            frame.pack();
            frame.setVisible(true);

        } catch (TranscoderException e2) {
            e2.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * An image transcoder that stores the resulting image.
     */
    protected class Rasterizer
            extends ImageTranscoder {

        public BufferedImage createImage(int w, int h) {

            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            ((Graphics2D) bi.getGraphics()).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            return bi;
        }

        public void writeImage(BufferedImage img, TranscoderOutput output)
                throws TranscoderException {

            SVGRasterizer.this.img = img;
        }
    }

}