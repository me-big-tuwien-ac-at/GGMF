package com.modcmga.backendservice.infrastructure.export;
/**
 * @Package: com.modcmga.backendservice.infrastructure.export
 * @Class: ObjectivesResultExport
 * @Author: Jan
 * @Date: 23.01.2022
 */

import com.modcmga.backendservice.domain.objective.Objective;
import com.modcmga.backendservice.domain.objective.common.ObjectiveUtil;
import com.modcmga.backendservice.model.export.ParetoOptimalSolution;
import com.modcmga.backendservice.model.parameter.GraphExportParameter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exports a summary for all objectives for each pareto optimal solution.
 */
@Component
public class ObjectivesResultSummaryCSVExport {

    private static final int COUNT_ROUND_DECIMAL_PLACES = 3;

    /**
     * Creates a CSV file containing all objective values for each parameter in a table format. Each column represents
     * a pareto optimal solution and each row represents the respective objective values.
     * @param paretoOptimalSolutions the list of pareto optimal solutions
     * @param exportParameter the export parameter
     * @param objectives the list of objectives
     * @return a CSV file containing all objective values for each parameter in a table format. Each column represents
     * a pareto optimal solution and each row represents the respective objective values.
     */
    public File exportMultiObjectiveFile(final List<ParetoOptimalSolution> paretoOptimalSolutions,
                                         final GraphExportParameter exportParameter,
                                         final List<Objective> objectives) {
        final var fileName = String.format("objectiveValuesSummary.csv",
                exportParameter.getFolderName());
        final var file = new File(fileName);

        // Order pareto optimal solutions
        Collections.sort(paretoOptimalSolutions, Comparator.comparingDouble(ParetoOptimalSolution::getRanking));

        try (final var fileWriter = new FileWriter(file);
             final var bufferedWriter = new BufferedWriter(fileWriter)) {

            // Create header
            var headerLine = "Objectives;";

            for (int i = 0; i < paretoOptimalSolutions.size(); i++) {
                headerLine += String.format("Pareto optimal solution %d;", i);
            }

            bufferedWriter.write(headerLine);
            bufferedWriter.newLine();

            // Accumulate for each objective of the fitness function the values from the pareto set
            for (int i = 0; i < objectives.size(); i++) {
                final var objectiveValues = new ArrayList<>();

                final var objectiveText = objectives.get(i).objectiveText();
                var objectiveSummaryLine = objectiveText + ";";
                for (final var paretoOptimalSolution : paretoOptimalSolutions) {
                    if (paretoOptimalSolution.getFitnessValues() != null) {
                        final var fitnessValues = paretoOptimalSolution.getFitnessValues();

                        objectiveValues.add(roundValue(fitnessValues[i]));
                    } else if (paretoOptimalSolution.getFitnessValue() != null) {
                        objectiveValues.add(roundValue(paretoOptimalSolution.getFitnessValue()));
                    }
                }

                // Write objective values
                objectiveSummaryLine += objectiveValues.stream()
                        .map(value -> value.toString())
                        .collect(Collectors.joining(";"));

                bufferedWriter.write(objectiveSummaryLine);
                bufferedWriter.newLine();
            }

            // Accumulate for each objective of the fitness function the values from the pareto set
            for (int i = 0; i < objectives.size(); i++) {
                final var objectiveValues = new ArrayList<>();
                final var normalisedObjectiveValues = new ArrayList<>();

                final var objectiveText = objectives.get(i).objectiveText();
                var normalisedObjectiveSummaryLine = String.format("Normalised %s;", objectiveText);
                for (final var paretoOptimalSolution : paretoOptimalSolutions) {
                    if (paretoOptimalSolution.getFitnessValues() != null) {
                        final var normalisedFitnessValues = paretoOptimalSolution.getNormalisedFitnessValues();

                        normalisedObjectiveValues.add(normalisedFitnessValues[i]);
                    } else if (paretoOptimalSolution.getFitnessValue() != null) {
                        objectiveValues.add(roundValue(paretoOptimalSolution.getFitnessValue()));
                    }
                }

                // Write normalised objective values
                normalisedObjectiveSummaryLine += normalisedObjectiveValues.stream()
                        .map(value -> value.toString())
                        .collect(Collectors.joining(";"));

                bufferedWriter.write(normalisedObjectiveSummaryLine);
                bufferedWriter.newLine();
            }

            // Create last line for calculated single objective based on all objectives
            var singleObjectiveSummaryLine = "Single objective (Sum of normalised objective as minimisation problem);";
            final var singleObjectiveValues = new ArrayList<>();
            for (final var paretoOptimalSolution : paretoOptimalSolutions) {
                final var singleObjectiveValue = ObjectiveUtil.calculateSingleObjectiveValue(
                        objectives, paretoOptimalSolution.getNormalisedFitnessValues());
                singleObjectiveValues.add(singleObjectiveValue);
            }
            singleObjectiveSummaryLine += singleObjectiveValues.stream()
                    .map(value -> value.toString())
                    .collect(Collectors.joining(";"));

            bufferedWriter.write(singleObjectiveSummaryLine);
            bufferedWriter.newLine();

        } catch (IOException e) {
            // TODO: handle write error
            e.printStackTrace();
        }

        return file;
    }

    /**
     * Creates a CSV file containing the best single objective value for all pareto-optimal solutions with the same
     * value.
     * @param bestSingleObjectiveValue the best single objective vlaue
     * @param exportParameter the export parameter
     * @return a CSV file containing the best single objective value for all pareto-optimal solutions with the same
     * value.
     */
    public File exportSingleObjectiveFile(final double bestSingleObjectiveValue,
                                          final GraphExportParameter exportParameter) {
        final var fileName = String.format("singleObjectiveSummary.csv",
                exportParameter.getFolderName());
        final var file = new File(fileName);

        try (final var fileWriter = new FileWriter(file);
             final var bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write("Best single-objective value: " + roundValue(bestSingleObjectiveValue));

        } catch (IOException e) {
            // TODO: handle write error
            e.printStackTrace();
        }

        return file;
    }

    private static double roundValue(final double value) {
        return Math.round(value * 100) / 100;
    }
}
