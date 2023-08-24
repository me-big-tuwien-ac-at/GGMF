package com.modcmga.backendservice.domain.objective.common;

import com.modcmga.backendservice.repository.WordEmbeddingRepository;
import com.modcmga.backendservice.util.StringUtil;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SemanticSimilarityImpl implements SemanticSimilarity {
    private final WordEmbeddingRepository wordEmbeddingRepository;
    private final DistanceMeasure distanceMeasure;

    @Autowired
    public SemanticSimilarityImpl(WordEmbeddingRepository wordEmbeddingRepository) {
        this.wordEmbeddingRepository = wordEmbeddingRepository;
        this.distanceMeasure = new EuclideanDistance();
    }

    @Override
    public double determineSemanticSimilarity(final String label1, final String label2) {
        final var separatedLabel1 = StringUtil.separateCamelOrTitleCase(label1);
        final var separatedLabel2 = StringUtil.separateCamelOrTitleCase(label2);

        final var embedding1 = wordEmbeddingRepository.findOne(separatedLabel1)
                .orElseThrow(() -> new RuntimeException(String.format("No embedding was found for %s", separatedLabel1)));

        final var embedding2 = wordEmbeddingRepository.findOne(separatedLabel2)
                .orElseThrow(() -> new RuntimeException(String.format("No embedding was found for %s", separatedLabel1)));

        final var embeddingValues1 = embedding1.getEmbedding();
        final var embeddingValues2 = embedding2.getEmbedding();

        if (embeddingValues1.length != embeddingValues2.length)
            throw new RuntimeException("The length of the word embedding vectors are different");


        return distanceMeasure.compute(embeddingValues1, embeddingValues2);
    }
}
