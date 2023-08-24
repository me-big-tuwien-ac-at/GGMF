package com.modcmga.backendservice.util;
/**
 * @Package: com.modcmga.backendservice.util
 * @Class: GraphMLParser
 * @Author: Jan
 * @Date: 18.11.2021
 */

import com.modcmga.backendservice.domain.knowledgegraph.Edge;
import com.modcmga.backendservice.domain.knowledgegraph.KnowledgeGraph;
import com.modcmga.backendservice.domain.knowledgegraph.Vertex;
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

/**
 * Provides functionalities to parse a GraphML file
 */
@Component
public class GraphMLParser {
    /**
     * Parses the GraphML file and its content to return the Knowledge Graph.
     * @param graphMLFIle The GraphML file.
     * @return the Knowledge Graph with the parsed information
     */
    public KnowledgeGraph parseGraphMLFile(final File graphMLFIle) {
        try {
            final var dbf = DocumentBuilderFactory.newInstance();
            final var db = dbf.newDocumentBuilder();
            final var document = db.parse(new FileInputStream(graphMLFIle));

            return createKnowledgeGraph(document);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }

    private KnowledgeGraph createKnowledgeGraph(final Document document) {
        final var attributes = parseAttributes(document);

        final var node = document.getElementsByTagName("graph").item(0);

        KnowledgeGraph knowledgeGraph = null;

        if (node.getNodeType() == Node.ELEMENT_NODE) {
            final var element = (Element) node;

            final var edgeDefault = element.getAttribute("edgedefault");
            final var isDirected = edgeDefault.equals("directed");

            final var id = element.getAttribute("id");

            knowledgeGraph = new KnowledgeGraph(isDirected, id);
        }

        int moduleIndex = 0;
        final var vertices = new HashSet<Vertex>();
        final var vertexNodes = document.getElementsByTagName("node");
        for (int i = 0; i < vertexNodes.getLength(); i++) {
            final var vertexNode = vertexNodes.item(i);

            if (vertexNode.getNodeType() == Node.ELEMENT_NODE) {
                final var parsedVertex = parseVertexInformation((Element) vertexNode, attributes);

                final var containsVertex = vertices
                        .stream()
                        .anyMatch(vertex -> vertex.equals(parsedVertex));

                if (!containsVertex) {
                    parsedVertex.setVertexNumber(moduleIndex);
                    vertices.add(parsedVertex);

                    knowledgeGraph.addVertex(parsedVertex);

                    moduleIndex++;
                }
            }
        }

        final var edges = new HashSet<Edge>();
        final var edgeNodes = document.getElementsByTagName("edge");
        for (int i = 0; i < edgeNodes.getLength(); i++) {
            final var edgeNode = edgeNodes.item(i);

            if (edgeNode.getNodeType() == Node.ELEMENT_NODE) {
                final var parsedEdge = parseEdgeInformation((Element) edgeNode, vertices);

                final var containsEdge = edges
                        .stream()
                        .anyMatch(edge -> edge.equals(parsedEdge));

                if (!containsEdge) {
                    parsedEdge.setEdgeNumber(moduleIndex);
                    edges.add(parsedEdge);

                    knowledgeGraph.addEdge(parsedEdge);

                    moduleIndex++;
                }
            }
        }

        return knowledgeGraph;
    }

    private List<String> parseAttributes(final Document document) {
        final var attributes = new ArrayList<String>();

        final var keyElements = document.getElementsByTagName("key");

        for (int i = 0; i < keyElements.getLength(); i++) {
            if (keyElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                final var keyNodeElement = (Element) keyElements.item(i);

                if (keyNodeElement.hasAttribute("attr.name")) {
                    attributes.add(keyNodeElement.getAttribute("attr.name"));
                }
            }
        }

        return attributes;
    }

    private Vertex parseVertexInformation(final Element vertexElement, final List<String> attributes) {
        final var vertex = new Vertex();

        final var attributesMap = new HashMap<String, String>();

        vertex.setId(vertexElement.getAttribute("id"));
        vertex.setConceptualModelAttributes(attributesMap);

        final var dataElements = vertexElement.getElementsByTagName("data");

        for (int i = 0; i < dataElements.getLength(); i++) {
            if (dataElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                final var dataNodeElement = (Element) dataElements.item(i);

                final var keyType = dataNodeElement.getAttribute("key");

                if (keyType.equals("ClassName")) {
                    vertex.setClassName(dataNodeElement.getTextContent());
                } else if (keyType.equals("Label")) {
                    vertex.setLabel(dataNodeElement.getTextContent());
                } else if (keyType.equals("name")) {
                    vertex.setName(dataNodeElement.getTextContent());
                }

                if (attributes.contains(keyType) ) {
                    final var firstChild = dataNodeElement.getFirstChild();
                    if(firstChild != null)
                        attributesMap.put(keyType, firstChild.getNodeValue());
                }
            }
        }

        return vertex;
    }

    private Edge parseEdgeInformation(final Element edgeElement, final Set<Vertex> vertices) {
        final var edge = new Edge();
        edge.setWeight(1);

        final var sourceVertexId = edgeElement.getAttribute("source");
        final var sourceVertex = vertices.stream()
                .filter(v -> v.getId().equals(sourceVertexId))
                .findAny()
                .get();
        edge.setSourceVertex(sourceVertex);

        final var targetVertexId = edgeElement.getAttribute("target");
        final var targetVertex = vertices.stream()
                .filter(v -> v.getId().equals(targetVertexId))
                .findAny()
                .get();
        edge.setTargetVertex(targetVertex);

        final var dataElements = edgeElement.getElementsByTagName("data");

        for (int k = 0; k < dataElements.getLength(); k++) {
            if (dataElements.item(k).getNodeType() == Node.ELEMENT_NODE) {
                final var dataNodeElement = (Element) dataElements.item(k);

                final var keyType = dataNodeElement.getAttribute("key");

                if (keyType.equals("ReferenceName")) {
                    edge.setReferenceName(dataNodeElement.getTextContent());
                } else if (keyType.equals("Label")) {
                    edge.setLabel(dataNodeElement.getTextContent());
                } else if (keyType.equals("Label")) {
                    edge.setLabel(dataNodeElement.getTextContent());
                } else if (keyType.equals("Label")) {
                    edge.setLabel(dataNodeElement.getTextContent());
                } else if (keyType.equals("d6")) {
                    edge.setD6(dataNodeElement.getTextContent());
                }
            }
        }

        return edge;
    }
}
