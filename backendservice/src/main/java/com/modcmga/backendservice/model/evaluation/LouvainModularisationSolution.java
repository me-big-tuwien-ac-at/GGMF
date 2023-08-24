package com.modcmga.backendservice.model.evaluation;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Represents a simple modularisation result where each list represents a module in the set.
 */
@Getter
@Setter
public class LouvainModularisationSolution {
    private List<List<String>> modules;
}
