package com.modcmga.backendservice.infrastructure.transform;

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;

import java.io.File;

public interface ModulERTransformer {
    /**
     * Transforms the ModulER into a LLE to represent a solution.
     * @param modulERFile the ModulER file.
     * @return a Knowledge Graph.
     */
    LinearLinkageEncoding transform(File modulERFile);
}
