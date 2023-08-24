package com.modcmga.backendservice.domain.geneticalgorithm.engine;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.engine
 * @Class: GeneticAlgorithmEngine2
 * @Author: Jan
 * @Date: 04.12.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.alterer.GraftMutator;
import com.modcmga.backendservice.domain.geneticalgorithm.alterer.GroupCrossover;
import com.modcmga.backendservice.domain.geneticalgorithm.constraint.InvalidAssignmentConstraint;
import com.modcmga.backendservice.domain.geneticalgorithm.encoding.GenotypeInitialiser;
import com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.FitnessFunction;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import com.modcmga.backendservice.model.parameter.GeneticAlgorithmParameter;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.ext.moea.UFTournamentSelector;
import io.jenetics.ext.moea.Vec;

/**
 * This class is responsible for the creation of the Genetic Algorithm with
 * respect to the passed information. It uses the Builder pattern to pass the
 * necessary information and create the engine.
 */
public class GeneticAlgorithmEngineBuilder {
    public static class Builder {
        private KnowledgeGraph knowledgeGraph;
        private ModularisationParameter modularisationParameter;
        private FitnessFunction fitnessFunction;

        public Builder knowledgeGraph(final KnowledgeGraph knowledgeGraph) {
            this.knowledgeGraph = knowledgeGraph;
            return this;
        }

        public Builder parameter(
                final ModularisationParameter modularisationParameter) {
            this.modularisationParameter = modularisationParameter;
            return this;
        }

        public Builder fitnessFunction(final FitnessFunction fitnessFunction) {
            this.fitnessFunction = fitnessFunction;
            return this;
        }

        public Engine<IntegerGene, Vec<double[]>> createEngineForMultiObjectiveProblem() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();
            final var genotype = genotype(knowledgeGraph, geneticAlgorithmParameter);
            final var connectedElementsConstraint = new InvalidAssignmentConstraint(knowledgeGraph);

            return Engine
                    .builder(fitnessFunction::calculateMultiObjectiveFitnessValue, genotype)
                    .populationSize(geneticAlgorithmParameter.getCountPopulation())
                    .offspringFraction(0.7)
                    .offspringSelector(multiObjectiveOffspringSelector())
                    .survivorsSelector(multiObjectiveSurvivorsSelector())
                    .alterers(mutator(), crossover())
                    .constraint(connectedElementsConstraint)
                    .build();
        }

        public Engine<IntegerGene, Double> createEngineForWeightedSumProblem() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();
            final var genotype = genotype(knowledgeGraph, geneticAlgorithmParameter);
            final var connectedElementsConstraint = new InvalidAssignmentConstraint(knowledgeGraph);

            return Engine
                    .builder(fitnessFunction::calculateWeightedSumFitnessValue, genotype)
                    .populationSize(geneticAlgorithmParameter.getCountPopulation())
                    .offspringFraction(0.7)
                    .offspringSelector(singleObjectiveOffspringSelector())
                    .survivorsSelector(singleObjectiveSurvivorsSelector())
                    .alterers(mutator(), crossover())
                    .constraint(connectedElementsConstraint)
                    .optimize(Optimize.MINIMUM)
                    .build();
        }

        private Genotype<IntegerGene> genotype(
                final KnowledgeGraph knowledgeGraph, final GeneticAlgorithmParameter geneticAlgorithmParameter) {

            Genotype<IntegerGene> genotype;
            // Extend for new encodings for the genetic algorithm
            switch (geneticAlgorithmParameter.getChromosomeEncoding()) {
                default: {
                    genotype = GenotypeInitialiser.generateGenotypeWithModulesForEachConnectedComponent(knowledgeGraph);
                    break;
                }
            }

            return genotype;
        }

        private Selector<IntegerGene, Vec<double[]>> multiObjectiveOffspringSelector() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();
            switch (geneticAlgorithmParameter.getOffspringSelector()) {
                default:
                    return new TournamentSelector<>(geneticAlgorithmParameter.getTournamentSize());
            }
        }

        private Selector<IntegerGene, Vec<double[]>> multiObjectiveSurvivorsSelector() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();

            switch (geneticAlgorithmParameter.getSurvivorSelector()) {
                default:
                    return UFTournamentSelector.ofVec();
            }
        }

        private Selector<IntegerGene, Double> singleObjectiveOffspringSelector() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();

            switch (geneticAlgorithmParameter.getOffspringSelector()) {
                default:
                    return new TournamentSelector<>(geneticAlgorithmParameter.getTournamentSize());
            }
        }

        private Selector<IntegerGene, Double> singleObjectiveSurvivorsSelector() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();
            switch (geneticAlgorithmParameter.getSurvivorSelector()) {
                default:
                    return new RouletteWheelSelector<>();
            }
        }

        private Mutator mutator() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();
            final var mutationWeight = modularisationParameter.getMutationWeight();

            // Extend for different types of mutation
            switch (geneticAlgorithmParameter.getMutationType()) {
                default:
                    return new GraftMutator(geneticAlgorithmParameter.getMutationProbability(), mutationWeight, knowledgeGraph);
            }
        }

        private Crossover crossover() {
            final var geneticAlgorithmParameter = modularisationParameter.getGeneticAlgorithmParameter();

            // Extend for different types of crossover
            switch (geneticAlgorithmParameter.getCrossoverType()) {
                default:
                    return new GroupCrossover(geneticAlgorithmParameter.getCrossoverProbability(), knowledgeGraph);
            }
        }
    }
}
