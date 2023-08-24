package com.modcmga.backendservice.model.conceptualmodel;
/**
 * @Package: com.modcmga.backendservice.model.conceptualmodel
 * @Class: ConceptualModelType
 * @Author: Jan
 * @Date: 22.02.2022
 */

/**
 * The type of the conceptual model.
 */
public enum ConceptualModelType {
    UML, BPMN, ARCHI;

    public static ConceptualModelType getConceptualModelTypeFromString(String conceptualModelTypeAsString) {
        return ConceptualModelType.valueOf(conceptualModelTypeAsString);
    }
}
