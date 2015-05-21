/* 
 * <copyright>
 *  Copyright 2014 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.omGraphics.awt;

import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import javax.imageio.ImageIO;

/**
 * A wrapper around TexturePaint objects that can be used to serialize them. Use
 * these in OMGraphics if you set it in one of the paint attributes.
 *
 * @author carsten madsen
 */
public class SerializableTexturePaint extends TexturePaint implements Serializable {

    public SerializableTexturePaint(BufferedImage txtr, Rectangle2D anchor) {
        super(txtr, anchor);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        try {

            Field field = TexturePaint.class.getDeclaredField("tx");
            field.setAccessible(true);
            double ttx = (Double) field.get(this);
            oos.writeDouble(ttx);

            field = TexturePaint.class.getDeclaredField("ty");
            field.setAccessible(true);
            double tty = (Double) field.get(this);
            oos.writeDouble(tty);

            field = TexturePaint.class.getDeclaredField("sx");
            field.setAccessible(true);
            double tsx = (Double) field.get(this);
            oos.writeDouble(tsx);

            field = TexturePaint.class.getDeclaredField("sy");
            field.setAccessible(true);
            double tsy = (Double) field.get(this);
            oos.writeDouble(tsy);

            field = TexturePaint.class.getDeclaredField("bufImg");
            field.setAccessible(true);
            BufferedImage img = (BufferedImage) field.get(this);
            ImageIO.write(img, "jpeg", ImageIO.createImageOutputStream(oos));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        try {
            Field field = TexturePaint.class.getDeclaredField("tx");
            field.setAccessible(true);
            field.set(this, ois.readDouble());

            field = TexturePaint.class.getDeclaredField("ty");
            field.setAccessible(true);
            field.set(this, ois.readDouble());

            field = TexturePaint.class.getDeclaredField("sx");
            field.setAccessible(true);
            field.set(this, ois.readDouble());

            field = TexturePaint.class.getDeclaredField("sy");
            field.setAccessible(true);
            field.set(this, ois.readDouble());

            field = TexturePaint.class.getDeclaredField("bufImg");
            field.setAccessible(true);
            field.set(this, ImageIO.read(ImageIO.createImageInputStream(ois)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
