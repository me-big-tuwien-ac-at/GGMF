package com.modcmga.backendservice.entity;

import lombok.Builder;
import lombok.Data;

/**
 * Represents the word embedding representing the text as a real vector.
 */
@Data
@Builder
public class WordEmbedding {
    /**
     * The original form of the word embedding.
     */
    private String text;

    /**
     * The real vector representing the text.
     */
    private double[] embedding;
}
