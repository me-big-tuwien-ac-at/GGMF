package com.modcmga.backendservice.infrastructure.export;
/**
 * @Package: com.modcmga.poc.knowledgegraph.export
 * @Class: YedKnowledgeGraphExport
 * @Author: Jan
 * @Date: 28.10.2021
 */

import com.modcmga.backendservice.domain.geneticalgorithm.module.Module;
import com.modcmga.backendservice.domain.geneticalgorithm.module.ModuleInformationProvider;
import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
import com.modcmga.backendservice.model.parameter.GraphExportParameter;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Exports the Knowledge Graph as a GraphML file which contains yEd attributes
 * for visualisation.
 */
@Component("GraphMLKnowledgeGraphExport")
public class GraphMLKnowledgeGraphExport implements KnowledgeGraphExport {

    public final static int SHAPE_WIDTH = 30;
    public final static int SHAPE_HEIGHT = 30;
    public final static double LINE_WIDTH = 1.0d;
    public final static String HEX_BLACK_COLOR = "#000000";
    public final static int FONT_SIZE = 12;
    public final static double FONT_HEIGHT = 18.7d;
    public final static String NODE_FORM = "rectangle";

    @Override
    public File exportAsGraphML(KnowledgeGraph knowledgeGraph,
                                List<Module> modules,
                                GraphExportParameter exportParameter) {
        var fileName = "";
        if (exportParameter.getFolderName() != null) {
            fileName += String.format("%s/", exportParameter.getFolderName());
        }
        fileName += String.format("%s.graphml",
                exportParameter.getFileName());

        File file = new File(fileName);

        createYeDGraphMLFile(knowledgeGraph, file, modules);

        return file;
    }

    private void createYeDGraphMLFile(KnowledgeGraph knowledgeGraph, File file, List<Module> modules) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();

            Element root = defineGraphMLRoot(document);
            document.appendChild(root);

            defineYeDDefinition(document, root);

            Element graph = document.createElement("graph");
            graph.setAttribute("id", knowledgeGraph.getId());
            graph.setAttribute("edgedefault", knowledgeGraph.isDirectedGraph() ? "directed" : "undirected");

            var edgesWithOneVertexNotInSameModule = new HashSet<Edge>();

            // for each module create subgraph
            for (int i = 0; i < modules.size(); i++) {
                var module = modules.get(i);

                var modularisableElements = ModuleInformationProvider.getModularisableElements(module, knowledgeGraph);

                var verticesOfModule = modularisableElements
                        .stream()
                        .filter(Vertex.class::isInstance)
                        .map(Vertex.class::cast)
                        .collect(Collectors.toList());

                var edgesOfModule = modularisableElements
                        .stream()
                        .filter(Edge.class::isInstance)
                        .map(Edge.class::cast)
                        .collect(Collectors.toList());

                var completeEdgeInModule = edgesOfModule
                        .stream()
                        .filter(edge -> {
                            var sourceVertex = edge.getSourceVertex();
                            var targetVertex = edge.getTargetVertex();

                            return module.isIndexInModule(sourceVertex.getIndex()) &&
                                    module.isIndexInModule(targetVertex.getIndex());
                        })
                        .collect(Collectors.toList());

                if (!verticesOfModule.isEmpty() || !completeEdgeInModule.isEmpty()) {
                    var subGraphNode = document.createElement("node");
                    subGraphNode.setAttribute("id", String.format("Module %d", i));

                    var subGraph = document.createElement("graph");
                    subGraph.setAttribute("edgedefault", knowledgeGraph.isDirectedGraph() ? "directed" : "undirected");

                    for (var modularisableElement : modularisableElements) {
                        if (modularisableElement instanceof Vertex) {
                            subGraph.appendChild(createVertexGraphMLElement(
                                    document,
                                    (Vertex) modularisableElement));
                        } else if (modularisableElement instanceof Edge) {
                            var edgeInModule = (Edge) modularisableElement;
                            var sourceVertex = edgeInModule.getSourceVertex();
                            var targetVertex = edgeInModule.getTargetVertex();

                            if (module.isIndexInModule(sourceVertex.getIndex()) &&
                                    module.isIndexInModule(targetVertex.getIndex())) {
                                subGraph.appendChild(createEdgeGraphMLElement(document, (Edge) modularisableElement));
                            } else {
                                edgesWithOneVertexNotInSameModule.add(edgeInModule);
                            }
                        }
                    }

                    subGraphNode.appendChild(subGraph);
                    graph.appendChild(subGraphNode);
                } else if (completeEdgeInModule.isEmpty()){
                    edgesWithOneVertexNotInSameModule.addAll(edgesOfModule);
                }
            }

            // Create edges in graph where one vertex is not in the same module.
            // Therefore, it connects 2 modules
            for (var edgeWithOneVertexNotInSameModule : edgesWithOneVertexNotInSameModule ) {
                graph.appendChild(createEdgeGraphMLElement(
                        document,
                        edgeWithOneVertexNotInSameModule));
            }

            root.appendChild(graph);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(file);

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, streamResult);

        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void defineYeDDefinition(Document document, Element root) {
        Element attributeName = document.createElement("key");
        attributeName.setAttribute("attr.name", "Description");
        attributeName.setAttribute("attr.type", "string");
        attributeName.setAttribute("for", "graph");
        attributeName.setAttribute("id", "d0");
        root.appendChild(attributeName);

        Element portgraphicsAttribute = document.createElement("key");
        portgraphicsAttribute.setAttribute("id", "d1");
        portgraphicsAttribute.setAttribute("for", "port");
        portgraphicsAttribute.setAttribute("yfiles.type", "portgraphics");
        root.appendChild(portgraphicsAttribute);

        Element portgeometryAttribute = document.createElement("key");
        portgeometryAttribute.setAttribute("id", "d2");
        portgeometryAttribute.setAttribute("for", "port");
        portgeometryAttribute.setAttribute("yfiles.type", "portgeometry");
        root.appendChild(portgeometryAttribute);

        Element portuserdataAttribute = document.createElement("key");
        portuserdataAttribute.setAttribute("id", "d3");
        portuserdataAttribute.setAttribute("for", "port");
        portuserdataAttribute.setAttribute("yfiles.type", "portuserdata");
        root.appendChild(portuserdataAttribute);

        Element urlAttribute = document.createElement("key");
        urlAttribute.setAttribute("id", "d4");
        urlAttribute.setAttribute("attr.name", "url");
        urlAttribute.setAttribute("attr.type", "string");
        urlAttribute.setAttribute("for", "node");
        root.appendChild(urlAttribute);

        Element descriptionAttribute = document.createElement("key");
        descriptionAttribute.setAttribute("id", "d5");
        descriptionAttribute.setAttribute("attr.name", "description");
        descriptionAttribute.setAttribute("attr.type", "string");
        descriptionAttribute.setAttribute("for", "node");
        root.appendChild(descriptionAttribute);

        Element nodeAttribute = document.createElement("key");
        nodeAttribute.setAttribute("id", "d6");
        nodeAttribute.setAttribute("for", "node");
        nodeAttribute.setAttribute("yfiles.type", "nodegraphics");
        root.appendChild(nodeAttribute);

        Element graphMLAttribute = document.createElement("key");
        graphMLAttribute.setAttribute("id", "d7");
        graphMLAttribute.setAttribute("for", "graphML");
        graphMLAttribute.setAttribute("yfiles.type", "resources");
        root.appendChild(graphMLAttribute);

        Element edgeURLAttribute = document.createElement("key");
        edgeURLAttribute.setAttribute("id", "d8");
        edgeURLAttribute.setAttribute("for", "edge");
        edgeURLAttribute.setAttribute("attr.name", "url");
        edgeURLAttribute.setAttribute("attr.type", "string");
        root.appendChild(edgeURLAttribute);

        Element edgeDescriptionAttribute = document.createElement("key");
        edgeDescriptionAttribute.setAttribute("id", "d9");
        edgeDescriptionAttribute.setAttribute("for", "edge");
        edgeDescriptionAttribute.setAttribute("attr.name", "description");
        edgeDescriptionAttribute.setAttribute("attr.type", "string");
        root.appendChild(edgeDescriptionAttribute);

        Element edgeAttribute = document.createElement("key");
        edgeAttribute.setAttribute("for", "edge");
        edgeAttribute.setAttribute("id", "d10");
        edgeAttribute.setAttribute("yfiles.type", "edgegraphics");
        root.appendChild(edgeAttribute);

        Element nodeClassNameAttribute = document.createElement("key");
        nodeClassNameAttribute.setAttribute("id", "ClassName");
        nodeClassNameAttribute.setAttribute("for", "node");
        nodeClassNameAttribute.setAttribute("attr.name", "ClassName");
        nodeClassNameAttribute.setAttribute("attr.type", "string");
        root.appendChild(nodeClassNameAttribute);

        Element labelAttribute = document.createElement("key");
        labelAttribute.setAttribute("id", "Label");
        labelAttribute.setAttribute("for", "node");
        labelAttribute.setAttribute("attr.name", "Label");
        labelAttribute.setAttribute("attr.type", "string");
        root.appendChild(labelAttribute);

        Element nameAttribute = document.createElement("key");
        nameAttribute.setAttribute("id", "name");
        nameAttribute.setAttribute("for", "node");
        nameAttribute.setAttribute("attr.name", "name");
        nameAttribute.setAttribute("attr.type", "string");
        root.appendChild(nameAttribute);

        Element xmiIDAttribute = document.createElement("key");
        xmiIDAttribute.setAttribute("id", "xmi_id");
        xmiIDAttribute.setAttribute("for", "node");
        xmiIDAttribute.setAttribute("attr.name", "xmi_id");
        xmiIDAttribute.setAttribute("attr.type", "string");
        root.appendChild(xmiIDAttribute);

        Element xsiTypeAttribute = document.createElement("key");
        xsiTypeAttribute.setAttribute("id", "xsi_type");
        xsiTypeAttribute.setAttribute("for", "node");
        xsiTypeAttribute.setAttribute("attr.name", "xsi_type");
        xsiTypeAttribute.setAttribute("attr.type", "string");
        root.appendChild(xsiTypeAttribute);

        Element edgeReferenceNameAttribute = document.createElement("key");
        edgeReferenceNameAttribute.setAttribute("id", "ReferenceName");
        edgeReferenceNameAttribute.setAttribute("for", "edge");
        edgeReferenceNameAttribute.setAttribute("attr.name", "ReferenceName");
        edgeReferenceNameAttribute.setAttribute("attr.type", "string");
        root.appendChild(edgeReferenceNameAttribute);
    }

    private Element defineGraphMLRoot(Document document) {
        Element root = document.createElement("graphml");
        root.setAttribute("xmlns", "http://graphml.graphdrawing.org/xmlns");
        root.setAttribute("xmlns:java", "http://www.yworks.com/xml/yfiles-common/1.0/java");
        root.setAttribute("xmlns:sys", "http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0");
        root.setAttribute("xmlns:x", "http://www.yworks.com/xml/yfiles-common/markup/2.0");
        root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        root.setAttribute("xmlns:y", "http://www.yworks.com/xml/graphml");
        root.setAttribute("xmlns:yed", "http://www.yworks.com/xml/yed/3");
        root.setAttribute("xsi:schemaLocation", "http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd");

        return root;
    }

    private Element createVertexGraphMLElement(Document document, Vertex vertex) {
        Element node = document.createElement("node");
        node.setAttribute("id", vertex.getId());

        if (vertex.getClassName() != null) {
            Element classNameElement = document.createElement("data");
            classNameElement.setAttribute("key", "ClassName");
            classNameElement.appendChild(document.createTextNode(vertex.getClassName()));
            node.appendChild(classNameElement);
        }

        if (vertex.getLabel() != null) {
            Element labelElement = document.createElement("data");
            labelElement.setAttribute("key", "Label");
            labelElement.appendChild(document.createTextNode(vertex.getLabel()));
            node.appendChild(labelElement);
        }

        if (vertex.getName() != null) {
            Element nameElement = document.createElement("data");
            nameElement.setAttribute("key", "name");
            nameElement.appendChild(document.createTextNode(vertex.getName()));
            node.appendChild(nameElement);
        }

        /**
         *  TODO: do this for attributes map
         *         if (vertex.getXmiId() != null) {
         *             Element xmiIdElement = document.createElement("data");
         *             xmiIdElement.setAttribute("key", "xmi_id");
         *             xmiIdElement.appendChild(document.createTextNode(vertex.getXmiId()));
         *             node.appendChild(xmiIdElement);
         *         }
         */

        Element d6element = document.createElement("data");
        d6element.setAttribute("key", "d6");
        d6element.appendChild(createD6Element(
                document,
                vertex.getLabel()));
        node.appendChild(d6element);

        return node;
    }

    private Element createD6Element(Document document, String label) {
        Element shapeNode = document.createElement("y:ShapeNode");

        Element geometrieElement = document.createElement("y:Geometry");
        geometrieElement.setAttribute("width", Integer.toString(SHAPE_WIDTH));
        geometrieElement.setAttribute("height", Integer.toString(SHAPE_HEIGHT));
        shapeNode.appendChild(geometrieElement);

        Element fillElement = document.createElement("y:Fill");
        fillElement.setAttribute("transparent", "false");
        shapeNode.appendChild(fillElement);

        Element borderStyleElement = document.createElement("y:BorderStyle");
        borderStyleElement.setAttribute("color", HEX_BLACK_COLOR);
        borderStyleElement.setAttribute("raised", "false");
        borderStyleElement.setAttribute("type", "line");
        borderStyleElement.setAttribute("width", Double.toString(LINE_WIDTH));
        shapeNode.appendChild(borderStyleElement);

        Element nodeLabel = document.createElement("y:NodeLabel");
        nodeLabel.setAttribute("alignment", "center");
        nodeLabel.setAttribute("autoSizePolicy", "content");
        nodeLabel.setAttribute("fontFamily", "Dialog");
        nodeLabel.setAttribute("fontSize", Integer.toString(FONT_SIZE));
        nodeLabel.setAttribute("fontStyle", "plain");
        nodeLabel.setAttribute("hasBackgroundColor", "false");
        nodeLabel.setAttribute("hasLineColor", "false");
        nodeLabel.setAttribute("height", Double.toString(FONT_HEIGHT));
        nodeLabel.setAttribute("horizontalTextPosition", "center");
        nodeLabel.setAttribute("iconTextGap", "4");
        nodeLabel.setAttribute("modelName", "custom");
        nodeLabel.setAttribute("verticalTextPosition", "bottom");
        nodeLabel.setAttribute("visible", "true");
        nodeLabel.setAttribute("textColor", HEX_BLACK_COLOR);
        nodeLabel.setAttribute("width", "40");
        nodeLabel.appendChild(document.createTextNode(label));
        shapeNode.appendChild(nodeLabel);

        Element shapeElement = document.createElement("y:Shape");
        shapeElement.setAttribute("type", NODE_FORM);
        shapeNode.appendChild(shapeElement);

        return shapeNode;
    }

    private Element createEdgeGraphMLElement(Document document, Edge edge) {
        Element node = document.createElement("edge");
        node.setAttribute("source", edge.getSourceVertex().getId());
        node.setAttribute("target", edge.getTargetVertex().getId());

        if (edge.getReferenceName() != null) {
            Element classNameElement = document.createElement("data");
            classNameElement.setAttribute("key", "ReferenceName");
            classNameElement.appendChild(document.createTextNode(edge.getReferenceName()));
            node.appendChild(classNameElement);
        }

        if (edge.getLabel() != null) {
            Element labelElement = document.createElement("data");
            labelElement.setAttribute("key", "Label");
            labelElement.appendChild(document.createTextNode(edge.getLabel()));
            node.appendChild(labelElement);
        }

        Element d6element = document.createElement("data");
        d6element.setAttribute("key", "d10");
        d6element.appendChild(createD10Element(
                document,
                edge.getLabel()));
        node.appendChild(d6element);

        return node;
    }

    private Element createD10Element(Document document, String label) {
        Element shapeNode = document.createElement("y:PolyLineEdge");

        Element yPathElement = document.createElement("y:Path");
        yPathElement.setAttribute("sx", "0.0");
        yPathElement.setAttribute("sy", "0.0");
        yPathElement.setAttribute("tx", "0.0");
        yPathElement.setAttribute("ty", "0.0");
        shapeNode.appendChild(yPathElement);

        Element yLineStyleElement = document.createElement("y:LineStyle");
        yLineStyleElement.setAttribute("type", "line");
        yLineStyleElement.setAttribute("tx", "0.0");
        yLineStyleElement.setAttribute("ty", "0.0");
        shapeNode.appendChild(yLineStyleElement);

        Element yArrowsElement = document.createElement("y:Arrows");
        yArrowsElement.setAttribute("source", "none");
        yArrowsElement.setAttribute("target", "standard");
        shapeNode.appendChild(yArrowsElement);

        Element yEdgeLabel = document.createElement("y:EdgeLabel");
        yEdgeLabel.setAttribute("alignment", "center");
        yEdgeLabel.setAttribute("fontFamily", "Dialog");
        yEdgeLabel.setAttribute("fontSize", Integer.toString(12));
        yEdgeLabel.setAttribute("fontStyle", "plain");
        yEdgeLabel.setAttribute("hasBackgroundColor", "false");
        yEdgeLabel.setAttribute("hasLineColor", "false");
        yEdgeLabel.setAttribute("height", Double.toString(FONT_HEIGHT));
        yEdgeLabel.setAttribute("iconTextGap", Integer.toString(4));
        yEdgeLabel.setAttribute("modelName", "custom");
        yEdgeLabel.setAttribute("preferredPlacement", "anywhere");
        yEdgeLabel.setAttribute("modelName", "custom");
        yEdgeLabel.setAttribute("ratio", "0.5");
        yEdgeLabel.setAttribute("verticalTextPosition", "bottom");
        yEdgeLabel.setAttribute("visible", "true");
        yEdgeLabel.appendChild(document.createTextNode(label));
        shapeNode.appendChild(yEdgeLabel);

        Element yBendStyleElement = document.createElement("y:BendStyle");
        yBendStyleElement.setAttribute("smoothed", "false");
        shapeNode.appendChild(yBendStyleElement);

        return shapeNode;
    }
}
