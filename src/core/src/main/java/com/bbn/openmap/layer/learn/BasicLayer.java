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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/learn/BasicLayer.java,v $
// $RCSfile: BasicLayer.java,v $
// $Revision: 1.1 $
// $Date: 2007/02/26 16:56:06 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.layer.learn;

import java.awt.BasicStroke;
import java.awt.Color;

import com.bbn.openmap.layer.OMGraphicHandlerLayer;
import com.bbn.openmap.layer.policy.BufferedImageRenderPolicy;
import com.bbn.openmap.omGraphics.OMGraphic;
import com.bbn.openmap.omGraphics.OMGraphicConstants;
import com.bbn.openmap.omGraphics.OMGraphicList;
import com.bbn.openmap.omGraphics.OMLine;
import com.bbn.openmap.omGraphics.OMPoint;
import com.bbn.openmap.omGraphics.OMTextLabeler;

/**
 * This layer is a good place to start learning how to create OpenMap layers. It
 * extends OMGraphicHandler, which contains a good bit of functionality, but
 * exposes only the methods you need to start putting features (OMGraphics) on
 * the map.
 * 
 * Note that this is a layer where the objects never change, and the map objects
 * used by this layer never change. They always get managed and drawn, even if
 * they are off the visible map. When the projection changes, the OMGraphics are
 * told what the new projection is so they can reposition themselves, and then
 * they are redrawn.
 * 
 * If you want the OMGraphics on the layer to change depending on where the map
 * view is, look at ProjectionResponseLayer. You'll want to look at that layer
 * if you have a lot of map stuff to display on your layer, so you only render
 * what you need.
 * 
 * If you want to learn more about interacting with your OMGraphics after you
 * get the hang of displaying them efficiently, then move to the
 * InteractionLayer.
 */
public class BasicLayer
      extends OMGraphicHandlerLayer {

   /**
    * The empty constructor is necessary for any layer being created using the
    * openmap.properties file, via the openmap.layers property. This method
    * needs to be public, too. Don't try to do too much in the constructor -
    * remember, this code gets executed whether the user uses the layer or not.
    * Performance-wise, it's better to do most initialization the first time the
    * layer is made part of the map. You can test for that in the prepare()
    * method, by testing whether the OMGraphicList for the layer is null or not.
    * 
    * @see #prepare
    */
   public BasicLayer() {
      // Sets the name of the layer that is visible in the GUI. Can also be
      // set with properties with the 'prettyName' property.
      setName("Basic Layer");
      // This is how to set the ProjectionChangePolicy, which
      // dictates how the layer behaves when a new projection is
      // received. The StandardPCPolicy is the default policy and you don't
      // need to set it, this method call is here to illustrate where and how
      // you would make that call with a different policy.
      setProjectionChangePolicy(new com.bbn.openmap.layer.policy.StandardPCPolicy(this, true));
      // Improves performance
      setRenderPolicy(new BufferedImageRenderPolicy());
   }

   /**
    * This is an important Layer method to override. The prepare method gets
    * called when the layer is added to the map, or when the map projection
    * changes. We need to make sure the OMGraphicList returned from this method
    * is what we want painted on the map. The OMGraphics need to be generated
    * with the current projection. We test for a null OMGraphicList in the layer
    * to see if we need to create the OMGraphics. This layer doesn't change its
    * OMGraphics for different projections, if your layer does, you need to
    * clear out the OMGraphicList and add the OMGraphics you want for the
    * current projection.
    */
   public synchronized OMGraphicList prepare() {
      OMGraphicList list = getList();

      // Here's a test to see if it's the first time that the layer has been
      // added to the map. This list object will be whatever was returned from
      // this method the last time prepare() was called. In this
      // example, we always return an OMGraphicList object, so if it's null,
      // prepare() must not have been called yet.

      if (list == null) {
         list = init();
      }

      /*
       * This call to the list is critical! OMGraphics need to be told where to
       * paint themselves, and they figure that out when they are given the
       * current Projection in the generate(Projection) call. If an OMGraphic's
       * location is changed, it will need to be regenerated before it is
       * rendered, otherwise it won't draw itself. You generally know you have a
       * generate problem when OMGraphics show up with the projection changes
       * (zooms and pans), but not at any other time after something about the
       * OMGraphic changes.
       * 
       * If you want to be more efficient, you can replace this call to the list
       * as an else clause to the (list == null) check above, and call
       * generate(Projection) on all the OMGraphics in the init() method below
       * as you create them. This will prevent the
       * OMGraphicList.generate(Projection) call from making an additional loop
       * through all of the OMGraphics before they are returned.
       */
      list.generate(getProjection());

      return list;
   }

   /**
    * Called from the prepare() method if the layer discovers that its
    * OMGraphicList is null.
    * 
    * @return new OMGraphicList with OMGraphics that you always want to display
    *         and reproject as necessary.
    */
   public OMGraphicList init() {

      // This layer keeps a pointer to an OMGraphicList that it uses
      // for painting. It's initially set to null, which is used as
      // a flag in prepare() to signal that the OMGraphcs need to be
      // created. The list returned from prepare() gets set in the
      // layer.
      // This layer uses the StandardPCPolicy for new
      // projections, which keeps the list intact and simply calls
      // generate() on it with the new projection, and repaint()
      // which calls paint().

      OMGraphicList omList = new OMGraphicList();

      // Add an OMLine
      OMLine line = new OMLine(40f, -145f, 42f, -70f, OMGraphic.LINETYPE_GREATCIRCLE);
      // line.addArrowHead(true);
      line.setStroke(new BasicStroke(2));
      line.setLinePaint(Color.red);
      line.putAttribute(OMGraphicConstants.LABEL, new OMTextLabeler("Line Label"));

      omList.add(line);

      // Add a list of OMPoints.
      OMGraphicList pointList = new OMGraphicList();
      for (int i = 0; i < 100; i++) {
         OMPoint point = new OMPoint((float) (Math.random() * 89f), (float) (Math.random() * -179f), 3);
         point.setFillPaint(Color.yellow);
         point.setOval(true);
         pointList.add(point);
      }
      omList.add(pointList);

      return omList;
   }

}