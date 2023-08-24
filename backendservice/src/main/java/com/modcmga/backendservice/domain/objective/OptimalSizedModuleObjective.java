package com.modcmga.backendservice.domain.objective;

import com.modcmga.backendservice.domain.geneticalgorithm.Constants;
import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines the objectives to reduce the amount of modules, which don't have optimal number of  assignedvertices of
 * {@value Constants#COUNT_OPTIMAL_NUMBER_OF_MODULARISABLE_ELEMENTS_PER_MODULE}.
 */
@Component
public class OptimalSizedModuleObjective extends Objective {
    @Override
    public Optimize getOptimize() {
        return Optimize.MINIMUM;
    }

    @Override
    public boolean isNumberOfElementsNeeded() {
        return true;
    }

    @Override
    public double calculateValue(List<Module> modules) {
        return modules.stream()
                .filter(module -> {
                    if (ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                        return false;

                    final var countVerticesOfModule = module.getIndices()
                            .stream()
                            .map(i -> knowledgeGraph.getModularisableElement(i))
                            .filter(modularisableElement -> modularisableElement instanceof Vertex)
                            .map(modularisableElement -> (Vertex) modularisableElement)
                            .count();

                    return countVerticesOfModule != Constants.COUNT_OPTIMAL_NUMBER_OF_MODULARISABLE_ELEMENTS_PER_MODULE;
                })
                .count();
    }

    @Override
    public String objectiveText() {
        return "The number of modules with non optimal size of vertices";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.OPTIMAL_SIZED_MODULE;
    }
}
