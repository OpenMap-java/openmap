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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/ComponentFactory.java,v $
// $RCSfile: ComponentFactory.java,v $
// $Revision: 1.12 $
// $Date: 2005/02/11 22:42:01 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Vector;

import com.bbn.openmap.BasicI18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressSupport;

/**
 * The OpenMap ComponentFactory is a class that can construct objects
 * from class names, with the added capability of passing the new
 * object a Properties object to initialize itself. The new object may
 * also receive a property prefix to use to scope its properties from
 * the Properties object. It is sensitive to the OpenMap paradigm of
 * marker names in a list: That a list of objects can be defined as a
 * space separated names (marker names) within a String. Those marker
 * names can serve as a prefix for other properties within a
 * Properties object, as well as the prefix for a '.class' property to
 * define the class name for the new object.
 */
public class ComponentFactory {

    /**
     * The property to use for the class name of new objects -
     * ".class". Expects that a prefix will be prepended to it.
     */
    public static final String ClassNameProperty = ".class";

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object. Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @return Vector containing the new Objects.
     */
    public static Vector create(Vector markerNames, Properties properties) {
        return create(markerNames, null, properties, null, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object. Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param prefix The prefix that should be prepended to the marker
     *        names.
     * @param properties Properties object containing the details.
     * @return Vector containing the new Objects.
     */
    public static Vector create(Vector markerNames, String prefix,
                                Properties properties) {
        return create(markerNames, prefix, properties, null, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object. Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param prefix The prefix that should be prepended to the marker
     *        names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide
     *        progress updates to. It's OK if this is null to not have
     *        progress events sent.
     * @return Vector containing the new Objects.
     */
    public static Vector create(Vector markerNames, String prefix,
                                Properties properties,
                                ProgressSupport progressSupport) {
        return create(markerNames, prefix, properties, progressSupport, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object. Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide
     *        progress updates to. It's OK if this is null to not have
     *        progress events sent.
     * @return Vector containing the new Objects.
     */
    public static Vector create(Vector markerNames, Properties properties,
                                ProgressSupport progressSupport) {
        return create(markerNames, null, properties, progressSupport, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object. Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide
     *        progress updates to. It's OK if this is null to not have
     *        progress events sent.
     * @param matchInOutVectorSize if true, then if there is any
     *        trouble creating an object, it's marker name will be
     *        placed in the returned vector instead of a component. If
     *        false, only valid objects will be returned in the
     *        vector.
     * @return Vector containing the new Objects. If a component could
     *         not be created, the markerName is returned in its
     *         place, so you can figure out which one couldn't be
     *         created. In any case, the size of the returned vector
     *         is the same size as the markerNames vector, so you can
     *         figure out which markerNames go with which objects.
     */
    public static Vector create(Vector markerNames, Properties properties,
                                ProgressSupport progressSupport,
                                boolean matchInOutVectorSize) {
        return create(markerNames,
                null,
                properties,
                progressSupport,
                matchInOutVectorSize);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object,
     * look in the Properties object for the markerName.class property
     * to get a class name to create each object. Then, if the new
     * objects are PropertyConsumers, use the marker name as a
     * property prefix to get properties for that object out of the
     * Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param prefix The prefix that should be prepended to the marker
     *        names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide
     *        progress updates to. It's OK if this is null to not have
     *        progress events sent.
     * @param matchInOutVectorSize if true, then if there is any
     *        trouble creating an object, it's marker name will be
     *        placed in the returned vector instead of a component. If
     *        false, only valid objects will be returned in the
     *        vector.
     * @return Vector containing the new Objects. If a component could
     *         not be created, the markerName is returned in its
     *         place, so you can figure out which one couldn't be
     *         created. In any case, the size of the returned vector
     *         is the same size as the markerNames vector, so you can
     *         figure out which markerNames go with which objects.
     */
    public static Vector create(Vector markerNames, String prefix,
                                Properties properties,
                                ProgressSupport progressSupport,
                                boolean matchInOutVectorSize) {

        int size = markerNames.size();
        Vector vector = new Vector(size);

        if (progressSupport != null) {
            progressSupport.fireUpdate(ProgressEvent.UPDATE,
                    "Creating Components",
                    100,
                    0);
        }

        for (int i = 0; i < size; i++) {
            String componentName = PropUtils.getScopedPropertyPrefix(prefix)
                    + (String) markerNames.elementAt(i);

            String classProperty = componentName + ClassNameProperty;
            String className = properties.getProperty(classProperty);

            if (className == null) {
                Debug.error("ComponentFactory.create: Failed to locate property \""
                        + componentName
                        + "\" with class \""
                        + classProperty
                        + "\"\n  Skipping component \"" + componentName + "\"");
                if (matchInOutVectorSize) {
                    vector.add(componentName);
                }
                continue;
            }

            if (progressSupport != null) {
                progressSupport.fireUpdate(ProgressEvent.UPDATE,
                        "Creating Components",
                        size,
                        i);
            }

            Object component = create(className, componentName, properties);

            if (component != null) {
                vector.add(component);
                if (Debug.debugging("componentfactory")) {
                    Debug.output("ComponentFactory: [" + className + "(" + i
                            + ")] created");
                }
            } else {
                if (matchInOutVectorSize) {
                    vector.add(componentName);
                }
                Debug.output("ComponentFactory: [" + componentName + " : "
                        + className + "(" + i + ")] NOT created. -- "
                        + "Set 'componentfactory' debug flag for details.");
            }
        }

        if (progressSupport != null) {
            progressSupport.fireUpdate(ProgressEvent.UPDATE,
                    "Configuring...",
                    size,
                    size);
        }
        return vector;
    }

    /**
     * Create a single object.
     * 
     * @param className Class name to instantiate, empty constructor.
     * @return object if all goes well, null if not.
     */
    public static Object create(String className) {
        return create(className, (Object[]) null, null, null);
    }

    /**
     * Create a single object.
     * 
     * @param className Class name to instantiate.
     * @param properties Properties to use to initalize the object, if
     *        the object is a PropertyConsumer.
     * @return object if all goes well, null if not.
     */
    public static Object create(String className, Properties properties) {
        return create(className, (Object[]) null, null, properties);
    }

    /**
     * Create a single object. If you want it to complain about
     * classes it can't find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param prefix Properties prefix to use by the object to scope
     *        its properties.
     * @param properties Properties to use to initalize the object, if
     *        the object is a PropertyConsumer.
     */
    public static Object create(String className, String prefix,
                                Properties properties) {

        return create(className, (Object[]) null, prefix, properties);
    }

    /**
     * Create a single object. If you want it to complain about
     * classes it can't find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in
     *        the constructor of the component.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs) {
        return create(className, constructorArgs, null, null, null);
    }

    /**
     * Create a single object. If you want it to complain about
     * classes it can't find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in
     *        the constructor of the component.
     * @param argClasses an array of classes to use to scope which
     *        constructor to use. If null, then an array will be built
     *        from the constructorArgs.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs,
                                Class[] argClasses) {
        return create(className, constructorArgs, argClasses, null, null);
    }

    /**
     * Create a single object. If you want it to complain about
     * classes it can't find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in
     *        the constructor of the component.
     * @param prefix Properties prefix to use by the object to scope
     *        its properties.
     * @param properties Properties to use to initalize the object, if
     *        the object is a PropertyConsumer.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs,
                                String prefix, Properties properties) {
        return create(className, constructorArgs, null, prefix, properties);
    }

    /**
     * Create a single object. If you want it to complain about
     * classes it can't find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in
     *        the constructor of the component.
     * @param argClasses an array of classes to use to scope which
     *        constructor to use. If null, then an array will be built
     *        from the constructorArgs.
     * @param prefix Properties prefix to use by the object to scope
     *        its properties.
     * @param properties Properties to use to initalize the object, if
     *        the object is a PropertyConsumer.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs,
                                Class[] argClasses, String prefix,
                                Properties properties) {

        String errorMessage = null;
        boolean DEBUG = false;
        try {

            if (Debug.debugging("componentfactorydetail")) {
                DEBUG = true;
                Debug.output("ComponentFactory.create: " + className);
            }

            Class newObjClass = Class.forName(className);
            if (DEBUG)
                Debug.output(" - got class for " + className);

            if (argClasses == null) {
                if (constructorArgs != null && constructorArgs.length > 0) {

                    argClasses = new Class[constructorArgs.length];
                    for (int i = 0; i < argClasses.length; i++) {
                        argClasses[i] = constructorArgs[i].getClass();
                    }
                } else {
                    // If empty, make null
                    constructorArgs = null;
                }
            }

            if (DEBUG) {
                StringBuffer sb = new StringBuffer();
                if (constructorArgs == null) {
                    sb.append("null");
                } else {
                    for (int i = 0; i < constructorArgs.length; i++) {
                        sb.append(constructorArgs[i].getClass().getName());
                        if (i < constructorArgs.length - 1)
                            sb.append(", ");
                    }
                }
                Debug.output(" - created class arguments [" + sb.toString()
                        + "]");
            }

            Constructor constructor = null;
            Object obj = null;

            try {
                constructor = newObjClass.getConstructor(argClasses);

                if (DEBUG)
                    Debug.output(" - got constructor");

                // Create component
                obj = constructor.newInstance(constructorArgs);
                if (DEBUG)
                    Debug.output(" - got object");

            } catch (NoSuchMethodException nsmei) {
                // The argClasses may have subclasses of what the
                // desired
                // constructor needs, so we need to check explicitly.
                obj = createWithSubclassConstructorArgs(newObjClass,
                        argClasses,
                        constructorArgs);
                if (DEBUG && obj != null)
                    Debug.output(" - got object on try #2");
            }

            if (obj instanceof PropertyConsumer && properties != null) {
                if (DEBUG) {
                    Debug.output("  setting properties with prefix \"" + prefix
                            + "\"");
                }
                ((PropertyConsumer) obj).setProperties(prefix, properties);

                if (Debug.debugging(BasicI18n.DEBUG_CREATE)) {
                    // If we're interested in creating resource bundle
                    // files, we should cause these PropertyConsumers
                    // to ask for their property info, since this is
                    // where most of the elective GUI strings are
                    // queried and found.
                    ((PropertyConsumer) obj).getPropertyInfo(null);
                }

                if (DEBUG)
                    Debug.output(" - set properties");
            }
            return obj;

        } catch (NoSuchMethodException nsme) {
            errorMessage = "NoSuchMethodException: " + nsme.getMessage();
        } catch (InstantiationException ie) {
            errorMessage = "InstantiationException: " + ie.getMessage()
                    + " - Might be trying to create an abstract class";
        } catch (IllegalAccessException iae) {
            if (DEBUG)
                iae.printStackTrace();
            errorMessage = "IllegalAccessException: " + iae.getMessage();
        } catch (IllegalArgumentException iae2) {
            if (DEBUG)
                iae2.printStackTrace();
            errorMessage = "IllegalArgumentException: " + iae2.getMessage();
        } catch (InvocationTargetException ite) {
            if (DEBUG)
                ite.printStackTrace();
            errorMessage = "InvocationTargetException: " + ite.getMessage();
        } catch (ClassNotFoundException cnfe) {
            errorMessage = "ClassNotFoundException: " + cnfe.getMessage();
        }

        if (Debug.debugging("componentfactory")) {
            Debug.error("ComponentFactory.create: Failed to create \""
                    + className
                    + (prefix != null ? "\" using component marker name \""
                            + prefix + "\"" : "") + " - error message: "
                    + errorMessage);
        }

        return null;
    }

    protected static Object createWithSubclassConstructorArgs(
                                                              Class newObjClass,
                                                              Class[] argClasses,
                                                              Object[] constructorArgs)
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        boolean DEBUG = Debug.debugging("componentfactorydetail");

        int numArgClasses = 0;

        if (argClasses != null) {
            numArgClasses = argClasses.length;
        }

        Constructor[] constructors = newObjClass.getConstructors();
        int numConstructors = constructors.length;

        if (DEBUG) {
            Debug.output(" - searching " + numConstructors
                    + " possible constructor"
                    + (numConstructors == 1 ? "" : "s"));
        }

        for (int i = 0; i < numConstructors; i++) {
            Constructor constructor = constructors[i];

            Class[] arguments = constructor.getParameterTypes();
            int numArgs = arguments.length;

            // First, check the number of arguments for a match
            if (numArgs != numArgClasses) {
                if (DEBUG) {
                    Debug.output(" - constructor " + i + " with " + numArgs
                            + " arguments not a match");
                }

                continue; // Nope, not it.
            }

            // OK, empty constructor desired, punch...
            if (numArgs == 0) {
                if (DEBUG) {
                    Debug.output(" - constructor " + i
                            + " with no arguments is a match");
                }
                return constructor;
            }

            // Check to see if the argument classes of the Constructor
            // are
            // assignable to the desired argClasses being sought.
            boolean good = false;
            for (int j = 0; j < numArgs; j++) {
                if (arguments[j] == argClasses[j]) {
                    if (DEBUG) {
                        Debug.output(" - direct arg class match, arg " + j);
                    }
                    good = true; // Maintain true...
                } else if (arguments[j].isAssignableFrom(argClasses[j])) {

                    //  Doesn't work quite yet. May have to check for
                    //  super-super class,etc, and we still get an
                    //  IllegalArgumentException due to argument type
                    //  mismatch.

                    // Is this even necessary? Don't think so...
                    argClasses[j] = argClasses[j].getSuperclass();
                    if (DEBUG) {
                        Debug.output(" - superclass arg class match, arg " + j
                                + " reassigning to " + argClasses[j].toString());
                    }
                    good = true; // Maintain true...
                    //              } else if (constructorArgs[j] instanceof
                    // Number) {
                    //                  if (DEBUG) {
                    //                      Debug.output(" - Number type match, arg " + j);
                    //                  }
                    //                  good = true; // Maintain true...

                } else {
                    if (DEBUG) {
                        Debug.output(" - arg class mismatch on arg " + j
                                + ", bailing (" + arguments[j].getName()
                                + " vs. " + argClasses[j].getName() + ")");
                    }
                    good = false; // Punch with false
                    break;
                }
            }

            if (good) {
                if (DEBUG) {
                    Debug.debugging(" - creating object");
                }
                Object obj = constructor.newInstance(constructorArgs);
                if (DEBUG) {
                    Debug.debugging(" - created object");
                }
                return obj;
            }
        }

        return null;
    }

}