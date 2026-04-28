package com.bbn.openmap.proj.coords;

import com.bbn.openmap.proj.Ellipsoid;

/**
 * Helmert 7-parameter transformation
 * 
 * http://www.posc.org/Epicentre.2_2/DataModel/ExamplesofUsage/eu_cs35.html
 * https://www.og.berr.gov.uk/regulation/guidance/co_systems/co_sys_12.htm
 * http://www.ogp.org.uk/pubs/373-10.pdf
 */
public class HelmertTransformation {

    private static final int TYPE_POSITION_VECTOR = 1;

    private static final int TYPE_COORDINATE_FRAME = 2;

    /**
     * Parameters to transform from WGS84 to ED50 north of 62deg N.
     */
    public static final HelmertTransformation WGS84_TO_ED50_N62N = new HelmertTransformation(116.641d, 56.931d, 110.559d, -0.000004327d, -0.000004464d, 0.000004444d, ppmToM(3.520d), TYPE_POSITION_VECTOR);

    /**
     * Parameters to transform from ED50 to WGS84 north of 62deg N.
     */
    public static final HelmertTransformation ED50_TO_WGS84_N62N = WGS84_TO_ED50_N62N.createReverse();

    /**
     * Parameters to transform from WGS84 to ED50 south of 62deg N.
     */
    public static final HelmertTransformation WGS84_TO_ED50_S62N = new HelmertTransformation(90.365d, 101.130d, 123.384d, -0.000001614d, -0.000000373d, -0.000004334d, ppmToM(-1.994d), TYPE_POSITION_VECTOR);

    /**
     * Parameters to transform from ED50 to WGS84 south of 62deg N.
     */
    private static final HelmertTransformation ED50_TO_WGS84_S62N = WGS84_TO_ED50_S62N.createReverse();

    private final double dX;
    private final double dY;
    private final double dZ;

    private final double rX;
    private final double rY;
    private final double rZ;

    private final double neg_rX;
    private final double neg_rY;
    private final double neg_rZ;

    private final double m;

    private final int type;

    public static final HelmertTransformation find(Ellipsoid source, Ellipsoid dest) {
        // TODO: include projection center
        if ((source == Ellipsoid.WGS_84) && (dest == Ellipsoid.INTERNATIONAL)) {
            return WGS84_TO_ED50_S62N;
        }
        if ((source == Ellipsoid.INTERNATIONAL) && (dest == Ellipsoid.WGS_84)) {
            return ED50_TO_WGS84_S62N;
        }
        throw new IllegalArgumentException("Unknown transformation from " + source + " to " + dest);
    }

    /**
     * 
     * @param dX
     * @param dY
     * @param dZ
     * @param rX
     * @param rY
     * @param rZ
     * @param m a scale correction factor near 1. see {@link #ppmToM(double)}
     * @param type
     */
    private HelmertTransformation(double dX, double dY, double dZ, double rX, double rY, double rZ,
            double m, int type) {
        this.dX = dX;
        this.dY = dY;
        this.dZ = dZ;

        this.rX = rX;
        this.rY = rY;
        this.rZ = rZ;

        this.neg_rX = -1d * rX;
        this.neg_rY = -1d * rY;
        this.neg_rZ = -1d * rZ;

        this.m = m;

        this.type = type;
    }

    private HelmertTransformation createReverse() {
        // m should be switched around 1. So 0.9 should be 1.1 and so on.
        double newM = 2d - m;
        return new HelmertTransformation(-1d * dX, -1d * dY, -1d * dZ, neg_rX, neg_rY, neg_rZ, newM, type);
    }

    public void apply(ECEFPoint coord) {
        double x = coord.x_;
        double y = coord.y_;
        double z = coord.z_;

        x *= m;
        y *= m;
        z *= m;

        switch (type) {
        case TYPE_POSITION_VECTOR:
            x = x + (neg_rZ * y) + (rX * z);
            y = (rZ * x) + y + (neg_rX * z);
            z = (neg_rY * x) + (rX * y) + z;
            break;
        case TYPE_COORDINATE_FRAME:
            x = x + (rZ * y) + (neg_rX * z);
            y = (neg_rZ * x) + y + (rX * z);
            z = (rY * x) + (neg_rX * y) + z;
            break;
        }

        x += dX;
        y += dY;
        z += dZ;

        coord.setECEF(x, y, z);
    }

    /**
     * Convert the scale factor from parts per million.
     * 
     * @param ppm scale factor in parts per million
     * @return scale correction factor.
     */
    private static final double ppmToM(double ppm) {
        return 1d + (ppm / 1000000d);
    }

}
