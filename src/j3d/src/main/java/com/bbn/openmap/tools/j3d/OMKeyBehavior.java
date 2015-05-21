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
// $Source: /cvs/distapps/openmap/src/j3d/com/bbn/openmap/tools/j3d/OMKeyBehavior.java,v $
// $RCSfile: OMKeyBehavior.java,v $
// $Revision: 1.5 $
// $Date: 2005/08/11 19:27:04 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.tools.j3d;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOr;
import javax.vecmath.Vector3d;

import com.bbn.openmap.proj.Projection;
import com.bbn.openmap.util.Debug;

/**
 * OMKeyBehavior is a modified version of KeyBehavior, available from
 * http://www.J3D.org. The modifications include having a notion of
 * body position, and view position. You can modify a view, which can
 * be thought of as where your eyes are pointing. You can also modify
 * the position, which can be thought of where your body is pointing.
 * So, you can look in a different directin than your motion.This
 * allows the user to adjust the view angle to the map, but not have
 * it interfere with navigation - for instance, you can look down at
 * the ground, but not fly into it, instead keeping a constant
 * distance above it.
 * 
 * <P>
 * The controls are:
 * 
 * <pre>
 * 
 *  left - turn left
 *  right - turn right
 *  up - move forward
 *  down - move backward
 * 
 *  Cntl left - look down
 *  Cntl right - look up
 *  Cntl up - move up (elevation)
 *  Cntl down - move down (elevation)
 * 
 *  Alt right - move right
 *  Alt left - move left
 *  Alt up - rotate up (movement forward will increase elevation).
 *  Alt down - rotate down (movement forward will decrease elevation).
 * 
 *  
 * </pre>
 * 
 * From the original KeyBehavier header:
 * 
 * <pre>
 * 
 *   KeyBehavior is a generic behavior class to take key presses and move a
 *   TransformGroup through a Java3D scene. The actions resulting from the key strokes
 *   are modified by using the Ctrl, Alt and Shift keys.
 * 
 *   (version 1.0) reconstructed class to make more generic.
 * 
 *  MODIFIED:
 * 
 * 
 *  @author    Andrew AJ Cain, Swinburne University,
 *       Australia &lt;acain@it.swin.edu.au&gt; edited from code
 *       by: Gary S. Moss &lt;moss@arl.mil&gt; U. S. Army Research
 *       Laboratory * CLASS NAME: KeyBehavior PUBLIC
 *       FEATURES: // Data // Constructors // Methods:
 *       COLLABORATORS: 
 * </pre>
 * 
 * @version 1.0, 25 September 1998 aajc
 */
public class OMKeyBehavior extends Behavior {

    protected final static double FAST_SPEED = 2.0;
    protected final static double NORMAL_SPEED = 1.0;
    protected final static double SLOW_SPEED = 0.5;

    private TransformGroup cameraTransformGroup;
    private Transform3D transform3D;
    private Transform3D locationTransform3D;
    private Transform3D xRotLookTransform;
    private Transform3D yRotLookTransform;
    private Transform3D zRotLookTransform;

    private WakeupCondition keyCriterion;

    protected final static double TWO_PI = (2.0 * Math.PI);
    protected double rotateXAmount = Math.PI / 16.0;
    protected double rotateYAmount = Math.PI / 16.0;
    protected double rotateZAmount = Math.PI / 16.0;

    protected double moveRate = 0.3;
    protected double speed = NORMAL_SPEED;

    protected int forwardKey = KeyEvent.VK_UP;
    protected int backKey = KeyEvent.VK_DOWN;
    protected int leftKey = KeyEvent.VK_LEFT;
    protected int rightKey = KeyEvent.VK_RIGHT;

    protected boolean DEBUG = false;

    protected Projection projection;

    public OMKeyBehavior(TransformGroup cameraTG, Projection proj) {
        this(cameraTG, proj, null);
    }

    public OMKeyBehavior(TransformGroup cameraTG, Projection proj,
            Vector3d initialLocation) {

        projection = proj;

        DEBUG = Debug.debugging("3dkey");

        cameraTransformGroup = cameraTG;
        transform3D = new Transform3D();
        locationTransform3D = new Transform3D();

        // These are the looking transforms, for the view.
        xRotLookTransform = new Transform3D();
        yRotLookTransform = new Transform3D();
        zRotLookTransform = new Transform3D();

        setViewerLocation(initialLocation);
        setEnable(true);
    }

    public void initialize() {
        WakeupCriterion[] keyEvents = new WakeupCriterion[2];

        keyEvents[0] = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
        keyEvents[1] = new WakeupOnAWTEvent(KeyEvent.KEY_RELEASED);

        keyCriterion = new WakeupOr(keyEvents);
        wakeupOn(keyCriterion);
    }

    public void setViewerLocation(Vector3d initialLocation) {
        cameraTransformGroup.getTransform(locationTransform3D);

        // scale of < 1 shrinks the object. (.5) is the scale.
        float scale = 1f;

        if (initialLocation == null) {

            initialLocation = new Vector3d();

            // So, this lays out where the land is, in relation to the
            // viewer. We should get the projection from the MapBean,
            // and
            // offset the transform to the middle of the map.
            if (projection != null) {
                float centerXOffset = projection.getWidth() / 2f * scale;
                float centerYOffset = projection.getHeight() * 2 / 3f * scale;

                Debug.message("3d", "OM3DViewer with projection " + projection
                        + ", setting center of scene to " + centerXOffset
                        + ", " + centerYOffset);

                initialLocation.set((double) centerXOffset,
                        (double) 50,
                        (double) centerYOffset);
            } else {
                initialLocation.set(0.0, 50, 0.0);
            }
        }

        Transform3D toMove = new Transform3D();
        toMove.set(scale, initialLocation);
        locationTransform3D.mul(toMove);
        cameraTransformGroup.setTransform(locationTransform3D);
    }

    public void processStimulus(Enumeration criteria) {

        if (DEBUG) {
            Debug.output("OMKeyBehavior: processStimulus");
        }

        WakeupCriterion wakeup;
        AWTEvent[] event;

        while (criteria.hasMoreElements()) {
            wakeup = (WakeupCriterion) criteria.nextElement();

            if (!(wakeup instanceof WakeupOnAWTEvent)) {
                continue;
            }

            event = ((WakeupOnAWTEvent) wakeup).getAWTEvent();

            for (int i = 0; i < event.length; i++) {
                if (event[i].getID() == KeyEvent.KEY_PRESSED) {
                    processKeyEvent((KeyEvent) event[i]);
                }
            }
        }
        wakeupOn(keyCriterion);
    }

    protected void processKeyEvent(KeyEvent event) {
        int keycode = event.getKeyCode();

        if (event.isShiftDown()) {
            speed = FAST_SPEED;
        } else {
            speed = NORMAL_SPEED;
        }

        if (event.isAltDown()) {
            altMove(keycode);
        } else if (event.isControlDown()) {
            controlMove(keycode);
        } else {
            standardMove(keycode);
        }
    }

    //moves forward backward or rotates left right
    protected void standardMove(int keycode) {
        if (keycode == forwardKey) {
            moveForward();
        } else if (keycode == backKey) {
            moveBackward();
        } else if (keycode == leftKey) {
            rotLeft();
        } else if (keycode == rightKey) {
            rotRight();
        }
    }

    //moves left right, rotate up down
    protected void altMove(int keycode) {
        if (DEBUG) {
            Debug.output("altMove");
        }
        if (keycode == forwardKey) {
            rotUp();
        } else if (keycode == backKey) {
            rotDown();
        } else if (keycode == leftKey) {
            moveLeft();
        } else if (keycode == rightKey) {
            moveRight();
        }
    }

    //move up down, rot left right
    protected void controlMove(int keycode) {

        if (keycode == forwardKey) {
            moveUp();
        } else if (keycode == backKey) {
            moveDown();
        } else if (keycode == leftKey) {
            //          rollLeft();
            lookUp();
        } else if (keycode == rightKey) {
            //          rollRight();
            lookDown();
        }
    }

    public void moveForward() {
        if (DEBUG) {
            Debug.output("Moving forward +");
        }
        doMove(new Vector3d(0.0, 0.0, -getMovementRate()));
    }

    public void moveBackward() {
        if (DEBUG) {
            Debug.output("Moving Backward _");
        }
        doMove(new Vector3d(0.0, 0.0, getMovementRate()));
    }

    public void moveLeft() {
        if (DEBUG) {
            Debug.output("Moving left <");
        }
        doMove(new Vector3d(-getMovementRate(), 0.0, 0.0));
    }

    public void moveRight() {
        if (DEBUG) {
            Debug.output("Moving right >");
        }
        doMove(new Vector3d(getMovementRate(), 0.0, 0.0));
    }

    public void moveUp() {
        if (DEBUG) {
            Debug.output("Moving up ^");
        }
        doMove(new Vector3d(0.0, getMovementRate(), 0.0));
    }

    public void moveDown() {
        if (DEBUG) {
            Debug.output("Moving down v ");
        }
        doMove(new Vector3d(0.0, -getMovementRate(), 0.0));
    }

    public void rotRight() {
        if (DEBUG) {
            Debug.output("Rotating right");
        }
        doRotateY(getRotateRightAmount());
    }

    public void lookRight() {
        if (DEBUG) {
            Debug.output("Looking right");
        }
        doLookY(getRotateRightAmount());
    }

    public void rotUp() {
        if (DEBUG) {
            Debug.output("Rotating up");
        }
        doRotateX(getRotateUpAmount());
    }

    public void lookUp() {
        if (DEBUG) {
            Debug.output("Looking up");
        }
        doLookX(getRotateUpAmount());
    }

    public void rotLeft() {
        if (DEBUG) {
            Debug.output("Rotating left");
        }
        doRotateY(getRotateLeftAmount());
    }

    public void lookLeft() {
        if (DEBUG) {
            Debug.output("Looking left");
        }
        doLookY(getRotateLeftAmount());
    }

    public void rotDown() {
        if (DEBUG) {
            Debug.output("Rotating down");
        }
        doRotateX(getRotateDownAmount());
    }

    public void lookDown() {
        if (DEBUG) {
            Debug.output("Looking down");
        }
        doLookX(getRotateDownAmount());
    }

    /**
     * Rotating position on the z axis, negative.
     */
    public void rollLeft() {
        if (DEBUG) {
            Debug.output("Rolling left");
        }
        doRotateZ(getRollLeftAmount());
    }

    /**
     * Tilting the view to the left.
     */
    public void rollLookLeft() {
        if (DEBUG) {
            Debug.output("Tilting left");
        }
        doLookZ(getRollLeftAmount());
    }

    /**
     * Rotating position on the z axis, positive.
     */
    public void rollRight() {
        if (DEBUG) {
            Debug.output("Rolling right");
        }
        doRotateZ(getRollRightAmount());
    }

    /**
     * Tilting the view to the right.
     */
    public void rollLookRight() {
        if (DEBUG) {
            Debug.output("Tilting right");
        }
        doLookZ(getRollRightAmount());
    }

    protected void changePosition(Transform3D toMove) {
        cameraTransformGroup.getTransform(transform3D);

        // Gather the total look transform on all three axis
        Transform3D viewTransform = new Transform3D();
        viewTransform.invert(xRotLookTransform);
        viewTransform.mulInverse(yRotLookTransform);
        viewTransform.mulInverse(zRotLookTransform);

        transform3D.mul(viewTransform);
        transform3D.mul(toMove);

        // May have to create and multiply the non-inverse look
        // transforms.
        transform3D.mulInverse(viewTransform);

        cameraTransformGroup.setTransform(transform3D);
    }

    public void doRotateY(double radians) {
        if (DEBUG) {
            Debug.output("OMKeyBehavior: rotating Y " + radians + " radians");
        }
        Transform3D toMove = new Transform3D();
        toMove.rotY(radians);
        changePosition(toMove);
    }

    public void doLookY(double radians) {
        if (DEBUG) {
            Debug.output("OMKeyBehavior: rotating view Y " + radians
                    + " radians");
        }
        cameraTransformGroup.getTransform(transform3D);
        Transform3D toMove = new Transform3D();
        toMove.rotY(radians);
        transform3D.mul(toMove);
        cameraTransformGroup.setTransform(transform3D);

        //Keep track of the view y rotation.
        yRotLookTransform.mul(toMove);
    }

    public void doRotateX(double radians) {
        if (DEBUG) {
            Debug.output("OMKeyBehavior: rotating X " + radians + " radians");
        }
        Transform3D toMove = new Transform3D();
        toMove.rotX(radians);
        changePosition(toMove);
    }

    public void doLookX(double radians) {
        if (DEBUG) {
            Debug.output("OMKeyBehavior: rotating view X " + radians
                    + " radians");
        }
        cameraTransformGroup.getTransform(transform3D);
        Transform3D toMove = new Transform3D();
        toMove.rotX(radians);
        transform3D.mul(toMove);
        cameraTransformGroup.setTransform(transform3D);

        //Keep track of the view x rotation.
        xRotLookTransform.mul(toMove);
    }

    public void doRotateZ(double radians) {
        if (DEBUG) {
            Debug.output("OMKeyBehavior: rotating Z " + radians + " radians");
        }
        Transform3D toMove = new Transform3D();
        toMove.rotZ(radians);
        changePosition(toMove);
    }

    public void doLookZ(double radians) {
        if (DEBUG) {
            Debug.output("OMKeyBehavior: rotating view Z " + radians
                    + " radians");
        }
        cameraTransformGroup.getTransform(transform3D);
        Transform3D toMove = new Transform3D();
        toMove.rotZ(radians);
        transform3D.mul(toMove);
        cameraTransformGroup.setTransform(transform3D);

        //Keep track of the view z rotation.
        zRotLookTransform.mul(toMove);
    }

    public void doMove(Vector3d theMove) {
        if (DEBUG) {
            Debug.output("OMKeyBehavior: moving " + theMove);
            Debug.output("     transform before:\n " + transform3D);
        }

        Transform3D toMove = new Transform3D();
        toMove.setTranslation(theMove);
        changePosition(toMove);
    }

    public double getMovementRate() {
        return moveRate * speed;
    }

    public double getRollLeftAmount() {
        return rotateZAmount * speed;
    }

    public double getRollRightAmount() {
        return -rotateZAmount * speed;
    }

    public double getRotateUpAmount() {
        return rotateYAmount * speed;
    }

    public double getRotateDownAmount() {
        return -rotateYAmount * speed;
    }

    public double getRotateLeftAmount() {
        return rotateYAmount * speed;
    }

    public double getRotateRightAmount() {
        return -rotateYAmount * speed;
    }

    public void setRotateXAmount(double radians) {
        rotateXAmount = radians;
    }

    public void setRotateYAmount(double radians) {
        rotateYAmount = radians;
    }

    public void setRotateZAmount(double radians) {
        rotateZAmount = radians;
    }

    public void setMovementRate(double meters) {
        moveRate = meters;
        // Travel rate in meters/frame
    }

    public void setForwardKey(int key) {
        forwardKey = key;
    }

    public void setBackKey(int key) {
        backKey = key;
    }

    public void setLeftKey(int key) {
        leftKey = key;
    }

    public void setRightKey(int key) {
        rightKey = key;
    }
}

