package com.modcmga.backendservice.dto.dataccess;

import lombok.Data;

/**
 * Represents the embedding output containing all the embeddings.
 */
@Data
public class EmbeddingsOutput {
    private double[] values;
}
