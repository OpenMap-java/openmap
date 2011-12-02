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
// $Revision: 1.15 $
// $Date: 2006/05/19 15:26:26 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bbn.openmap.BasicI18n;
import com.bbn.openmap.PropertyConsumer;
import com.bbn.openmap.event.ProgressEvent;
import com.bbn.openmap.event.ProgressSupport;

/**
 * The OpenMap ComponentFactory is a class that can construct objects from class
 * names, with the added capability of passing the new object a Properties
 * object to initialize itself. The new object may also receive a property
 * prefix to use to scope its properties from the Properties object. It is
 * sensitive to the OpenMap paradigm of marker names in a list: That a list of
 * objects can be defined as a space separated names (marker names) within a
 * String. Those marker names can serve as a prefix for other properties within
 * a Properties object, as well as the prefix for a '.class' property to define
 * the class name for the new object.
 */
public class ComponentFactory {

    public static Logger logger = Logger.getLogger("com.bbn.openmap.util.ComponentFactory");

    /**
     * The property to use for the class name of new objects - ".class". Expects
     * that a prefix will be prepended to it.
     */
    public static final String DotClassNameProperty = ".class";
    /**
     * A property to use for the class name of new objects - "class". Can be
     * used with the PropUtils.objectsFromProperties method as the defining
     * property.
     */
    public static final String ClassNameProperty = "class";

    /**
     * The singleton instance of the ComponentFactory.
     */
    private static ComponentFactory singleton;

    protected ComponentFactory() {
    }

    /**
     * Method call to retrieve the singleton instance of the ComponentFactory.
     * 
     * @return ComponentFactory.
     */
    protected static ComponentFactory getInstance() {
        if (singleton == null) {
            singleton = new ComponentFactory();
        }
        return singleton;
    }

    /**
     * Set the singleton instance of the ComponentFactory.
     * 
     * @param cf
     */
    protected static void setInstance(ComponentFactory cf) {
        singleton = cf;
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object, look in
     * the Properties object for the markerName.class property to get a class
     * name to create each object. Then, if the new objects are
     * PropertyConsumers, use the marker name as a property prefix to get
     * properties for that object out of the Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @return Vector containing the new Objects.
     */
    public static Vector<?> create(Vector<String> markerNames, Properties properties) {
        return getInstance()._create(markerNames, null, properties, null, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object, look in
     * the Properties object for the markerName.class property to get a class
     * name to create each object. Then, if the new objects are
     * PropertyConsumers, use the marker name as a property prefix to get
     * properties for that object out of the Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param prefix The prefix that should be prepended to the marker names.
     * @param properties Properties object containing the details.
     * @return Vector containing the new Objects.
     */
    public static Vector<?> create(Vector<String> markerNames, String prefix, Properties properties) {
        return getInstance()._create(markerNames, prefix, properties, null, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object, look in
     * the Properties object for the markerName.class property to get a class
     * name to create each object. Then, if the new objects are
     * PropertyConsumers, use the marker name as a property prefix to get
     * properties for that object out of the Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param prefix The prefix that should be prepended to the marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide progress updates
     *        to. It's OK if this is null to not have progress events sent.
     * @return Vector containing the new Objects.
     */
    public static Vector<?> create(Vector<String> markerNames, String prefix, Properties properties, ProgressSupport progressSupport) {
        return getInstance()._create(markerNames, prefix, properties, progressSupport, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object, look in
     * the Properties object for the markerName.class property to get a class
     * name to create each object. Then, if the new objects are
     * PropertyConsumers, use the marker name as a property prefix to get
     * properties for that object out of the Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide progress updates
     *        to. It's OK if this is null to not have progress events sent.
     * @return Vector containing the new Objects.
     */
    public static Vector<?> create(Vector<String> markerNames, Properties properties, ProgressSupport progressSupport) {
        return getInstance()._create(markerNames, null, properties, progressSupport, false);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object, look in
     * the Properties object for the markerName.class property to get a class
     * name to create each object. Then, if the new objects are
     * PropertyConsumers, use the marker name as a property prefix to get
     * properties for that object out of the Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide progress updates
     *        to. It's OK if this is null to not have progress events sent.
     * @param matchInOutVectorSize if true, then if there is any trouble
     *        creating an object, it's marker name will be placed in the
     *        returned vector instead of a component. If false, only valid
     *        objects will be returned in the vector.
     * @return Vector containing the new Objects. If a component could not be
     *         created, the markerName is returned in its place, so you can
     *         figure out which one couldn't be created. In any case, the size
     *         of the returned vector is the same size as the markerNames
     *         vector, so you can figure out which markerNames go with which
     *         objects.
     */
    public static Vector<?> create(Vector<String> markerNames, Properties properties, ProgressSupport progressSupport,
                                   boolean matchInOutVectorSize) {
        return getInstance()._create(markerNames, null, properties, progressSupport, matchInOutVectorSize);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object, look in
     * the Properties object for the markerName.class property to get a class
     * name to create each object. Then, if the new objects are
     * PropertyConsumers, use the marker name as a property prefix to get
     * properties for that object out of the Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param prefix The prefix that should be prepended to the marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide progress updates
     *        to. It's OK if this is null to not have progress events sent.
     * @param matchInOutVectorSize if true, then if there is any trouble
     *        creating an object, it's marker name will be placed in the
     *        returned vector instead of a component. If false, only valid
     *        objects will be returned in the vector.
     * @return Vector containing the new Objects. If a component could not be
     *         created, the markerName is returned in its place, so you can
     *         figure out which one couldn't be created. In any case, the size
     *         of the returned vector is the same size as the markerNames
     *         vector, so you can figure out which markerNames go with which
     *         objects.
     */
    public static Vector<?> create(Vector<String> markerNames, String prefix, Properties properties,
                                   ProgressSupport progressSupport, boolean matchInOutVectorSize) {

        return getInstance()._create(markerNames, prefix, properties, progressSupport, matchInOutVectorSize);
    }

    /**
     * Given a Vector of marker name Strings, and a Properties object, look in
     * the Properties object for the markerName.class property to get a class
     * name to create each object. Then, if the new objects are
     * PropertyConsumers, use the marker name as a property prefix to get
     * properties for that object out of the Properties.
     * 
     * @param markerNames String of space separated marker names.
     * @param prefix The prefix that should be prepended to the marker names.
     * @param properties Properties object containing the details.
     * @param progressSupport ProgressSupport object to provide progress updates
     *        to. It's OK if this is null to not have progress events sent.
     * @param matchInOutVectorSize if true, then if there is any trouble
     *        creating an object, it's marker name will be placed in the
     *        returned vector instead of a component. If false, only valid
     *        objects will be returned in the vector.
     * @return Vector containing the new Objects. If a component could not be
     *         created, the markerName is returned in its place, so you can
     *         figure out which one couldn't be created. In any case, the size
     *         of the returned vector is the same size as the markerNames
     *         vector, so you can figure out which markerNames go with which
     *         objects.
     */
    protected Vector<?> _create(Vector<String> markerNames, String prefix, Properties properties, ProgressSupport progressSupport,
                                boolean matchInOutVectorSize) {

        int size = markerNames.size();
        Vector<Object> vector = new Vector<Object>(size);

        if (progressSupport != null) {
            progressSupport.fireUpdate(ProgressEvent.UPDATE, "Creating Components", 100, 0);
        }

        for (int i = 0; i < size; i++) {
            String componentName = PropUtils.getScopedPropertyPrefix(prefix) + markerNames.elementAt(i);

            String classProperty = componentName + DotClassNameProperty;
            String className = properties.getProperty(classProperty);

            if (className == null) {
                logger.warning("Failed to locate property \"" + componentName + "\" with class \"" + classProperty
                        + "\"\n  Skipping component \"" + componentName + "\"");
                if (matchInOutVectorSize) {
                    vector.add(componentName);
                }
                continue;
            }

            if (progressSupport != null) {
                progressSupport.fireUpdate(ProgressEvent.UPDATE, "Creating Components", size, i);
            }

            Object component = create(className, componentName, properties);

            if (component != null) {
                vector.add(component);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("ComponentFactory: [" + className + "(" + i + ")] created");
                }
            } else {
                if (matchInOutVectorSize) {
                    vector.add(componentName);
                }
                logger.info("[" + componentName + " : " + className + "(" + i
                        + ")] NOT created. -- Set logging flag to FINE/FINER for details.");
            }
        }

        if (progressSupport != null) {
            progressSupport.fireUpdate(ProgressEvent.UPDATE, "Configuring...", size, size);
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
     * @param properties Properties to use to initialize the object, if the
     *        object is a PropertyConsumer.
     * @return object if all goes well, null if not.
     */
    public static Object create(String className, Properties properties) {
        return create(className, (Object[]) null, null, properties);
    }

    /**
     * Create a single object. If you want it to complain about classes it can't
     * find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param prefix Properties prefix to use by the object to scope its
     *        properties.
     * @param properties Properties to use to initialize the object, if the
     *        object is a PropertyConsumer.
     */
    public static Object create(String className, String prefix, Properties properties) {

        return create(className, (Object[]) null, prefix, properties);
    }

    /**
     * Create a single object. If you want it to complain about classes it can't
     * find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in the
     *        constructor of the component.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs) {
        return create(className, constructorArgs, null, null, null);
    }

    /**
     * Create a single object. If you want it to complain about classes it can't
     * find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in the
     *        constructor of the component.
     * @param argClasses an array of classes to use to scope which constructor
     *        to use. If null, then an array will be built from the
     *        constructorArgs.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs, Class<?>[] argClasses) {
        return create(className, constructorArgs, argClasses, null, null);
    }

    /**
     * Create a single object. If you want it to complain about classes it can't
     * find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in the
     *        constructor of the component.
     * @param prefix Properties prefix to use by the object to scope its
     *        properties.
     * @param properties Properties to use to initialize the object, if the
     *        object is a PropertyConsumer.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs, String prefix, Properties properties) {
        return create(className, constructorArgs, null, prefix, properties);
    }

    /**
     * Create a single object. If you want it to complain about classes it can't
     * find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in the
     *        constructor of the component.
     * @param argClasses an array of classes to use to scope which constructor
     *        to use. If null, then an array will be built from the
     *        constructorArgs.
     * @param prefix Properties prefix to use by the object to scope its
     *        properties.
     * @param properties Properties to use to initialize the object, if the
     *        object is a PropertyConsumer.
     * @return object if all goes well, null if anything bad happens.
     */
    public static Object create(String className, Object[] constructorArgs, Class<?>[] argClasses, String prefix,
                                Properties properties) {
        return getInstance()._create(className, constructorArgs, argClasses, prefix, properties);
    }

    /**
     * Create a single object. If you want it to complain about classes it can't
     * find, then set the 'basic' debug flag.
     * 
     * @param className Class name to instantiate.
     * @param constructorArgs an Object array of arguments to use in the
     *        constructor of the component.
     * @param argClasses an array of classes to use to scope which constructor
     *        to use. If null, then an array will be built from the
     *        constructorArgs.
     * @param prefix Properties prefix to use by the object to scope its
     *        properties.
     * @param properties Properties to use to initialize the object, if the
     *        object is a PropertyConsumer.
     * @return object if all goes well, null if anything bad happens.
     */
    protected Object _create(String className, Object[] constructorArgs, Class<?>[] argClasses, String prefix, Properties properties) {
        String errorMessage = null;
        Throwable exceptionCaught = null;
        boolean DEBUG = false;
        try {

            if (logger.isLoggable(Level.FINER)) {
                DEBUG = true;
                logger.finer("creating: " + className);
            }

            // Apparently, this fails in certain cases where OpenMap is being
            // used as a plugin in a NetBeans or Eclipse architecture and the
            // application classloader isn't aware of the plugins classes. It
            // limits the creation of the object to classes in the caller's
            // classloader.
            // Class newObjClass = Class.forName(className.trim());
            // replaced with:

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = this.getClass().getClassLoader();
            }
            Class<?> newObjClass = Class.forName(className.trim(), true, cl);

            if (DEBUG)
                logger.finer(" - got class for " + className);

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
                logger.finer(" - created class arguments [" + sb.toString() + "]");
            }

            Constructor<?> constructor = null;
            Object obj = null;

            try {
                constructor = newObjClass.getConstructor(argClasses);

                if (DEBUG)
                    logger.finer(" - got constructor");

                // Create component
                obj = constructor.newInstance(constructorArgs);
                if (DEBUG)
                    logger.finer(" - got object");

            } catch (NoSuchMethodException nsmei) {
                /*
                 * The argClasses may have subclasses of what the desired
                 * constructor needs, so we need to check explicitly.
                 */
                obj = createWithSubclassConstructorArgs(newObjClass, argClasses, constructorArgs);
                if (DEBUG && obj != null)
                    logger.finer(" - got object on try #2");
            }

            if (obj instanceof PropertyConsumer && properties != null) {
                if (DEBUG) {
                    logger.finer("  setting properties with prefix \"" + prefix + "\"");
                }
                ((PropertyConsumer) obj).setProperties(prefix, properties);

                if (Debug.debugging(BasicI18n.DEBUG_CREATE)) {
                    /*
                     * If we're interested in creating resource bundle files, we
                     * should cause these PropertyConsumers to ask for their
                     * property info, since this is where most of the elective
                     * GUI strings are queried and found.
                     */
                    ((PropertyConsumer) obj).getPropertyInfo(null);
                }

                if (DEBUG)
                    logger.finer(" - set properties");
            }
            return obj;

        } catch (NoSuchMethodException nsme) {
            exceptionCaught = nsme;
            errorMessage = "NoSuchMethodException: " + nsme.getMessage();
        } catch (InstantiationException ie) {
            exceptionCaught = ie;
            errorMessage = "InstantiationException: " + ie.getMessage() + " - Might be trying to create an abstract class";
        } catch (IllegalAccessException iae) {
            if (DEBUG)
                iae.printStackTrace();
            exceptionCaught = iae;
            errorMessage = "IllegalAccessException: " + iae.getMessage();
        } catch (IllegalArgumentException iae2) {
            if (DEBUG)
                iae2.printStackTrace();
            exceptionCaught = iae2;
            errorMessage = "IllegalArgumentException: " + iae2.getMessage();
        } catch (InvocationTargetException ite) {
            if (DEBUG)
                ite.printStackTrace();
            exceptionCaught = ite;
            errorMessage = "InvocationTargetException: " + ite.getMessage();
        } catch (ClassNotFoundException cnfe) {
            exceptionCaught = cnfe;
            errorMessage = "ClassNotFoundException: " + cnfe.getMessage();
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Failed to create \"" + className
                    + (prefix != null ? "\" using component marker name \"" + prefix + "\"" : "") + " - error message: "
                    + errorMessage);

            if (exceptionCaught != null) {
                logger.log(Level.WARNING, "Exception reported is as follows:", exceptionCaught);
            }
        }

        return null;
    }

    /**
     * Method to create Object with arguments.
     * 
     * @param newObjClass the Class to be created.
     * @param argClasses an array of Classes describing the arguments.
     * @param constructorArgs an array of Objects for arguments.
     * @return Object created from the Class and arguments.
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected Object createWithSubclassConstructorArgs(Class<?> newObjClass, Class<?>[] argClasses, Object[] constructorArgs)
            throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {

        boolean DEBUG = logger.isLoggable(Level.FINER);

        int numArgClasses = 0;

        if (argClasses != null) {
            numArgClasses = argClasses.length;
        }

        Constructor<?>[] constructors = newObjClass.getConstructors();
        int numConstructors = constructors.length;

        if (DEBUG) {
            logger.finer(" - searching " + numConstructors + " possible constructor" + (numConstructors == 1 ? "" : "s"));
        }

        for (int i = 0; i < numConstructors; i++) {
            Constructor<?> constructor = constructors[i];

            Class<?>[] arguments = constructor.getParameterTypes();
            int numArgs = arguments.length;

            // First, check the number of arguments for a match
            if (numArgs != numArgClasses) {
                if (DEBUG) {
                    logger.finer(" - constructor " + i + " with " + numArgs + " arguments not a match");
                }

                continue; // Nope, not it.
            }

            // OK, empty constructor desired, punch...
            // If argClasses == null, then numArgs will equal zero. Makes the
            // compiler happy.
            if (numArgs == 0 || argClasses == null) {
                if (DEBUG) {
                    logger.finer(" - constructor " + i + " with no arguments is a match");
                }
                return constructor;
            }

            // Check to see if the argument classes of the Constructor
            // are assignable to the desired argClasses being sought.
            boolean good = false;
            for (int j = 0; j < numArgs; j++) {
                if (arguments[j] == argClasses[j]) {
                    if (DEBUG) {
                        logger.finer(" - direct arg class match, arg " + j);
                    }
                    good = true; // Maintain true...
                } else if (arguments[j].isAssignableFrom(argClasses[j])) {

                    // Doesn't work quite yet. May have to check for
                    // super-super class,etc, and we still get an
                    // IllegalArgumentException due to argument type
                    // mismatch.

                    // Is this even necessary? Don't think so...
                    argClasses[j] = argClasses[j].getSuperclass();
                    if (DEBUG) {
                        logger.finer(" - superclass arg class match, arg " + j + " reassigning to " + argClasses[j].toString());
                    }
                    good = true; // Maintain true...
                    // } else if (constructorArgs[j] instanceof
                    // Number) {
                    // if (DEBUG) {
                    // Debug.output(" - Number type match, arg " + j);
                    // }
                    // good = true; // Maintain true...

                } else {
                    if (DEBUG) {
                        logger.finer(" - arg class mismatch on arg " + j + ", bailing (" + arguments[j].getName() + " vs. "
                                + argClasses[j].getName() + ")");
                    }
                    good = false; // Punch with false
                    break;
                }
            }

            if (good) {
                if (DEBUG) {
                    logger.finer(" - creating object");
                }
                Object obj = constructor.newInstance(constructorArgs);
                if (DEBUG) {
                    logger.finer(" - created object");
                }
                return obj;
            }
        }

        return null;
    }

}