package com.modcmga.backendservice.domain.geneticalgorithm.encoding;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import io.jenetics.IntegerGene;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Provides methods for initialising linear linkage encodings
 */
public final class LinearLinkageInitialiser {

    /**
     * Returns a linear linkage encoding, which contains modules for each connected components.
     * @param knowledgeGraph the knowledge graph to determine the linear linkage encoding-
     * @return a linear linkage encoding, which contains modules for each connected components.
     */
    public static LinearLinkageEncoding initialiseLinearLinkageEncodingWithModulesForEachConnectedComponent(
            final KnowledgeGraph knowledgeGraph) {
        var connectivityInspector = new ConnectivityInspector<>(knowledgeGraph.getGraph());

        var connectedComponents = connectivityInspector.connectedSets();

        // Create a module for each connected component
        var modules = new ArrayList<Module>();
        for (var verticesOfConnectedComponent : connectedComponents) {
            Module module = new Module();

            var edgesOfConnectedComponent = knowledgeGraph.getEdges()
                    .stream()
                    .filter(edge -> verticesOfConnectedComponent.contains(edge.getSourceVertex()) ||
                            verticesOfConnectedComponent.contains(edge.getTargetVertex()))
                    .distinct()
                    .collect(Collectors.toList());

            verticesOfConnectedComponent.stream()
                    .forEach(vertex -> module.addIndex(vertex.getIndex()));

            edgesOfConnectedComponent.stream()
                    .forEach(edge -> module.addIndex(edge.getIndex()));

            modules.add(module);
        }

        // Create chromosome with list of modules
        final var modularisableElementSize = knowledgeGraph.getModularisableElements().size();
        final var integerGenes = IntStream.range(0, modularisableElementSize)
                .mapToObj(i -> IntegerGene.of(0, modularisableElementSize - 1))
                .collect(Collectors.toList());

        return LinearLinkageOperator.updateIntegerGenes(
                modules, new LinearLinkageEncoding(integerGenes, knowledgeGraph));
    }
}
