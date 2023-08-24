package com.modcmga.backendservice.domain.objective.common;

public interface SemanticSimilarity {

    /**
     * Determines the semantic similarity between {@code s1} and {@code s2} and returns a real value where a higher
     * value indicates higher semantic similarity.
     * @param s1 the first string
     * @param s2 the second string
     * @return real value determining semantic similarity
     */
    double determineSemanticSimilarity(final String s1, final String s2);
}
