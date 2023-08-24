package com.modcmga.backendservice.infrastructure.export;
/**
 * @Package: com.modcmga.poc.knowledgegraph.export
 * @Class: KnowledgeGraphExport
 * @Author: Jan
 * @Date: 28.10.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.model.parameter.GraphExportParameter;

import java.io.File;
import java.util.List;

/**
 * Exports the Knowlege Graph into a GraphML file. It implements different
 * attributes for the different graph visualisation tools.
 */
public interface KnowledgeGraphExport {

    /**
     * Exports the knowledge graph using the list of modules {@code modules} to
     * modularize the knowledge graph.
     * @param knowledgeGraph the knowledge graph which will be exported.
     * @param modules the list of modules used to modularise the knowledge
     *                graph.
     * @param exportParameter The paramater to configure the export behaviour.
     * @return The GraphML file.
     */
    File exportAsGraphML(
            KnowledgeGraph knowledgeGraph,
            List<Module> modules,
            GraphExportParameter exportParameter);
}
