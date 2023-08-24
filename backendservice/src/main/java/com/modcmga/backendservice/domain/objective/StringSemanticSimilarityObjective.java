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
 * Defines the objective of maximising the semantic similarity between elements in modules.
 */
@Component
public class StringSemanticSimilarityObjective extends Objective {
    private final SemanticSimilarity semanticSimilarity;

    @Autowired
    public StringSemanticSimilarityObjective(SemanticSimilarity semanticSimilarity) {
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
                    final var moduleEdges = ModuleInformationProvider.getModuleEdges(module, this.knowledgeGraph);

                    var sumSemanticSimilarity = 0;

                    for (var edge : moduleEdges) {
                        final var sourceVertex = edge.getSourceVertex();
                        final var targetVertex = edge.getTargetVertex();

                        var edgeSimilarity = 0d;

                        if (edge.getLabel() != null && !edge.getLabel().isBlank()) {
                            edgeSimilarity += semanticSimilarity.determineSemanticSimilarity(
                                    sourceVertex.getLabel(), edge.getLabel());
                            edgeSimilarity += semanticSimilarity.determineSemanticSimilarity(
                                    targetVertex.getLabel(), edge.getLabel());
                        } else {
                            edgeSimilarity = semanticSimilarity.determineSemanticSimilarity(
                                    sourceVertex.getLabel(), targetVertex.getLabel());
                        }

                        if (module.isIndexInModule(sourceVertex.getIndex()) &&
                                module.isIndexInModule(targetVertex.getIndex()))
                            edgeSimilarity /= 2;

                        sumSemanticSimilarity += edgeSimilarity;
                    }

                    return sumSemanticSimilarity;
                })
                .average()
                .getAsDouble();
    }

    @Override
    public String objectiveText() {
        return "Maximise the semantic similarity within modules";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MAX_STRING_SEMANTIC_SIMILARITY_WITHIN_MODULES_OBJECTIVE;
    }
}
