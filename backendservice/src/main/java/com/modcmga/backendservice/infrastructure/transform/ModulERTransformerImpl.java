package com.modcmga.backendservice.infrastructure.transform;

import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageEncoding;
import com.modcmga.backendservice.domain.geneticalgorithm.encoding.LinearLinkageOperator;
import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import io.jenetics.IntegerGene;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ModulERTransformerImpl implements ModulERTransformer {
    private final String MODULARISABLE_ELEMENT_ENTITY_NAMING = "\\/\\/@modules\\.([0-9]+)/@modularizableElements\\.([0-9]+)";
    private final Pattern modularisableElementsPattern;
    private Map<Integer, List<ModularisableElement>> modules;
    private List<Vertex> vertices;
    private List<Edge> edges;

    public ModulERTransformerImpl() {
        this.modularisableElementsPattern = Pattern.compile(MODULARISABLE_ELEMENT_ENTITY_NAMING);
    }

    @Override
    public LinearLinkageEncoding transform(File modulERFile) {
        // Read file
        try {
            this.modules = new HashMap<>();
            this.vertices = new ArrayList<>();
            this.edges = new ArrayList<>();

            final var dbf = DocumentBuilderFactory.newInstance();
            final var db = dbf.newDocumentBuilder();
            final var document = db.parse(new FileInputStream(modulERFile));

            return createLLE(document);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private LinearLinkageEncoding createLLE(final Document document) {
        // Parse each module to create all modules and their vertices
        final var moduleNodes = document.getElementsByTagName("modules");

        for (int i = 0; i < moduleNodes.getLength(); i++) {
            final var modularisableElements = new ArrayList<ModularisableElement>();
            modules.put(i, modularisableElements);

            final var module = new Module();

            final var moduleNode = moduleNodes.item(i);
            final var modularisableElementNodes = getElementNodes(moduleNode);

            for (int k = 0; k < modularisableElementNodes.size(); k++) {
                final var modularisableElementNode = modularisableElementNodes.get(k);

                final var name = modularisableElementNode.getAttribute("name");

                final var vertex = new Vertex();
                vertex.setVertexNumber(vertices.size());
                vertex.setId(name);
                vertex.setLabel(name);
                vertex.setName(name);

                vertices.add(vertex);
                modularisableElements.add(vertex);
                module.addIndex(vertex.getVertexNumber());
            }
        }

        // Parse each module to create edges
        int index = 0;
        for (int i = 0; i < moduleNodes.getLength(); i++) {
            final var moduleNode = moduleNodes.item(i);
            final var modularisableElementNodes = getElementNodes(moduleNode);

            for (int k = 0; k < modularisableElementNodes.size(); k++) {
                final var modularisableElementNode = modularisableElementNodes.get(k);

                final var sourceVertex = vertices.get(index);

                final var xsiType = modularisableElementNode.getAttribute("xsi:type");

                if (xsiType.equals("moduleeer:RelationshipType")) {
                    // Element is a relationship type.
                    for (var linksToEntitiesNode : getElementNodes(modularisableElementNode)) {
                        var targetVertex =
                                getVertexFromLinksToEntities(linksToEntitiesNode.getAttribute("entity"));

                        final int edgeNumber = vertices.size() + edges.size();

                        var edge = new Edge();
                        edge.setEdgeNumber(edgeNumber);
                        edge.setSourceVertex(sourceVertex);
                        edge.setTargetVertex(targetVertex);

                        edges.add(edge);
                    }
                }

                index++;
            }
        }

        // Create knowledge graph
        final var knowledgeGraph = new KnowledgeGraph(false, document.getDocumentURI());
        vertices.stream()
                .forEach(vertex -> knowledgeGraph.addVertex(vertex));
        edges.stream()
                .forEach(edge -> knowledgeGraph.addEdge(edge));

        // Create modules list
        final var moduleList = modules.values().stream()
                .filter(modularisableElements -> !modularisableElements.isEmpty())
                .map(modularisableElements -> {
                    final var module = new Module();

                    modularisableElements.stream()
                            .forEach(modularisableElement -> module.addIndex(modularisableElement.getIndex()));
                    return module;
                })
                .collect(Collectors.toList());

        final var modularisableElementSize = knowledgeGraph.getModularisableElements().size();
        final var integerGenes = IntStream.range(0, modularisableElementSize)
                .mapToObj(i -> IntegerGene.of(0, modularisableElementSize - 1))
                .collect(Collectors.toList());

        return LinearLinkageOperator.updateIntegerGenes(
                moduleList, new LinearLinkageEncoding(integerGenes, knowledgeGraph));
    }

    private Vertex getVertexFromLinksToEntities(final String link) {
        final var matcher = modularisableElementsPattern.matcher(link);

        if (!matcher.find()) {
            throw new RuntimeException("The link does not follow ModulER standard");
        }

        final int moduleNumber = Integer.parseInt(matcher.group(1));
        final int vertexNumber = Integer.parseInt(matcher.group(2));

        final var module = modules.get(moduleNumber);

        // Assume it is always a vertex
        return (Vertex) module.get(vertexNumber);
    }

    private static List<Element> getElementNodes(final Node node) {
        final var childNodes = node.getChildNodes();
        return IntStream.range(0, childNodes.getLength())
                .mapToObj(i -> childNodes.item(i))
                .filter(childeNode -> childeNode.getNodeType() ==  Node.ELEMENT_NODE)
                .map(childNode -> (Element) childNode)
                .collect(Collectors.toList());
    }
}
