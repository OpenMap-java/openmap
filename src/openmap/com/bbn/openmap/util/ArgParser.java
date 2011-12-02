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
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/util/ArgParser.java,v $
// $RCSfile: ArgParser.java,v $
// $Revision: 1.6 $
// $Date: 2005/08/24 20:17:48 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util;

import java.util.Vector;

/**
 * A simple class to manage the line arguments of a program. Takes the String[]
 * argv that is provided to the main method of a class, and separates them out,
 * depending on the options given to the ArgParser. After you create the parser,
 * add your options that you want.
 */
public class ArgParser {
    /**
     * The length to submit if you want a variable length list at the end of the
     * command line, like all the arguments left over.
     */
    public final static int TO_END = -1;
    /** The program name that's using the parser. */
    protected String programName;
    /** The Args that the parser is looking for. */
    protected Vector args;
    /** The String array that holds all of the leftover argvs. */
    protected String[] rest = new String[0];
    /** The character flag for an option. */
    protected char option = '-';
    /**
     * Tells the Args to accept the first letter of their name for argv options
     * specified with one letter.
     */
    protected boolean allowAbbr = true;

    /**
     * Create a parser for the named program. Automatically adds the -help
     * option.
     * 
     * @param pName the program name.
     */
    public ArgParser(String pName) {
        programName = pName;
        args = new Vector();
        args.add(new HelpArg());
    }

    /**
     * Add a argument to the parser. Don't include the '-' in the argName,
     * that's added automatically. Assumes that the option expects no arguments.
     * 
     * @param argName the command line option
     * @param desc a help line description.
     */
    public void add(String argName, String desc) {
        add(argName, desc, 0);
    }

    /**
     * Add a argument to the parser. Don't include the '-' in the argName,
     * that's added automatically.
     * 
     * @param argName the command line option
     * @param desc a help line description.
     * @param expectedNumberOfArguments the number of option parameters expected
     *        for this option.
     */
    public void add(String argName, String desc, int expectedNumberOfArguments) {
        add(argName, desc, expectedNumberOfArguments, false);
    }

    /**
     * Add a argument to the parser. Don't include the '-' in the argName,
     * that's added automatically.
     * 
     * @param argName the command line option
     * @param desc a help line description.
     * @param expectedNumberOfArguments the number of option parameters expected
     *        for this option.
     * @param expectDashedArguments tell the parser that this option may have
     *        arguments that may start with dashes, for instance, a negative
     *        number. False by default.
     */
    public void add(String argName, String desc, int expectedNumberOfArguments, boolean expectDashedArguments) {
        Arg newArg = new Arg(argName, desc, expectedNumberOfArguments, expectDashedArguments);
        args.add(newArg);
        if (Debug.debugging("parse")) {
            Debug.output("ArgParser: adding " + argName);
        }
    }

    /**
     * Parse and organize the array of Strings. If something goes wrong, bail()
     * may be called.
     * 
     * @return true if everything goes well, false if not.
     */
    public boolean parse(String[] argv) {
        try {
            if (argv == null || argv.length == 0) {
                return false;
            }
            for (int i = 0; i < argv.length; i++) {
                boolean hit = false;
                if (argv[i].charAt(0) == option) {
                    String eval = argv[i].substring(1);
                    for (int j = 0; j < args.size(); j++) {
                        Arg curArg = (Arg) args.elementAt(j);
                        if (curArg.is(eval, allowAbbr)) {
                            if (Debug.debugging("parse")) {
                                Debug.output("ArgParser: arg " + curArg.name + " reading values.");
                            }
                            if (!curArg.readArgs(argv, ++i)) {
                                // Something's wrong with the
                                // arguments.
                                bail("ArgParser: Unexpected arguments with option " + curArg.name + ".", true);
                            }
                            hit = true;
                            if (curArg.numExpectedValues != TO_END) {
                                i += (curArg.numExpectedValues - 1);
                            } else {
                                i = argv.length;
                            }
                        }
                    }
                    if (hit == false) {
                        // option flagged, but option unknown.
                        bail(programName + ": unknown option " + argv[i], false);
                    }
                }

                if (hit == false) {
                    if (i == 0) {
                        rest = argv;
                    } else {
                        int diff = argv.length - i;
                        rest = new String[diff];
                        for (int k = 0; k < diff; k++) {
                            rest[k] = argv[i + k];
                            if (rest[k].charAt(0) == option) {
                                bail("ArgParser: Not expecting option in list of arguments.", true);
                            }
                        }
                    }
                    if (Debug.debugging("parse")) {
                        Debug.output("ArgParser: adding " + rest.length + " strings to the leftover list.");
                    }

                    return true;
                }
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            bail("Expecting more arguments for option", true);
        } catch (NegativeArraySizeException nase) {
            return false;
        }
        return true;
    }

    /**
     * Called if something is messed up. Prints a message, and the usage
     * statement, if desired.
     * 
     * @param message a message to display.
     * @param printUsageStatement true to display a list of available options.
     */
    public void bail(String message, boolean printUsageStatement) {
        Debug.output(message);
        if (printUsageStatement)
            printUsage();
        System.exit(0);
    }

    /**
     * Tell the parser to accept first-letter representations of the options.
     */
    public void setAllowAbbr(boolean set) {
        allowAbbr = set;
    }

    /**
     * Tells whether the parser accepts first-letter representations of the
     * options.
     */
    public boolean getAllowAbbr() {
        return allowAbbr;
    }

    /**
     * Returns a Vector of Arg objects.
     */
    public Vector getArgs() {
        return args;
    }

    /**
     * Return a Arg object with a particular name. This method shouldn't be used
     * to figure out if values have been passed in to an application. It's to
     * find out if an option is available to be chosen, not if it has.
     */
    public Arg getArg(String name) {
        for (int i = 0; i < args.size(); i++) {
            ArgParser.Arg arg = (ArgParser.Arg) args.elementAt(i);
            if (name.equalsIgnoreCase(arg.name)) {
                return arg;
            }
        }
        return null;
    }

    /**
     * Given an Arg name, return the values. Returns a zero length array
     * (non-null) value for options that don't require arguments. Returns null
     * if the option name wasn't found in the list, or if the option wasn't
     * chosen in the parsed array of Strings.
     */
    public String[] getArgValues(String name) {
        for (int i = 0; i < args.size(); i++) {
            ArgParser.Arg arg = (ArgParser.Arg) args.elementAt(i);
            if (name.equalsIgnoreCase(arg.name)) {
                if (arg.flagged) {
                    return arg.values;
                }
            }
        }
        return null;
    }

    /**
     * Get the String[] that makes up the trailing Strings after the options
     * were parsed.
     */
    public String[] getRest() {
        return rest;
    }

    /**
     * Print a list of options added to the parser.
     */
    public void printUsage() {
        Debug.output(programName + " Arguments:");
        for (int i = 0; i < args.size(); i++) {
            ArgParser.Arg arg = (ArgParser.Arg) args.elementAt(i);
            StringBuffer sb = new StringBuffer();
            String filler = arg.name.length() < 6 ? "\t\t" : "\t";

            sb.append("  -").append(arg.name).append(filler).append(arg.description);
            if (arg.numExpectedValues == TO_END) {
                sb.append(" (Variable number of arguments expected)");
            } else if (arg.numExpectedValues == 1) {
                sb.append(" (1 argument expected)");
            } else {
                sb.append(" (").append(arg.numExpectedValues).append(" arguments expected)");
            }
            Debug.output(sb.toString());
        }
    }

    public static void main(String[] argv) {
        Debug.init();
        ArgParser ap = new ArgParser("ArgParser");
        ap.add("first", "First test argument, no parameters expected");
        ap.add("second", "Second test argument, two parameters expected", 2);
        ap.add("third", "Third test argument, no parameters expected");
        ap.add("fourth", "Fourth test argument, one parameter expected", 1);

        if (!ap.parse(argv)) {
            ap.printUsage();
            System.exit(0);
        }

        int i;
        Vector args = ap.getArgs();
        for (i = 0; i < args.size(); i++) {
            ArgParser.Arg a = (ArgParser.Arg) args.elementAt(i);
            Debug.output(a.toString());
        }

        String[] rest = ap.getRest();
        Debug.output("Rest:");
        for (i = 0; i < rest.length; i++) {
            Debug.output(rest[i]);
        }
    }

    /**
     * A default version of the Arg class used to represent options for the
     * ArgParser to use.
     */
    public class Arg {
        public String name;
        public String description;
        public int numExpectedValues;
        public String[] values = null;
        public char c;
        public boolean flagged = false;
        public boolean dashedArguments = false;

        /**
         * Create an Arg with a name and help line description.
         */
        public Arg(String aName, String desc) {
            this(aName, desc, 0);
        }

        /**
         * Create an Arg with a name and help line description, along with a
         * number of expected arguments to follow this option.
         */
        public Arg(String aName, String desc, int expectedNumberOfArguments) {
            this(aName, desc, expectedNumberOfArguments, false);
        }

        /**
         * Create an Arg with a name and help line description, along with a
         * number of expected arguments to follow this option. Has an argument
         * to not check for arguments that may start with dashes, in case one of
         * the arguments may be a negative number.
         */
        public Arg(String aName, String desc, int expectedNumberOfArguments, boolean expectDashedArguments) {
            name = aName;
            description = desc;
            numExpectedValues = expectedNumberOfArguments;
            c = name.charAt(0);
            dashedArguments = expectDashedArguments;
        }

        /**
         * Returns true if the atg string matches the name of the Arg, or, if
         * allowAbbr is true, returns true if the arg length is one and it
         * matches the first letter of the arg name.
         */
        public boolean is(String arg, boolean allowAbbr) {
            if (name.equalsIgnoreCase(arg)) {
                return true;
            }

            if (allowAbbr && arg.length() == 1) {
                if (arg.charAt(0) == c) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Runs through the array of Strings, starting at the argIndex, and
         * creates the values array from it. Uses the expected number of
         * arguments to tell when it's done. Returns true if everything happens
         * as expected.
         * 
         * @param argv the entire array passed to the parser.
         * @param argIndex the index of the first option argument value.
         * @return true if what was read was what was expected.
         */
        public boolean readArgs(String[] argv, int argIndex)
                throws ArrayIndexOutOfBoundsException, NegativeArraySizeException {

            if (numExpectedValues != TO_END) {
                values = new String[numExpectedValues];
            } else {
                values = new String[argv.length - argIndex];
            }

            for (int i = 0; i < values.length; i++) {
                values[i] = argv[argIndex + i];
                if (values[i].charAt(0) == option && !dashedArguments) {
                    if (numExpectedValues != TO_END) {
                        Debug.output("ArgParser: Option " + name + " expects " + numExpectedValues
                                + (numExpectedValues == 1 ? " argument." : " arguments."));

                    } else {
                        Debug.output("ArgParser: Option " + name + " not expecting options after its values.");
                    }
                    return false; // Unexpected argument.
                }
            }
            flagged = true;
            return true;
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("Arg: ").append(name).append(" expects ").append(numExpectedValues)
              .append((numExpectedValues == 1 ? " value.\n" : " values.\n"));
            if (values != null) {
                sb.append("Values: ");
                for (int i = 0; i < values.length; i++) {
                    sb.append("[").append(values[i]).append("]");
                }
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * A Arg class to spur off help messages. Gets added automatically to the
     * parser.
     */
    public class HelpArg
            extends ArgParser.Arg {

        public HelpArg() {
            super("help", "Print usage statement, with arguments.", 0);
        }

        public boolean is(String arg, boolean allowAbbr) {
            boolean askingForHelp = super.is(arg, allowAbbr);
            if (askingForHelp) {
                ArgParser.this.bail("", true);
            }
            return false;
        }
    }

}