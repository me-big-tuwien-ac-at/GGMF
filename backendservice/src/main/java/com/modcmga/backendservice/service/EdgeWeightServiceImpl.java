package com.modcmga.backendservice.service;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: EdgeWeightServiceImpl
 * @Author: Jan
 * @Date: 16.05.2022
 */

import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class EdgeWeightServiceImpl implements EdgeWeightService {
    @Override
    public void assignEdgeWeight(final KnowledgeGraph knowledgeGraph, final Map<String, Double> edgeWeights) {
        final var labelTypes = knowledgeGraph.getEdges()
                .stream().map(edge -> edge.getLabel())
                .distinct()
                .collect(Collectors.toList());

        final var edgeWeightsForKnowledgeGraph = edgeWeights.entrySet().stream()
                .filter(edgeWeight -> labelTypes.contains(edgeWeight.getKey()))
                .collect(Collectors.toList());

        knowledgeGraph.getEdges()
                .stream()
                .forEach(edge -> {
                    final var edgeWeightForEdge =
                            edgeWeightsForKnowledgeGraph.stream()
                                    .filter(edgeWeight ->
                                            edgeWeight.getKey().equals(edge.getLabel()))
                                    .findAny()
                                    .orElse(null);

                    if (edgeWeightForEdge != null) {
                        knowledgeGraph.setWeight(edge, edgeWeightForEdge.getValue());
                    }
                });
    }
}
