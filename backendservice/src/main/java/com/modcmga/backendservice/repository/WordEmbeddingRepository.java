package com.modcmga.backendservice.repository;

import com.modcmga.backendservice.entity.WordEmbedding;

import java.util.Optional;

/**
 * Represents the repository for {@link WordEmbedding}.
 */
public interface WordEmbeddingRepository {
    /**
     * Retrieves the one {@link WordEmbedding} based on {@code text}.
     * @param text the text to find the corresponding word embedding.
     * @return {@link WordEmbedding}
     */
    Optional<WordEmbedding> findOne(String text);
}
