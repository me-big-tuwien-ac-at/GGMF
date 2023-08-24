package com.modcmga.backendservice.domain.objective;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.SimilarityScore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StringDifferenceObjective extends Objective {

    private SimilarityScore<Double> editDistance;

    public StringDifferenceObjective() {
        this.editDistance = new CosineDistance();
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
                            edgeSimilarity += determineSimilarity(sourceVertex.getLabel(), edge.getLabel());
                            edgeSimilarity += determineSimilarity(targetVertex.getLabel(), edge.getLabel());
                        } else {
                            edgeSimilarity = determineSimilarity(sourceVertex.getLabel(), targetVertex.getLabel());
                        }

                        if (module.isIndexInModule(sourceVertex.getIndex()) && module.isIndexInModule(targetVertex.getIndex()))
                            edgeSimilarity /= 2;

                        sumSemanticSimilarity += edgeSimilarity;
                    }

                    return sumSemanticSimilarity;
                })
                .sum();
    }

    private double determineSimilarity(final String label1, final String label2) {
        return editDistance.apply(label1, label2);
    }

    @Override
    public String objectiveText() {
        return "Maximise average string difference between modules";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MAX_STRING_DIFFERENCE_BETWEEN_MODULES;
    }
}
