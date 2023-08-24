package com.modcmga.backendservice.model.enums;
/**
 * @Package: com.modcmga.poc.model
 * @Class: GraphMLType
 * @Author: Jan
 * @Date: 03.11.2021
 */

/**
 * Defines the type for which Graph editor the GraphML is tailored for.
 */
public enum GraphMLType {
    YED;

    public static GraphMLType getGraphMLType(String graphMLTypeAsString) {
        var upperCaseInput = graphMLTypeAsString.toUpperCase();
        return GraphMLType.valueOf(upperCaseInput);
    }
}
