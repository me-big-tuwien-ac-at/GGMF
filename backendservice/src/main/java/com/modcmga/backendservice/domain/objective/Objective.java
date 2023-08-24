package com.modcmga.backendservice.domain.objective;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.fitnessfunction.objective
 * @Class: Objective
 * @Author: Jan
 * @Date: 23.01.2022
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.enums.ObjectiveType;
import io.jenetics.Optimize;

import java.util.List;

/**
 * Represents the objective for optimisation which is used for the
 * multi-objective function. The objective depends on the list of modules.
 */
public abstract class Objective {
    protected double weight;
    protected KnowledgeGraph knowledgeGraph;
    protected int numberOfElementsPerModule;
    protected boolean isUMLDiagram;
    /**
     * Returns how the objective is optimised (maximised, minimised).
     * @return how the objective is optimised.
     */
    public abstract Optimize getOptimize();

    /**
     * Returns the value which represents the state of the objective depending
     * on {@code modules}.
     * @param modules the modules
     * @return Returns the value which represents the state of the objective.
     */
    public abstract double calculateValue(List<Module> modules);

    /**
     * Returns the name of the objective.
     * @return the name of the objective.
     */
    public abstract String objectiveText();

    /**
     * Returns the objective type.
     * @return the objective type.
     */
    public abstract ObjectiveType objectiveType();

    public double getWeight() {
        return weight;
    }

    /**
     * Sets the weight for the objective, which can be used to influence the objective.
     *
     * @param weight the weight influencing the objective.
     */
    public void setWeight(final double weight) {
        this.weight = weight;
    }

    /**
     * Sets the knowledge graph, which can be used for determining the objectives.
     *
     * @param knowledgeGraph the knowledge graph for determining objectives
     */
    public void setKnowledgeGraph(final KnowledgeGraph knowledgeGraph) {
        this.knowledgeGraph = knowledgeGraph;
    }

    /**
     * Returns true, if the number of elements is needed for the objective.
     * @return true, if the number of elements is needed for the objective.
     */
    public boolean isNumberOfElementsNeeded() {
        return false;
    }

    public void setNumberOfElementsPerModule(int numberOfElementsPerModule) {
        this.numberOfElementsPerModule = numberOfElementsPerModule;
    }

    /**
     * Returns true, if the information is needed if the conceptual model is a UML diagram.
     * @return true, if the information is needed if the conceptual model is a UML diagram.
     */
    public boolean isUMLInformationNeeded() {
        return false;
    }

    public void setUMLDiagram(boolean UMLDiagram) {
        isUMLDiagram = UMLDiagram;
    }

    /**
     * Placeholder for preparing the objective.
     */
    public void prepare() {

    }

    @Override
    public String toString() {
        return String.format(
                "optimization; %s; objectiveName; %s; weight: %,.2f",
                this.getOptimize(),
                this.objectiveText(),
                this.weight);
    }
}
