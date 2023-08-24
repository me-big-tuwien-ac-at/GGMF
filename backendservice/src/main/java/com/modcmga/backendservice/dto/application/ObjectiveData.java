package com.modcmga.backendservice.dto.application;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the specified objective-related data.
 */
@Getter
@Setter
public class ObjectiveData {
    /**
     * The number of vertices a module should have.
     */
    private int numberOfElementsPerModule;

    /**
     * The flag to determine if the weighted sum method should be applied.
     */
    private boolean isUseWeightedSumMethod;

    /**
     * The specified objectives used for the fitness function.
     */
    private ObjectiveSpecification[] objectiveSpecifications;

    /**
     * Sets the value {@link #isUseWeightedSumMethod}
     * @param isUseWeightedSumMethod the flag to determine if the weighted sum method should be applied.
     */
    public void setIsUseWeightedSumMethod(final boolean isUseWeightedSumMethod) {
        this.isUseWeightedSumMethod = isUseWeightedSumMethod;
    }
}
