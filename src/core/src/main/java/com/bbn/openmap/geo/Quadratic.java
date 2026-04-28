/**
 * Copyright (C) BBNT Solutions LLC.  All Rights Reserved
 * $Id: Quadratic.java,v 1.1 2005/11/18 14:57:46 mthome Exp $
 */

package com.bbn.openmap.geo;

/**
 * Computes 0 to 2 real roots of the quadratic:
 * 
 * <pre>
 *    a*x^2 + b*x + c = 0
 * </pre>
 * 
 * See http://web.cs.mun.ca/courses/cs2710-w02/lab5/assignment/assign5.html.
 * http://www.1728.com/quadratc.htm
 * <p>
 * Access the roots as a double iterator with methods hasNext() and next().
 */

public class Quadratic {
    double a, b, c;
    double[] solution;
    int index = 0;

    public Quadratic(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
        solve();
    }

    /** Returns the solution, x, to the equation a*x+b = 0 **/
    public static double linear(double a, double b) {
        return -b / a;
    }

    /**
     * Solve a*x^2 + b*x + c = 0 for x for real values of x.
     * <p>
     * 
     * See Winston and Horn, LISP p. 167-169.
     */
    public void solve() {
        if (a == 0.0 && b == 0.0)
            values();
        else if (a == 0.0 && b != 0.0)
            values(linear(b, c));
        else if (a != 0.0 && c == 0.0)
            values(0.0, linear(a, b));
        else {
            double d = b * b - 4.0 * a * c;
            if (d < 0.0)
                values(); // Complex roots.
            else if (d == 0.0)
                values(-b / (2.0 * a));
            else
                values((-b + Math.sqrt(d)) / (2.0 * a), (-b - Math.sqrt(d)) / (2.0 * a));
        }
    }

    /** Evaluate the Quadratic at x. **/
    public double eval(double x) {
        return (a * x + b) * x + c;
    }

    public boolean hasNext() {
        return index < solution.length;
    }

    public double next() {
        if (this.hasNext())
            return solution[index++];
        else
            throw new java.util.NoSuchElementException();
    }

    public String toString() {
        return "{" + a + "*x^2 + " + b + "*x + " + c + " = 0 [" + solutionToString() + "]}";
    }

    private String solutionToString() {
        switch (solution.length) {
            case 0:
                return "";
            case 1:
                return String.valueOf(solution[0]);
            case 2:
                return String.valueOf(solution[0]) + " " + String.valueOf(solution[1]);
            default:
                return ""; // Can't happen.
        }
    }

    private void values() {
        solution = new double[0];
    }

    private void values(double s1) {
        solution = new double[] {
            s1
        };
    }

    private void values(double s1, double s2) {
        solution = new double[] {
            s1,
            s2
        };
    }

    public String test() {
        String s = this + " test: ";
        switch (solution.length) {
            case 0:
                return "";
            case 1:
                return s + this.eval(solution[0]);
            case 2:
                return s + this.eval(solution[0]) + " " + this.eval(solution[1]);
            default:
                return ""; // Can't happen.
        }
    }

    static private void p(double a, double b, double c) {
        System.out.println((new Quadratic(a, b, c)).test());
    }

    public static void main(String[] args) {
        p(0.0, 0.0, -30.0);
        p(0.0, 4.0, 0.0);
        p(0.0, 4.0, -30.0);
        p(2.0, 4.0, 0.0);
        p(2.0, 4.0, -30.0);
        p(1.0, 4.0, 1.0);
    }
}
