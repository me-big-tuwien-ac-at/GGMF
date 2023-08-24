package com.modcmga.backendservice.model.evaluation;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Contains all evaluation relevant information such as the modules and the fitness values for the ModulER file.
 */
@Data
@AllArgsConstructor
public class ModulErEvaluationResult {
    private String modulERName;
    private List<Module> modules;
    private double[] multiObjectiveFitnessValue;
    private double[] normalisedMultiObjectiveFitnessValue;
}
