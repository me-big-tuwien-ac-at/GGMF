package com.modcmga.backendservice.dto.application;
/**
 * @Package: com.modcmga.backendservice.dto
 * @Class: EdgeWeight
 * @Author: Jan
 * @Date: 16.05.2022
 */

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the weight of a specific edge.
 */
@Getter
@Setter
public class EdgeWeight {
    private String name;
    private double weight;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonSetter("name")
    public double getWeight() {
        return weight;
    }

    @JsonSetter("weight")
    public void setWeight(double weight) {
        this.weight = weight;
    }
}
