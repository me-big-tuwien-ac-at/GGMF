package com.modcmga.backendservice.domain.geneticalgorithm.engine;
/**
 * @Package: com.modcmga.poc.geneticalgorithm
 * @Class: GeneticAlgorithmEngine
 * @Author: Jan
 * @Date: 22.10.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.FitnessFunction;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.objective.common.ObjectiveUtil;
import com.modcmga.backendservice.model.export.GeneticAlgorithmExecutionResult;
import com.modcmga.backendservice.model.export.GeneticAlgorithmResults;
import com.modcmga.backendservice.model.export.ParetoOptimalSolution;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import com.modcmga.backendservice.util.CalculationUtil;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.ext.moea.MOEA;
import io.jenetics.util.IntRange;
import io.jenetics.util.RandomRegistry;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Executes the Genetic Algorithm with the passed parameter and returns the
 * result.
 */
@Component
public class MainGeneticAlgorithmEngine implements GeneticAlgorithmEngine {
    private final static long RANDOM_GENERATOR_SEED = 12345L;

    @Override
    public GeneticAlgorithmExecutionResult run(final KnowledgeGraph knowledgeGraph,
                                               final ModularisationParameter modularisationParameter) {
        RandomRegistry.random(new Random(RANDOM_GENERATOR_SEED));

        if (modularisationParameter != null && modularisationParameter.getObjectiveSetup().isUseWeightedSumMethod()) {
            return modulariseWithWeightedSumFitnessFunction(modularisationParameter, knowledgeGraph);
        }

        return modulariseWithMultiObjectiveFitnessFunction(modularisationParameter, knowledgeGraph);
    }

    private GeneticAlgorithmExecutionResult modulariseWithWeightedSumFitnessFunction(
            final ModularisationParameter modularisationParameter, final KnowledgeGraph knowledgeGraph) {
        final var objectives = modularisationParameter.getObjectiveSetup().getObjectives();
        final var fitnessFunction = new FitnessFunction(objectives, knowledgeGraph);

        final var engineForMultiObjectiveProblem = new GeneticAlgorithmEngineBuilder.Builder()
                .knowledgeGraph(knowledgeGraph)
                .parameter(modularisationParameter)
                .fitnessFunction(fitnessFunction)
                .createEngineForWeightedSumProblem();

        final var modularisationTimeStart = System.currentTimeMillis();

        final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();
        final var modularisationResult = engineForMultiObjectiveProblem.stream()
                .limit(Limits.byFixedGeneration(geneticAlgorithmParameter.getCountGeneration()))
                .collect(EvolutionResult.toBestEvolutionResult());

        final var modularisationTimeInMillis = System.currentTimeMillis() - modularisationTimeStart;

        final var bestObjectiveValue = modularisationResult.bestFitness();
        final var paretoSet = modularisationResult.population().stream()
                .filter(result -> result.fitness() == bestObjectiveValue)
                .map(result -> {
                    final var linearLinkageEncoding = new LinearLinkageEncoding(
                            result.genotype().chromosome(), knowledgeGraph);
                    final var paretoOptimalSolution = new ParetoOptimalSolution();
                    paretoOptimalSolution.setModules(linearLinkageEncoding.getModules());
                    paretoOptimalSolution.setFitnessValue(result.fitness());
                    return paretoOptimalSolution;
                })
                .collect(Collectors.toCollection(HashSet::new));

        final var resultExecutionExport = new GeneticAlgorithmExecutionResult();

        resultExecutionExport.setParetoSet
                (paretoSet);

        final var geneticAlgorithmResults = new GeneticAlgorithmResults();
        geneticAlgorithmResults.setParetoSetSize(paretoSet.size());
        geneticAlgorithmResults.setModularizationTimeInMillisecond(modularisationTimeInMillis);
        resultExecutionExport.setGeneticAlgorithmResults(geneticAlgorithmResults);

        return resultExecutionExport;
    }

    private GeneticAlgorithmExecutionResult modulariseWithMultiObjectiveFitnessFunction(
            final ModularisationParameter modularisationParameter, final KnowledgeGraph knowledgeGraph) {
        final var objectives = modularisationParameter.getObjectiveSetup().getObjectives();
        final var fitnessFunction = new FitnessFunction(objectives, knowledgeGraph);

        final var engineForMultiObjectiveProblem = new GeneticAlgorithmEngineBuilder.Builder()
                .knowledgeGraph(knowledgeGraph)
                .parameter(modularisationParameter)
                .fitnessFunction(fitnessFunction)
                .createEngineForMultiObjectiveProblem();

        final var modularisationTimeStart = System.currentTimeMillis();

        final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();
        final var paretoSetSizeRange = IntRange.of(
                geneticAlgorithmParameter.getMinimumParetoSetSize(),
                geneticAlgorithmParameter.getMaximumParetoSetSize());
        final var paretoSetResult = engineForMultiObjectiveProblem.stream()
                .limit(Limits.byGeneConvergence(geneticAlgorithmParameter.getConvergenceRate(), geneticAlgorithmParameter.getConvergedGeneRate()))
                .limit(Limits.byFixedGeneration(geneticAlgorithmParameter.getCountGeneration()))
                .collect(MOEA.toParetoSet(paretoSetSizeRange));

        final var modularisationTimeInMillis = System.currentTimeMillis() - modularisationTimeStart;

        // Create pareto list
        final var paretoList = paretoSetResult.stream()
                .map(unmappedParetoOptimalSolution -> {
                    var linearLinkageEncoding = new LinearLinkageEncoding(
                            unmappedParetoOptimalSolution.genotype().chromosome(), knowledgeGraph);

                    var paretoOptimalSolution = new ParetoOptimalSolution();
                    paretoOptimalSolution.setModules(linearLinkageEncoding.getModules());
                    paretoOptimalSolution.setFitnessValues(unmappedParetoOptimalSolution.fitness().data());

                    return paretoOptimalSolution;
                })
                .collect(Collectors.toList());

        // Normalise objective values and assign them to each paretoSet
        final var objectiveValuesAsMatrix = paretoList.stream()
                .map(paretoOptimalSolution -> paretoOptimalSolution.getFitnessValues())
                .collect(Collectors.toList());
        final var normalisedObjectiveValuesAsMatrix = CalculationUtil.normalise(objectiveValuesAsMatrix);

        for (int i = 0; i < paretoList.size(); i++) {
            final var paretoOptimalSolution = paretoList.get(i);
            paretoOptimalSolution.setNormalisedFitnessValues(normalisedObjectiveValuesAsMatrix.get(i));
        }

        // Calculates the ranking based on the single objective value of the multiobjective value
        final var singleNormalisedObjectiveValues = paretoList.stream()
                .map(paretoOptimalSolution ->
                        ObjectiveUtil.calculateSingleObjectiveValue(
                                objectives, paretoOptimalSolution.getNormalisedFitnessValues()))
                .collect(Collectors.toList());
        final var rankings = CalculationUtil.determineRanking(singleNormalisedObjectiveValues);
        for (int i = 0; i < paretoList.size(); i++) {
            final var paretoOptimalSolution = paretoList.get(i);
            paretoOptimalSolution.setRanking(rankings.get(i));
        }

        final var paretoSet = new HashSet<>(paretoList);

        final var resultExecutionExport = new GeneticAlgorithmExecutionResult();
        resultExecutionExport.setParetoSet(paretoSet);

        final var geneticAlgorithmResults = new GeneticAlgorithmResults();
        geneticAlgorithmResults.setParetoSetSize(paretoSet.size());
        geneticAlgorithmResults.setModularizationTimeInMillisecond(modularisationTimeInMillis);
        resultExecutionExport.setGeneticAlgorithmResults(geneticAlgorithmResults);

        return resultExecutionExport;
    }
}
