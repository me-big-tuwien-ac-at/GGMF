package com.modcmga.backendservice.domain.objective;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.objective
 * @Class: CohesionObjective
 * @Author: Jan
 * @Date: 23.01.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines the objective of cohesion which aims to maximize cohesion inside
 * modules.
 */
@Component
public class MedianCohesionObjective extends Objective {
    @Override
    public Optimize getOptimize() {
        return Optimize.MAXIMUM;
    }

    @Override
    public double calculateValue(final List<Module> modules) {
        final var cohesionPerModule = modules.stream()
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
                .toArray();

        final var median = new Median();
        return median.evaluate(cohesionPerModule);
    }

    @Override
    public String objectiveText() {
        return "Maximise cohesion";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MAXIMISE_MEDIAN_COHESION;
    }
}
