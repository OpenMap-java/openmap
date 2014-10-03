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
// $Source:
// /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/e00/ArcData.java,v
// $
// $RCSfile: ArcData.java,v $
// $Revision: 1.5 $
// $Date: 2009/01/21 01:24:41 $
// $Author: dietrick $
//
// **********************************************************************

package com.bbn.openmap.layer.e00;

public class ArcData
        extends E00Data {
    double[] coords;
    int deja = 0;

    public ArcData() {
    }

    public ArcData(E00Data d) {
        valeur = d.valeur;
        valeur2 = d.valeur2;
        id = d.id;
        type = d.type;
    }

    ArcData c0, c1, f0, f1, c2, f2;
    int numpoint;

    void setC(ArcData d) {
        if (d.type == 0) {
            if (c0 == null)
                c0 = d;
            else
                c2 = d;
            if (type == 1)
                d.valeur2 = valeur;
        } else if (d.type == 1) {
            if (c1 == null)
                c1 = d;
            else
                c2 = d;
            if (type == 0)
                valeur = d.valeur;
        }
    }

    void setF(ArcData d) {
        if (d.type == 0) {
            if (f0 == null)
                f0 = d;
            else
                f2 = d;
            if (type == 1)
                d.valeur = valeur;
        } else if (d.type == 1) {
            if (f1 == null)
                f1 = d;
            else
                f2 = d;
            if (type == 0)
                valeur2 = d.valeur;
        }
    }

    void print() {
        System.out.print("arc " + id + " type " + type + " ");
        System.out.print((c0 != null) ? c0.id : 0);
        System.out.print(' ');
        System.out.print((f0 != null) ? f0.id : 0);
        System.out.print(' ');
        System.out.print((c1 != null) ? c1.id : 0);
        System.out.print(' ');
        System.out.print((f1 != null) ? f1.id : 0);
        System.out.print(' ');
        System.out.print((c2 != null) ? c2.id : 0);
        System.out.print(' ');
        System.out.print((f2 != null) ? f2.id : 0);
        System.out.println();
    }

    ArcData visit(ArcData d) {
        ArcData g1, g2, h1, h2;
        if (d.type > 1)
            return null;
        if ((d == c0) || (d == c1) || (d == c2)) {
            deja = 1;
            if (f0 != null)
                return f0;
            h1 = f1;
            h2 = f2;
        } else if ((d == f0) || (d == f1) || (d == f2)) {
            deja = -1;
            if (c0 != null)
                return c0;
            h1 = c1;
            h2 = c2;
        } else {
            System.out.println("ERREUR " + id);
            return null;
        }
        g1 = teste(h1);
        g2 = teste(h2);
        if (g1 == null)
            return h2;
        if (g2 != null)
            System.out.print("(ALT " + h2.id + ")");
        return h1;
    }

    ArcData teste(ArcData d) {
        if (d == null)
            return null;
        if (d.deja != 0)
            return null;
        ArcData h = null;
        if (d.c0 == this)
            h = d.f0;
        else if (d.f0 == this)
            h = d.c0;
        else
            System.out.println("ERREUR " + id + " " + d.id);
        if (h == null)
            return null;
        if (((h.f1 == d) || (h.f2 == d)) && (h.f0 == null) && (h.deja <= 0))
            return h;
        if (((h.c1 == d) || (h.c2 == d)) && (h.c0 == null) && (h.deja >= 0))
            return h;
        return null;
    }

    ArcData visit() {
        if (f0 != null)
            return f0;
        if (c0 != null)
            return c0;
        return null;
    }

    ArcData[] getChaine0() {
        deja = 1;
        ArcData d1, d2, di, df;
        d1 = this;
        d2 = c0;
        while (d2 != null) {
            if (d2 == this) {
                System.out.println("Arc " + id);
                return new ArcData[] {
                    this,
                    null
                };
            }
            d2.deja = 1;
            if (d2.c0 == d1) {
                d1 = d2;
                d2 = d2.f0;
            } else if (d2.f0 == d1) {
                d1 = d2;
                d2 = d2.c0;
            } else {
                System.out.println("Erreur " + d1.id + "->" + d2.id);
                break;
            }
        }
        di = d1;
        d1 = this;
        d2 = f0;
        while (d2 != null) {
            d2.deja = 1;
            if (d2.c0 == d1) {
                d1 = d2;
                d2 = d2.f0;
            } else if (d2.f0 == d1) {
                d1 = d2;
                d2 = d2.c0;
            } else {
                System.out.println("Erreur " + d1.id + "->" + d2.id);
                break;
            }
        }
        df = d1;
        System.out.println("arc " + di.id + "->" + df.id);
        return new ArcData[] {
            di,
            df
        };
    }

}