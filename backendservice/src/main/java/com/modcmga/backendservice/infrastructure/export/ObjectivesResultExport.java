package com.modcmga.backendservice.infrastructure.export;
/**
 * @Package: com.modcmga.backendservice.infrastructure.export
 * @Class: ObjectivesResultExport
 * @Author: Jan
 * @Date: 23.01.2022
 */

import com.modcmga.backendservice.domain.objective.Objective;
import com.modcmga.backendservice.model.export.ParetoOptimalSolution;
import com.modcmga.backendservice.model.parameter.GraphExportParameter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exports for each Pareto optimal solution the values into a file
 */
@Setter
@Component
public class ObjectivesResultExport {
    private List<Objective> objectives;

    public File exportObjectiveFile(ParetoOptimalSolution paretoOptimalSolution,
                                    GraphExportParameter exportParameter) {;
        if (paretoOptimalSolution.getFitnessValues() != null &&
                paretoOptimalSolution.getNormalisedFitnessValues() != null)
            return exportMultiObjectiveFile(
                    paretoOptimalSolution.getFitnessValues(),
                    paretoOptimalSolution.getNormalisedFitnessValues(),
                    exportParameter.getFolderName());

        return exportSingleObjectiveFile(paretoOptimalSolution.getFitnessValue(), exportParameter.getFolderName());
    }

    /**
     * Creates the file for the multi objective values including the normalised objective values.
     * @param multiObjectiveFitnessValues the multi objective fitness value
     * @param normalisedMultiObjectiveFitnessValues the normalised multi objective fitness value
     * @param folderName the folder name where the file is added to
     * @return the file for the multi objective values including the normalised objective values.
     */
    public File exportMultiObjectiveFile(final double[] multiObjectiveFitnessValues,
                                         final double[] normalisedMultiObjectiveFitnessValues,
                                         final String folderName) {
        final var fileName = String.format("%s/multiObjectiveFitnessValues.csv",
                folderName);
        final var file = new File(fileName);

        try (final var fileWriter = new FileWriter(file);
             final var bufferedWriter = new BufferedWriter(fileWriter)) {

            // Write for each the objective values for all results
            for (int i = 0; i < objectives.size(); i++) {
                final var objective = objectives.get(i);
                final var line = String.format("%s; %.3f",
                        objective.objectiveText(),
                        multiObjectiveFitnessValues[i]);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            // Write for each the normalised objective values for all results
            for (int i = 0; i < objectives.size(); i++) {
                final var objective = objectives.get(i);
                final var line = String.format("Normalised %s; %.3f",
                        objective.objectiveText(),
                        normalisedMultiObjectiveFitnessValues[i]);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            // Write for each line the objective values for all results
        } catch (IOException e) {
            // TODO: handle write error
            e.printStackTrace();
        }

        return file;
    }

    /**
     * Creates the multi objective file.
     * @param multiObjectiveFitnessValues the multi objective fitness values.
     * @return the multi objective file.
     */
    public File exportMultiObjectiveFile(final double[] multiObjectiveFitnessValues) {
        final var file = new File("multiObjectiveFitnessValues.csv");

        try (final var fileWriter = new FileWriter(file);
             final var bufferedWriter = new BufferedWriter(fileWriter)) {

            // Write for each the objective values for all results
            for (int i = 0; i < objectives.size(); i++) {
                final var objective = objectives.get(i);
                final var line = String.format("%s; %.3f",
                        objective.objectiveText(),
                        multiObjectiveFitnessValues[i]);
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            // Write for each line the objective values for all results
        } catch (IOException e) {
            // TODO: handle write error
            e.printStackTrace();
        }

        return file;
    }

    /**
     * Creates the file for the single objective value.
     * @param singleObjectiveFitnessValue the single objective fitness value
     * @param folderName the folder where the file is created
     * @return the file for the single objective value.
     */
    public File exportSingleObjectiveFile(final double singleObjectiveFitnessValue,
                                         final String folderName) {
        final var fileName = String.format("%s/singleObjectiveFitnessValue.csv",
                folderName);
        final var file = new File(fileName);

        try (var fileWriter = new FileWriter(file);
             final var bufferedWriter = new BufferedWriter(fileWriter)) {
            final var line = String.format("Weighted sum objective; %.3f",
                    singleObjectiveFitnessValue);
            bufferedWriter.write(line);
            bufferedWriter.newLine();

        } catch (IOException e) {
            // TODO: handle write error
            e.printStackTrace();
        }

        return file;
    }
}
