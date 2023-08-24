package com.modcmga.backendservice.model.conceptualmodel;
/**
 * @Package: com.modcmga.poc.model
 * @Class: ConceptualModelMetaData
 * @Author: Jan
 * @Date: 18.10.2021
 */

import lombok.Data;

import java.util.Map;

/**
 * Provides additional metadata of the metamodel.
 */
@Data
public class ConceptualModelData {
    /**
     * Represents the metamodel used for the conceptual model.
     */
    private String metaModelType;

    /**
     * Defines the export type of the graphML export type.
     */
    private String conceptualModelType;

    /**
     * The map of edge weights for edge types. Each entry consists of the name
     * of the edge and a weight assigned to the edge.
     */
    private Map<String, Double> edgeWeights;

    public MetaModelType getMetaModelType() {
        return MetaModelType.getMetaModelTypeFromString(metaModelType);
    }

    @Override
    public String toString() {
        var output = String.format(
                "metaModelType; %s\n" +
                        "conceptualModelType; %s",
                this.metaModelType,
                this.conceptualModelType);

        if (this.edgeWeights != null) {
            output += "\nEdge weights\n";
            for (var edgeWeight : edgeWeights.entrySet()) {
                output += String.format(
                        "%s;%s\n",
                        edgeWeight.getKey(),
                        edgeWeight.getValue());
            }
        }
        return output;
    }
}
