package com.modcmga.backendservice.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provide utility methods for calculation.
 */
public final class CalculationUtil {
    /**
     * Returns the normalised values along the horizontal values.
     * @param values the matrix values that need to be normalised.
     * @return the normalised values.
     */
    public static List<double[]> normalise(final List<double[]> values) {
        var verticalDimension = values.get(0).length;

        final var rows = new ArrayList<List<Double>>();

        for (int i = 0; i < verticalDimension; i++) {
            var row = new ArrayList<Double>();

            for (var column : values) {
                row.add(column[i]);
            }

            rows.add(row);
        }

        final var columns = new ArrayList<List<Double>>();

        final var normalisedRows = rows.stream()
                .map(vector -> normaliseVector(vector).stream().mapToDouble(v -> v).toArray())
                .collect(Collectors.toList());

        final var normalisedValues = new ArrayList<double[]>();
        final var horizontalDimension = rows.get(0).size();

        for (int x = 0; x < horizontalDimension; x++) {
            final var vector = new double[verticalDimension];
            for (int y = 0; y < verticalDimension; y++) {
                vector[y] = normalisedRows.get(y)[x];
            }
            normalisedValues.add(vector);
        }

        return normalisedValues;
    }

    private static List<Double> normaliseVector(final List<Double> values) {
        final var min = values.stream()
                .mapToDouble(v -> v)
                .min()
                .getAsDouble();
        final var max = values.stream()
                .mapToDouble(v -> v)
                .max()
                .getAsDouble();

        if (max == min)
            return values.stream()
                    .map(v -> 0d)
                    .collect(Collectors.toList());

        return values.stream()
                .map(v -> (v - min) / (max - min))
                .collect(Collectors.toList());
    }

    /**
     * Determines the ranking for each value where the key is the value and the value the integer ranking.
     * @param values the values to be ranked
     * @return the ranking of the values.
     */
    public static List<Integer> determineRanking(final List<Double> values) {
        final var valuesCopy = new ArrayList<>(values);

        // Sort values in ascending order
        final var valuesSorted = new ArrayList<>(valuesCopy);
        Collections.sort(valuesSorted);

        return values.stream()
                .map(v -> valuesSorted.indexOf(v))
                .collect(Collectors.toList());
    }
}
