package com.modcmga.backendservice.domain.objective.common;

import com.modcmga.backendservice.domain.objective.Objective;
import io.jenetics.Optimize;

import java.util.List;

/**
 * Provides utility functionalities related to objectives.
 */
public final class ObjectiveUtil {
    /**
     * Calculates a single objective value from the objectives. Maximisation problems are transformed
     * @param objectives the objectives to determine if the objectives are maximisation problem which have to
     *                   transformed to a minimisation problem.
     * @param objectiveValues the objective values.
     * @return a single objective value from the objectives.
     */
    public static double calculateSingleObjectiveValue(
            final List<Objective> objectives, final double[] objectiveValues) {
        var singleObjective = 0d;

        for (int i = 0; i < objectives.size(); i++) {
            double objectiveValue = objectiveValues[i];

            // Transform maximisation problem to minimisation problem
            if (objectives.get(i).getOptimize() == Optimize.MAXIMUM)
                objectiveValue *= -1;

            singleObjective += objectiveValue;
        }

        return singleObjective;
    }
}
