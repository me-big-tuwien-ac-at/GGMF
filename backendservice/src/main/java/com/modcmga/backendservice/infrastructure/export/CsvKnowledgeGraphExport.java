package com.modcmga.backendservice.infrastructure.export;
/**
 * @Package: com.modcmga.backendservice.infrastructure.export
 * @Class: CsvKnowledgeGraphExport
 * @Author: Jan
 * @Date: 29.12.2021
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import com.modcmga.backendservice.model.parameter.GraphExportParameter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exports the modularised knowledge graph in a csv file where
 */
@Component("CsvKnowledgeGraphExport")
public class CsvKnowledgeGraphExport implements KnowledgeGraphExport {

    private final ObjectMapper objectMapper;

    public CsvKnowledgeGraphExport() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public File exportAsGraphML(final KnowledgeGraph knowledgeGraph,
                                final List<Module> modules,
                                final GraphExportParameter exportParameter) {
        var fileName = "";
        if (exportParameter.getFolderName() != null) {
            fileName += String.format("%s/", exportParameter.getFolderName());
        }
        fileName += String.format("%s.csv",
                exportParameter.getFileName());

        final File file = new File(fileName);

        try (final var fileWriter = new FileWriter(file);
             final var bufferedWriter = new BufferedWriter(fileWriter)) {

            final var moduleMap = new HashMap<String, List<ModularisableElement>>();

            int moduleNumber = 0;
            for (var module : modules) {
                final var modularisableElements =
                        ModuleInformationProvider.getModularisableElements(module, knowledgeGraph);

                var modularisableElementsOfModule = new ArrayList<ModularisableElement>();

                var verticesOfModule = modularisableElements
                        .stream()
                        .filter(modularisableElement -> modularisableElement instanceof Vertex)
                        .collect(Collectors.toList());

                var edgesOfModule = modularisableElements
                        .stream()
                        .filter(modularisableElement -> modularisableElement instanceof Edge)
                        .collect(Collectors.toList());

                modularisableElementsOfModule.addAll(verticesOfModule);
                modularisableElementsOfModule.addAll(edgesOfModule);

                String moduleKey = String.format("Module %d", moduleNumber++);
                moduleMap.put(moduleKey, modularisableElementsOfModule);
            }

            bufferedWriter.write(objectMapper.writeValueAsString(moduleMap));

        } catch (IOException e) {
            // TODO: handle write error
            e.printStackTrace();
        }

        return file;
    }
}
