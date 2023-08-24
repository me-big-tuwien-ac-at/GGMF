package com.modcmga.backendservice.model.parameter;
/**
 * @Package: com.modcmga.poc.dto
 * @Class: GeneticAlgorithmParameter
 * @Author: Jan
 * @Date: 16.10.2021
 */

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

/**
 * Represents the Genetic Algorithm parameter input of the service.
 * The default values for all parameters are 0.
 */
@NoArgsConstructor
@Component
@Getter
@Setter
public class GeneticAlgorithmParameter {

    /**
     * Defines the type of chromosome encoding used in the genetic algorithm.
     */
    private String chromosomeEncoding;

    /**
     * Defines the selection of offspring individuals for the subsequent generations.
     */
    private String offspringSelector;

    /**
     * Defines the selection of individuals for that should survivate for the subsequent generations.
     */
    private String survivorSelector;

    /**
     * The type of crossover operations used in the genetic algorithm.
     */
    private String crossoverType;

    /**
     * The type of mutation operations used in the genetic algorithm.
     */
    private String mutationType;

    /**
     * The count of population the Genetic Algorithm has in each generation.
     */
    private int countPopulation;

    /**
     * The probability of mutating an individual represented by a value between
     * 0.0 and 1.0
     */
    private double mutationProbability;

    /**
     * The probability of crossover between 2 parents represented by a value
     * between 0.0 and 1.0
     */
    private double crossoverProbability;

    /**
     * Defines the size used for each round in the tournament selection.
     */
    private int tournamentSize;

    /**
     * The convergence rate. It defines when a gene as converged when the
     * average value of that gene accross all of the genotypes in the current
     * population is less than the convergence rate. The value is between
     * 0.0 and 1.0
     */
    private double convergenceRate;

    /**
     * The rate of genes which must be converged to truncate the evolution
     * stream.
     */
    private double convergedGeneRate;
    /**
     * The count of generation which the Genetic Algorithm has to go through.
     */
    private int countGeneration;

    /**
     * The minimum number of elements in a pareto Set.
     */
    private int minimumParetoSetSize;

    /**
     * The maximum number of elements in a pareto Set.
     */
    private int maximumParetoSetSize;

    @Override
    public String toString() {
        return String.format(
                "countGeneration; %s\n" +
                        "countPopulation; %s\n" +
                        "crossoverProbability; %s\n" +
                        "mutationProbability; %s\n" +
                        "convergenceRate; %s\n" +
                        "convergedGeneRate; %s\n" +
                        "minimumParetoSetSize; %s\n" +
                        "maximumParetoSetSize; %s",
                this.countGeneration,
                this.countPopulation,
                this.crossoverProbability,
                this.mutationProbability,
                this.convergenceRate,
                this.convergedGeneRate,
                this.minimumParetoSetSize,
                this.maximumParetoSetSize);
    }
}
