package com.modcmga.backendservice.infrastructure.export;
/**
 * @Package: com.modcmga.backendservice.infrastructure.export
 * @Class: GeneticAlgorithmResultExport
 * @Author: Jan
 * @Date: 16.01.2022
 */

import com.modcmga.backendservice.model.export.GeneticAlgorithmResults;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Creates a CSV file with all the genetic algorithm result relevant information.
 */
@Component
public class GeneticAlgorithmResultExport {

    /**
     * Creates the CSV file containing the result of the genetic algorithm
     * application.
     * @param geneticAlgorithmResults The genetic algorithm results
     * @return the file containing the result of the genetic algorithm
     * application.
     */
    public File createGeneticAlgorithmResultFile(
            GeneticAlgorithmResults geneticAlgorithmResults) {
        var fileName = "geneticAlgorithmResultExport.csv";
        var file = new File(fileName);

        try (var fileWriter = new FileWriter(file);
             var bufferedWriter = new BufferedWriter(fileWriter)) {

            var modularizationTimeText = getModularizationTimeInMillsecondsText(
                    geneticAlgorithmResults.getModularizationTimeInMillisecond());
            var modularizationLine = String.format(
                    "Modularization time; %s",
                    modularizationTimeText);
            bufferedWriter.write(modularizationLine);
            bufferedWriter.newLine();

            var paretoSetSizeLine = String.format("Pareto set size; %s",
                    geneticAlgorithmResults.getParetoSetSize());
            bufferedWriter.write(paretoSetSizeLine);
            bufferedWriter.newLine();

        } catch (IOException e) {
            // TODO: handle write error
        }

        return file;
    }

    private String getModularizationTimeInMillsecondsText(final long modularizationTimeInMillseconds) {
        return String.format("%d min:%d sec",
                TimeUnit.MILLISECONDS.toMinutes(modularizationTimeInMillseconds),
                TimeUnit.MILLISECONDS.toSeconds(modularizationTimeInMillseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(modularizationTimeInMillseconds))
        );
    }
}
