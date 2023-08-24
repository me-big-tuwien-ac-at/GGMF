package com.modcmga.backendservice.domain.objective;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.jgrapht.graph.AsUndirectedGraph;
import org.springframework.stereotype.Component;
import org.jgrapht.alg.clustering.UndirectedModularityMeasurer;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the modularity objective according to paper Newman M.E.J "Modularity and community structure in networks"
 * doi: 10.1073/pnas.0601602103.
 */
@Component
public class ModularityObjective extends Objective {

    private UndirectedModularityMeasurer<Vertex, Edge>  undirectedModularityMeasurer;

    @Override
    public void setKnowledgeGraph(final KnowledgeGraph knowledgeGraph) {
        super.setKnowledgeGraph(knowledgeGraph);

        final var undirectedGraph = knowledgeGraph.isDirectedGraph() ?
                new AsUndirectedGraph<>(knowledgeGraph.getGraph()) :
                knowledgeGraph.getGraph();
        this.undirectedModularityMeasurer = new UndirectedModularityMeasurer<>(undirectedGraph);
    }

    @Override
    public Optimize getOptimize() {
        return Optimize.MAXIMUM;
    }

    @Override
    public double calculateValue(final List<Module> modules) {
        final var partitions = modules.stream()
                .map(module -> ModuleInformationProvider.getVerticesOfModule(module, knowledgeGraph))
                .collect(Collectors.toList());

        return undirectedModularityMeasurer.modularity(partitions);
    }

    @Override
    public String objectiveText() {
        return "Maximise modularity score";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MAX_MODULARITY;
    }
}
