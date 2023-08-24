package com.modcmga.backendservice.model.export;
/**
 * @Package: com.modcmga.backendservice.model.export
 * @Class: ResultExecutionExport
 * @Author: Jan
 * @Date: 04.12.2021
 */

import lombok.Data;

import java.util.Set;

/**
 * This class consists of all export relevant information after the execution
 * of the genetic algorithm information. It mostly consists of the
 * modularization result and the execution relevant information.
 */
@Data
public class GeneticAlgorithmExecutionResult {

    /**
     * The set of pareto optimal solutions which are the list of modules. This is only populated and relevant, when
     * {@link com.modcmga.backendservice.domain.objective.ObjectiveSetup#isUseWeightedSumMethod()} is false.
     */
    private Set<ParetoOptimalSolution> paretoSet;

    /**
     * The genetic algorithm result relevant information.
     */
    private GeneticAlgorithmResults geneticAlgorithmResults;
}
