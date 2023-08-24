package com.modcmga.backendservice.domain.knowledgegraph;
/**
 * @Package: com.modcmga.backendservice.model.knowledgegraph
 * @Class: AdjancencyMatrix
 * @Author: Jan
 * @Date: 17.12.2021
 */

import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import org.jgrapht.Graph;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the n x n adjacency matrix of the passed graph G = (V,E) where n is
 * |V|. It encapsulates methods for accessing certain edges.
 */
public class KnowledgeGraphUtil {
    /**
     * Creates a directed graph.
     * @return a directed graph.
     */
    public static Graph<Vertex, Edge> createDirectedGraph() {
        return GraphTypeBuilder
                .<Vertex, Edge> directed()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(true)
                .weighted(true)
                .buildGraph();
    }

    /**
     * Creates a undirected graph.
     * @return a undirected graph.
     */
    public static Graph<Vertex, Edge> createUndirectedGraph() {
        return GraphTypeBuilder
                .<Vertex, Edge> undirected()
                .allowingMultipleEdges(true)
                .allowingSelfLoops(true)
                .weighted(true)
                .buildGraph();
    }

    /**
     * Creates a subgraph from {@code knowledgeGraph} using only the vertices and
     * edges with the selected indices from {@code indices}.
     * @param indices the selected indices
     * @param knowledgeGraph the knowledge graph, which is used to create the subgraph
     * @return a subgraph with only elements with the selected indices
     */
    public static Graph<Vertex, Edge> createSubGraphFromIndices(final List<Integer> indices, final KnowledgeGraph knowledgeGraph) {
        return createSubGraphFromIndices(indices, knowledgeGraph, knowledgeGraph.isDirectedGraph());
    }

    /**
     * Creates a subgraph from {@code knowledgeGraph} using only the vertices and edges with the selected indices from
     * {@code indices}.
     * @param indices the indices of the modularisable elements of the subgraph
     * @param knowledgeGraph the knowledge graph to create the subgraph from
     * @param isDirectedGraph the flag that defines if the final sub graph should be directed
     * @return a subgraph with only elements with the selected indices
     */
    public static Graph<Vertex, Edge> createSubGraphFromIndices(
            final List<Integer> indices, final KnowledgeGraph knowledgeGraph, final boolean isDirectedGraph) {
        final var modularisableElements = indices.stream()
                .map(index -> knowledgeGraph.getModularisableElement(index))
                .collect(Collectors.toList());
        final var vertices = modularisableElements.stream()
                .filter(modularisableElement -> modularisableElement instanceof Vertex)
                .map(modularisableElement -> (Vertex) modularisableElement)
                .collect(Collectors.toList());
        final var edges = modularisableElements.stream()
                .filter(modularisableElement -> modularisableElement instanceof Edge)
                .map(modularisableElement -> (Edge) modularisableElement)
                .filter(edge -> vertices.contains(edge.getSourceVertex()) && vertices.contains(edge.getTargetVertex()))
                .collect(Collectors.toList());

        final var subGraph = isDirectedGraph ? createDirectedGraph() : createUndirectedGraph();

        vertices.stream()
                .forEach(vertex -> subGraph.addVertex(vertex));

        edges.stream()
                .forEach(edge -> subGraph.addEdge(edge.getSourceVertex(), edge.getTargetVertex(), edge));

        return subGraph;
    }

    /**
     * Returns the list of incident modularisable elements.
     * @param modularisableElement the modularisable element to determine the incident modularisable element.
     * @param knowledgeGraph the knowledge graph to determin incidenet modularisable element.
     * @return the list of incident modularisable elements.
     */
    public static List<ModularisableElement> getIncidentModularisableElement(
            final ModularisableElement modularisableElement, final KnowledgeGraph knowledgeGraph) {
        final var modularisableElements = new ArrayList<ModularisableElement>();

        if (modularisableElement instanceof Edge) {
            final var edge = (Edge) modularisableElement;

            modularisableElements.add(edge.getTargetVertex());
            modularisableElements.add(edge.getTargetVertex());
        } else {
            final var vertex = (Vertex) modularisableElement;

            var incidentEdges = new HashSet<Edge>();
            incidentEdges.addAll(knowledgeGraph.getGraph().incomingEdgesOf(vertex));
            incidentEdges.addAll(knowledgeGraph.getGraph().outgoingEdgesOf(vertex));

            incidentEdges.stream()
                    .forEach(edge -> modularisableElements.add(edge));
        }

        return modularisableElements;
    }
}
