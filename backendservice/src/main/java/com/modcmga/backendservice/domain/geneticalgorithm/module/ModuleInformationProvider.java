package com.modcmga.backendservice.domain.geneticalgorithm.module;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.module
 * @Class: ModuleInformationProvider
 * @Author: Jan
 * @Date: 13.08.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraphUtil;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides methods that derives information from the module
 */
public final class ModuleInformationProvider {
    /**
     * Returns the set of vertices assigned to {@code module}.
     * @param module the module to determine the vertices from
     * @param knowledgeGraph the knowledge graph to determine edges.
     * @return the set of vertices assigned to {@code module}.
     */
    public static Set<Vertex> getVerticesOfModule(Module module, KnowledgeGraph knowledgeGraph) {
        return module.getIndices()
                .stream()
                .map(i -> knowledgeGraph.getModularisableElement(i))
                .filter(modularisableElement -> modularisableElement instanceof Vertex)
                .map(modularisableElement -> (Vertex) modularisableElement)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the assigned module of {@code modularisableElement}.
     * @param modularisableElement the modularisable element for which the assigned module is determined
     * @param linearLinkageEncoding the linear linkage encoding, which is used to determine the module
     * @return
     */
    public static Module getModule(ModularisableElement modularisableElement, LinearLinkageEncoding linearLinkageEncoding) {
        return linearLinkageEncoding.getModules()
                .stream()
                .filter(module -> module.isIndexInModule(modularisableElement.getIndex()))
                .findFirst()
                .get();
    }

    /**
     * Returns the list of neighboring modules for this module. A module is a
     * neighbour when in this module there is a vertex and in the other module
     * is an incident edge or when in this module there is a edge, which is
     * incident to the vertex in this module.
     * @param linearLinkageEncoding The linear linkage encoding used to determine the neighboring moduels
     * @return the list of neighboring modules for this module.
     */
    public static List<Module> getNeighboringModules(Module module, LinearLinkageEncoding linearLinkageEncoding) {
        var remainingModules = new ArrayList<>(linearLinkageEncoding.getModules());

        var neighboringModuleWhereModuleVerticesAreIncidentToOtherModuleEdge =
                getNeighboringModuleWhereModuleVerticesAreIncidentToOtherModuleEdge(
                        module, linearLinkageEncoding.getKnowledgeGraph(), remainingModules);
        remainingModules.removeAll(neighboringModuleWhereModuleVerticesAreIncidentToOtherModuleEdge);

        var neighboringModuleWhereModuleEdgesAreIncidentToOtherModuleVertex =
                getNeighboringModuleWhereModuleEdgesAreIncidentToOtherModuleVertex(
                        module, linearLinkageEncoding.getKnowledgeGraph(), remainingModules);

        var neighboringModules = new ArrayList<>(neighboringModuleWhereModuleVerticesAreIncidentToOtherModuleEdge);
        neighboringModules.addAll(neighboringModuleWhereModuleEdgesAreIncidentToOtherModuleVertex);

        if (neighboringModules.contains(module)) {
            neighboringModules.remove(module);
        }

        return neighboringModules;
    }

    private static List<Module> getNeighboringModuleWhereModuleVerticesAreIncidentToOtherModuleEdge(Module investigatedModule, KnowledgeGraph knowledgeGraph, List<Module> modules) {
        var neighboringModule = new ArrayList<Module>();
        for (var module : modules) {
            var edgesInOtherModule = module.getIndices()
                    .stream()
                    .map(index -> knowledgeGraph.getModularisableElement(index))
                    .filter(Edge.class::isInstance)
                    .map(Edge.class::cast)
                    .collect(Collectors.toList());

            for (var edgeInOtherModule : edgesInOtherModule) {
                var sourceVertex = edgeInOtherModule.getSourceVertex();
                var targetVertex = edgeInOtherModule.getTargetVertex();

                if (investigatedModule.isIndexInModule(sourceVertex.getIndex()) ||
                        investigatedModule.isIndexInModule(targetVertex.getIndex())) {
                    neighboringModule.add(module);
                    break;
                }
            }
        }

        return neighboringModule;
    }

    private static List<Module> getNeighboringModuleWhereModuleEdgesAreIncidentToOtherModuleVertex(Module investigatedModule, KnowledgeGraph knowledgeGraph, List<Module> modules) {
        var neighboringModule = new ArrayList<Module>();

        var edgesInThisModule = investigatedModule.getIndices()
                .stream()
                .map(index -> knowledgeGraph.getModularisableElement(index))
                .filter(Edge.class::isInstance)
                .map(Edge.class::cast)
                .collect(Collectors.toList());

        for (var module : modules) {
            var verticesInOtherModule = module.getIndices()
                    .stream()
                    .map(index -> knowledgeGraph.getModularisableElement(index))
                    .filter(Vertex.class::isInstance)
                    .map(Vertex.class::cast)
                    .collect(Collectors.toList());

            for (var edgeInThisModule : edgesInThisModule) {
                var sourceVertex = edgeInThisModule.getSourceVertex();
                var targetVertex = edgeInThisModule.getTargetVertex();

                if (verticesInOtherModule.contains(sourceVertex) || verticesInOtherModule.contains(targetVertex)) {
                    neighboringModule.add(module);
                    break;
                }
            }
        }

        return neighboringModule;
    }

    /**
     * Returns true, if the module contains only 1 vertex which has degree 0 i.e.
     * it does not have any edges connected to it.
     * @param knowledgeGraph The knowledge graph
     * @return true, if the module contains only 1 vertex which has degree 0.
     */
    public static boolean isModuleConsistOfIsolatedVertex(Module module, KnowledgeGraph knowledgeGraph) {
        var indices = module.getIndices();

        if (indices.size() > 1) {
            return false;
        }

        var modularisableElement = knowledgeGraph.getModularisableElement(indices.get(0));

        if (modularisableElement instanceof Vertex) {
            return knowledgeGraph.isIsolated((Vertex) modularisableElement);
        }

        return false;
    }

    /**
     * Returns true, if all vertices in the module are connected by an edge.
     * @param knowledgeGraph The knowledge graph
     * @return true, if all vertices in the module are connected by an edge.
     */
    public static boolean isModuleConnected(final Module module, final KnowledgeGraph knowledgeGraph) {
        return isModuleConnected(module.getIndices(), knowledgeGraph);
    }

    /**
     * Returns true, if the subgraph containing all vertices in {@code indices} and the edges where the source and
     * target vertices are in the vertices and the remaining edges where either the source or target vertices are
     * incident to one of the vertices in {@code indices}.
     *
     * @param indices The indices of the module to check for
     * @return true, if all vertices in the module are connected by an edge.
     */
    public static boolean isModuleConnected(final List<Integer> indices, final KnowledgeGraph knowledgeGraph) {
        final var subGraph =
                KnowledgeGraphUtil.createSubGraphFromIndices(indices, knowledgeGraph, false);

        final var connectivityInspector = new ConnectivityInspector<>(subGraph);

        if (!connectivityInspector.isConnected())
            return false;

        final var remainingEdges = indices.stream()
                .map(i -> knowledgeGraph.getModularisableElement(i))
                .filter(modularisableElement -> {
                    if (!(modularisableElement instanceof Edge)) {
                        return false;
                    }
                    var edge = (Edge) modularisableElement;
                    return !subGraph.edgeSet().contains(edge);
                })
                .map(modularisableElement -> (Edge) modularisableElement)
                .collect(Collectors.toList());

        // Check if all edges are incident to any vertex in the subgraph
        return remainingEdges.stream()
                .allMatch(edge -> subGraph.vertexSet().contains(edge.getSourceVertex()) ||
                        subGraph.vertexSet().contains(edge.getTargetVertex()));
    }

    /**
     * Returns the list of edges which are incident to 2 vertices in the module.
     * @param knowledgeGraph The Knowledge Graph which the edges are used from
     * @return the list of edges which are incident to only one of the vertices
     * in the module.
     */
    public static List<Edge> getModuleEdges(final Module module, final KnowledgeGraph knowledgeGraph) {
        final var indices = module.getIndices();

        return knowledgeGraph.getEdges()
                .stream()
                .filter(edge -> module.isIndexInModule(edge.getIndex()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of modularisable elements.
     * @param knowledgeGraph The knowledge graph
     * @return the list of modularisable elements.
     */
    public static List<ModularisableElement> getModularisableElements(Module module, KnowledgeGraph knowledgeGraph) {
        var indices = module.getIndices();

        return indices
                .stream()
                .map(index -> knowledgeGraph.getModularisableElement(index))
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of all edges which are incident to only one of the vertices between the modules.
     * @param modules The list of modules from where the boundary edges are determined from.
     * @param knowledgeGraph The Knowledge Graph which the edges are used from
     * @return the list of all edges which are incident to only one of the vertices between the modules.
     */
    public static Set<Edge> getAllBoundaryEdges(final List<Module> modules, final KnowledgeGraph knowledgeGraph) {
        var boundaryEdges = new HashSet<Edge>();

        modules.stream()
                .forEach(module -> {
                    var boundaryEdgesOfModule = getBoundaryEdges(module, knowledgeGraph);

                    boundaryEdges.addAll(boundaryEdgesOfModule);
                });

        return boundaryEdges;
    }

    /**
     * Returns the list of edges which are incident to only one of the vertices in the module.
     * @param module The module from where the boundary edges are determined from.
     * @param knowledgeGraph The Knowledge Graph which the edges are used from
     * @return the list of edges which are incident to only one of the vertices in the module.
     */
    public static Set<Edge> getBoundaryEdges(final Module module, final KnowledgeGraph knowledgeGraph) {
        return knowledgeGraph.getEdges().stream()
                .filter(edge -> {
                    if (!module.isIndexInModule(edge.getIndex())) {
                        return false;
                    }

                    var containsSourceVertex = module.isIndexInModule(edge.getSourceVertex().getIndex());
                    var containsTargetVertex = module.isIndexInModule(edge.getTargetVertex().getIndex());

                    return containsSourceVertex ^ containsTargetVertex;
                })
                .collect(Collectors.toSet());
    }

    /**
     * Returns the list of incident modules to {@code modularisableElement}, which does not include the module, where
     * {@code modularisableElement} is assigned to is not included.
     * @param vertex the vertex which is used to determine the list of modules
     * @param linearLinkageEncoding the linear linkage encoding used to determine the incident modules
     *                       the incident elements.
     * @return the list of incident modules to vertex {@code vertex}
     */
    public static List<Module> getIncidentModules(
            ModularisableElement modularisableElement, LinearLinkageEncoding linearLinkageEncoding) {
        if (modularisableElement instanceof Vertex) {
            return ModuleInformationProvider.getIncidentModulesToVertex(
                    (Vertex) modularisableElement, linearLinkageEncoding);

        }

        return getIncidentModulesToEdge(
                    (Edge) modularisableElement, linearLinkageEncoding);
    }

    private static List<Module> getIncidentModulesToVertex(
            Vertex vertex, LinearLinkageEncoding linearLinkageEncoding) {
        var knowledgeGraph = linearLinkageEncoding.getKnowledgeGraph();
        var incidentEdges = knowledgeGraph.getGraph().edgesOf(vertex);
        var modules = linearLinkageEncoding.getModules();

        return incidentEdges.stream()
                .map(edge ->
                        modules.stream()
                                .filter(module ->
                                        !module.isIndexInModule(vertex.getIndex()) &&
                                        module.isIndexInModule(edge.getIndex()))
                                .findFirst()
                                .orElse(null)
                )
                .filter(module -> module != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private static List<Module> getIncidentModulesToEdge(Edge edge, LinearLinkageEncoding linearLinkageEncoding) {
        var moduleOfEdge = getModule(edge, linearLinkageEncoding);
        var moduleOfSourceVertex = getModule(edge.getSourceVertex(), linearLinkageEncoding);
        var moduleOfTargetVertex = getModule(edge.getTargetVertex(), linearLinkageEncoding);

        var modules = new ArrayList<Module>();

        // Add only module to list if it is not the same assigned module of the edge
        if (moduleOfSourceVertex != moduleOfEdge)
            modules.add(moduleOfSourceVertex);

        if (moduleOfTargetVertex != moduleOfEdge && !modules.contains(moduleOfTargetVertex))
            modules.add(moduleOfTargetVertex);

        return modules;
    }

    /**
     * Returns the list of modularisable elements, which are not connected.
     * @param module the module to determine the non connected elements
     * @param knowledgeGraph the knowledge graph, which is used to determine non connection.
     * @return the list of modularisable elements, which are not connected.
     */
    public static List<ModularisableElement> getNonConnectedModularisableElements(
            final Module module, final KnowledgeGraph knowledgeGraph) {
        final var nonConnectedModularisableElement = new ArrayList<ModularisableElement>();

        final var modularisableElements = module.getIndices()
                .stream()
                .map(i -> knowledgeGraph.getModularisableElement(i))
                .collect(Collectors.toList());

        final var verticesOfModule = modularisableElements
                .stream()
                .filter(modularisableElement -> modularisableElement instanceof Vertex)
                .map(modularisableElement -> (Vertex) modularisableElement)
                .collect(Collectors.toList());

        final var edgesOfModule = modularisableElements
                .stream()
                .filter(modularisableElement -> modularisableElement instanceof Edge)
                .map(modularisableElement -> (Edge) modularisableElement)
                .collect(Collectors.toList());

        // Add non incident vertex i.e. vertices that don't have any incident edge
        verticesOfModule.stream()
                .filter(vertex -> {
                    var isVertexIncidentToEdgeOfModule = edgesOfModule
                            .stream()
                            .anyMatch(edge -> edge.getSourceVertex().equals(vertex) ||
                                    edge.getTargetVertex().equals(vertex));
                    return !isVertexIncidentToEdgeOfModule;
                })
                .forEach(nonConnectedVertex -> nonConnectedModularisableElement.add(nonConnectedVertex));

        edgesOfModule.stream()
                .filter(edge ->
                        !verticesOfModule.contains(edge.getSourceVertex()) && !verticesOfModule.contains(edge.getTargetVertex()))
                .forEach(nonConnectedEdge -> nonConnectedModularisableElement.add(nonConnectedEdge));

        return nonConnectedModularisableElement;
    }

    /**
     * Returns true, if the module contains only an isolated modularisable element.
     *
     * <p>
     *     It assumes that a module only contains modularisable elements, which only non-isolated elements or only
     *     one isolated modularisable element. Thus, when a module contains more than one element, then it cannot be
     *     be isolated. Only vertices can be isolated as they don't need to be incident to edges.
     * </p>
     * @param knowledgeGraph the knowledge graph to retrieve the modularisable element
     * @return true, if the module contains only an isolated modularisable element.
     */
    public static boolean isIsolated(final Module module, final KnowledgeGraph knowledgeGraph) {
        var indices = module.getIndices();

        if (indices.size() > 1) {
            // Assume, when a module is non-isolated, it is connected and has at leaset 2 indices.
            return false;
        }

        var modularisableElement = knowledgeGraph.getModularisableElement(indices.get(0));

        if (modularisableElement instanceof Edge) {
            return false;
        }
        var vertex = (Vertex) modularisableElement;

        return knowledgeGraph.isIsolated(vertex);
    }
}
