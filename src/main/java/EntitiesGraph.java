
import Model.EntitiesFactory;
import Model.Entity;
import net.sf.jsqlparser.JSQLParserException;
import org.jgrapht.Graphs;
import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class EntitiesGraph {
    private static final Logger logger = LoggerFactory.getLogger(EntitiesGraph.class);

    private ListenableGraph<Entity, DefaultEdge> entityGraph = new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));
    public static ArrayList<Exception> exceptionArrayList = new ArrayList<>();

    public EntitiesGraph() throws SQLException, JSQLParserException {
        ArrayList<Entity> allEntities = EntitiesFactory.getEntities();
        entityGraph = buildGraph(allEntities);
    }

    public EntitiesGraph(Integer srcid) throws SQLException, JSQLParserException {
        ArrayList<Entity> someEntities = EntitiesFactory.getEntities(srcid);
        entityGraph = buildGraph(someEntities);
    }

    public EntitiesGraph(ArrayList<Entity> entities) throws SQLException, JSQLParserException {
        entityGraph = buildGraph(entities);
    }

    private ListenableGraph<Entity, DefaultEdge> buildGraph(ArrayList<Entity> entities) throws SQLException, JSQLParserException {
        //entities.stream().peek(entityGraph::addVertex).forEach(entity -> entity.lookForTablesInRequest());
        entities.stream().forEach(entity -> entity.lookForTablesInRequest());
        //Model.ScriptEntitiesFactory.getScriptEntities().forEach(entityGraph::addVertex);

//        for (Model.Entity entity : entities) {
//            for (Model.ScriptEntity scriptEntity : entity.getSelectScriptEntities()) {
//                if (scriptEntity.getRelatedEntity() != null && !scriptEntity.getRelatedEntity().equals(entity)) {
//                    entityGraph.addEdge(entity, scriptEntity.getRelatedEntity());
//                } else {
//                    entities.stream()
//                            .filter(e -> e.makesScriptEntities(scriptEntity))
//                            .filter(e -> e.equals(entity))
//                            .filter(e -> entityGraph.containsEdge(entity, e))
//                            .forEach(e -> entityGraph.addEdge(entity, e));
//                }
//            }
//        }
        return entityGraph;
    }

    public String getXmlGraph(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().newDocument();

            Element root = doc.createElement("data");
            //root.setAttribute("xmlns", "http://www.javacore.ru/schemas/");
            doc.appendChild(root);

            for (Entity entity:entityGraph.vertexSet()){
                Element item1 = doc.createElement("nodes");
                //<nodes ID="7839" LABEL="KING" COLORVALUE="10" SIZEVALUE="5000"/>
                item1.setAttribute("id", String.valueOf(entity.getIndex()));
                item1.setAttribute("label", entity.toString());
                item1.setAttribute("srcid", entity.getSrc_id().toString());
                item1.setAttribute("isroot", Graphs.vertexHasPredecessors(entityGraph ,entity)? "true" : "false");
                root.appendChild(item1);
            }

            for (DefaultEdge edge:entityGraph.edgeSet()){
                Element item1 = doc.createElement("links");
                //<links FROMID="7839" TOID="7839" STYLE="solid"/>
                item1.setAttribute("fromid", String.valueOf(entityGraph.getEdgeSource(edge).getIndex()));
                item1.setAttribute("toid", String.valueOf(entityGraph.getEdgeTarget(edge).getIndex()));
                item1.setAttribute("style","solid");
                root.appendChild(item1);
            }

            //StreamResult streamResult = new StreamResult(System.out);
            StreamResult streamResult = new StreamResult(byteArrayOutputStream);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), streamResult);


        } catch (ParserConfigurationException | SQLException | TransformerException e) {
            exceptionArrayList.add(e);
            e.printStackTrace();
        }
        return byteArrayOutputStream.toString();
    }

    public ListenableGraph<Entity, DefaultEdge> getEntityGraph() {
        return entityGraph;
    }
}
