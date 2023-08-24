package com.modcmga.backendservice.application;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: ServiceFacadeImplementation
 * @Author: Jan
 * @Date: 06.02.2022
 */

import com.modcmga.backendservice.domain.evaluation.EvaluationService;
import com.modcmga.backendservice.domain.geneticalgorithm.Constants;
import com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.FitnessFunction;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.objective.AverageCohesionObjective;
import com.modcmga.backendservice.domain.objective.CouplingObjective;
import com.modcmga.backendservice.domain.objective.ObjectiveSetup;
import com.modcmga.backendservice.infrastructure.ExportService;
import com.modcmga.backendservice.infrastructure.dataaccess.CM2KGDataAccess;
import com.modcmga.backendservice.infrastructure.transform.ModulERTransformer;
import com.modcmga.backendservice.model.evaluation.EvaluationParameter;
import com.modcmga.backendservice.model.evaluation.ModulErEvaluationResult;
import com.modcmga.backendservice.model.evaluation.LouvainModularisationSolution;
import com.modcmga.backendservice.model.evaluation.ModularisationSolution;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import com.modcmga.backendservice.service.EdgeWeightService;
import com.modcmga.backendservice.service.ModularisationService;
import com.modcmga.backendservice.util.CalculationUtil;
import com.modcmga.backendservice.util.GraphMLParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 */
@Service
public class ServiceFacadeImpl implements ServiceFacade {
    private final static String META_MODEL_FILE_KEY = "metaModelFile";
    private final GraphMLParser graphMLParser;
    private final CM2KGDataAccess cm2KGDataAccess;
    private final ModularisationService modularisationService;
    private final ExportService exportService;
    private final EdgeWeightService edgeWeightService;
    private final EvaluationService evaluationService;
    private final ModulERTransformer modulERTransformer;

    @Autowired
    public ServiceFacadeImpl(
            final GraphMLParser graphMLParser,
            final CM2KGDataAccess cm2KGDataAccess,
            final ModularisationService modularisationService,
            final ExportService exportService,
            final EdgeWeightService edgeWeightService,
            final EvaluationService evaluationService,
            final ModulERTransformer modulERTransformer) {
        this.graphMLParser = graphMLParser;
        this.cm2KGDataAccess = cm2KGDataAccess;
        this.modularisationService = modularisationService;
        this.exportService = exportService;
        this.edgeWeightService = edgeWeightService;
        this.evaluationService = evaluationService;
        this.modulERTransformer = modulERTransformer;
    }

    @Override
    public File modulariseGraphML(final File graphMLFile, final ModularisationParameter modularisationParameter)
            throws IOException {
        return modulariseKnowledgeGraphFromGraphML(graphMLFile, modularisationParameter);
    }

    @Override
    public File modulariseConceptualModel(
            Map<String, MultipartFile> multipartFiles,
            ModularisationParameter modularisationParameter) throws IOException {

        final var files = new LinkedHashMap<String, File>();
        multipartFiles.entrySet().stream()
                .forEach(inputFileEntry -> {
                    try {
                        final var file = new File(inputFileEntry.getValue().getOriginalFilename());
                        FileUtils.writeByteArrayToFile(file, inputFileEntry.getValue().getBytes());

                        files.put(inputFileEntry.getKey(), file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

        final var conceptualModelMultipartFile = multipartFiles.get(META_MODEL_FILE_KEY);
        final var conceptualModelFile = new File(conceptualModelMultipartFile.getOriginalFilename());
        FileUtils.writeByteArrayToFile(conceptualModelFile, conceptualModelMultipartFile.getBytes());

        final var metaModelType = modularisationParameter.getConceptualModelData().getMetaModelType();

        var graphMLFile = cm2KGDataAccess.transformToGraphMLFile(
                files, metaModelType);

        conceptualModelFile.delete();

        files.values().stream()
                .forEach(file -> {
                    file.delete();
                });

        return modulariseKnowledgeGraphFromGraphML(graphMLFile, modularisationParameter);
    }
    private File modulariseKnowledgeGraphFromGraphML(final File graphMLFile,
                                                     final ModularisationParameter modularisationParameter)
            throws IOException {
        final var knowledgeGraph = graphMLParser.parseGraphMLFile(graphMLFile);

        return modulariseKnowledgeGraph(graphMLFile, knowledgeGraph, modularisationParameter);
    }

    private File modulariseKnowledgeGraph(final File graphMLFile,
                                          final KnowledgeGraph knowledgeGraph,
                                          final ModularisationParameter modularisationParameter) throws IOException {
        System.out.println("Start modularisation process");

        setDefaultObjectiveWeight(modularisationParameter);
        prepareObjectives(modularisationParameter, knowledgeGraph);

        if (modularisationParameter.getConceptualModelData().getEdgeWeights() != null) {
            edgeWeightService.assignEdgeWeight(
                    knowledgeGraph,
                    modularisationParameter.getConceptualModelData().getEdgeWeights());
        }

        var executionResult =
                modularisationService.modulariseKnowledgeGraph(
                        knowledgeGraph,
                        modularisationParameter);

        var geneticAlgorithmResultsFile = exportService.createModularisationResultFile(
                executionResult,
                modularisationParameter,
                graphMLFile,
                knowledgeGraph);

        return geneticAlgorithmResultsFile;
    }

    @Override
    public File evaluateModularisationResult(final File knowledgeGraphGraphMlFile,
                                             final ModularisationSolution modularisationSolution,
                                             final EvaluationParameter evaluationParameter) throws IOException {
        final var knowledgeGraph = graphMLParser.parseGraphMLFile(knowledgeGraphGraphMlFile);

        prepareObjectives(evaluationParameter, knowledgeGraph);

        final var evaluationResult = evaluationService.evaluateModularisation(
                modularisationSolution, knowledgeGraph,evaluationParameter);

        evaluationResult.setName(FilenameUtils.removeExtension(knowledgeGraphGraphMlFile.getName()));

        return exportService.createEvaluationResultFile(
                evaluationResult, knowledgeGraph, evaluationParameter.getObjectiveSetup().getObjectives());
    }


    @Override
    public File evaluateLouvainModularisation(final File knowledgeGraphGraphMlFile,
                                              final LouvainModularisationSolution louvainModularisationSolution,
                                              final EvaluationParameter evaluationParameter) throws IOException {
        final var knowledgeGraph = graphMLParser.parseGraphMLFile(knowledgeGraphGraphMlFile);

        prepareObjectives(evaluationParameter, knowledgeGraph);

        final var evaluationResult = evaluationService.evaluateLouvain(
                louvainModularisationSolution, knowledgeGraph,evaluationParameter);

        evaluationResult.setName(FilenameUtils.removeExtension(knowledgeGraphGraphMlFile.getName()));

        return exportService.createEvaluationResultFile(
                evaluationResult, knowledgeGraph, evaluationParameter.getObjectiveSetup().getObjectives());
    }

    @Override
    public File evaluateModulERFiles(
            final File monolithFile, final List<File> modulERFiles, final EvaluationParameter evaluationParameter)
            throws IOException {

        final var linearLinkageEncodings = modulERFiles.stream()
                .map(modulERFile -> modulERTransformer.transform(modulERFile))
                .collect(Collectors.toList());

        final var knowledgeGraph = linearLinkageEncodings.get(0).getKnowledgeGraph();
        prepareObjectives(evaluationParameter, knowledgeGraph);

        final var objectives = evaluationParameter.getObjectiveSetup().getObjectives();
        final var fitnessFunction = new FitnessFunction(objectives, knowledgeGraph);

        final var monolithModulERName = "monolith";
        final var monolithLinearLinkageEncoding = modulERTransformer.transform(monolithFile);
        final var monolithMultiObjectiveFitnessValue =
                fitnessFunction.calculateMultiObjectiveFitnessValue(monolithLinearLinkageEncoding).data();
        final var monolithEvaluation = new ModulErEvaluationResult(
                monolithModulERName, monolithLinearLinkageEncoding.getModules(), monolithMultiObjectiveFitnessValue, monolithMultiObjectiveFitnessValue);

        // normalised fitness values
        final var fitnessValues = linearLinkageEncodings.stream()
                .map(lle -> fitnessFunction.calculateMultiObjectiveFitnessValue(lle).data())
                .collect(Collectors.toList());
        final var normalisedFitnessValues = CalculationUtil.normalise(fitnessValues);

        final var evaluationExecutionResults = IntStream.range(0, linearLinkageEncodings.size())
                .boxed()
                .map(i -> {
                    final var modulERName = modulERFiles.get(i).getName();
                    final var lle = linearLinkageEncodings.get(i);
                    return new ModulErEvaluationResult(
                            modulERName, lle.getModules(), fitnessValues.get(i), normalisedFitnessValues.get(i));
                })
                .collect(Collectors.toList());

        return exportService.createEvaluationResultFile(
                monolithEvaluation,evaluationExecutionResults, knowledgeGraph, objectives);
    }

    private void setDefaultObjectiveWeight(ModularisationParameter modularisationParameter) {
        if (modularisationParameter.getObjectiveSetup() == null) {
            final var defaultObjectives = Stream.of(
                            new CouplingObjective(),
                            new AverageCohesionObjective())
                    .map(objective -> {
                        if (objective.getWeight() == 0)
                            // Default edge value
                            objective.setWeight(1);

                        if (objective.isNumberOfElementsNeeded() &&
                                modularisationParameter.getObjectiveSetup() != null &&
                                modularisationParameter.getObjectiveSetup().getNumberOfElementsPerModule() == 0)
                            objective.setWeight(Constants.COUNT_OPTIMAL_NUMBER_OF_MODULARISABLE_ELEMENTS_PER_MODULE);
                        return objective;
                    })
                    .collect(Collectors.toList());

            final var defaultObjectiveData = new ObjectiveSetup();
            defaultObjectiveData.setObjectives(defaultObjectives);
            defaultObjectiveData.setUseWeightedSumMethod(false);
            modularisationParameter.setObjectiveSetup(defaultObjectiveData);
        }
    }

    private void prepareObjectives(final ModularisationParameter modularisationParameter,
                                   final KnowledgeGraph knowledgeGraph) {
        if (modularisationParameter.getObjectiveSetup() != null)
            modularisationParameter.getObjectiveSetup().getObjectives()
                    .stream()
                    .forEach(objective -> {
                        objective.setKnowledgeGraph(knowledgeGraph);
                        objective.prepare();
                    });
    }

    private void prepareObjectives(final EvaluationParameter evaluationParameter,
                                   final KnowledgeGraph knowledgeGraph) {
        if (evaluationParameter.getObjectiveSetup() != null)
            evaluationParameter.getObjectiveSetup().getObjectives()
                    .stream()
                    .forEach(objective -> objective.setKnowledgeGraph(knowledgeGraph));
    }
}
