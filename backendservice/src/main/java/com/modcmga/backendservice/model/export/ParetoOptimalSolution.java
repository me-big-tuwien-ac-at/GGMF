package com.modcmga.backendservice.model.export;
/**
 * @Package: com.modcmga.backendservice.model.export
 * @Class: ParetoOptimalSolution
 * @Author: Jan
 * @Date: 15.01.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * The Pareto optimal solution is a solution which cannot be further improved
 * without worsening an objective.
 */
@Getter
@Setter
public class ParetoOptimalSolution {
    private List<Module> modules;
    private double[] fitnessValues;
    private double[] normalisedFitnessValues;
    private Double fitnessValue;
    private int ranking;

    @Override
    public int hashCode() {
        return modules.stream()
                .mapToInt(module -> module.hashCode())
                .sum();
    }
}
