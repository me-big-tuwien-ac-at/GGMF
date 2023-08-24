package com.modcmga.backendservice.repository;

import com.modcmga.backendservice.entity.WordEmbedding;
import com.modcmga.backendservice.infrastructure.dataaccess.SemanticsApiDataAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class WordEmbeddingRepositoryImpl implements WordEmbeddingRepository {
    private final SemanticsApiDataAccess semanticsApiDataAccess;

    // TODO: replace with database for persisting embeddings
    private Map<String, WordEmbedding> cache;

    @Autowired
    public WordEmbeddingRepositoryImpl(SemanticsApiDataAccess semanticsApiDataAccess) {
        this.semanticsApiDataAccess = semanticsApiDataAccess;
        this.cache = new HashMap<>();
    }

    @Override
    public Optional<WordEmbedding> findOne(String text) {
        if (cache.containsKey(text)) {
            // Prevent calling endpoint multiple times
            return Optional.of(cache.get(text));
        }

        final var output = semanticsApiDataAccess.embeddings(text)
                .orElseThrow(() -> new RuntimeException(String.format("No entity found for %s", text)));

        final var wordEmbedding = WordEmbedding.builder()
                .text(text)
                .embedding(output.getValues())
                .build();

        cache.put(text, wordEmbedding);

        return Optional.of(wordEmbedding);
    }
}
