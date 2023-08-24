package com.modcmga.backendservice.domain.objective;

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;
import org.jgrapht.alg.cycle.TarjanSimpleCycles;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *  Defines the objective of reducing the number of smells in the graph. If it is a UML diagram, additional smells
 *  related to UMLs are checked.
 */
@Component
public class NumberOfSmellsObjective extends Objective {
    /**
     * The number of dependencies to be counted like a hub (too many dependencies)
     */
    private final static int COUNT_HUB_ABSTRACTION = 15;

    private List<List<Vertex>> simpleCycles;

    @Override
    public void prepare() {
        if (simpleCycles == null) {
            // Determine generally all cycles in the graph
            final var cycleDetector = new TarjanSimpleCycles<>(this.knowledgeGraph.getGraph());
            simpleCycles = cycleDetector.findSimpleCycles();
        }
    }

    @Override
    public boolean isUMLInformationNeeded() {
        return true;
    }

    @Override
    public Optimize getOptimize() {
        return Optimize.MINIMUM;
    }

    @Override
    public double calculateValue(final List<Module> modules) {
        long countSmells = 0;

        countSmells += countCyclicDependencies(modules);
        countSmells += countBrokenModularisation(modules);

        return countSmells;
    }

    private long countCyclicDependencies(final List<Module> modules) {
        long cycles = 0;

        // Check if the vertices of the cycle in the unmodularisable knowledge graph, are still in the same module.
        for (List<Vertex> simpleCycle : simpleCycles) {
            for (var module : modules) {
                if (module.getIndices().containsAll(simpleCycle)) {
                    // The module contains the cycles
                    cycles++;
                }
            }
        }

        return cycles;
    }

    private long countBrokenModularisation(final List<Module> modules) {
        final var boundaryEdges = ModuleInformationProvider.getAllBoundaryEdges(modules, knowledgeGraph);
        return boundaryEdges.stream()
                .filter(boundaryEdge -> boundaryEdge.getReferenceName().equals("ownedAttribute"))
                .count();
    }

    @Override
    public String objectiveText() {
        return "Minimise the number of smells";
    }

    @Override
    public ObjectiveType objectiveType() {
        return ObjectiveType.MIN_SMELLS;
    }
}
