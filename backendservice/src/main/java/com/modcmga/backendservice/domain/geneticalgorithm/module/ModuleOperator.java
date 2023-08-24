package com.modcmga.backendservice.domain.geneticalgorithm.module;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.module
 * @Class: ModuleOperator
 * @Author: Jan
 * @Date: 14.08.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraphUtil;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Provides methods, which alters the content of a modules.
 */
public final class ModuleOperator{

    /**
     * Returns a new module where the indices of {@code module1} and
     * {@code module1} are combined and the combined indices are sorted in
     * ascending order.
     *
     * @param module1 the first module, which is used for merging.
     * @param module2 the second module, which is used for merging.
     * @return the module containing the indices of @code module1} and
     * {@code module1} in an ascending order.
     */
    public static Module mergeModules(Module module1, Module module2) {
        var indices1 = module1.getIndices();
        var indices2 = module2.getIndices();

        var combinedModule = new Module();

        int i = 0;
        int k = 0;

        boolean isDone = false;
        while(!isDone) {
            if (i < indices1.size() && k < indices2.size()) {
                if (indices1.get(i) < indices2.get(k)) {
                    combinedModule.addIndex(indices1.get(i));

                    i++;
                } else {
                    combinedModule.addIndex(indices2.get(k));

                    k++;
                }
            } else {
                for (; i < indices1.size(); i++) {
                    combinedModule.addIndex(indices1.get(i));
                }

                for (; k < indices2.size(); k++) {
                    combinedModule.addIndex(indices2.get(k));
                }
                isDone = true;
            }
        }

        return combinedModule;
    }

    public static Set<Module> divideModuleRandomWalk2(final Module module, final KnowledgeGraph knowledgeGraph) {
        final var splittedModules = new HashSet<Module>();

        final var indices = new ArrayList<>(module.getIndices());
        if (indices.size() == 1) {
            splittedModules.add(module);

            return splittedModules;
        } else if (indices.size() == 2) {
            final var module1 = new Module();
            module1.addIndex(indices.get(0));

            final var module2 = new Module();
            module2.addIndex(indices.get(1));

            return new HashSet<>(Arrays.asList(module1, module2));
        }

        final var randomSizeOfModule1 = indices.size() / 2; //determineSizeOfSplittedModule(indices);
        final var remainingIndices = new ArrayList<>(module.getIndices());

        while (!remainingIndices.isEmpty()) {
            final var randomStartElementIndex = ThreadLocalRandom.current().nextInt(remainingIndices.size());
            final var startElement = knowledgeGraph.getModularisableElement(remainingIndices.get(randomStartElementIndex));

            final var indicesOfSplittedModule = createIndicesOfSubGraphRandomWalk(startElement, knowledgeGraph, randomSizeOfModule1, remainingIndices);

            Module newModule = new Module();
            newModule.addIndices(indicesOfSplittedModule);
            splittedModules.add(newModule);

            remainingIndices.removeAll(indicesOfSplittedModule);
        }

        return splittedModules;
    }

    private static int determineSizeOfSplittedModule(final List<Integer> indices) {
        if (indices.size() > 4) {
            final var quarterUpperBound = indices.size() / 4;
            final var upperBound = quarterUpperBound >= 2 ? quarterUpperBound : 2;
            return ThreadLocalRandom.current().nextInt(1, upperBound) * 2 - 1 ;
        }

        return ThreadLocalRandom.current().nextInt(1, 2);
    }

    private static Set<Integer> createIndicesOfSubGraphRandomWalk(
            final ModularisableElement startElement, final KnowledgeGraph knowledgeGraph, final int subGraphSize,
            final List<Integer> indicesOfModule) {
        final var selectedIndices = new HashSet<Integer>();
        final var visitedModularisableElement = new HashSet<ModularisableElement>();

        // Put first element to list
        final var queue = new Stack<ModularisableElement>();
        queue.push(startElement);

        while (!queue.empty() && selectedIndices.size() < subGraphSize) {
            final var currentElement = queue.pop();
            selectedIndices.add(currentElement.getIndex());

            // TODO: remove visited index from indicesOfModule to
            if (currentElement instanceof Vertex) {
                final var currentVertex = (Vertex) currentElement;

                // Determine all possible incident edges, which are assigned to the current module, for potential path
                final var incidentEdges =
                        knowledgeGraph.getGraph().edgesOf(currentVertex)
                                .stream()
                                .filter(edge ->
                                        indicesOfModule.contains(edge.getIndex()) &&
                                                !visitedModularisableElement.contains(edge))
                                .collect(Collectors.toList());
                Collections.shuffle(incidentEdges);

                incidentEdges.stream()
                        .forEach(incidentEdge -> queue.push(incidentEdge));
            } else if (currentElement instanceof Edge) {
                final var currentEdge = (Edge) currentElement;
                final var sourceVertex = currentEdge.getSourceVertex();
                final var targetVertex = currentEdge.getTargetVertex();

                // Add either the source or target vertex as the next element as a target for the random walk
                if (indicesOfModule.contains(currentEdge.getIndex()) &&
                        indicesOfModule.contains(sourceVertex.getIndex()) &&
                        !visitedModularisableElement.contains(sourceVertex)) {
                    queue.push(currentEdge.getSourceVertex());
                }
                if (indicesOfModule.contains(currentEdge.getIndex()) &&
                        indicesOfModule.contains(targetVertex.getIndex()) &&
                        !visitedModularisableElement.contains(targetVertex)) {
                    queue.push(currentEdge.getTargetVertex());
                }
            }

            // Add the current element to list to prevent cycles in the random walk
            visitedModularisableElement.add(currentElement);
        }

        return selectedIndices;
    }

    /**
     * Returns a list of modules, after {@code nonConnectedModule} is split up. {@code nonConnectedModule} contains
     * modularisable elements, which are not connected anymore
     * @param nonConnectedModule the module where some modularisable elements are not connected.
     * @param linearLinkageEncoding the linear linkage to use information from to determine the split up modules
     * @return a list of modules, after {@code nonConnectedModule} is split up.
     */
    public static List<Module> splitUpNonConnectedModule(
            final Module nonConnectedModule, final LinearLinkageEncoding linearLinkageEncoding) {
        final KnowledgeGraph knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();
        final var subGraphOfModule = KnowledgeGraphUtil.createSubGraphFromIndices(
                nonConnectedModule.getIndices(), knowledgeGraph);

        // Determine the connected components
        final var connectivityInspector = new ConnectivityInspector<>(subGraphOfModule);
        final var connectedSets = connectivityInspector.connectedSets();

        final var indicesOfConnectedSets = new ArrayList<List<Integer>>();

        // Determine indices of connected components containing vertices and incident boundary edges
        final var edgesOfModule = ModuleInformationProvider.getModuleEdges(nonConnectedModule, linearLinkageEncoding.getKnowledgeGraph());

        for (final var verticesOfConnectedSet : connectedSets) {
            // Determine all edges where the edge is assigned to this set and either source and target vertex are
            // assigned to this module too or the edge is a boundary edge.
            final var edgesOfConnectedSet = edgesOfModule.stream()
                    .filter(edge -> verticesOfConnectedSet.contains(edge.getSourceVertex()) || verticesOfConnectedSet.contains(edge.getTargetVertex()))
                    .collect(Collectors.toList());

            var indexOfConnectedSet = new ArrayList<Integer>();

            verticesOfConnectedSet.stream()
                    .forEach(vertex -> indexOfConnectedSet.add(vertex.getIndex()));

            edgesOfConnectedSet.stream()
                    .forEach(edge -> indexOfConnectedSet.add(edge.getIndex()));

            indicesOfConnectedSets.add(indexOfConnectedSet);
        }

        // Add index of edge, which is not incident to any vertex of the connected
        edgesOfModule.stream()
                .filter(edge -> !connectedSets.contains(edge.getSourceVertex()) && !connectedSets.contains(edge.getTargetVertex()))
                .forEach(edge -> {
                    var indexOfConnectedSet = new ArrayList<Integer>();

                    indexOfConnectedSet.add(edge.getIndex());

                    indicesOfConnectedSets.add(indexOfConnectedSet);
                });

        // Create split up modules
        return indicesOfConnectedSets.stream()
                .map(indexOfConnectedSet -> {
                    Module module = new Module();

                    indexOfConnectedSet.stream()
                            .forEach(index -> module.addIndex(index));

                    return module;
                })
                .collect(Collectors.toList());
    }
}
