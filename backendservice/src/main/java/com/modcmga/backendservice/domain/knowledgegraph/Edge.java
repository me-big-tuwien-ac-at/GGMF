package com.modcmga.backendservice.domain.knowledgegraph;
/**
 * @Package: com.modcmga.poc.knowledgegraph
 * @Class: Edge
 * @Author: Jan
 * @Date: 19.10.2021
 */

import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents the edge of the knowledge graph.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Edge extends ModularisableElement {

    /**
     * Incremental integer to determine the edge. Each edge and vertex has a
     * distinct integer assigned.
     */
    @EqualsAndHashCode.Exclude
    private int edgeNumber;

    /**
     * Represents the source vertex.
     */
    private Vertex sourceVertex;

    /**
     * Represents the target vertex.
     */
    private Vertex targetVertex;

    /**
     * Defines the reference name which the edge represents.
     */
    private String referenceName;

    /**
     * Specifies the label of the edge.
     */
    private String label;

    /**
     * Contains shape information for Neo4j.
     */
    private String d6;

    /**
     * The weight of the edge;
     */
    private double weight;

    @Override
    public int getIndex() {
        return edgeNumber;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, index: %s, weight: %.2f)", sourceVertex, targetVertex, edgeNumber, weight);
    }
}
