package com.modcmga.backendservice.service;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: ModularisationService
 * @Author: Jan
 * @Date: 11.02.2022
 */

import com.modcmga.backendservice.model.export.GeneticAlgorithmExecutionResult;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;

/**
 * Provides the modularisation functionalities of the knowledge graph.
 */
public interface ModularisationService {

    /**
     * Modularises the knowledge graph and returns the execution results.
     * @param knowledgeGraph the knowledge graph for modularisation
     * @param modularisationParameter the application parameter
     * @return the modularisation execution results
     */
    GeneticAlgorithmExecutionResult modulariseKnowledgeGraph(
            KnowledgeGraph knowledgeGraph,
            ModularisationParameter modularisationParameter);
}
