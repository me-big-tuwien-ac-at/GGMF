package com.modcmga.backendservice.infrastructure;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: ExportService
 * @Author: Jan
 * @Date: 11.02.2022
 */

import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.objective.Objective;
import com.modcmga.backendservice.model.evaluation.ModulErEvaluationResult;
import com.modcmga.backendservice.model.evaluation.ModularisationEvaluationResult;
import com.modcmga.backendservice.model.export.GeneticAlgorithmExecutionResult;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Provides the export functionalities to create the result file.
 */
public interface ExportService {
    /**
     * Creates the compressed file consisting of multiple files for the modularisation process.
     * @param resultExecutionExport the modularisation result
     * @param modularisationParameter the modularisation parameter
     * @param graphMLFile the graphML file for the knowledge graph
     * @param knowledgeGraph the knowledge graph
     * @return the compressed file consisting of multiple files
     * @throws IOException
     */
    File createModularisationResultFile(GeneticAlgorithmExecutionResult resultExecutionExport,
                                        ModularisationParameter modularisationParameter,
                                        File graphMLFile,
                                        KnowledgeGraph knowledgeGraph) throws IOException;

    /**
     * Creates the compressed file consisting of multiple files containing the evaluation relevant information
     * @param modularisationEvaluationResult the evaluation result
     * @param knowledgeGraph the knowledge graph
     * @param objectives the list of objectives
     * @return  the compressed file consisting of multiple files
     */
    File createEvaluationResultFile(final ModularisationEvaluationResult modularisationEvaluationResult,
                                    final KnowledgeGraph knowledgeGraph,
                                    final List<Objective> objectives) throws IOException;


    /**
     * Creates the compressed file consisting of multiple files containing the evaluation relevant information
     * @param monolithModulErEvaluationResult the evaluation result of the monolith
     * @param modulErEvaluationResults the list of evaluation results
     * @param knowledgeGraph the knowledge graph
     * @param objectives the list of objectives
     * @return  the compressed file consisting of multiple files
     */
    File createEvaluationResultFile(final ModulErEvaluationResult monolithModulErEvaluationResult,
                                    final List<ModulErEvaluationResult> modulErEvaluationResults,
                                    final KnowledgeGraph knowledgeGraph,
                                    final List<Objective> objectives) throws IOException;
}
