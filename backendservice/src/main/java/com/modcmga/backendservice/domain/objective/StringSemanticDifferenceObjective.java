package com.modcmga.backendservice.domain.objective;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.objective.common.SemanticSimilarity;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines the objective of maximising the semantic difference between modules
 */
@Component
public class StringSemanticDifferenceObjective extends Objective {
    private final SemanticSimilarity semanticSimilarity;

    @Autowired
    public StringSemanticDifferenceObjective(SemanticSimilarity semanticSimilarity) {
        this.semanticSimilarity = semanticSimilarity;
    }

    @Override
    public Optimize getOptimize() {
        return Optimize.MAXIMUM;
    }

    @Override
    public double calculateValue(List<Module> modules) {
        return modules.stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                .mapToDouble(module -> {
                    final var boundaryEdges = ModuleInformationProvider.getBoundaryEdges(module, this.knowledgeGraph);

                    var sumSemanticSimilarity = 0;

                    for (var edge : boundaryEdges) {
                        final var sourceVertex = edge.getSourceVertex();
                        final var targetVertex = edge.getTargetVertex();

                        var edgeSimilarity = 0d;

                        if (!edge.getLabel().isBlank()) {
                            edgeSimilarity += semanticSimilarity.determineSemanticSimilarity(
                                    sourceVertex.getLabel(), edge.getLabel());
                            edgeSimilarity += semanticSimilarity.determineSemanticSimilarity(
                                    targetVertex.getLabel(), edge.getLabel());
                        } else {
                            edgeSimilarity = semanticSimilarity.determineSemanticSimilarity(sourceVertex.getLabel(), targetVertex.getLabel());
                        }

                        if (module.isIndexInModule(sourceVertex.getIndex()) && module.isIndexInModule(targetVertex.getIndex()))
                            edgeSimilarity /= 2;

                        sumSemanticSimilarity += edgeSimilarity;
                    }

                    return sumSemanticSimilarity;
                })
                .sum();
    }

    @Override
    public String objectiveText() {
        return "Maximise the semantic difference between the modules";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MAX_STRING_SEMANTIC_DIFFERENCE_BETWEEN_MODULES_OBJECTIVE;
    }
}
