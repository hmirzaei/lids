package org.uci.lids.utils;

/**
 * Created by hamid on 9/25/15.
 */
public class ZeroConsciousDouble {
    private double a;
    private boolean i;

    public ZeroConsciousDouble(double value) {
        if (value == 0) {
            a = 1;
            i = true;
        } else {
            a = value;
            i = false;
        }
    }

    public ZeroConsciousDouble(double a, boolean i) {
        this.a = a;
        this.i = i;
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

    public double toDouble() {
        if (i)
            return 0;
        else
            return a;
    }

    public ZeroConsciousDouble add(ZeroConsciousDouble z2) {
        if (!i && z2.i)
            return new ZeroConsciousDouble(a, false);
        else if (i && !z2.i)
            return new ZeroConsciousDouble(z2.a, false);
        else
            return new ZeroConsciousDouble(a + z2.a, i);
    }

    public ZeroConsciousDouble multiply(ZeroConsciousDouble z2) {
        if (!i && z2.toDouble() == 0)
            return new ZeroConsciousDouble(a, true);
        else
            return new ZeroConsciousDouble(z2.toDouble() * a, i);
    }

    public ZeroConsciousDouble divide(ZeroConsciousDouble z2) {
        if (!i)
            return new ZeroConsciousDouble(a / z2.toDouble(), i);
        else if (i && z2.toDouble() != 0)
            return new ZeroConsciousDouble(1, true);
        else
            return new ZeroConsciousDouble(a, false);
    }

    @Override
    public String toString() {
        return String.valueOf(this.toDouble());
    }
}
