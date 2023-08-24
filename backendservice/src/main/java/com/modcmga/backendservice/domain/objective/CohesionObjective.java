package com.modcmga.backendservice.domain.objective;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines the cohesion objective which aims to maximise the cohesion inside modules.
 */
@Component
public class CohesionObjective extends Objective {
    @Override
    public Optimize getOptimize() {
        return Optimize.MAXIMUM;
    }

    @Override
    public double calculateValue(final List<Module> modules) {
        return modules.stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                .mapToDouble(module -> {
                    final var edges = ModuleInformationProvider.getModuleEdges(module, this.knowledgeGraph);

                    var sum = 0.0d;

                    for (final var edge : edges) {
                        final var sourceVertex = edge.getSourceVertex();
                        final var targetVertex = edge.getTargetVertex();

                        if (module.isIndexInModule(sourceVertex.getIndex()) && module.isIndexInModule(targetVertex.getIndex()))
                            sum += edge.getWeight();
                        else
                            sum += edge.getWeight() / 2;
                    }

                    return sum;
                })
                .sum();
    }

    @Override
    public String objectiveText() {
        return "Maximise cohesion in all modules";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MAXIMISE_COHESION;
    }
}
