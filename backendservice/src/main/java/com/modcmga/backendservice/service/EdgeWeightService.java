package com.modcmga.backendservice.service;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: EdgeWeightService
 * @Author: Jan
 * @Date: 16.05.2022
 */

import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;

import java.util.Map;

/**
 * Assigns each edge the passed weight.
 */
public interface EdgeWeightService {
    /**
     * Assigns the edge weight to each edge of the knowledge graph.
     * @param knowledgeGraph The knowledge graph.
     * @param edgeWeights The weights which will be assigned to each edge which
     *                    has the name as a key of the map.
     */
    void assignEdgeWeight(KnowledgeGraph knowledgeGraph, Map<String, Double> edgeWeights);
}
