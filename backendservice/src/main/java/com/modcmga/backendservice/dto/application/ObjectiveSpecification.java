package com.modcmga.backendservice.dto.application;

import com.modcmga.backendservice.model.enums.ObjectiveType;
import lombok.Getter;
import lombok.Setter;

/**
 * Provides data about the objectives.
 */
@Getter
@Setter
public class ObjectiveSpecification {
    private ObjectiveType objectiveType;
    private boolean selected;
    private double weight;
}
