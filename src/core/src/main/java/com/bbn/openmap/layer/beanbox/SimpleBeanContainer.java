/* **********************************************************************
 * 
 *    Use, duplication, or disclosure by the Government is subject to
 *           restricted rights as set forth in the DFARS.
 *  
 *                         BBNT Solutions LLC
 *                             A Part of 
 *                  Verizon      
 *                          10 Moulton Street
 *                         Cambridge, MA 02138
 *                          (617) 873-3000
 *
 *    Copyright (C) 2002 by BBNT Solutions, LLC
 *                 All Rights Reserved.
 * ********************************************************************** */

package com.bbn.openmap.layer.beanbox;

import java.util.Vector;

import com.bbn.openmap.proj.Length;
import com.bbn.openmap.proj.ProjMath;
import com.bbn.openmap.tools.beanbox.BeanContainer;
import com.bbn.openmap.tools.beanbox.BeanLayoutManager;

/**
 * A SimpleBeanContainer is itself a bean. Hence this class extends
 * the {@link com.bbn.openmap.layer.beanbox.SimpleBeanObject}.
 * SimpleBeanContainer is a sample implementation of the
 * {@link com.bbn.openmap.tools.beanbox.BeanContainer}interface.
 */
public class SimpleBeanContainer extends SimpleBeanObject implements
        BeanContainer {

    private float widthInNM;
    private float heightInNM;

    private Vector contents;
    private BeanLayoutManager layoutManager;
    private String layoutClass;

    public SimpleBeanContainer() {
        this(System.currentTimeMillis(), 0, 0, 0, 200, 200, null);
    }

    public SimpleBeanContainer(long id, float centerLatitude,
            float centerLongitude, float bearingInDeg, float widthInNM,
            float heightInNM, BeanLayoutManager layoutManager) {

        super(id, centerLatitude, centerLongitude, bearingInDeg);
        setWidthInNM(widthInNM);
        setHeightInNM(heightInNM);
        setLayout(layoutManager);

        contents = new Vector();

        super.setCustomGraphicClassName("com.bbn.openmap.layer.beanbox.ContainerGraphic");

    }

    public Vector getContents() {
        return this.contents;
    }

    public void setContents(Vector contents) {
        this.contents = contents;
    }

    public void add(Object bean) {

        if (bean instanceof SimpleBeanObject) {
            SimpleBeanObject obj = (SimpleBeanObject) bean;

            if (!contents.contains(new Long(obj.getId()))) {
                contents.add(new Long(obj.getId()));
            }

            if (layoutManager != null) {
                layoutManager.layoutContainer();
            }
        }

    }

    public void remove(Object bean) {

        if (bean instanceof SimpleBeanObject) {
            SimpleBeanObject obj = (SimpleBeanObject) bean;

            if (contents.contains(new Long(obj.getId()))) {
                contents.remove(new Long(obj.getId()));
            }

            if (layoutManager != null)
                layoutManager.layoutContainer();
        }
    }

    public void removeAll() {

        if (!contents.isEmpty()) {

            contents.clear();

            if (layoutManager != null)
                layoutManager.layoutContainer();
        }

    }

    public boolean contains(Object obj) {

        if (obj instanceof SimpleBeanObject) {
            return this.contains(new Long(((SimpleBeanObject) obj).getId()));
        }

        return false;
    }

    public BeanLayoutManager getLayout() {
        return layoutManager;
    }

    public void setLayout(BeanLayoutManager layout) {

        layoutManager = layout;

        if (layoutManager == null)
            layoutManager = new NullLayout();

        layoutClass = layoutManager.getClass().getName();

        layoutManager.setContainer((BeanContainer) this);

        layoutManager.layoutContainer();

    }

    public String getLayoutClass() {
        return layoutClass;
    }

    public void setLayoutClass(String lc) {

        layoutClass = lc;

        if ((layoutClass == null)
                || ((layoutClass = layoutClass.trim()).length() == 0)) {
            System.out.println("loading null layout!");
            this.setLayout(new NullLayout());
        } else {
            try {
                Class lClass = Class.forName(layoutClass);
                BeanLayoutManager blm = (BeanLayoutManager) lClass.newInstance();
                this.setLayout(blm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void validate() {
        if (layoutManager != null)
            layoutManager.layoutContainer();
    }

    public void setLatitude(float lat) {
        super.setLatitude(lat);
        validate();
    }

    public void setLongitude(float lon) {
        super.setLongitude(lon);
        validate();
    }

    public void setBearingInDeg(float bearingInDeg) {
        super.setBearingInDeg(bearingInDeg);
        validate();
    }

    public float getWidthInNM() {
        return widthInNM;
    }

    public void setWidthInNM(float w) {
        widthInNM = w;
        this.validate();
    }

    public float getHeightInNM() {
        return heightInNM;
    }

    public void setHeightInNM(float h) {
        heightInNM = h;
        this.validate();
    }

    // utility methods

    public float getBottomLatitude() {
        return (float) (getLatitude() - ProjMath.radToDeg(Length.NM.toRadians(heightInNM / 2)));
    }

    public float getRightLongitude() {
        return (float) (getLongitude() + ProjMath.radToDeg(Length.NM.toRadians(widthInNM / 2)));
    }

    public float getTopLatitude() {
        return (float) (getLatitude() + ProjMath.radToDeg(Length.NM.toRadians(heightInNM / 2)));
    }

    public float getLeftLongitude() {
        return (float) (getLongitude() - ProjMath.radToDeg(Length.NM.toRadians(widthInNM / 2)));
    }

    public String toString() {
        return "[SBC " + id + " " + latitude + " " + longitude + " "
                + bearingInDeg + " " + customGraphicClassName + " "
                + graphicImage + " " + widthInNM + " " + heightInNM + " "
                + contents + " " + layoutClass + "]";
    }

}