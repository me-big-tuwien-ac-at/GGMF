package com.modcmga.backendservice.domain.geneticalgorithm.encoding;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.encoding
 * @Class: LinearLinkageEncoding
 * @Author: Jan
 * @Date: 19.12.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import io.jenetics.Chromosome;
import io.jenetics.Genotype;
import io.jenetics.IntegerGene;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *     This encoding represents a unique representation of modularised knowledge
 *     graphs. Each gene of this representation represents a modularisable element
 *     of the knowledge graph i.e. vertices and edges. Each allele represents an
 *     index of a gene in the chromosome. When the allele value and the index of
 *     this gene is the same, then it is the ending point. Otherwise, the allele
 *     points to the next modularisable element in the same module.
 * </p>
 * <p>
 * Each {@link LinearLinkageEncoding} is immutable and its allele values cannot
 * be changed.
 * </p>
 */
@Getter
public class LinearLinkageEncoding implements Chromosome<IntegerGene> {
    private final List<IntegerGene> integerGenes;
    private final List<Module> modules;
    private final KnowledgeGraph knowledgeGraph;

    public LinearLinkageEncoding(Genotype genotype, KnowledgeGraph knowledgeGraph) {
        this(genotype.chromosome(), knowledgeGraph);
    }

    public LinearLinkageEncoding(Chromosome<IntegerGene> chromosome, KnowledgeGraph knowledgeGraph) {
        this(chromosome.stream().collect(Collectors.toList()), knowledgeGraph);
    }

    public LinearLinkageEncoding(List<IntegerGene> integerGenes, KnowledgeGraph knowledgeGraph) {
        this.integerGenes = integerGenes;
        this.knowledgeGraph = knowledgeGraph;
        this.modules = LinearLinkageEncodingInformationProvider.determineModules(this);
    }

    @Override
    public Chromosome<IntegerGene> newInstance(ISeq<IntegerGene> iSeq) {
        // The random sequence iSeq never creates a valid sequence, and it is hard to repair the sequence. Therefore,
        // only mutation operations are used.
        return newInstance();
    }

    @Override
    public Chromosome<IntegerGene> newInstance() {
        final var linearLinkageEncoding = new LinearLinkageEncoding(new ArrayList<>(integerGenes), this. knowledgeGraph);

        return mutatedLinearLinkageEncoding(linearLinkageEncoding);
    }


    private LinearLinkageEncoding mutatedLinearLinkageEncoding(final LinearLinkageEncoding linearLinkageEncoding) {
        final var randomValue = RandomRegistry.random().nextDouble();
        if (randomValue < 1.0d / 3.0d)
            return LinearLinkageOperator.divideRandomModule(linearLinkageEncoding);
        else if (randomValue < 2.0d / 3.0d)
            if (LinearLinkageEncodingInformationProvider.getNumberOfNonIsolatedModules(linearLinkageEncoding) > 2)
                return LinearLinkageOperator.combineRandomGroup(linearLinkageEncoding);

        return LinearLinkageOperator.moveRandomGeneToIncidentModule(linearLinkageEncoding);
    }

    @Override
    public IntegerGene get(int i) {
        return integerGenes.get(i);
    }

    @Override
    public int length() {
        return integerGenes.size();
    }

    @Override
    public boolean isValid() {
        return LinearLinkageEncodingInformationProvider.isValidLinearLinkageEncoding(this);
    }

    @Override
    public String toString() {
        return integerGenes.toString();
    }

    /**
     * Returns the module where the allele is contained.
     * @param allele the allele where the module is determined for.
     * @return the module where the allele is contained.
     */
    public Module getModuleOfAllele(final int allele) {
        return modules.stream()
                .filter(module -> module.isIndexInModule(allele))
                .findFirst()
                .get();
    }
}
