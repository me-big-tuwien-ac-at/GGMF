package com.modcmga.backendservice.dto.application;

import lombok.Data;
import lombok.ToString;

/**
 * Represents the input to evaluate the modularisation including the objectives.
 */
@Data
@ToString
public class EvaluationInput {
    /**
     * The list of objective information.
     */
    private String objectiveData;
}
