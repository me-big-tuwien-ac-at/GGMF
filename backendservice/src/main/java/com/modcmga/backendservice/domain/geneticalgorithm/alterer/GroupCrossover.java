package com.modcmga.backendservice.domain.geneticalgorithm.alterer;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.alterer
 * @Class: GroupCrossover
 * @Author: Jan
 * @Date: 19.12.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import io.jenetics.Crossover;
import io.jenetics.IntegerGene;
import io.jenetics.util.MSeq;
import io.jenetics.util.RandomRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Custom crossover process tailored for this modularisation problem. At first the ending nodes using both parents are
 * determined. Then each gene is assigned to a specific module depending on the allele value in the parents. The result
 * for each offspring is that the elements in the module are still connected.
 */
public class GroupCrossover extends Crossover<IntegerGene, Integer> {

    private final KnowledgeGraph knowledgeGraph;

    public GroupCrossover(double probability, KnowledgeGraph knowledgeGraph) {
        super(probability);

        this.knowledgeGraph = knowledgeGraph;
    }

    @Override
    protected int crossover(
            MSeq<IntegerGene> parent1, MSeq<IntegerGene> parent2) {
        final var linearLinkageEncodingParent1 =
                new LinearLinkageEncoding(parent1.asList(), knowledgeGraph);

        final var linearLinkageEncodingParent2 =
                new LinearLinkageEncoding(parent2.asList(), knowledgeGraph);

        // Check if both parents are valid encodings to prevent creating non connected modularisable elements
        if (!linearLinkageEncodingParent1.isValid() || !linearLinkageEncodingParent2.isValid()) {
            return 0;
        }

        // impact on ending nodes
        final var newModulesForOffspring1 =
                determineNewModules(parent1, parent2);
        final var newModulesForOffspring2 = new HashMap<>(newModulesForOffspring1);

        for (int i = 0; i < parent1.length(); i++) {
            final var geneInParent1 = linearLinkageEncodingParent1.get(i);
            final var geneInParent2 = linearLinkageEncodingParent2.get(i);

            // if current gene in parent 1 and parent 2 is an ending node, then they are always added to the offspring,
            // and therefore, the gene off index i in parent1 and parent2 must not be changed.
            if (isEndingNode(geneInParent1, i) && isEndingNode(geneInParent2, i)) continue;

            // Assign gene to a new module or create new module for the current gene
            assignGeneToOneOfNewModules(i, linearLinkageEncodingParent1, newModulesForOffspring1);
            assignGeneToOneOfNewModules(i, linearLinkageEncodingParent2, newModulesForOffspring2);
        }

        // Update the parents to create the new offspring
        updateParentToOffspring(parent1, new ArrayList<>(newModulesForOffspring1.values()));
        updateParentToOffspring(parent2, new ArrayList<>(newModulesForOffspring2.values()));

        return parent1.length();
    }

    /**
     * Returns a map of newly created modules. Each newly created module contains an ending node as followed:
     *
     * <ol>
     *     <li>In 1st parent and 2nd parent the integer gene is an ending node</li>
     *     <li>In 1st parent integer gene is an ending node and is chosen by 50% probability</li>
     *     <li>In 2nd parent integer gene is an ending node and is chosen by 50% probability</li>
     * </ol>
     * @param firstParent The first chromosome, which is used for determining the new module
     * @param secondParent The second chromosome, which is used for determining the new module
     * @return the map where the index of the ending node is the key and the module is the value.
     */
    private Map<Integer, Module> determineNewModules(
            final MSeq<IntegerGene> firstParent, final MSeq<IntegerGene> secondParent) {
        final var random = RandomRegistry.random();

        final var newModules = new HashMap<Integer, Module>();

        for (int i = 0; i < firstParent.size(); i++) {
            final var modularisableElement = knowledgeGraph.getModularisableElement(i);

            if (((modularisableElement instanceof Vertex && knowledgeGraph.isIsolated((Vertex) modularisableElement)) ||
                    (isEndingNode(firstParent, i) && isEndingNode(secondParent, i)) ||
                    (isEndingNode(firstParent, i) && random.nextDouble() < 0.5) ||
                    (isEndingNode(secondParent, i) && random.nextDouble() < 0.5))) {
                final Module module = new Module();
                module.addIndex(i);

                newModules.put(i, module);
            }
        }

        return newModules;
    }

    private boolean isEndingNode(final MSeq<IntegerGene> mSeq, final int index) {
        return isEndingNode(mSeq.get(index), index);
    }

    private boolean isEndingNode(final IntegerGene integerGene, final int index) {
        return integerGene.allele() == index;
    }

    private void assignGeneToOneOfNewModules(
            final int index, final LinearLinkageEncoding linearLinkageEncodingParent, final Map<Integer, Module> newModulesForOffspring) {
        // The ending nodes of i-th gene is determined in parent1 and parent2. The allele of the ending node is
        // always higher due to the ascending order.
        final var geneInParent = linearLinkageEncodingParent.get(index);
        final var moduleOfGene = linearLinkageEncodingParent.getModuleOfAllele(geneInParent.allele());
        final var alleleEndingNodeInParent = moduleOfGene.getAlleleOfEndingNode();

        // Check if the ending node in parent 1 was selected by finding out if the ending node was selected for the
        // new modules by checking if a module was returned. If the module was not selected, null is returned.

        if (newModulesForOffspring.containsKey(alleleEndingNodeInParent)) {
            final var moduleWithSameEndingNode = newModulesForOffspring.get(alleleEndingNodeInParent);
            // assign this inter gene to this module
            moduleWithSameEndingNode.addIndex(index);
            return;
        }

        // Determine incident modularisable elements and check if they are in one these modules
        final var modularisableElement = knowledgeGraph.getModularisableElement(index);
        final var incidentModularisableElements =
                knowledgeGraph.getIncidentModularisableElements(modularisableElement);

        // Determine modules where incident modularisable elements of current element are assigned to
        var newModulesForOffspringList = new ArrayList<>(newModulesForOffspring.values());
        final var modulesOfIncidentModularisableElement = newModulesForOffspringList.stream()
                .filter(newModule -> incidentModularisableElements.stream()
                        .anyMatch(incidentModularisableElement ->
                                newModule.isIndexInModule(incidentModularisableElement.getIndex())))
                .collect(Collectors.toList());

        if (!modulesOfIncidentModularisableElement.isEmpty()) {
            // Randomly assign current modularisable element to any random module where any incident modularisable
            // element of current element is assigned to
            final var randomIndexModulesOfIncidentModularisableElement = ThreadLocalRandom.current()
                    .nextInt(modulesOfIncidentModularisableElement.size());

            final var randomModuleOfIncidentModularisableElement = modulesOfIncidentModularisableElement
                    .get(randomIndexModulesOfIncidentModularisableElement);

            randomModuleOfIncidentModularisableElement.addIndex(index);
        } else {
            // Create a new module for the current modularisable element as there are no incident modules to attach to
            final var newModule = new Module();
            newModule.addIndex(index);

            newModulesForOffspring.put(index, newModule);
        }
    }

    private void updateParentToOffspring(MSeq<IntegerGene> parent, List<Module> newDeterminedModules) {
        for (final var newDeterminedModule : newDeterminedModules) {
            final var indicesOfModule = newDeterminedModule.getIndices();

            for (int i = 0; i < indicesOfModule.size() - 1; i++) {
                // Update allele of current gene to next index in current module
                var integerGene = parent.get(i);

                var updatedIntegerGene = integerGene.newInstance(indicesOfModule.get(i + 1));
                parent.set(i, updatedIntegerGene);
            }

            // Let ending node point to itself
            var lastIndexOfModule = indicesOfModule.size() - 1;
            var endingNodeIntegerGene = parent.get(indicesOfModule.get(lastIndexOfModule));

            var updatedEndingNode = endingNodeIntegerGene.newInstance(lastIndexOfModule);
            parent.set(lastIndexOfModule, updatedEndingNode);
        }
    }
}
