package com.modcmga.backendservice.service;
/**
 * @Package: com.modcmga.poc.service
 * @Class: PoCService
 * @Author: Jan
 * @Date: 17.10.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.engine.GeneticAlgorithmEngine;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.export.GeneticAlgorithmExecutionResult;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
public class ModularisationServiceImpl implements ModularisationService {

    @Autowired
    private GeneticAlgorithmEngine mainGeneticAlgorithmEngine;

    /**
     * Modularises the knowledge graph.
     * @param knowledgeGraph the knowledge graph
     * @param modularisationParameter the parameter for the modularisation
     * @return the compressed file containing the modularisation result
     */
    public GeneticAlgorithmExecutionResult modulariseKnowledgeGraph(
            final KnowledgeGraph knowledgeGraph, final ModularisationParameter modularisationParameter) {
        return mainGeneticAlgorithmEngine.run(
                knowledgeGraph,
                modularisationParameter);
    }
}
