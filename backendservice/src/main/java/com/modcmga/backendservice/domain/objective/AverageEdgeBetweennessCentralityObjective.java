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
import org.jgrapht.alg.scoring.EdgeBetweennessCentrality;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Defines the objective of maximising the average edge betweenness centrality
 * per module.
 */
@Component
public class AverageEdgeBetweennessCentralityObjective extends Objective {
    private EdgeBetweennessCentrality<Vertex, Edge> edgeEdgeBetweennessCentrality;

    @Override
    public void setKnowledgeGraph(final KnowledgeGraph knowledgeGraph) {
        super.setKnowledgeGraph(knowledgeGraph);
        this.edgeEdgeBetweennessCentrality = new EdgeBetweennessCentrality<>(knowledgeGraph.getGraph());
    }

    @Override
    public Optimize getOptimize() {
        return Optimize.MAXIMUM;
    }

    @Override
    public double calculateValue(final List<Module> modules) {
        final var sum = modules.stream()
                .filter(module -> !ModuleInformationProvider.isIsolated(module, knowledgeGraph))
                .map(module -> {
                    var moduleOfEdges = ModuleInformationProvider.getModuleEdges(module, knowledgeGraph);
                    return calculateSumOfEdgeScorePerModule(moduleOfEdges);
                })
                .reduce(Double::sum)
                .get();

        return sum / modules.size();
    }

    private double calculateSumOfEdgeScorePerModule(final List<Edge> edges) {
        if (edges.isEmpty()) {
            return 0;
        }

        return edges.stream()
                .map(edge -> edgeEdgeBetweennessCentrality.getEdgeScore(edge))
                .reduce(Double::sum)
                .get();
    }

    @Override
    public String objectiveText() {
        return "Maximise average edge betweenness centrality per module";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.AVERAGE_EDGE_BETWEENNESS_CENTRALITY_PER_MODULE;
    }
}
