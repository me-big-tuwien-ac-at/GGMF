package com.modcmga.backendservice.model.evaluation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ModularisationSolution {
    private Map<String, List<ModuleElement>> moduleSolutions;
}
