package com.modcmga.backendservice.controller;
/**
 * @Package: com.modcmga.poc.controller
 * @Class: PoCController
 * @Author: Jan
 * @Date: 16.10.2021
 */

import com.google.gson.Gson;
import com.modcmga.backendservice.application.ServiceFacade;
import com.modcmga.backendservice.dto.application.EvaluationInput;
import com.modcmga.backendservice.dto.application.ModularisationInput;
import com.modcmga.backendservice.mapping.EvaluationInputConverter;
import com.modcmga.backendservice.mapping.ModularisationInputConverter;
import com.modcmga.backendservice.model.evaluation.EvaluationParameter;
import com.modcmga.backendservice.model.evaluation.LouvainModularisationSolution;
import com.modcmga.backendservice.model.evaluation.ModularisationSolution;
import com.modcmga.backendservice.model.parameter.ModularisationParameter;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
@RestController
public class BackendController {
    private ServiceFacade serviceFacade;
    private final ModelMapper modelMapper;

    @Autowired
    public BackendController(ServiceFacade serviceFacade,
                             ModularisationInputConverter modularisationInputConverter,
                             EvaluationInputConverter evaluationInputConverter) {
        this.serviceFacade = serviceFacade;

        this.modelMapper = new ModelMapper();
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        this.modelMapper.addConverter(modularisationInputConverter);
        this.modelMapper.addConverter(evaluationInputConverter);
    }

    /**
     * Applies the Genetic Algorithm on the Conceptual Model represented as a
     * Knowledge Graph
     * @param file The uploaded Knowledge Graph as a GraphML file
     * @param modularisationInput The parameter for the Genetic
     *                                         Algorithm
     * @return  HTTP response containing the zip file with the modularisation
     *          result.
     */
    @CrossOrigin
    @PostMapping(
            value = "/apply",
            produces = "application/zip")
    public @ResponseBody HttpEntity<byte[]> applyGeneticAlgorithm(
            @RequestParam("graphmlFile") final MultipartFile file,
            @ModelAttribute final ModularisationInput modularisationInput) {
        byte[] resultZipFileAsBytes = null;
        var headers = new HttpHeaders();
        try {
            System.out.println(String.format(
                    "Start modularising using the following parameter:\n" +
                    "%s",
                    modularisationInput));

            final var mappedApplicationParameter = map(modularisationInput);

            final var temporaryGraphMLFile = new File("knowledgeGraph.graphml");

            try (var outputStream = new FileOutputStream(temporaryGraphMLFile)) {
                outputStream.write(file.getBytes());
            }

            final var modularisationResultFile = serviceFacade.modulariseGraphML(
                    temporaryGraphMLFile, mappedApplicationParameter);

            resultZipFileAsBytes = Files.readAllBytes(modularisationResultFile.toPath());

            temporaryGraphMLFile.delete();
            modularisationResultFile.delete();

            final var contentDisposition = ContentDisposition
                    .attachment()
                    .filename(modularisationResultFile.getName())
                    .build();

            headers.setContentDisposition(contentDisposition);
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        System.out.println("Finished modularisation process");
        return new HttpEntity<>(resultZipFileAsBytes, headers);
    }

    private ModularisationParameter map(final ModularisationInput modularisationInput) {
        final var mappedApplicationParameter =
                modelMapper.map(modularisationInput,
                        ModularisationParameter.class);

        return mappedApplicationParameter;
    }

    @CrossOrigin
    @PostMapping(
            value = "/modularise",
            produces = "application/zip")
    public @ResponseBody HttpEntity<byte[]> modularise(
            @RequestParam final Map<String, MultipartFile> multipartFiles,
            @ModelAttribute final ModularisationInput modularisationInput) {
        System.out.println(String.format(
                "Start modularising using the following parameter:\n" +
                        "%s",
                modularisationInput));

        byte[] resultZipFileAsBytes = null;
        final var headers = new HttpHeaders();

        try {
            final var mappedApplicationParameter = map(modularisationInput);

            final var modularisationResultFile = serviceFacade.modulariseConceptualModel(
                    multipartFiles,
                    mappedApplicationParameter);

            final var contentDisposition = ContentDisposition
                    .attachment()
                    .filename(modularisationResultFile.getName())
                    .build();
            headers.setContentDisposition(contentDisposition);

            resultZipFileAsBytes = Files.readAllBytes(modularisationResultFile.toPath());
            modularisationResultFile.delete();
        } catch (IOException e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        System.out.println("Finished modularisation process");
        return new HttpEntity<>(resultZipFileAsBytes, headers);
    }

    @CrossOrigin
    @PostMapping(
            value = "/evaluateModularisationResult",
            produces = "application/zip")
    public @ResponseBody HttpEntity<byte[]> evaluateModularisationResult(
            @RequestParam("knowledgeGraph") final MultipartFile knowledgeGraphMLMultiPartFile,
            @RequestParam("modularisationResult") final MultipartFile modularisationMultiPartFile,
            @ModelAttribute final EvaluationInput evaluationInput) {
        System.out.println(String.format(
                "Start evaluating the GraphML with the following objectives:\n" +
                        "%s",
                evaluationInput));

        byte[] resultZipFileAsBytes;
        final var headers = new HttpHeaders();

        try {
            final var knowledgeGraphMlFile = createTemporaryFileInApplication(knowledgeGraphMLMultiPartFile);
            final var modularisationFile = createTemporaryFileInApplication(modularisationMultiPartFile);

            final var gson = new Gson();
            ModularisationSolution modularisationSolution;
            try (final var reader = new FileReader(modularisationFile)){
                modularisationSolution = gson.fromJson(reader, ModularisationSolution.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final var evaluationParameter =  modelMapper.map(evaluationInput,
                    EvaluationParameter.class);

             final var evaluationResultFile = serviceFacade.evaluateModularisationResult(
                    knowledgeGraphMlFile, modularisationSolution, evaluationParameter);

            final var contentDisposition = ContentDisposition
                    .attachment()
                    .filename(evaluationResultFile.getName())
                    .build();
            headers.setContentDisposition(contentDisposition);

            resultZipFileAsBytes = Files.readAllBytes(evaluationResultFile.toPath());

            knowledgeGraphMlFile.delete();
            evaluationResultFile.delete();

        } catch (final IOException e) {
            // TODO:
            throw new RuntimeException(e);
        }

        System.out.println("Finished evaluation process");
        return new HttpEntity<>(resultZipFileAsBytes, headers);
    }

    @CrossOrigin
    @PostMapping(
            value = "/evaluateLouvainModularisation",
            produces = "application/zip")
    public @ResponseBody HttpEntity<byte[]> evaluateLouvainModularisation(
            @RequestParam("knowledgeGraphMLMultiPartFile") final MultipartFile knowledgeGraphMLMultiPartFile,
            @RequestParam("modularisation") final MultipartFile modularisationMultiPartFile,
            @ModelAttribute final EvaluationInput evaluationInput) {
        System.out.println(String.format(
                "Start evaluating the GraphML with the following objectives:\n" +
                        "%s",
                evaluationInput));

        byte[] resultZipFileAsBytes;
        final var headers = new HttpHeaders();

        try {
            final var knowledgeGraphMlFile = createTemporaryFileInApplication(knowledgeGraphMLMultiPartFile);
            final var modularisationFile = createTemporaryFileInApplication(modularisationMultiPartFile);

            final var gson = new Gson();
            LouvainModularisationSolution louvainModularisationSolution;
            try (final var reader = new FileReader(modularisationFile)){
                louvainModularisationSolution = gson.fromJson(reader, LouvainModularisationSolution.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final var evaluationParameter =  modelMapper.map(evaluationInput,
                    EvaluationParameter.class);

            final var evaluationResultFile = serviceFacade.evaluateLouvainModularisation(
                    knowledgeGraphMlFile, louvainModularisationSolution, evaluationParameter);

            final var contentDisposition = ContentDisposition
                    .attachment()
                    .filename(evaluationResultFile.getName())
                    .build();
            headers.setContentDisposition(contentDisposition);

            resultZipFileAsBytes = Files.readAllBytes(evaluationResultFile.toPath());

            knowledgeGraphMlFile.delete();
            evaluationResultFile.delete();

        } catch (final IOException e) {
            // TODO:
            throw new RuntimeException(e);
        }

        System.out.println("Finished evaluation process");
        return new HttpEntity<>(resultZipFileAsBytes, headers);
    }

    @CrossOrigin
    @PostMapping(
            value = "/evaluateModulER",
            produces = "application/zip")
    public @ResponseBody HttpEntity<byte[]> evaluateModuleer(
            @RequestParam("monolithFile") final MultipartFile monolithMultiPartFile,
            @RequestParam("modulERFiles") final List<MultipartFile> modulERMultiPartFiles,
            @ModelAttribute final EvaluationInput evaluationInput) {
        System.out.println(String.format(
                "Start evaluating the ModulER files with the following objectives:\n" +
                        "%s",
                evaluationInput));

        byte[] resultZipFileAsBytes;
        final var headers = new HttpHeaders();

        try {
            final var monolithFile = createTemporaryFileInApplication(monolithMultiPartFile);

            final var modulERfiles = new ArrayList<File>();
            for (final var modulERFile : modulERMultiPartFiles) {
                modulERfiles.add(createTemporaryFileInApplication(modulERFile));
            }

            final var evaluationParameter =  modelMapper.map(evaluationInput,
                    EvaluationParameter.class);

            final var evaluationResultFile = serviceFacade.evaluateModulERFiles(monolithFile, modulERfiles, evaluationParameter);
            final var contentDisposition = ContentDisposition
                    .attachment()
                    .filename(evaluationResultFile.getName())
                    .build();
            headers.setContentDisposition(contentDisposition);

            resultZipFileAsBytes = Files.readAllBytes(evaluationResultFile.toPath());

            monolithFile.delete();
            evaluationResultFile.delete();
            modulERfiles.forEach(file -> file.delete());
        } catch (IOException e) {
            // TODO:
            throw new RuntimeException(e);
        }

        System.out.println("Finished evaluation process");
        return new HttpEntity<>(resultZipFileAsBytes, headers);
    }

    private File createTemporaryFileInApplication(final MultipartFile multipartFile) throws IOException {
        final var file = new File(multipartFile.getOriginalFilename());
        FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());

        return file;
    }
}
