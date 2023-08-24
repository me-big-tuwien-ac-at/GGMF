package com.modcmga.backendservice.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modcmga.backendservice.domain.objective.ObjectiveSetup;
import com.modcmga.backendservice.dto.application.EdgeWeight;
import com.modcmga.backendservice.dto.application.ModularisationInput;
import com.modcmga.backendservice.dto.application.ObjectiveData;
import com.modcmga.backendservice.model.conceptualmodel.ConceptualModelData;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import com.modcmga.backendservice.model.parameter.GeneticAlgorithmParameter;
import com.modcmga.backendservice.model.parameter.MutationWeight;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public final class ModularisationInputConverter implements Converter<ModularisationInput, ModularisationParameter> {
    private final ObjectiveMapper objectiveMapper;
    private final ObjectMapper mapper;

    @Autowired
    public ModularisationInputConverter(final ObjectiveMapper objectiveMapper) {
        this.objectiveMapper = objectiveMapper;
        this.mapper = new ObjectMapper();
    }

    @Override
    public ModularisationParameter convert(MappingContext<ModularisationInput, ModularisationParameter> mappingContext) {
        final var modularisationInput = mappingContext.getSource();

        final var applicationParameter = new ModularisationParameter();
        applicationParameter.setGeneticAlgorithmParameter(getGeneticAlgorithmParameter(modularisationInput));
        applicationParameter.setMutationWeight(getMutationWeight(modularisationInput));
        applicationParameter.setConceptualModelData(getConceptualModelData(modularisationInput));
        setObjective(modularisationInput, applicationParameter);

        return applicationParameter;
    }

    private GeneticAlgorithmParameter getGeneticAlgorithmParameter(
            final ModularisationInput modularisationInput) {
        final var geneticAlgorithmParameter = new GeneticAlgorithmParameter();

        geneticAlgorithmParameter.setChromosomeEncoding(modularisationInput.getChromosomeEncoding());
        geneticAlgorithmParameter.setOffspringSelector(modularisationInput.getOffspringSelector());
        geneticAlgorithmParameter.setSurvivorSelector(modularisationInput.getSurvivorSelector());
        geneticAlgorithmParameter.setCrossoverType(modularisationInput.getCrossoverType());
        geneticAlgorithmParameter.setMutationType(modularisationInput.getMutationType());

        geneticAlgorithmParameter.setCountPopulation(modularisationInput.getCountPopulation());
        geneticAlgorithmParameter.setCrossoverProbability(modularisationInput.getCrossoverProbability());
        geneticAlgorithmParameter.setMutationProbability(modularisationInput.getMutationProbability());
        geneticAlgorithmParameter.setTournamentSize(modularisationInput.getTournamentSize());

        geneticAlgorithmParameter.setConvergenceRate(modularisationInput.getConvergenceRate());
        geneticAlgorithmParameter.setConvergedGeneRate(modularisationInput.getConvergedGeneRate());
        geneticAlgorithmParameter.setCountGeneration(modularisationInput.getCountGeneration());

        geneticAlgorithmParameter.setMinimumParetoSetSize(modularisationInput.getMinimumParetoSetSize());
        geneticAlgorithmParameter.setMaximumParetoSetSize(modularisationInput.getMaximumParetoSetSize());

        return geneticAlgorithmParameter;
    }
    private MutationWeight getMutationWeight(final ModularisationInput modularisationInput) {
        try {
            return mapper.readValue(modularisationInput.getMutationWeight(), MutationWeight.class);
        } catch(Exception e) {
            System.err.println(e.getMessage());

            return null;
        }
    }

    private void setObjective(
            final ModularisationInput modularisationInput,
            final ModularisationParameter modularisationParameter
    ) {
        try {
            final var objectiveDataAsString = modularisationInput.getObjectiveData();
            final var objectiveData = mapper.readValue(objectiveDataAsString, ObjectiveData.class);

            final var objectives = Arrays.asList(objectiveData.getObjectiveSpecifications())
                    .stream()
                    .map(objectiveSpecification -> {
                        var mappedObjective = objectiveMapper.map(objectiveSpecification);

                        if (mappedObjective.isNumberOfElementsNeeded())
                            mappedObjective.setNumberOfElementsPerModule(objectiveData.getNumberOfElementsPerModule());

                        if (mappedObjective.isUMLInformationNeeded()) {
                            final var conceptualModelData =
                                    modularisationParameter.getConceptualModelData();
                            final var isUMLConceptualModel = conceptualModelData.getConceptualModelType()
                                    .equals("UML");
                            mappedObjective.setUMLDiagram(isUMLConceptualModel);
                        }

                        return mappedObjective;
                    } )
                    .collect(Collectors.toList());

            final var objectiveSetup = new ObjectiveSetup();
            objectiveSetup.setNumberOfElementsPerModule(objectiveData.getNumberOfElementsPerModule());
            objectiveSetup.setUseWeightedSumMethod(objectiveData.isUseWeightedSumMethod());
            objectiveSetup.setObjectives(objectives);
            modularisationParameter.setObjectiveSetup(objectiveSetup);

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private ConceptualModelData getConceptualModelData(
            final ModularisationInput modularisationInput) {
        try {
            final var conceptualModelMetaData = new ConceptualModelData();

            conceptualModelMetaData.setMetaModelType(modularisationInput.getMetaModelType());
            conceptualModelMetaData.setConceptualModelType(modularisationInput.getConceptualModelType());

            final var edgeWeights = modularisationInput.getEdgeWeights();
            if (edgeWeights != null) {
                final var mappedEdgeWeights = mapper.readValue(edgeWeights, EdgeWeight[].class);
                conceptualModelMetaData.setEdgeWeights(mapEdgeWeightArrayToMap(mappedEdgeWeights));
            }

            return conceptualModelMetaData;
        } catch(Exception e) {
            System.err.println(e.getMessage());

            return null;
        }
    }

    private Map<String, Double> mapEdgeWeightArrayToMap(final EdgeWeight[] edgeWeightsAsArray) {
        return Arrays.asList(edgeWeightsAsArray)
                .stream()
                .collect(Collectors.toMap(edgeWeight -> edgeWeight.getName(), edgeWeight -> edgeWeight.getWeight()));
    }
}
