package com.modcmga.backendservice.application;
/**
 * @Package: com.modcmga.backendservice.service
 * @Class: ServiceFacade
 * @Author: Jan
 * @Date: 06.02.2022
 */

import com.modcmga.backendservice.model.evaluation.EvaluationParameter;
import com.modcmga.backendservice.model.evaluation.LouvainModularisationSolution;
import com.modcmga.backendservice.model.evaluation.ModularisationSolution;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * The facade is abstracts the complex interactions between the various services.
 */
public interface ServiceFacade {
    /**
     * Modularises the graphML and creates a zip file containing the
     * modularisation result relevant files.
     * @param knowledgeGraphGraphMlFile the knowledge graph representation as a GraphML file
     * @param modularisationParameter the application parameter used for the
     *                             modularisation process
     * @return zip file containing the modularisation result relevant files.
     * @throws IOException is thrown when the I/O operation fails.
     */
    File modulariseGraphML(File knowledgeGraphGraphMlFile,
                           ModularisationParameter modularisationParameter) throws IOException;

    /**
     * Modularises the graphML and creates a zip file containing the modularisation result relevant files.
     * @param multipartFiles the input files from the HTTP request
     * @param modularisationParameter the modularisation parameter used for the modularisation process
     * @return zip file containing the modularisation result relevant files.
     * @throws IOException is thrown when the I/O operation fails.
     */
    File modulariseConceptualModel(
            Map<String, MultipartFile> multipartFiles, // TODO: refactor only use files from here
            ModularisationParameter modularisationParameter) throws IOException;

    /**
     * Evaluates the louvain modularisation solution in the {@code modularisationFile} file and creates a zip file containing
     * the evaluation result relevant files.
     * @param knowledgeGraphGraphMlFile the knowledge graph representation as a GraphML file
     * @param modularisationSolution the modularisation solution
     * @param evaluationParameter the evaluation parameter
     * @return zip file containing the evaluation result relevant files.
     */
    File evaluateModularisationResult(File knowledgeGraphGraphMlFile,
                                       ModularisationSolution modularisationSolution,
                                       EvaluationParameter evaluationParameter) throws IOException;

    /**
     * Evaluates the louvain modularisation solution in the {@code modularisationFile} file and creates a zip file containing
     * the evaluation result relevant files.
     * @param knowledgeGraphGraphMlFile the knowledge graph representation as a GraphML file
     * @param louvainModularisationSolution the modularisation solution
     * @param evaluationParameter the evaluation parameter
     * @return zip file containing the evaluation result relevant files.
     */
    File evaluateLouvainModularisation(File knowledgeGraphGraphMlFile,
                                       LouvainModularisationSolution louvainModularisationSolution,
                                       EvaluationParameter evaluationParameter) throws IOException;

    /**
     * Evaluates the modulER files and creates a zip file containing the evaluation result relevant files.
     * @param monolithFile the file containing the monolith as a ModulER file
     * @param files the ModulER file
     * @param evaluationParameter the evaluation parameter
     * @return zip file containing the evaluation result relevant files.
     */
    File evaluateModulERFiles(File monolithFile, List<File> files, EvaluationParameter evaluationParameter)
            throws IOException;
}
