package com.modcmga.backendservice.mapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modcmga.backendservice.model.evaluation.LouvainModularisationSolution;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;
import org.springframework.stereotype.Component;

@Component
public class ModularisationSolutionConverter implements Converter<String, LouvainModularisationSolution>  {
    private final ObjectiveMapper objectiveMapper;
    private final ObjectMapper mapper;

    public ModularisationSolutionConverter(ObjectiveMapper objectiveMapper, ObjectMapper mapper) {
        this.objectiveMapper = objectiveMapper;
        this.mapper = mapper;
    }

    @Override
    public LouvainModularisationSolution convert(MappingContext<String, LouvainModularisationSolution> mappingContext) {
        return null;
    }
}
