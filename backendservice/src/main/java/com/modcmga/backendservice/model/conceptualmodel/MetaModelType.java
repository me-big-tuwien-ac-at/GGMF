package com.modcmga.backendservice.model.conceptualmodel;
/**
 * @Package: com.modcmga.backendservice.model.conceptualmodel
 * @Class: ConceptualModelType
 * @Author: Jan
 * @Date: 24.01.2022
 */

/**
 * Type of meta model.
 */
public enum MetaModelType {
    ADOXX("adoxx"),
    ARCHI("archi"),
    PAPYRUSUML("papyrusuml");

    MetaModelType(String metaModelType) {
        this.metaModelType = metaModelType;
    }

    public String metaModelType;

    public static MetaModelType getMetaModelTypeFromString(String conceptualModelTypeAsString) {
        return MetaModelType.valueOf(conceptualModelTypeAsString);
    }
}
