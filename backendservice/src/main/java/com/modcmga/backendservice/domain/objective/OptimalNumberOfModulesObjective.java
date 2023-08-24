package com.modcmga.backendservice.domain.objective;

import com.modcmga.backendservice.domain.geneticalgorithm.Constants;
import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the objective to reduce the number of modules, which don't have
 * {@value Constants#COUNT_OPTIMAL_NUMBER_OF_MODULARISABLE_ELEMENTS_PER_MODULE} vertices.
 */
@Component
public class OptimalNumberOfModulesObjective extends Objective {

    private int countOptimalNumberOfModules;

    @Override
    public Optimize getOptimize() {
        return Optimize.MINIMUM;
    }

    @Override
    public boolean isNumberOfElementsNeeded() {
        return true;
    }

    @Override
    public void setKnowledgeGraph(KnowledgeGraph knowledgeGraph) {
        super.setKnowledgeGraph(knowledgeGraph);

        final var vertices = knowledgeGraph.getVertices()
                .stream()
                .filter(vertex -> !knowledgeGraph.isIsolated(vertex))
                .collect(Collectors.toList());
        double verticesSize = vertices.size();
        this.countOptimalNumberOfModules = (int) Math.ceil(verticesSize / this.numberOfElementsPerModule);
    }

    @Override
    public double calculateValue(List<Module> modules) {
        var nonIsolatedModules = modules
                .stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                .collect(Collectors.toList());
        return Math.abs(nonIsolatedModules.size() - countOptimalNumberOfModules);
    }

    @Override
    public String objectiveText() {
        return String.format(
                "The difference between the optimal number of modules %d and the final number of number of modules",
                this.countOptimalNumberOfModules);
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.OPTIMAL_NUMBER_OF_MODULES;
    }
}
