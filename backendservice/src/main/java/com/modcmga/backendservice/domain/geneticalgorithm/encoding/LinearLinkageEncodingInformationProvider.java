package com.modcmga.backendservice.domain.geneticalgorithm.encoding;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import io.jenetics.IntegerGene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Util class to determine the modules from the LLE.
 */
public final class LinearLinkageEncodingInformationProvider {

    /**
     * Determines the linear linkage encoding based on the list of {@modules}.
     * @param modules the modules used to determine the encoding
     * @param knowledgeGraph the knowledge graph containing the vertex and edge information.
     * @return the linear linkage encoding
     */
    public static LinearLinkageEncoding determineLinearLinkageEncoding(
            final List<Module> modules, final KnowledgeGraph knowledgeGraph) {
        // Generate initial genes
        final var modularisableElementSize = knowledgeGraph.getModularisableElements().size();
        final var integerGenes = IntStream.range(0, modularisableElementSize)
                .mapToObj(i -> IntegerGene.of(0, modularisableElementSize - 1))
                .collect(Collectors.toList());

        // Adapt the allele for each gene
        for (final var module : modules) {
            final var indices = module.getIndices();

            for (int i = 0; i < indices.size() - 1; i++) {
                final var integerGene = integerGenes.get(i);

                final var indexOfModule = indices.get(i);
                final var successor = indices.get(i + 1);

                // Point gene to successor in module of LLE
                var updatedGene = integerGene.newInstance(successor);
                integerGenes.set(indexOfModule, updatedGene);
            }

            // last gene (ending node) point to itself
            final var endingNode = integerGenes.get(indices.size() - 1);
            final var updatedEndingNode = endingNode.newInstance(indices.get(indices.size() - 1));

            integerGenes.set(indices.getLast(), updatedEndingNode);
        }


        return new LinearLinkageEncoding(integerGenes, knowledgeGraph);
    }

    /**
     * Determines the module from the linear linkage encoding.
     * @param linearLinkageEncoding the encoding to determine the modules.
     * @return the list of modules
     */
    public static List<Module> determineModules(LinearLinkageEncoding linearLinkageEncoding) {
        var chromosomes = linearLinkageEncoding.getIntegerGenes()
                .stream()
                .map(integerGene -> integerGene.allele())
                .collect(Collectors.toList());
        var visitedNodes = new boolean[chromosomes.size()];

        var modules = new ArrayList<Module>();
        for (int i = 0; i < chromosomes.size(); i++) {
            if (!visitedNodes[i]) {
                // starting node is given
                int index = chromosomes.get(i);

                boolean isAlleleAlreadyInModule = modules.stream()
                        .anyMatch(m -> m.isIndexInModule(index));

                if (isAlleleAlreadyInModule) {
                    var module = modules.stream()
                            .filter(m -> m.isIndexInModule(index))
                            .findAny()
                            .get();

                    buildModuleDFS(
                            module,
                            chromosomes,
                            visitedNodes,
                            i);
                } else {
                    var module = new Module();

                    buildModuleDFS(
                            module,
                            chromosomes,
                            visitedNodes,
                            i);

                    modules.add(module);
                }
            }
        }
        return modules;
    }

    private static void buildModuleDFS(
            Module module,
            List<Integer> chromosomes,
            boolean[] visitedNode,
            int index) {
        visitedNode[index] = true;

        var nextNode = chromosomes.get(index);
        if (nextNode != index && !visitedNode[nextNode]) {
            buildModuleDFS(
                    module,
                    chromosomes,
                    visitedNode,
                    nextNode);
        }

        module.addIndex(index);
    }

    /**
     * Returns true, if all constraint for the linear linkage encoding is true. The linear linkage encoding satisfies
     * the following constraints:
     *
     * <ul>
     *     <li>Each allele value appears at most twice.</li>
     *     <li>Each module consists only of incident modularisable elements.</li>
     *     <li>No module consists of 1 edge or 1 vertex.</li>
     * </ul>
     *
     * @param linearLinkageEncoding the linear linkage encoding, which is checked.
     * @return true, if all constraint for the linear linkage encoding is true.
     */
    public static boolean isValidLinearLinkageEncoding(final LinearLinkageEncoding linearLinkageEncoding) {
        if (!isValidAlleleValues(linearLinkageEncoding))
            return false;

        if (!isAllElementsInModuleConnected(linearLinkageEncoding))
            return false;

        if (isOneModuleConsistOfOneEdge(linearLinkageEncoding))
            return false;

        if (isMonolith(linearLinkageEncoding)) {
            return false;
        }

        return true;
    }

    /**
     * Returns true, if each allele value appears at most twice.
     * @param linearLinkageEncoding the linear linkage encoding, which is checked.
     * @return true, if each allele value appears at most twice.
     */
    public static boolean isValidAlleleValues(final LinearLinkageEncoding linearLinkageEncoding) {
        final var interGenes = linearLinkageEncoding.getIntegerGenes();

        // Create a map to track the number of already existing allele values. At most an linear linkage encoding can
        // have up to 2 allele values
        final var alleleCountMap = new HashMap<Integer, Integer>();

        for (int i = 0; i < interGenes.size(); i++) {
            final var currentAlleleValue = interGenes.get(i).allele();

            alleleCountMap.merge(currentAlleleValue, 1, (oldCount, newCount) -> ++oldCount);

            if (alleleCountMap.get(currentAlleleValue) > 2) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns true, if modularisable elements in all modules are connected i.e. are incident to another modularisable
     * element.
     * @param linearLinkageEncoding the linear linkage encoding, which is checked.
     * @return true, if modularisable elements in all modules are connected i.e. are incident to another modularisable
     * element.
     */
    public static boolean isAllElementsInModuleConnected(final LinearLinkageEncoding linearLinkageEncoding) {
        final var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();

        final var modulesToCheck =
                linearLinkageEncoding.getModules()
                        .stream()
                        .filter(module -> module.getIndices().size() > 1)
                        .collect(Collectors.toList());

        return modulesToCheck.stream().allMatch(module ->
                ModuleInformationProvider.isModuleConnected(module, knowledgeGraph));
    }

    /**
     * Returns true, if there exists a module, which only consists of 1 vertex or 1 edge.
     * @param linearLinkageEncoding the linear linkage encoding, which is checked.
     * @return true, if there exists a module, which only consists of 1 edge.
     */
    public static boolean isOneModuleConsistOfOneEdge(final LinearLinkageEncoding linearLinkageEncoding) {
        final var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();

        return linearLinkageEncoding.getModules()
                .stream()
                .anyMatch(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph) &&
                        module.getIndices().size() <= 2);
    }

    /**
     * Returns true, if there is one non-isolated module i.e. all elements are connected with eachother in a
     * monolith structure.
     *
     * <p>It is assumed that a monolith is an invalid structure</p>
     *
     * @param linearLinkageEncoding the linear linkage encoding, which is checked.
     * @return true, if there is one non-isolated module.
     */
    public static boolean isMonolith(final LinearLinkageEncoding linearLinkageEncoding) {
        return getNumberOfNonIsolatedModules(linearLinkageEncoding) == 1;
    }

    /**
     * Returns the number of modules which are not isolated.
     * @param linearLinkageEncoding the linear linkage encoding, which is checked.
     * @return the number of modules which are not isolated.
     */
    public static long getNumberOfNonIsolatedModules(final LinearLinkageEncoding linearLinkageEncoding) {
        final var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();

        return linearLinkageEncoding.getModules()
                .stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                .count();
    }
}
