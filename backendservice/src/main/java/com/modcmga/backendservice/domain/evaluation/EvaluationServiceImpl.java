package com.modcmga.backendservice.domain.evaluation;

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncodingInformationProvider;
import com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.FitnessFunction;
import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.evaluation.EvaluationParameter;
import com.modcmga.backendservice.model.evaluation.LouvainModularisationSolution;
import com.modcmga.backendservice.model.evaluation.ModularisationEvaluationResult;
import com.modcmga.backendservice.model.evaluation.ModularisationSolution;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class EvaluationServiceImpl implements EvaluationService {
    @Override
    public ModularisationEvaluationResult evaluateModularisation(final ModularisationSolution modularisationSolution,
                                                                 final KnowledgeGraph knowledgeGraph,
                                                                 final EvaluationParameter evaluationParameter) {
        final var modules = new ArrayList<Module>();

        // Maintain list of vertices to iterate
        final var remainingVertices = new HashSet<>(knowledgeGraph.getVertices());
        final var remainingEdges = new HashSet<>(knowledgeGraph.getEdges());

        // Retrieve the elements  from the modules
        final var moduleSolutions = modularisationSolution.getModuleSolutions();
        for (final var moduleKey : moduleSolutions.keySet()) {
            final var module = new Module();

            final var moduleSolution = moduleSolutions.get(moduleKey);
            final var idsInModule = moduleSolution.stream()
                    .map(moduleElement -> moduleElement.getId())
                    .collect(Collectors.toSet());

            // Add all vertices to module
            var verticesTobeRemoved = new ArrayList<Vertex>();
            remainingVertices
                    .stream()
                    .filter(vertex -> idsInModule.contains(vertex.getId()))
                    .forEach(vertex -> {
                        module.addIndex(vertex.getIndex());
                        verticesTobeRemoved.add(vertex);
                    });
            remainingVertices.removeAll(verticesTobeRemoved);

            // Add all edges in the module where the source and target vertices are in the module
            var edgesTobeRemoved = new ArrayList<Edge>();
            knowledgeGraph.getEdges().stream()
                    .filter(edge -> idsInModule.contains(edge.getSourceVertex()) &&
                            idsInModule.contains(edge.getTargetVertex()))
                    .forEach(edge -> {
                        module.addIndex(edge.getIndex());
                        edgesTobeRemoved.add(edge);
                    });
            remainingEdges.removeAll(edgesTobeRemoved);

            modules.add(module);
        }

        // Create modules for remaining vertices if there are any left
        for (final var vertex : remainingVertices) {
            final var module = new Module();
            module.addIndex(vertex.getIndex());

            modules.add(module);
        }

        // Randomly assigns unassigned edges to any module of incident source or target vertex
        for (final var edge : remainingEdges) {
            final var randomValue = ThreadLocalRandom.current().nextDouble();

            if (randomValue < 0.5) {
                final var sourceVertexModule = modules.stream()
                        .filter(module -> module.isIndexInModule(edge.getSourceVertex().getIndex()))
                        .findFirst()
                        .get();
                sourceVertexModule.addIndex(edge.getIndex());
            } else {
                final var targetVertexModule = modules.stream()
                        .filter(module -> module.isIndexInModule(edge.getTargetVertex().getIndex()))
                        .findFirst()
                        .get();
                targetVertexModule.addIndex(edge.getIndex());
            }
        }

        // Create LLE
        final var lle =
                LinearLinkageEncodingInformationProvider.determineLinearLinkageEncoding(modules, knowledgeGraph);

        // Calculate multi objective and normalised multi objectve fitness value
        final var objectives = evaluationParameter.getObjectiveSetup().getObjectives();
        final var fitnessFunction = new FitnessFunction(objectives, knowledgeGraph);
        final var fitnessValue = fitnessFunction.calculateMultiObjectiveFitnessValue(lle).data();

        return ModularisationEvaluationResult.builder()
                .modules(modules)
                .multiObjectiveFitnessValue(fitnessValue)
                .build();
    }

    @Override
    public ModularisationEvaluationResult evaluateLouvain(final LouvainModularisationSolution louvainModularisationSolution,
                                                          final KnowledgeGraph knowledgeGraph,
                                                          final EvaluationParameter evaluationParameter) {
        final var modules = new ArrayList<Module>();

        final var remainingVertices = new HashSet<>(knowledgeGraph.getVertices());
        final var remainingEdges = new HashSet<>(knowledgeGraph.getEdges());

        // Read the existing modules with the vertices
        for (final var vertexIdsInModule : louvainModularisationSolution.getModules()) {
            final var module = new Module();
            // Add all vertices in the module
            vertexIdsInModule.stream()
                    .map(vertexId -> knowledgeGraph.getVertices().stream()
                            .filter(vertex -> vertex.getId().equals(vertexId))
                            .findFirst()
                            .get())
                    .forEach(vertex -> {
                        module.addIndex(vertex.getIndex());
                        remainingVertices.remove(vertex);
                    });

            // Add all edges in the module where the source and target vertices are in the module
            knowledgeGraph.getEdges().stream()
                    .filter(edge -> vertexIdsInModule.contains(edge.getSourceVertex()) &&
                            vertexIdsInModule.contains(edge.getTargetVertex()))
                    .forEach(edge -> {
                        module.addIndex(edge.getIndex());
                        remainingEdges.remove(edge);
                    });

            modules.add(module);
        }

        // Create modules for remaining vertices if there are any left
        for (final var vertex : remainingVertices) {
            final var module = new Module();
            module.addIndex(vertex.getIndex());

            modules.add(module);
        }

        // Randomly assigns unassigned edges to any module of incident source or target vertex
        for (final var edge : remainingEdges) {
            final var randomValue = ThreadLocalRandom.current().nextDouble();

            if (randomValue < 0.5) {
                final var sourceVertexModule = modules.stream()
                        .filter(module -> module.isIndexInModule(edge.getSourceVertex().getIndex()))
                        .findFirst()
                        .get();
                sourceVertexModule.addIndex(edge.getIndex());
            } else {
                final var targetVertexModule = modules.stream()
                        .filter(module -> module.isIndexInModule(edge.getTargetVertex().getIndex()))
                        .findFirst()
                        .get();
                targetVertexModule.addIndex(edge.getIndex());
            }
        }

        // Create LLE
        final var lle =
                LinearLinkageEncodingInformationProvider.determineLinearLinkageEncoding(modules, knowledgeGraph);

        // Calculate multi objective and normalised multi objectve fitness value
        final var objectives = evaluationParameter.getObjectiveSetup().getObjectives();
        final var fitnessFunction = new FitnessFunction(objectives, knowledgeGraph);
        final var fitnessValue = fitnessFunction.calculateMultiObjectiveFitnessValue(lle).data();

        return ModularisationEvaluationResult.builder()
                .modules(modules)
                .multiObjectiveFitnessValue(fitnessValue)
                .build();
    }
}
