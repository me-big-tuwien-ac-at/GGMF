package com.modcmga.backendservice.domain.knowledgegraph;
/**
 * @Package: com.modcmga.poc.knowledgegraph
 * @Class: KnowledgeGraph
 * @Author: Jan
 * @Date: 19.10.2021
 */

import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.Graph;

import java.util.*;
import java.util.stream.Collectors;

import static com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraphUtil.createDirectedGraph;
import static com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraphUtil.createUndirectedGraph;

/**
 *
 */
@Getter
@Setter
public class KnowledgeGraph {
    private final boolean isDirectedGraph;
    private final String id;
    private final Graph<Vertex, Edge> graph;
    private final Map<Integer, Vertex> verticesMap;
    private final Map<Integer, Edge> edgesMap;
    private final Map<ModularisableElement, List<ModularisableElement>> incidentModularisableElements;

    public KnowledgeGraph(final boolean isDirectedGraph, final String id) {
        this.isDirectedGraph = isDirectedGraph;
        this.id = id;
        this.verticesMap = new HashMap();
        this.edgesMap = new HashMap<>();
        this.incidentModularisableElements = new HashMap<>();

        this.graph = isDirectedGraph ?
                createDirectedGraph() :
                createUndirectedGraph();
    }

    /**
     * Adds {@code vertex} to the knowledge graph.
     *
     * @param vertex the vertex to be added
     */
    public void addVertex(final Vertex vertex) {
        this.graph.addVertex(vertex);

        this.verticesMap.put(vertex.getIndex(), vertex);
    }

    /**
     * Adds {@code edge} to the knowledge graph.
     *
     * @param edge the vertex to be added
     */
    public void addEdge(final Edge edge) {
        this.graph.addEdge(edge.getSourceVertex(), edge.getTargetVertex(), edge);

        graph.setEdgeWeight(edge, edge.getWeight());

        this.edgesMap.put(edge.getIndex(), edge);
    }

    /**
     * Returns all vertices of the knowledge graph.
     * @return all vertices of the knowledge graph.
     */
    public Set<Vertex> getVertices() {
        return graph.vertexSet();
    }

    /**
     * Returns all edges of the knowledge graph.
     * @return all edges of the knowledge graph.
     */
    public Set<Edge> getEdges() {
        return graph.edgeSet();
    }

    /**
     * Returns true if the {@code vertex} has a vertex degree 0 i.e. does not
     * have any incident edges.
     * @param vertex the checked vertex
     * @return true if the {@code vertex} has a vertex degree
     */
    public boolean isIsolated(Vertex vertex) {
        return graph.degreeOf(vertex) == 0;
    }

    public void setWeight(Edge edge, double weight) {
        graph.setEdgeWeight(edge, weight);
    }

    /**
     * Returns the list of all modularisable elements of the knowledge graph.
     * @return the list of all modularisable elements of the knowledge graph.
     */
    public List<ModularisableElement> getModularisableElements() {
        final var modularisableElements = new ArrayList<ModularisableElement>();

        graph.vertexSet().stream()
                .map(vertex -> modularisableElements.add(vertex))
                .collect(Collectors.toList());

        graph.edgeSet().stream()
                .map(edge -> modularisableElements.add(edge))
                .collect(Collectors.toList());

        return modularisableElements;
    }

    /**
     * Returns the modularisable element with index {@code index}.
     * @param index the index of the element.
     * @return the modularisable element with index {@code index}.
     */
    public ModularisableElement getModularisableElement(int index) {
        if (verticesMap.containsKey(index)) {
            return verticesMap.get(index);
        }

        if (edgesMap.containsKey(index)) {
            return edgesMap.get(index);
        }

        throw new NoSuchElementException(String.format("The modularisable element with index %d was not found", index));
    }

    /**
     * Returns the list of incident modularisable elements for either a {@link Vertex} or an {@link Vertex}.
     * @param modularisableElement the modularisable element to determine the incident modules.
     * @return the list of incident modularisable elements for either a {@link Vertex} or an {@link Vertex}.
     */
    public List<ModularisableElement> getIncidentModularisableElements(final ModularisableElement modularisableElement) {
        return incidentModularisableElements.merge(
                modularisableElement,
                KnowledgeGraphUtil.getIncidentModularisableElement(modularisableElement, this),
                (incidentElements, newIncidentElements) -> incidentElements);
    }
}
