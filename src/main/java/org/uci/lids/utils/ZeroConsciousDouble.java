package org.uci.lids.utils;

/**
 * Created by hamid on 9/25/15.
 */
public class ZeroConsciousDouble {
    private double a;
    private int i;

    public ZeroConsciousDouble(double value) {
        if (value == 0) {
            a = 0;
            i = 0;
        } else {
            a = value;
            i = 0;
        }
    }

    public double toDouble() {
        if (i == 1)
            return 0;
        else
            return a;
    }

    public ZeroConsciousDouble(double a, int i) {
        this.a = a;
        this.i = i;
    }

    public ZeroConsciousDouble add(ZeroConsciousDouble z2) {
        return new ZeroConsciousDouble(a + z2.a, 0);
    }

    public ZeroConsciousDouble multiply(ZeroConsciousDouble z2) {
        return new ZeroConsciousDouble(a * z2.a, 0);
    }

    public ZeroConsciousDouble divide(ZeroConsciousDouble z2) {
        return new ZeroConsciousDouble(a / z2.a, 0);
    }

    public static ZeroConsciousDouble[] fromDoubleArray(double[] da) {
        ZeroConsciousDouble[] result = new ZeroConsciousDouble[da.length];
        for (int i = 0; i < da.length; i++) {
            result[i] = new ZeroConsciousDouble(da[i]);
        }
        return result;
    }

    public static double[] toDoubleArray(ZeroConsciousDouble[] za) {
        double[] result = new double[za.length];
        for (int i = 0; i < za.length; i++) {
            result[i] = za[i].toDouble();
        }
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(this.toDouble());
    }
}
