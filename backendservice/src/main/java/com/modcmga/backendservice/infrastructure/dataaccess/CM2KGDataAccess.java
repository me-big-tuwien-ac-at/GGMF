package com.modcmga.backendservice.infrastructure.dataaccess;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: CM2KgService
 * @Author: Jan
 * @Date: 20.02.2022
 */

import com.modcmga.backendservice.model.conceptualmodel.MetaModelType;

import java.io.File;
import java.util.Map;

/**
 * Transforms the file to a generic GraphML file.
 */
public interface CM2KGDataAccess {
    /**
     * Transforms the file to a GraphML-file depending on
     * {@code conceptualModelType}.
     *
     * @param files The input files
     * @param metaModelType The type of the input file
     * @return The transformed GraphML-file.
     */
    File transformToGraphMLFile(Map<String, File> files, MetaModelType metaModelType);
}
