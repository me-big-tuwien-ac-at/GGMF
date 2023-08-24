package com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction
 * @Class: FitnessFunction2
 * @Author: Jan
 * @Date: 17.12.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.objective.Objective;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Optimize;
import io.jenetics.ext.moea.Vec;
import io.jenetics.ext.moea.VecFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculates the fitness of individuals.
 */
public class FitnessFunction {
    private final VecFactory<double[]> optimizationTarget;
    private final List<Objective> objectives;
    private final KnowledgeGraph knowledgeGraph;
    private final double sumObjectiveWeights;

    public FitnessFunction(final List<Objective> objectives, final KnowledgeGraph knowledgeGraph) {
        this.objectives = objectives;
        this.optimizationTarget = VecFactory.ofDoubleVec(
                objectives.stream()
                        .map(objective -> objective.getOptimize())
                        .collect(Collectors.toList())
        );
        this.knowledgeGraph = knowledgeGraph;

        this.sumObjectiveWeights = objectives.stream()
                .mapToDouble(objective -> objective.getWeight())
                .sum();
    }

    /**
     * See {@link #calculateMultiObjectiveFitnessValue(LinearLinkageEncoding)}
     * @param genotype The genotype representing the modularisation representation.
     * @return the fitness value vector.
     */
    public Vec<double[]> calculateMultiObjectiveFitnessValue(final Genotype<IntegerGene> genotype) {
        final var linearLinkageEncoding = new LinearLinkageEncoding(genotype, knowledgeGraph);
        return calculateMultiObjectiveFitnessValue(linearLinkageEncoding);
    }

    /**
     * Calculates the multi-objective fitness value and returns a vector containing the fitness values.
     * @param linearLinkageEncoding The LLE representing the modularisation solution.
     * @return the fitness value vector.
     */
    public Vec<double[]> calculateMultiObjectiveFitnessValue(final LinearLinkageEncoding linearLinkageEncoding) {
        final var modules = linearLinkageEncoding.getModules();

        final var objectiveValues = objectives.stream()
                .map(objective -> {
                    final var weight = objective.getWeight();
                    final var objectiveValue = objective.calculateValue(modules);

                    return weight * objectiveValue;
                })
                .mapToDouble(Double::doubleValue)
                .toArray();

        return optimizationTarget.newVec(objectiveValues);
    }

    /**
     * See {@link #calculateWeightedSumFitnessValue(LinearLinkageEncoding)}
     * @param genotype The genotype representing the modularisation representation.
     * @return the weighted sum fitness function.
     */
    public double calculateWeightedSumFitnessValue(final Genotype<IntegerGene> genotype) {
        final var linearLinkageEncoding = new LinearLinkageEncoding(genotype, knowledgeGraph);
        return calculateWeightedSumFitnessValue(linearLinkageEncoding);
    }

    /**
     * Calculates the weighted sum of all objectives. Maximisation problems are turned into minimisation problems.
     * @param linearLinkageEncoding The LLE representing the modularisation solution.
     * @return the weighted sum fitness function.
     */
    public double calculateWeightedSumFitnessValue(final LinearLinkageEncoding linearLinkageEncoding) {
        final var modules = linearLinkageEncoding.getModules();

        return objectives.stream()
                .mapToDouble(objective -> {
                    final var weight = objective.getWeight() / this.sumObjectiveWeights;
                    final var objectiveValue = objective.calculateValue(modules);

                    var weightedObjectiveValue = weight * objectiveValue;

                    if (objective.getOptimize() == Optimize.MAXIMUM)
                        weightedObjectiveValue *= -1;

                    return weightedObjectiveValue;
                })
                .sum();
    }
}
