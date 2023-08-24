package com.modcmga.backendservice.domain.geneticalgorithm.encoding;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.module
 * @Class: ModuleOperator
 * @Author: Jan
 * @Date: 13.08.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleOperator;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import io.jenetics.IntegerGene;
import io.jenetics.util.RandomRegistry;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provide methods, which creates updated linear linkage encodings with 
 * new allele values of genes.
 */
public final class LinearLinkageOperator {
    /**
     * The number of the repeating process of moving non connected modularisable elements to incident modules until it
     * terminates.
     */
    private static final int COUNT_LOOP_TERMINATION = 100;

    /**
     * Returns a linear linkage encoding where a randomly selected non-isolated module is splitted in
     * {@value linearLinkageEncoding}.
     * @param linearLinkageEncoding the linear linkage encoding, which will used for randomly splitting a module.
     * @return a linear linkage encoding where a randomly selected non-isolated module is splitted in
     * {@value linearLinkageEncoding}.
     */
    public static LinearLinkageEncoding divideRandomModule(LinearLinkageEncoding linearLinkageEncoding) {
        var modulesWithMultipleIndices = linearLinkageEncoding.getModules().stream()
                .filter(module -> module.getIndices().size() > 1 &&
                        ModuleInformationProvider.isModuleConnected(module.getIndices(), linearLinkageEncoding.getKnowledgeGraph()))
                .collect(Collectors.toList());

        if (modulesWithMultipleIndices.isEmpty()) {
            return linearLinkageEncoding;
        }

        Collections.shuffle(modulesWithMultipleIndices);

        var randomModule = modulesWithMultipleIndices.get(0);

        var splittedModule = ModuleOperator.divideModuleRandomWalk2(randomModule, linearLinkageEncoding.getKnowledgeGraph());

        return LinearLinkageOperator.updateIntegerGenes(splittedModule, linearLinkageEncoding);
    }

    /**
     * Returns a linear linkage encoding where a 2 randomly selected non-isolated modules are merged together in
     * {@value linearLinkageEncoding}.
     * @param linearLinkageEncoding the linear linkage encoding, which will used for randomly merging modules.
     * @return a linear linkage encoding where a 2 randomly selected non-isolated modules are merged together in
     *      * {@value linearLinkageEncoding}.
     */
    public static LinearLinkageEncoding combineRandomGroup(LinearLinkageEncoding linearLinkageEncoding) {
        var modules = linearLinkageEncoding.getModules();
        Collections.shuffle(modules);

        var firstModule = modules.get(0);

        // Only combine random groups together which are neighbor
        var neighboringModules = ModuleInformationProvider.getNeighboringModules(
                firstModule, linearLinkageEncoding);

        if (neighboringModules.isEmpty()) {
            // The random module is not connected to any module
            // Therefore, it cannot be combined with other modules

            return linearLinkageEncoding;
        }

        Collections.shuffle(neighboringModules);

        var secondModule = neighboringModules.get(0);

        var combinedModule = ModuleOperator.mergeModules(firstModule, secondModule);
        var combinedModuleList = Arrays.asList(combinedModule);

        return LinearLinkageOperator.updateIntegerGenes(combinedModuleList, linearLinkageEncoding);
    }

    /**
     * Moves the gene of an edge to a random possible incident module and
     * returns an updated linear linkage encoding. If the source and target
     * vertex of {@code edge} is in the same module, then the input
     * {@code linearLinkageEncoding} is returned.
     *
     * @param edge the edge which is moved to a different module
     * @param linearLinkageEncoding the linear linkage encoding which is used to
     *                              create the updated encoding where the edge
     *                              is moved to a different module.
     * @return the updated linear linkage encoding
     */
    public static LinearLinkageEncoding moveGeneOfEdgeToRandomPossibleModule(
            Edge edge, LinearLinkageEncoding linearLinkageEncoding) {
        var modules = linearLinkageEncoding.getModules();

        var moduleOfEdge = modules.stream()
                .filter(module -> module.isIndexInModule(edge.getIndex()))
                .findFirst()
                .get();

        var sourceVertex = edge.getSourceVertex();
        var targetVertex = edge.getTargetVertex();

        if (moduleOfEdge.isIndexInModule(sourceVertex.getIndex()) &&
                moduleOfEdge.isIndexInModule(targetVertex.getIndex())) {
            // Don't move edges that connect two vertices that are both in the
            // module of this edge.

            return linearLinkageEncoding;
        }

        // Move edge to either to the module where source or target vertex are
        // assigned to.
        var moduleOfSourceVertex = modules.stream()
                .filter(module -> module.isIndexInModule(sourceVertex.getIndex()))
                .findFirst()
                .get();

        var moduleOfTargetVertex = modules.stream()
                .filter(module -> module.isIndexInModule(targetVertex.getIndex()))
                .findFirst()
                .get();

        var randomValue = RandomRegistry.random().nextDouble();
        if (randomValue < 0.5 && !moduleOfSourceVertex.isIndexInModule(edge.getIndex())) {
            // Move edge to module of source vertex
            return moveGeneToTargetModule(linearLinkageEncoding, edge, moduleOfEdge, moduleOfSourceVertex);
        } else if (randomValue >= 0.5 && !moduleOfTargetVertex.isIndexInModule(edge.getIndex())){
            // Move edge to module of target vertex
            return moveGeneToTargetModule(linearLinkageEncoding, edge, moduleOfEdge, moduleOfTargetVertex);
        }
        return linearLinkageEncoding;
    }

    /**
     * Moves the {@code modularisableElement} from {@code sourceModule} to
     * {@code targetModule} and generates an updated LinearLinkageEncoding
     *
     * @param linearLinkageEncoding the linear linkage encoding which is used to
     *                              create the updated encoding where the
     *                              modularisable element is moved to a
     *                              different module.
     * @param modularisableElement the modularisable element which is moved to
     *                             a different module
     * @param sourceModule the source module of the {@code modularisableElement}
     * @param targetModule the target module where {@code modularisableElement}
     *
     */
    public static LinearLinkageEncoding moveGeneToTargetModule(
            LinearLinkageEncoding linearLinkageEncoding,
            ModularisableElement modularisableElement,
            Module sourceModule,
            Module targetModule) {
        sourceModule.removeIndex(modularisableElement.getIndex());
        targetModule.addIndex(modularisableElement.getIndex());

        var affectedModules = Arrays.asList(sourceModule, targetModule)
                .stream()
                .filter(module -> !module.getIndices().isEmpty())
                .collect(Collectors.toList());

        return updateIntegerGenes(affectedModules, linearLinkageEncoding);
    }

    /**
     * <p>
     *     Updates the allele values for each gene of
     *     {@code linearLinkageEncoding}, which belongs to one of the
     *     modules in {@code modules} and returns an updated linear linkage
     *     encoding with the updated allele values.
     * </p>
     *
     * <p>
     *     Furthermore, this update ensures that the linear linkage still have
     *     ascending order in each module.
     * </p>
     *
     * @param affectedModules the modules, with the updated indices
     * @param linearLinkageEncoding the encoding with genes containing the
     *                              alleles which will be updated
     * @return an updated linear linkage encoding with the updated allele
     * values.
     */
    public static LinearLinkageEncoding updateIntegerGenes(final Set<Module> affectedModules,
                                                           final LinearLinkageEncoding linearLinkageEncoding) {
        if (affectedModules.isEmpty()) {
            return linearLinkageEncoding;
        }

        final var integerGenes = linearLinkageEncoding.getIntegerGenes();
        affectedModules.stream()
                .forEach(module -> {
                    updateModule(module, integerGenes);
                });

        return new LinearLinkageEncoding(integerGenes, linearLinkageEncoding.getKnowledgeGraph());
    }

    /**
     * <p>
     *     Updates the allele values for each gene of
     *     {@code linearLinkageEncoding}, which belongs to one of the
     *     modules in {@code modules} and returns an updated linear linkage
     *     encoding with the updated allele values.
     * </p>
     *
     * <p>
     *     Furthermore, this update ensures that the linear linkage still have
     *     ascending order in each module.
     * </p>
     *
     * @param affectedModules the modules, with the updated indices
     * @param linearLinkageEncoding the encoding with genes containing the
     *                              alleles which will be updated
     * @return an updated linear linkage encoding with the updated allele
     * values.
     */
    public static LinearLinkageEncoding updateIntegerGenes(final List<Module> affectedModules,
                                                           final LinearLinkageEncoding linearLinkageEncoding) {
        if (affectedModules.isEmpty()) {
            return linearLinkageEncoding;
        }

        final var integerGenes = linearLinkageEncoding.getIntegerGenes();
        affectedModules.stream()
                .forEach(module -> {
                    updateModule(module, integerGenes);
                });

        return new LinearLinkageEncoding(integerGenes, linearLinkageEncoding.getKnowledgeGraph());
    }

    private static void updateModule(final Module module, final List<IntegerGene> integerGenes) {
        var indices = module.getIndices();
        // Determine affected integer genes
        var affectedIntegerGenes = indices.stream()
                .map(index -> integerGenes.get(index))
                .collect(Collectors.toList());

        for (int i = 0; i < affectedIntegerGenes.size() - 1; i++) {
            var integerGene = affectedIntegerGenes.get(i);

            var indexOfModule = indices.get(i);
            var successor = indices.get(i + 1);

            // Point gene to successor in module of LLE
            var updatedGene = integerGene.newInstance(successor);
            integerGenes.set(indexOfModule, updatedGene);
        }

        // Point last index to itself for ending node
        var lastAffectedIntegerGene = affectedIntegerGenes.get(affectedIntegerGenes.size() - 1);
        var updatedGene = lastAffectedIntegerGene.newInstance(indices.getLast());
        integerGenes.set(indices.getLast(), updatedGene);
    }

    public static LinearLinkageEncoding moveRandomGeneToIncidentModule(final LinearLinkageEncoding linearLinkageEncoding) {
        // Select only modules which have neighboring modules and don't consist of an isolated modularisable element
        final var modulesWithIncidentModules =
                linearLinkageEncoding.getModules()
                        .stream()
                        .filter(module -> {
                            if (ModuleInformationProvider.isIsolated(module, linearLinkageEncoding.getKnowledgeGraph())) {
                                return false;
                            }

                            return ModuleInformationProvider.getNeighboringModules(module, linearLinkageEncoding).size() > 0;
                        })
                        .collect(Collectors.toList());

        if (modulesWithIncidentModules.isEmpty()) {
            // the modules of the linear linkage encoding has not at least 2 modules where a gene from a source module
            // can be moved to any target module
            return linearLinkageEncoding;
        }

        // Randomly pick a source module from which a modularisableElement is moved to a different module
        var sourceModuleIndex = ThreadLocalRandom.current().nextInt(modulesWithIncidentModules.size());
        final var sourceModule = modulesWithIncidentModules.get(sourceModuleIndex);

        final var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();

        // Randomly pick element, which can be moved to another module i.e. it has a potential incident element, which
        // is in a different module
        final var modularisableElementsWithNeighboringModulesInDifferentModules = sourceModule.getIndices()
                .stream()
                .map(index -> knowledgeGraph.getModularisableElement(index))
                .collect(Collectors.toMap(
                        modularisableElement -> modularisableElement,
                        modularisableElement -> ModuleInformationProvider.getIncidentModules(modularisableElement, linearLinkageEncoding)));

        final var candidateModularisableElement =
                modularisableElementsWithNeighboringModulesInDifferentModules.entrySet()
                .stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(
                        entry -> entry.getKey(),
                        entry -> entry.getValue()
                ));

        // Randomly select modularisable element, which will be used for moving
        final var possibleModularisableElements = new ArrayList<>(candidateModularisableElement.keySet());
        final var randomModularisableElementKeySetIndex = ThreadLocalRandom.current().nextInt(possibleModularisableElements.size());
        final var modularisableElementToMove = possibleModularisableElements.get(randomModularisableElementKeySetIndex);

        // Determine the target modules, which the modularisable element can be moved to
        final var possibleTargetModules = candidateModularisableElement.get(modularisableElementToMove);

        // Randomly pick one target module
        final var possibleTargetModuleIndex = ThreadLocalRandom.current().nextInt(possibleTargetModules.size());
        final var targetModule = possibleTargetModules.get(possibleTargetModuleIndex);

        // Move modularisable element to random target module by removing it from the source module and adding the index
        // to the target module
        final var indexOfModularisableElementToMove = modularisableElementToMove.getIndex();
        sourceModule.removeIndex(indexOfModularisableElementToMove);
        targetModule.addIndex(indexOfModularisableElementToMove);

        final var affectedModules = new ArrayList<Module>();
        affectedModules.add(targetModule);

        // Only add source module if the module still has any indices left
        if (!sourceModule.getIndices().isEmpty()) {
            affectedModules.add(sourceModule);
        }

        // Check if the source module is still connected
        final var isSourceModeConnected = ModuleInformationProvider.isModuleConnected(sourceModule, knowledgeGraph);

        // If the module is not connected anymore, the module has to be split up
        if (!isSourceModeConnected) {
            final var splitUpSourceModules = ModuleOperator.splitUpNonConnectedModule(sourceModule, linearLinkageEncoding);
            affectedModules.addAll(splitUpSourceModules);
        }

        return updateIntegerGenes(affectedModules, linearLinkageEncoding);
    }

    /**
     * Returns an encoding where the modules are fixed s.t. that they only contain connected elements.
     * @param linearLinkageEncoding the linear linkage encoding with modules that have non connected elements
     * @return an encoding where the modules are fixed s.t. that they only contain connected elements.
     */
    public static LinearLinkageEncoding repairNonConnectedModules(final LinearLinkageEncoding linearLinkageEncoding) {
        final var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();

        // Determine modules with non connected elements
        final var modules = new ArrayList<>(linearLinkageEncoding.getModules());
        var invalidModules = modules
                .stream()
                .filter(module -> module.getIndices().size() > 1 &&
                        !ModuleInformationProvider.isModuleConnected(module, knowledgeGraph))
                .collect(Collectors.toList());

        int iterations = 0;
        // Repeat until all modules are connected and no module with unconnected elements are left
        while (!invalidModules.isEmpty()) {
            for (final var invalidModule : invalidModules) {
                // Determine non connected elements
                final var nonConnectedModularisableElements =
                        ModuleInformationProvider.getNonConnectedModularisableElements(invalidModule, knowledgeGraph);

                Module targetIncidentModule = null;
                // Move them to modules where incident elements are
                for (final var nonConnectedModularisableElement : nonConnectedModularisableElements) {
                    final var incidentModules =
                            ModuleInformationProvider.getIncidentModules(nonConnectedModularisableElement, linearLinkageEncoding);

                    if (incidentModules.isEmpty()) {
                        // Proceed if no target module has been found
                        continue;
                    }

                    final var randomIncidentModulesIndex = ThreadLocalRandom.current().nextInt(incidentModules.size());
                    targetIncidentModule = incidentModules.get(randomIncidentModulesIndex);

                    // Move index to the target module and remove it from current module
                    invalidModule.removeIndex(nonConnectedModularisableElement.getIndex());
                    targetIncidentModule.addIndex(nonConnectedModularisableElement.getIndex());

                    if (invalidModule.getIndices().isEmpty())
                        modules.remove(invalidModule);
                }
            }

            invalidModules = modules
                    .stream()
                    .filter(module -> module.getIndices().size() > 1 &&
                            !ModuleInformationProvider.isModuleConsistOfIsolatedVertex(module, knowledgeGraph) &&
                            !ModuleInformationProvider.isModuleConnected(module, knowledgeGraph))
                    .collect(Collectors.toList());

            iterations++;

            if (iterations >= COUNT_LOOP_TERMINATION) {
                // Terminate the process at it is stuck in constellation where it non connected modularisable elements
                // are moved in an endless loop. Therefore, return linear linkage encoding where each connected
                // component is in a module
                var initialLinearLinkageEncoding =
                        LinearLinkageInitialiser.initialiseLinearLinkageEncodingWithModulesForEachConnectedComponent(knowledgeGraph);

                var randomlySplitUpLinearLinkageEncoding = randomlySplitUpModules(initialLinearLinkageEncoding);

                return randomlySplitUpLinearLinkageEncoding;
            }
        }

        return LinearLinkageOperator.updateIntegerGenes(invalidModules, linearLinkageEncoding);
    }

    /**
     * Returns a linear linkage encoding where a module is randomly split up.
     * @param linearLinkageEncoding the linear linkage encoding, which will be altered.
     * @return a linear linkage encoding where a module is randomly split up.
     */
    public static LinearLinkageEncoding randomlySplitUpModules(final LinearLinkageEncoding linearLinkageEncoding) {
        var possibleModules = linearLinkageEncoding.getModules()
                .stream()
                .filter(module -> module.getIndices().size() > 1)
                .collect(Collectors.toList());

        if (possibleModules.isEmpty()) {
            return linearLinkageEncoding;
        }

        var indexOfModuleToBeSplit = ThreadLocalRandom.current().nextInt(possibleModules.size());
        var moduleToBeSplit = possibleModules.get(indexOfModuleToBeSplit);

        var splitUpModules = ModuleOperator.divideModuleRandomWalk2(moduleToBeSplit, linearLinkageEncoding.getKnowledgeGraph());

        return updateIntegerGenes(splitUpModules, linearLinkageEncoding);
    }

    /**
     * Returns a linear linkage encoding which follows the following constraints:
     * TODO: Finish describing
     * <ol>
     *     <li>Each allele value occures twice.</li>
     *     <li>All modularisable elements in a module are incident.</li>
     *     <li>There is no module, which consist only of one edge.</li>
     * </ol>
     *
     * @param linearLinkageEncoding the linear linkage to be fixed
     * @return a correct linear linkage encoding
     */
    public static LinearLinkageEncoding fixLinearLinkageEncoding(final LinearLinkageEncoding linearLinkageEncoding) {
        var repairedLinearLinkageEncoding = new LinearLinkageEncoding(linearLinkageEncoding.getIntegerGenes(), linearLinkageEncoding.getKnowledgeGraph());

        if (!LinearLinkageEncodingInformationProvider.isValidAlleleValues(repairedLinearLinkageEncoding))
            repairedLinearLinkageEncoding =
                    repairInvalidGeneAssignment(repairedLinearLinkageEncoding);

        if (!LinearLinkageEncodingInformationProvider.isAllElementsInModuleConnected(repairedLinearLinkageEncoding))
            repairedLinearLinkageEncoding =
                    repairNonConnectedModules2(repairedLinearLinkageEncoding);

        if (LinearLinkageEncodingInformationProvider.isOneModuleConsistOfOneEdge(repairedLinearLinkageEncoding))
            repairedLinearLinkageEncoding =
                    repairModulesWithOnlyOneVertexOrEdge(repairedLinearLinkageEncoding);

        if (LinearLinkageEncodingInformationProvider.isMonolith(linearLinkageEncoding)) {
            repairedLinearLinkageEncoding = randomlySplitUpModules(linearLinkageEncoding);
        }

        return repairedLinearLinkageEncoding;
    }

    /**
     * Returns a linear linkage encoding where the number of same allele is at most
     * {@value LinearLinkageConstant#MAX_NUMBER_OF_SAME_ALLELE}.
     * @param linearLinkageEncoding the linear linkage encoding to check.
     * @return a linear linkage encoding where the number of same allele is at most
     * {@value LinearLinkageConstant#MAX_NUMBER_OF_SAME_ALLELE}.
     */
    public static LinearLinkageEncoding repairInvalidGeneAssignment(final LinearLinkageEncoding linearLinkageEncoding) {
        final var interGenes = linearLinkageEncoding.getIntegerGenes();

        // Create a list of remaining allele values, which should assigned afterwards
        final var remainingUnassignedAlleles = IntStream.range(0, linearLinkageEncoding.length())
                .boxed()
                .collect(Collectors.toList());

        // Create a map to track the number of already existing allele values. At most an linear linkage encoding can
        // have up to 2 allele values
        final var alleleCountMap = new HashMap<Integer, Integer>();

        // Keep track of number of alleles and determine the remaining unused alleles
        for (final var integerGene : interGenes) {
            var allele = integerGene.allele();

            // Keep track of how many alleles the map still have
            if (alleleCountMap.containsKey(allele)) {
                var alleleCount = alleleCountMap.get(allele);
                alleleCountMap.put(allele, ++alleleCount);
            } else {
                alleleCountMap.put(allele, 1);
            }

            // Remove alleles to keep unassigned alleles for later assignment
            if (remainingUnassignedAlleles.contains(allele))
                remainingUnassignedAlleles.remove(allele);
        }

        // Create a map for the remaining unused alleles
        var remainingUnassignedAllelesMap = remainingUnassignedAlleles.stream()
                .collect(Collectors.toMap(Function.identity(), i -> 2));

        // Start assigning remaining alleles to one of invalid assigned genes i.e. allele values that are used more
        // than twice.
        final var updatedInterGenes = new ArrayList<>(interGenes);

        alleleCountMap.entrySet()
                .stream()
                .filter(alleleCountEntry -> alleleCountEntry.getValue() > LinearLinkageConstant.MAX_NUMBER_OF_SAME_ALLELE)
                .forEach(invalidAssignedAlleleEntry -> {
                    final var allele = invalidAssignedAlleleEntry.getKey();

                    // Determine the index of allele values
                    final var indicesOfOverusedAllele = IntStream.range(0, updatedInterGenes.size())
                            .boxed()
                            .filter(i -> {
                                final var integerGene = interGenes.get(i);
                                return integerGene.allele() == allele;
                            })
                            .collect(Collectors.toList());

                    // Repeat process until 2 allele values are left
                    final int countReassignments = indicesOfOverusedAllele.size() - LinearLinkageConstant.MAX_NUMBER_OF_SAME_ALLELE;
                    for (int i = 0; i < countReassignments; i++) {
                        final var remainingPossibleUnassignedAlleles = new ArrayList<>(remainingUnassignedAllelesMap.keySet());

                        // Randomly assign one of the reamining unused allele to a random integer gene that has the allele
                        // that was assigned more than twice
                        final var randomRemainingUnusedAlleleIndex = ThreadLocalRandom.current()
                                .nextInt(remainingUnassignedAllelesMap.size());
                        final var randomRemainingUnusedAllele = remainingPossibleUnassignedAlleles
                                .get(randomRemainingUnusedAlleleIndex);

                        final var randomIndexOfOverusedAllelesIndex = ThreadLocalRandom.current()
                                .nextInt(indicesOfOverusedAllele.size());
                        final var randomIndexOfOverusedAlleles = indicesOfOverusedAllele
                                .get(randomIndexOfOverusedAllelesIndex);

                        final var interGeneToBeChanged = interGenes.get(randomIndexOfOverusedAlleles);
                        final var updatedIntegerGene = interGeneToBeChanged.newInstance(randomRemainingUnusedAllele);

                        updatedInterGenes.set(randomIndexOfOverusedAlleles, updatedIntegerGene);

                        // Decrease usage of this element
                        int countRemainingUnusedAssignedAllele =
                                remainingUnassignedAllelesMap.get(randomRemainingUnusedAllele) - 1;

                        if (countRemainingUnusedAssignedAllele == 0)
                            remainingUnassignedAllelesMap.remove(randomRemainingUnusedAllele);
                        else
                            remainingUnassignedAllelesMap.put(randomRemainingUnusedAllele, countRemainingUnusedAssignedAllele);

                        indicesOfOverusedAllele.remove(randomIndexOfOverusedAlleles);
                    }
                });

        return new LinearLinkageEncoding(updatedInterGenes, linearLinkageEncoding.getKnowledgeGraph());
    }

    /**
     * Returns an encoding where non connected moduels are split up into their own connected modules.
     * @param linearLinkageEncoding the linear linkage encoding with modules that have non connected elements
     * @return an encoding where non connected moduels are split up into their own connected modules.
     */
    public static LinearLinkageEncoding repairNonConnectedModules2(final LinearLinkageEncoding linearLinkageEncoding) {
        final var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();
        final var modules = new ArrayList<>(linearLinkageEncoding.getModules());

        final var invalidModules = modules
                .stream()
                .filter(module -> module.getIndices().size() > 1 &&
                        !ModuleInformationProvider.isModuleConnected(module, knowledgeGraph))
                .collect(Collectors.toList());

        // Split up invalid modules
        final var updatedSplitUpModules = new ArrayList<Module>();

        for (final var invalidModule : invalidModules) {
            final var splitUpModules =
                    ModuleOperator.splitUpNonConnectedModule(invalidModule, linearLinkageEncoding);

            updatedSplitUpModules.addAll(splitUpModules);
        }


        return LinearLinkageOperator.updateIntegerGenes(updatedSplitUpModules, linearLinkageEncoding);
    }

    public static LinearLinkageEncoding repairModulesWithOnlyOneVertexOrEdge(final LinearLinkageEncoding linearLinkageEncoding) {
        //final var updatedModules = new HashSet<Module>();
        final var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();

        final var updatedModules = new HashSet<>(linearLinkageEncoding.getModules());

        final var invalidModules = linearLinkageEncoding.getModules()
                .stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph) &&
                        module.getIndices().size() <= 2)
                .collect(Collectors.toList());

        var invalidModulesToBeIgnored = new HashSet<Module>();

        for (Iterator<Module> iterator = invalidModules.iterator(); iterator.hasNext();) {
            var invalidModule = iterator.next();

            if (invalidModule.getIndices().size() <= 2) {
                // Merge invalid module with other module
                final var neighboringModules = ModuleInformationProvider.getNeighboringModules(
                        invalidModule, linearLinkageEncoding);

                final var randomNeighboringModuleIndex = ThreadLocalRandom.current().nextInt(neighboringModules.size());
                final var randomNeighboringModule = neighboringModules.get(randomNeighboringModuleIndex);

                final var mergedModule = ModuleOperator.mergeModules(randomNeighboringModule, invalidModule);

                // Because of the merge, it is not invalid anymore
                if (invalidModules.contains(randomNeighboringModule)) {
                    invalidModules.set(invalidModules.indexOf(randomNeighboringModule), mergedModule);
                }
                // Update modules
                updatedModules.removeAll(Arrays.asList(invalidModule, randomNeighboringModule));
                updatedModules.add(mergedModule);
            }
        }

        return LinearLinkageOperator.updateIntegerGenes(updatedModules, linearLinkageEncoding);
    }
}
