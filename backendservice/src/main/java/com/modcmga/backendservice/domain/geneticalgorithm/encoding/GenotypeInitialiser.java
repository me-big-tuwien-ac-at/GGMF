package com.modcmga.backendservice.domain.geneticalgorithm.encoding;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.encoding
 * @Class: LinearLinkageEncodingInitialiser
 * @Author: Jan
 * @Date: 14.08.2022
 */

import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;

/**
 * Provides methods to generate an initial genotype.
 */
public final class GenotypeInitialiser {

    /**
     * Returns a genotype where all elements of connected components are in one
     * module except isolated elements. Isolated elements are in their own
     * module.
     *
     * @param knowledgeGraph the knowledge graph used for generating the
     *                       genotype.
     * @return a genotype for {@code knowledgeGraph}.
     */
    public static Genotype<IntegerGene> generateGenotypeWithModulesForEachConnectedComponent(final KnowledgeGraph knowledgeGraph) {
        final var linearLinkageEncoding =
                LinearLinkageInitialiser.initialiseLinearLinkageEncodingWithModulesForEachConnectedComponent(knowledgeGraph);

        return Genotype.of(linearLinkageEncoding);
    }
}
