package com.modcmga.backendservice.model.evaluation;

import com.modcmga.backendservice.domain.objective.ObjectiveSetup;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class provides parameter to evaluate
 */
@Data
@NoArgsConstructor
public class EvaluationParameter {
    private ObjectiveSetup objectiveSetup;
}
