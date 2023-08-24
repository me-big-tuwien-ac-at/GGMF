package com.modcmga.backendservice.domain.evaluation;

import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.evaluation.EvaluationParameter;
import com.modcmga.backendservice.model.evaluation.LouvainModularisationSolution;
import com.modcmga.backendservice.model.evaluation.ModularisationEvaluationResult;
import com.modcmga.backendservice.model.evaluation.ModularisationSolution;

/**
 * Provides functionalities for evaluating modularisations.
 */
public interface EvaluationService {
    /**
     * Evaluates {@code modularisationSolution} according to the objectives in {@code evaluationParameter} and
     * returns the evaluation information.
     * @param modularisationSolution the modularisation solution to be evaluated.
     * @param knowledgeGraph the knowledge graph providing the structural information.
     * @param evaluationParameter the parameter of the evaluation.
     * @return the evaluation information.
     */
    ModularisationEvaluationResult evaluateModularisation(final ModularisationSolution modularisationSolution,
                                                   final KnowledgeGraph knowledgeGraph,
                                                   final EvaluationParameter evaluationParameter);

    /**
     * Evaluates {@code louvainModularisationSolution} according to the objectives in {@code evaluationParameter} and
     * returns the evaluation information.
     * @param louvainModularisationSolution the Louvain modularisation solution to be evaluated.
     * @param knowledgeGraph the knowledge graph providing the structural information.
     * @param evaluationParameter the parameter of the evaluation.
     * @return the evaluation information.
     */
    ModularisationEvaluationResult evaluateLouvain(final LouvainModularisationSolution louvainModularisationSolution,
                                                   final KnowledgeGraph knowledgeGraph,
                                                   final EvaluationParameter evaluationParameter);
}
