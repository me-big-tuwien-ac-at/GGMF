package com.modcmga.backendservice.infrastructure;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: ExportServiceImpl
 * @Author: Jan
 * @Date: 11.02.2022
 */

import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.objective.Objective;
import com.modcmga.backendservice.infrastructure.export.*;
import com.modcmga.backendservice.model.evaluation.ModulErEvaluationResult;
import com.modcmga.backendservice.model.evaluation.ModularisationEvaluationResult;
import com.modcmga.backendservice.model.export.ExportFile;
import com.modcmga.backendservice.model.export.GeneticAlgorithmExecutionResult;
import com.modcmga.backendservice.model.export.ParetoOptimalSolution;
import com.modcmga.backendservice.model.parameter.GraphExportParameter;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 */
@Service
public class ExportServiceImpl implements ExportService {
    private final GeneticAlgorithmResultExport geneticAlgorithmResultExport;
    private final KnowledgeGraphExport graphMLKnowledgeGraphExport;
    private final KnowledgeGraphExport csvKnowledgeGraphExport;
    private final ParameterExport parameterExport;
    private final ObjectivesResultExport objectivesResultExport;
    private final ObjectivesResultSummaryCSVExport objectivesResultSummaryCSVExport;

    @Autowired
    public ExportServiceImpl(GeneticAlgorithmResultExport geneticAlgorithmResultExport,
                             @Qualifier("GraphMLKnowledgeGraphExport") KnowledgeGraphExport graphMLKnowledgeGraphExport,
                             @Qualifier("CsvKnowledgeGraphExport") KnowledgeGraphExport csvKnowledgeGraphExport,
                             ParameterExport parameterExport,
                             ObjectivesResultExport objectivesResultExport,
                             ObjectivesResultSummaryCSVExport objectivesResultSummaryCSVExport) {
        this.geneticAlgorithmResultExport = geneticAlgorithmResultExport;
        this.graphMLKnowledgeGraphExport = graphMLKnowledgeGraphExport;
        this.csvKnowledgeGraphExport = csvKnowledgeGraphExport;
        this.parameterExport = parameterExport;
        this.objectivesResultExport = objectivesResultExport;
        this.objectivesResultSummaryCSVExport = objectivesResultSummaryCSVExport;
    }

    @Override
    public File createModularisationResultFile(final GeneticAlgorithmExecutionResult resultExecutionExport,
                                               final ModularisationParameter modularisationParameter,
                                               final File graphMLFile,
                                               final KnowledgeGraph knowledgeGraph) throws IOException {
        final var graphExportParameter = new GraphExportParameter();

        final var objectives = modularisationParameter.getObjectiveSetup().getObjectives();
        this.objectivesResultExport.setObjectives(objectives);

        final var exportFiles = new ArrayList<ExportFile>();

        exportFiles.add(new ExportFile(graphMLFile, false, null));

        addParetoOptimalSolutionsToExportFile(
                exportFiles,
                resultExecutionExport.getParetoSet(),
                knowledgeGraph,
                graphExportParameter);

        final var paretoSetList = new ArrayList<>(resultExecutionExport.getParetoSet());

        if (modularisationParameter.getObjectiveSetup() != null &&
                modularisationParameter.getObjectiveSetup().isUseWeightedSumMethod()) {
            // All Pareto optimal solutions in single-objective have the same best fitness
            final var firstParetoOptimalSolution = resultExecutionExport.getParetoSet()
                    .stream()
                    .findFirst()
                    .get();
            final var bestSingleObjectiveValue = firstParetoOptimalSolution.getFitnessValue();
            final var singleObjectiveSummaryFile =
                    objectivesResultSummaryCSVExport.exportSingleObjectiveFile(bestSingleObjectiveValue, graphExportParameter);
            exportFiles.add(new ExportFile(singleObjectiveSummaryFile, false, null));
        } else {
            final var objectivesResultSummaryExportCSVFile = objectivesResultSummaryCSVExport.exportMultiObjectiveFile(
                    paretoSetList, graphExportParameter, objectives);

            exportFiles.add(new ExportFile(objectivesResultSummaryExportCSVFile, false, null));
        }

        final var parameterExportFile = parameterExport.createParameterFile(modularisationParameter);
        exportFiles.add(new ExportFile(parameterExportFile, false, null));

        final var geneticAlgorithmResultExportFile =
                geneticAlgorithmResultExport.createGeneticAlgorithmResultFile(
                        resultExecutionExport.getGeneticAlgorithmResults());
        exportFiles.add(new ExportFile(geneticAlgorithmResultExportFile, false, null));

        return createZipFileFromResults(exportFiles);
    }

    private void addParetoOptimalSolutionsToExportFile(
            final List<ExportFile> exportFiles,
            final Set<ParetoOptimalSolution> paretoSet,
            final KnowledgeGraph knowledgeGraph,
            final GraphExportParameter graphExportParameter) {
        final var orderedParetoList = new ArrayList<>(paretoSet);
        Collections.sort(orderedParetoList, Comparator.comparingDouble(ParetoOptimalSolution::getRanking));

        int i = 0;
        for (final var paretoOptimalSolution : orderedParetoList) {
            final var folderName = String.format("Pareto solution %d", i);

            graphExportParameter.setFolderName(folderName);
            graphExportParameter.setFileName(UUID.randomUUID().toString());

            final var directoryFile = new File(String.format("%s/", folderName));
            directoryFile.mkdirs();

            final var modularizedKnowledgeGraphFile = graphMLKnowledgeGraphExport.exportAsGraphML(
                    knowledgeGraph, paretoOptimalSolution.getModules(), graphExportParameter);

            final var csvKnowledgeGraphFile = csvKnowledgeGraphExport.exportAsGraphML(
                    knowledgeGraph, paretoOptimalSolution.getModules(), graphExportParameter);

            final var objectiveResultsFile = objectivesResultExport.exportObjectiveFile(
                    paretoOptimalSolution, graphExportParameter);

            exportFiles.add(new ExportFile(modularizedKnowledgeGraphFile, true, folderName));
            exportFiles.add(new ExportFile(csvKnowledgeGraphFile, true, folderName));
            exportFiles.add(new ExportFile(objectiveResultsFile, true, folderName));
            i++;
        }
    }

    private File createZipFileFromResults(final List<ExportFile> exportFiles) throws IOException {
        final var formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy hh.mm.ss", Locale.GERMAN);
        final var zipFileName = LocalDateTime.now().format(formatter);
        final var zipFile = new File(String.format("%s.zip",
                zipFileName));

        try (var fileOutputStream = new FileOutputStream(zipFile);
             final var zipOutputStream = new ZipOutputStream(fileOutputStream)) {

            for (var exportFile : exportFiles) {
                final var fileToExport = exportFile.getFile();
                try (var fileInputStream = new FileInputStream(fileToExport)) {
                    final var fileName = String.format("%s/%s",
                            exportFile.isNested() ? exportFile.getFolder() : "",
                            exportFile.getFile().getName());
                    final var zipEntry = new ZipEntry(fileName);
                    zipOutputStream.putNextEntry(zipEntry);

                    final byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fileInputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }
                }
                fileToExport.delete();

                if (exportFile.isNested()) {
                    final var directoryFile = new File(exportFile.getFolder());
                    directoryFile.delete();
                }
            }
        }
        return zipFile;
    }

    @Override
    public File createEvaluationResultFile(
            final ModularisationEvaluationResult modularisationEvaluationResult,
            final KnowledgeGraph knowledgeGraph,
            final List<Objective> objectives) throws IOException {
        final var fileName = String.format("%s_evaluation", modularisationEvaluationResult.getName());

        final var graphExportParameter = new GraphExportParameter();
        graphExportParameter.setFileName(fileName);

        final var exportFiles = new ArrayList<ExportFile>();

        // Create files
        this.objectivesResultExport.setObjectives(objectives);
        var objectiveResultsFile = objectivesResultExport.exportMultiObjectiveFile(
                modularisationEvaluationResult.getMultiObjectiveFitnessValue());

        var csvKnowledgeGraphFile = csvKnowledgeGraphExport.exportAsGraphML(
                knowledgeGraph, modularisationEvaluationResult.getModules(), graphExportParameter);

        // Add files to list for zip file
        exportFiles.add(new ExportFile(objectiveResultsFile, false, null));
        exportFiles.add(new ExportFile(csvKnowledgeGraphFile, false, null));

        // Create zip file
        return createZipFileFromResults(exportFiles);
    }

    @Override
    public File createEvaluationResultFile(final ModulErEvaluationResult monolithModulErEvaluationResult,
                                           final List<ModulErEvaluationResult> modulErEvaluationResults,
                                           final KnowledgeGraph knowledgeGraph,
                                           final List<Objective> objectives) throws IOException {
        final var exportFiles = new ArrayList<ExportFile>();
        final var moduleGraphExportParameter = new GraphExportParameter();

        moduleGraphExportParameter.setFileName("monolith");
        var monolithKnowledgeGraphFile = graphMLKnowledgeGraphExport.exportAsGraphML(
                knowledgeGraph, monolithModulErEvaluationResult.getModules(), moduleGraphExportParameter);
        exportFiles.add(new ExportFile(monolithKnowledgeGraphFile, false, ""));

        this.objectivesResultExport.setObjectives(objectives);

        for (int i = 0; i < modulErEvaluationResults.size(); i++) {
            final var evaluationResult = modulErEvaluationResults.get(i);
            var folderName = String.format("%d. %s", i, evaluationResult.getModulERName());

            moduleGraphExportParameter.setFolderName(folderName);
            moduleGraphExportParameter.setFileName(folderName);

            var directoryFile = new File(String.format("%s/", folderName));
            directoryFile.mkdirs();

            var modularisedKnowledgeGraphFile = graphMLKnowledgeGraphExport.exportAsGraphML(
                    knowledgeGraph, evaluationResult.getModules(), moduleGraphExportParameter);

            var csvKnowledgeGraphFile = csvKnowledgeGraphExport.exportAsGraphML(
                    knowledgeGraph, evaluationResult.getModules(), moduleGraphExportParameter);

            var objectiveResultsFile = objectivesResultExport.exportMultiObjectiveFile(
                    evaluationResult.getMultiObjectiveFitnessValue(),
                    evaluationResult.getNormalisedMultiObjectiveFitnessValue(),
                    moduleGraphExportParameter.getFolderName());

            exportFiles.add(new ExportFile(modularisedKnowledgeGraphFile, true, folderName));
            exportFiles.add(new ExportFile(csvKnowledgeGraphFile, true, folderName));
            exportFiles.add(new ExportFile(objectiveResultsFile, true, folderName));
        }

        return createZipFileFromResults(exportFiles);
    }
}
