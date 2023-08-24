package com.modcmga.backendservice.dto.application;
/**
 * @Package: com.modcmga.poc.dto
 * @Class: GeneticAlgorithmApplicationInput
 * @Author: Jan
 * @Date: 17.10.2021
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents the input of the to modularise conceptual models.
 */
@Data
@NoArgsConstructor
@ToString
public class ModularisationInput {

    private final ObjectMapper mapper = new ObjectMapper();

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
     * Provides the weight for the mutation operations.
     */
    private String mutationWeight;

    /**
     * The convergence rate. It defines when a gene as converged when the
     * average value of that gene across all of the genotypes in the current
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

    /**
     * Represents the metamodel used for the conceptual model.
     */
    private String metaModelType;

    /**
     * Represents the type of the conceptual model.
     */
    private String conceptualModelType;

    /**
     * The array of edge weights as a string. Each entry consists of the name
     * of the edge and a weight assigned to the edge.
     */
    private String edgeWeights;

    /**
     * The list of objective information.
     */
    private String objectiveData;
}
