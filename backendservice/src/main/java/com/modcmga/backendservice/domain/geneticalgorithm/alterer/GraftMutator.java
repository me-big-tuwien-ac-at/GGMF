package com.modcmga.backendservice.domain.geneticalgorithm.alterer;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.alterer
 * @Class: GraftMutator
 * @Author: Jan
 * @Date: 20.12.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncodingInformationProvider;
import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageOperator;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.parameter.MutationWeight;
import io.jenetics.Chromosome;
import io.jenetics.IntegerGene;
import io.jenetics.Mutator;
import io.jenetics.MutatorResult;

import java.util.Random;

/**
 *
 */
public class GraftMutator extends Mutator<IntegerGene, Integer> {

    private final KnowledgeGraph knowledgeGraph;
    private double divideModuleProbability;
    private double combineModuleProbability;
    private double moveGeneToDifferentModuleProbability;

    public GraftMutator(
            final double probability, final MutationWeight mutationWeight, final KnowledgeGraph knowledgeGraph) {
        super(probability);

        this.knowledgeGraph = knowledgeGraph;

        determineMutationOperatorProbability(mutationWeight);
    }

    private void determineMutationOperatorProbability(final MutationWeight mutationWeight) {
        final double sumPossbility =
                mutationWeight.getCombineModulesWeight() +
                mutationWeight.getSplitModulesWeight() +
                mutationWeight.getMoveElementsBetweenModulesWeight();

        this.divideModuleProbability = mutationWeight.getSplitModulesWeight() / sumPossbility;
        this.combineModuleProbability = mutationWeight.getCombineModulesWeight() / sumPossbility;
        this.moveGeneToDifferentModuleProbability = mutationWeight.getMoveElementsBetweenModulesWeight() / sumPossbility;
    }

    @Override
    protected MutatorResult<Chromosome<IntegerGene>> mutate(
            final Chromosome<IntegerGene> chromosome, final double mutationProbability, final Random random) {
        var chromosomeResult = chromosome;

        if (random.nextDouble() < mutationProbability) {
            final var randomValue = random.nextDouble();

            final var linearLinkageEncoding = new LinearLinkageEncoding(chromosome, knowledgeGraph);

            if (randomValue < divideModuleProbability) {
                chromosomeResult = LinearLinkageOperator.divideRandomModule(linearLinkageEncoding);
            } else if (randomValue < combineModuleProbability) {
                // Check if it does not result in a monolith again
                if (LinearLinkageEncodingInformationProvider.getNumberOfNonIsolatedModules(linearLinkageEncoding) > 2)
                    chromosomeResult = LinearLinkageOperator.combineRandomGroup(linearLinkageEncoding);
            } else {
                chromosomeResult = LinearLinkageOperator.moveRandomGeneToIncidentModule(linearLinkageEncoding);
            }
        }

        return MutatorResult.of(chromosomeResult);
    }
}
