package com.modcmga.backendservice.domain.geneticalgorithm.constraint;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.constraint
 * @Class: ConnectedElementsConstraint
 * @Author: Jan
 * @Date: 04.01.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageConstant;
import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncodingInformationProvider;
import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageOperator;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.Phenotype;
import io.jenetics.engine.Constraint;
import io.jenetics.ext.moea.Vec;

/**
 * Checks if the following constraints are fulfilled:
 *
 * <ol>
 *     <li>The number of allele values in the linear linkage encoding is at most
 *     {@value LinearLinkageConstant#MAX_NUMBER_OF_SAME_ALLELE}</li>
 *     <li>All modules are connected s.t. all modularisable elements are incident to another element</li>
 *     <li>There are no moduels that only consist of an edge</li>
 * </ol>
 * <p>
 *     If one the constraints is violated, the repair process is started.
 * </p>
 */
public class InvalidAssignmentConstraint implements Constraint<IntegerGene, Vec<double[]>> {

    private KnowledgeGraph knowledgeGraph;

    /**
     * Ctor.
     * @param knowledgeGraph the knowledge graph
     */
    public InvalidAssignmentConstraint(final KnowledgeGraph knowledgeGraph) {
        this.knowledgeGraph = knowledgeGraph;
    }

    @Override
    public boolean test(final Phenotype<IntegerGene, Vec<double[]>> phenotype) {
        final var genotype = phenotype.genotype();

        final var linearLinkageEncoding = new LinearLinkageEncoding(genotype, knowledgeGraph);

        return LinearLinkageEncodingInformationProvider.isValidLinearLinkageEncoding(linearLinkageEncoding);
    }

    @Override
    public Phenotype<IntegerGene, Vec<double[]>> repair(final Phenotype<IntegerGene, Vec<double[]>> phenotype,
                                                        final long l) {
        final var linearLinkageEncoding = new LinearLinkageEncoding(phenotype.genotype(), knowledgeGraph);

        final var repairedLinearLinkageEncoding = LinearLinkageOperator.fixLinearLinkageEncoding(linearLinkageEncoding);

        final var genotype = Genotype.of(repairedLinearLinkageEncoding);
        return Phenotype.of(genotype, l);
    }
}
