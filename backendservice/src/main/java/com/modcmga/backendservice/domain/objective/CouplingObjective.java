package com.modcmga.backendservice.domain.objective;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.objective
 * @Class: CouplingObjective
 * @Author: Jan
 * @Date: 23.01.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Defines the coupling objective which aims to reduce the coupling between modules.
 */
@Component
public class CouplingObjective extends Objective {
    @Override
    public Optimize getOptimize() {
        return Optimize.MINIMUM;
    }

    @Override
    public double calculateValue(final List<Module> modules) {
        return modules.stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                .mapToDouble(module ->
                        calculateSumOfWeightOfEdges(ModuleInformationProvider.getBoundaryEdges(module, this.knowledgeGraph)))
                .sum();
    }

    private double calculateSumOfWeightOfEdges(final Set<Edge> edges) {
        return edges.stream()
                .mapToDouble(edge -> edge.getWeight())
                .sum();
    }

    @Override
    public String objectiveText() {
        return "Minimise coupling";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MINIMISE_COUPLING;
    }
}
