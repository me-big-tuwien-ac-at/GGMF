package com.modcmga.backendservice.mapping;

import com.modcmga.backendservice.domain.objective.*;
import com.modcmga.backendservice.dto.application.ObjectiveSpecification;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps the objective specifications into the list of objective calculators. It also contains all possible objectives
 * that can be specified and used for the fitness function.
 */
@Component
public final class ObjectiveMapper {
    private final List<Objective> objectives;

    @Autowired
    public ObjectiveMapper(BalancednessObjective balancednessObjective,
                           CouplingObjective couplingObjective,
                           CohesionObjective cohesionObjective,
                           AverageCohesionObjective averageCohesionObjective,
                           MedianCohesionObjective medianCohesionObjective,
                           OptimalSizedModuleObjective optimalSizedModuleObjective,
                           OptimalNumberOfModulesObjective optimalNumberOfModulesObjective,
                           AverageEdgeBetweennessCentralityObjective averageEdgeBetweennessCentralityObjective,
                           AverageClosenessCentralityObjective averageClosenessCentralityObjective,
                           StringSimilarityObjective stringSimilarityObjective,
                           StringDifferenceObjective stringDifferenceObjective,
                           StringSemanticSimilarityObjective stringSemanticSimilarityObjective,
                           StringSemanticDifferenceObjective stringSemanticDifferenceObjective,
                           NumberOfSmellsObjective numberOfSmellsObjective,
                           ModularityObjective modularityObjective) {
        this.objectives = List.of(
                balancednessObjective,
                couplingObjective,
                cohesionObjective,
                averageCohesionObjective,
                medianCohesionObjective,
                optimalSizedModuleObjective,
                optimalNumberOfModulesObjective,
                averageEdgeBetweennessCentralityObjective,
                averageClosenessCentralityObjective,
                stringSimilarityObjective,
                stringDifferenceObjective,
                stringSemanticSimilarityObjective,
                stringSemanticDifferenceObjective,
                numberOfSmellsObjective,
                modularityObjective
        );
    }

    /**
     * Returns the list of objective based on the objective data DTOs.
     * @param objectiveSpecifications the list objective data
     * @return the list of objective
     */
    public List<Objective> map(List<ObjectiveSpecification> objectiveSpecifications) {
        return objectiveSpecifications
                .stream()
                .filter(objectiveSpecification -> objectiveSpecification.isSelected())
                .map(objectiveSpecification -> {
                    final var objective = getObjective(objectiveSpecification.getObjectiveType());

                    objective.setWeight(objectiveSpecification.getWeight());

                    return objective;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns the mapped, respective objective based on {@link ObjectiveSpecification}.
     * @param objectiveSpecification the objective data used to retrieve the objective.
     * @return the mapped, respective objective based on {@link ObjectiveSpecification}.
     */
    public Objective map(ObjectiveSpecification objectiveSpecification) {
        final var objective = getObjective(objectiveSpecification.getObjectiveType());

        objective.setWeight(objectiveSpecification.getWeight());

        return objective;
    }

    private Objective getObjective(final ObjectiveType objectiveType) {
        return objectives
                .stream()
                .filter(objective -> objective.objectiveType() == objectiveType)
                .findAny()
                .get();
    }
}
