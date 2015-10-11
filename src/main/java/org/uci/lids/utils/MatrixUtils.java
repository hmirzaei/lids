package org.uci.lids.utils;

import org.ejml.simple.SimpleMatrix;

/**
 * Created by hamid on 10/4/15.
 */
public class MatrixUtils {
    public static SimpleMatrix subMatrixByColumns(SimpleMatrix m, int[] columns) {
        SimpleMatrix result = new SimpleMatrix(m.numRows(), columns.length);
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] != -1)
                result.insertIntoThis(0, i, m.extractVector(false, columns[i]));
        }
        return result;
    }

    public static SimpleMatrix subMatrixByRows(SimpleMatrix m, int[] rows) {
        SimpleMatrix result = new SimpleMatrix(rows.length, m.numCols());
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] != -1)
                result.insertIntoThis(i, 0, m.extractVector(true, rows[i]));
        }
        return result;
    }

    public static SimpleMatrix subMatrix(SimpleMatrix m, int[] rows, int[] columns) {
        SimpleMatrix result = new SimpleMatrix(rows.length, columns.length);
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < columns.length; j++) {
                result.set(i, j, m.get(rows[i], columns[j]));
            }
        }
        return result;
    }
}
