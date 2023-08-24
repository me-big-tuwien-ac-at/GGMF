package com.modcmga.backendservice.infrastructure.dataaccess;

import com.modcmga.backendservice.dto.dataccess.EmbeddingsOutput;

import java.util.Optional;

public interface SemanticsApiDataAccess {

    Optional<EmbeddingsOutput> embeddings(final String text);
}
