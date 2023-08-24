package com.modcmga.backendservice.domain.knowledgegraph;
/**
 * @Package: com.modcmga.poc.knowledgegraph
 * @Class: Vertex
 * @Author: Jan
 * @Date: 19.10.2021
 */

import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Contains vertex information from the parsed Knowledge Graph.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class Vertex extends ModularisableElement {

    /**
     * Incremental integer to determine the vertex. Each edge and vertex has a
     * distinct integer assigned.
     */
    @EqualsAndHashCode.Exclude
    private int vertexNumber;

    /**
     * The id of the vertex.
     */
    private String id;

    /**
     * The class name which the vertex represents.
     */
    private String className;

    /**
     * The label of the vertex which it represents.
     */
    private String label;

    /**
     * Contains shape information for Neo4j.
     */
    private String d6;

    /**
     * The name which the vertex represents.
     */
    private String name;

    /**
     * The map containing for each conceptual model related attributes the value.
     */
    private Map<String, String> conceptualModelAttributes;

    @Override
    public int getIndex() {
        return vertexNumber;
    }

    @Override
    public String toString() {
        return String.format("(Nr: %d: Label: %s)", vertexNumber, label);
    }
}
