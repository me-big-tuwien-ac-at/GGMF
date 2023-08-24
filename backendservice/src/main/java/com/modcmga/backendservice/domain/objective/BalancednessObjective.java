package com.modcmga.backendservice.domain.objective;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.objective
 * @Class: BalancednessObjective
 * @Author: Jan
 * @Date: 23.01.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines the objective of balancedness which aims to reduce the standard
 * deviation.
 */
@Component
public class BalancednessObjective extends Objective {
    @Override
    public Optimize getOptimize() {
        return Optimize.MINIMUM;
    }

    @Override
    public double calculateValue(List<Module> modules) {
        // TODO: only consider vertices
        var moduleSizes = modules.stream()
                .filter(module -> !ModuleInformationProvider.isModuleConsistOfIsolatedVertex(module, knowledgeGraph))
                .mapToDouble(module -> module.getIndices().size())
                .toArray();

        var descriptiveStatistics = new DescriptiveStatistics(moduleSizes);
        return descriptiveStatistics.getStandardDeviation();
    }

    @Override
    public String objectiveText() {
        return "Minimise standard deviation of vertex size per module (Balancedness)";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.BALANCEDNESS;
    }
}
