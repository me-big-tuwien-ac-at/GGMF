package com.modcmga.backendservice.model.parameter;
/**
 * @Package: com.modcmga.poc.dto
 * @Class: GeneticAlgorithmApplicationInput
 * @Author: Jan
 * @Date: 17.10.2021
 */

import com.modcmga.backendservice.domain.objective.ObjectiveSetup;
import com.modcmga.backendservice.model.conceptualmodel.ConceptualModelData;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * This class consists of various parameters which are necessary to modularise
 * the conceptual model. The parameter can be passed by the service call or
 * are determined during in one of the steps during the modularisation.
 */
@Data
@NoArgsConstructor
public class ModularisationParameter {
    private GeneticAlgorithmParameter geneticAlgorithmParameter;
    private ConceptualModelData conceptualModelData;
    private MutationWeight mutationWeight;
    private ObjectiveSetup objectiveSetup;
    private Map<String, Double> edgeWeights;

    @Override
    public String toString() {
        var edgeWeightString = edgeWeights != null ?
                edgeWeights.toString() :
                "{default=1}";

        return String.format("%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s\n" +
                        "%s",
                this.conceptualModelData,
                this.mutationWeight,
                this.geneticAlgorithmParameter,
                this.objectiveSetup,
                edgeWeightString);
    }
}
