package com.modcmga.backendservice.domain.geneticalgorithm.engine;
/**
 * @Package: com.modcmga.backendservice
 * @Class: GeneticAlgorithmEngine
 * @Author: Jan
 * @Date: 04.12.2021
 */

import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.export.GeneticAlgorithmExecutionResult;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;

/**
 * Executes the genetic algorithm based on the passed parameter and the given
 * knowledge graph. The result of the execution is a modularized
 */
public interface GeneticAlgorithmEngine {

    /**
     * Executes the Genetic Algorithm on the passed Knowledge Graph with the
     * passed parameters and returns a Knowledge Graph with assigned modules
     * to each vertex or edge and assigns boundaries to edges.
     * @param knowledgeGraph The Knowledge Graph.
     * @param parameter The parameters for the application.
     * @param objectives The list of objectives
     * @return The execution result after the execution.
     */
    GeneticAlgorithmExecutionResult run(KnowledgeGraph knowledgeGraph,
                                        ModularisationParameter parameter);
}
