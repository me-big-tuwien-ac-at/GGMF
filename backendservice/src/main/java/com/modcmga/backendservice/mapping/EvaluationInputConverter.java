package com.modcmga.backendservice.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modcmga.backendservice.domain.objective.ObjectiveSetup;
import com.modcmga.backendservice.dto.application.EvaluationInput;
import com.modcmga.backendservice.dto.application.ObjectiveData;
import com.modcmga.backendservice.model.evaluation.EvaluationParameter;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public final class EvaluationInputConverter implements Converter<EvaluationInput, EvaluationParameter> {
    private final ObjectiveMapper objectiveMapper;
    private final ObjectMapper mapper;

    @Autowired
    public EvaluationInputConverter(final ObjectiveMapper objectiveMapper) {
        this.objectiveMapper = objectiveMapper;
        this.mapper = new ObjectMapper();
    }

    @Override
    public EvaluationParameter convert(MappingContext<EvaluationInput, EvaluationParameter> mappingContext) {
        final var evaluationInput = mappingContext.getSource();

        final var evaluationParameter = new EvaluationParameter();
        setObjective(evaluationInput, evaluationParameter);

        return evaluationParameter;
    }

    private void setObjective(
            final EvaluationInput evaluationInput,
            final EvaluationParameter evaluationParameter
    ) {
        try {
            final var objectiveDataAsString = evaluationInput.getObjectiveData();
            final var objectiveData = mapper.readValue(objectiveDataAsString, ObjectiveData.class);

            final var objectives = Arrays.asList(objectiveData.getObjectiveSpecifications())
                    .stream()
                    .map(objectiveSpecification -> {
                        var mappedObjective = objectiveMapper.map(objectiveSpecification);

                        return mappedObjective;
                    } )
                    .collect(Collectors.toList());

            final var objectiveSetup = new ObjectiveSetup();
            objectiveSetup.setNumberOfElementsPerModule(objectiveData.getNumberOfElementsPerModule());
            objectiveSetup.setUseWeightedSumMethod(objectiveData.isUseWeightedSumMethod());
            objectiveSetup.setObjectives(objectives);
            evaluationParameter.setObjectiveSetup(objectiveSetup);

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
