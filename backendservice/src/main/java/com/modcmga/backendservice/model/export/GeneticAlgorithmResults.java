package com.modcmga.backendservice.model.export;
/**
 * @Package: com.modcmga.backendservice.model.export
 * @Class: GeneticAlgorithmResults
 * @Author: Jan
 * @Date: 16.01.2022
 */

import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
public class GeneticAlgorithmResults {
    /**
     * The time it took to complete the whole modularisation.
     */
    private long modularizationTimeInMillisecond;

    /**
     * The actual size of the pareto set of the final result. This is only relevant used, when
     * {@link com.modcmga.backendservice.domain.objective.ObjectiveSetup#isUseWeightedSumMethod()} is false.
     */
    private int paretoSetSize;
}
