package com.modcmga.backendservice.domain.objective;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.objective
 * @Class: EdgeBetweennessCentralityObjective
 * @Author: Jan
 * @Date: 08.08.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.apache.commons.math.stat.descriptive.rank.Median;
import org.jgrapht.alg.scoring.ClosenessCentrality;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the objective of maximising the average edge betweenness centrality
 * per module.
 */
public class MedianClosenessCentralityObjective extends Objective {
    private ClosenessCentrality<Vertex, Edge> closenessCentrality;

    @Override
    public void setKnowledgeGraph(final KnowledgeGraph knowledgeGraph) {
        super.setKnowledgeGraph(knowledgeGraph);
        this.closenessCentrality = new ClosenessCentrality<>(knowledgeGraph.getGraph());
    }

    @Override
    public Optimize getOptimize() {
        return Optimize.MAXIMUM;
    }

    @Override
    public double calculateValue(final List<Module> modules) {
        final var closenessCentralityPerModule = modules.stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                .mapToDouble(
                        module -> {
                    var verticesInModule = ModuleInformationProvider.getModularisableElements(module, knowledgeGraph)
                            .stream()
                            .filter(modularisableElement -> modularisableElement instanceof Vertex)
                            .map(modularisableElement -> (Vertex)modularisableElement)
                            .collect(Collectors.toList());
                    return calculateSumOfVertexScorePerModule(verticesInModule);
                })
                .toArray();

        final var median = new Median();
        return median.evaluate(closenessCentralityPerModule);
    }

    private double calculateSumOfVertexScorePerModule(final List<Vertex> vertices) {
        if (vertices.isEmpty()) {
            return 0;
        }

        return vertices.stream()
                .map(vertex -> {
                    var value = closenessCentrality.getVertexScore(vertex);
                    return value != null ? value : 0;
                })
                .reduce(Double::sum)
                .get();
    }

    @Override
    public String objectiveText() {
        return "Maximise average closeness centrality per module";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MEDIAN_CLOSENESS_CENTRALITY_PER_MODULE;
    }
}
