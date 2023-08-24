package com.modcmga.backendservice.domain.objective;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Getter
@Setter
public class ObjectiveSetup {
    /**
     * The number of vertices a module should have.
     */
    private int numberOfElementsPerModule;

    /**
     * Represents the flag to determine if the weighted sum method should be utilised.
     */
    private boolean isUseWeightedSumMethod;

    /**
     * The specified objectives, which calculates the objective value.
     */
    private List<Objective> objectives;

    @Override
    public String toString() {
        String objectivesString = "";

        if (this.objectives != null) {
            objectivesString = this.objectives.stream()
                    .map(objective -> objective.toString())
                    .collect(Collectors.joining("\n"));
        }

        return String.format(
                "isUseWeightedSumMethod: %s\n" +
                "objectives: %s",
                isUseWeightedSumMethod,
                objectivesString);
    }
}
