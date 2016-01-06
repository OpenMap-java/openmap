package com.bbn.openmap.layer.policy;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.PropUtils;

/**
 * The ShadowRenderPolicy uses a ConvolveOp to create a shadow of the
 * OMGraphicList set on a layer. This is expensive, I think - the OMGraphicList
 * is painted several times, depending on the dimensions of the Kernel used on
 * the ConvolveOp. The idea is that the list is faintly painted with a wider
 * dimension, and then the dimension is shrunk down as the list keeps getting
 * repainted. The rendering becomes more bold/opaque as you get closer to the
 * actual location of the OMGraphics. You can vary the effect by setting the
 * dimension (the number of pixels away that you want the starting traces of
 * shadow to take effect) and the value for the kernel data (how
 * transparent/opaque you want the rendering to be, with 1f to ba opaque and
 * something like .005 being really subtly transparent).
 *
 * @author dietrick
 */
public class ShadowRenderPolicy extends BufferedImageRenderPolicy {

	// <editor-fold defaultstate="collapsed" desc="Logger Code">
	/**
	 * Holder for this class's Logger. This allows for lazy initialization of
	 * the logger.
	 */
	private static final class LoggerHolder {

		/**
		 * The logger for this class
		 */
		private static final Logger LOGGER = Logger
				.getLogger(ShadowRenderPolicy.class.getName());

		/**
		 * Prevent instantiation
		 */
		private LoggerHolder() {
			throw new AssertionError("This should never be instantiated");
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

	/**
	 * This is the pixel dimension of the ConvolveOp, creates the evaluation
	 * area.
	 */
	int dimension = 14;
	/**
	 * The kernelData assigns weight to the ConvolveOp area.
	 */
	float kernelValue = .004f;
	/**
	 * Property from the property file to set kernel dimesion.
	 */
	public final static String DIM_PROPERTY = "dim";
	/**
	 * Property name for Kernel value.
	 */
	public final static String KERNEL_VAL_PROPERTY = "kernel";
	
	/**
	 * The operation, created with dimension and kernelData settings.
	 */
	ConvolveOp convolveOp;

	public ShadowRenderPolicy() {
		updateOp(dimension, kernelValue);
		
		setImageBuffer(new BufferedImageRenderPolicy.ImageBuffer() {

	        /**
	         * Get the updated BufferedImage, with current OMGraphics rendered into
	         * it. Called with the results of layer.prepare().
	         * 
	         * @param list OMGraphicList from layer's prepare method.
	         * @param proj current projection that has buffer size information.
	         */
	        protected void update(OMGraphicList list, Projection proj) {
	            BufferedImage currentImageBuffer = null;

	            if (proj != null && layer != null) {

	                int w = proj.getWidth();
	                int h = proj.getHeight();

	                currentImageBuffer = getImageBuffer();
	                BufferedImage bufferedImage = scrubOrGetNewBufferedImage(currentImageBuffer, w, h);

	                // Updated image for projection
	                if (bufferedImage != null) {
	                    if (currentImageBuffer != null) {
	                        currentImageBuffer.flush();
	                    }

	                    currentImageBuffer = bufferedImage;
	                }

	                Graphics2D g2d = (Graphics2D) currentImageBuffer.getGraphics();
	                setRenderingHints(g2d);

	                if (list != null) {
	                    list.render(g2d);

	                    currentImageBuffer = convolveOp.filter(bufferedImage, null);
	                }

	                g2d.dispose();

	                setImageRaster(updateRaster(currentImageBuffer, proj));
	            }

	            setImageBuffer(currentImageBuffer);
	            currentProjection = proj;
	        }			
			
		});
	}

	/**
	 * Don't pass in a null layer.
	 */
	public ShadowRenderPolicy(OMGraphicHandlerLayer layer) {
		this();
		setLayer(layer);
	}
	
	/**
	 * Create the Kernel and ConvolveOp based on the basic settings. The Kernel
	 * float array will be created, all values matching the given value, the
	 * size of the dimension given.
	 * 
	 * @param dim The edge size of the kernel data array.
	 * @param krnlValue the value to fill the kernel array with.
	 */
	protected void updateOp(int dim, float krnlValue) {

		float[] kernelData = new float[dim * dim];

		for (int i = 0; i < kernelData.length; i++) {
			kernelData[i] = krnlValue;
		}

		setConvolveOp(new ConvolveOp(new Kernel(dim, dim, kernelData)));
	}

	/**
	 * If you're going to do something fancy, set up your own Op. You can mess
	 * with the Kernel data and the dimension yourself.
	 * 
	 * @param op
	 */
	public void setConvolveOp(ConvolveOp op) {
		convolveOp = op;
	}

	public ConvolveOp getConvolveOp() {
		return convolveOp;
	}

	public void setProperties(String prefix, Properties props) {
		super.setProperties(prefix, props);
		prefix = PropUtils.getScopedPropertyPrefix(prefix);
		
		dimension = PropUtils.intFromProperties(props, prefix + DIM_PROPERTY, dimension);
		kernelValue = PropUtils.floatFromProperties(props, prefix + KERNEL_VAL_PROPERTY, kernelValue);
		
		updateOp(dimension, kernelValue);
	}

	public Properties getProperties(Properties props) {
		props = super.getProperties(props);
		String prefix = PropUtils.getScopedPropertyPrefix(this);
		props.setProperty(prefix + DIM_PROPERTY, Float.toString(dimension));
		props.setProperty(prefix + KERNEL_VAL_PROPERTY, Float.toString(kernelValue));
		
		return props;
	}
	
	public Properties getPropertyInfo(Properties props) {
		props = super.getPropertyInfo(props);
		PropUtils.setI18NPropertyInfo(i18n, props, ShadowRenderPolicy.class, DIM_PROPERTY, "Kernel Dimension", "The size of the kernel array edge", null);
		PropUtils.setI18NPropertyInfo(i18n, props, ShadowRenderPolicy.class, KERNEL_VAL_PROPERTY, "Kernel Data Value", "The value set in the kernel data", null);		

		return props;
	}
	
	
	/**
	 * Assumes that the OMGraphicList to be rendered is set on the
	 * OMGraphicHandlerLayer, available via setList().
	 */
	public void paint(Graphics g) {
		Logger logger = getLogger();
		
        if (layer == null) {
            logger.warning("NULL layer, skipping...");
            return;
        }

        Projection proj = layer.getProjection();

        if (layer.isProjectionOK(proj)) {

            Graphics2D g2 = (Graphics2D) g.create();
            ImageBuffer imageBuffer = getImageBuffer();
            setCompositeOnGraphics(g2);

            if (!imageBuffer.paint(g2, proj)) {
                OMGraphicList list = layer.getList();
                if (list != null) {
                    layer.getList().render(g2);
                    
                    BufferedImage srcImg = new BufferedImage(proj.getWidth(),
    						proj.getHeight(), BufferedImage.TYPE_INT_ARGB);

                    list.render(srcImg.getGraphics());

    				BufferedImage dstImage = convolveOp.filter(srcImg, null);

    				g2.drawImage(dstImage, 0, 0, null);
                }
            }

            g2.dispose();

        } else if (logger.isLoggable(Level.FINE)) {
            logger.fine(layer.getName() + " ShadowPolicy.paint(): skipping due to projection.");
        }		
	}
}
