package com.modcmga.backendservice.model.parameter;

import lombok.Data;

/**
 * Represents the weights of the mutation operations.
 */
@Data
public class MutationWeight {
    /**
     * Defines how much emphasis should be put on splitting modules when mutating linear linkage encodings.
     */
    private double splitModulesWeight;

    /**
     * Defines how much emphasis should be put on combining modules when mutating linear linkage encodings.
     */
    private double combineModulesWeight;


    /**
     * Defines how much emphasis should be put on moving elements between modules when mutating linear linkage
     * encodings.
     */
    private double moveElementsBetweenModulesWeight;

    @Override
    public String toString() {
        return String.format(
                "splitModulesWeight; %s\n" +
                        "combineModulesWeight; %s\n" +
                        "moveElementsBetweenModulesWeight; %s",
                this.splitModulesWeight,
                this.combineModulesWeight,
                this.moveElementsBetweenModulesWeight);
    }
}
