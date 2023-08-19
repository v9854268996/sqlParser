import Model.EntitiesFactory;
import Model.Entity;
import Model.ScriptEntitiesFactory;
import Model.ScriptEntity;
import net.sf.jsqlparser.JSQLParserException;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class ScriptEntitiesGraph {
    private static final Logger logger = LoggerFactory.getLogger(AllScriptsTester.class);
    //private ListenableGraph<Model.ScriptEntity, DefaultEdge> entityGraph = new DefaultListenableGraph<>(new DefaultDirectedGraph<>(DefaultEdge.class));
    private DefaultDirectedWeightedGraph<ScriptEntity, DefaultWeightedEdge> entityGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    public static ArrayList<Exception> exceptionArrayList = new ArrayList<>();

    public ScriptEntitiesGraph() throws SQLException, JSQLParserException {
        ArrayList<Entity> allEntities = EntitiesFactory.getEntities();
        entityGraph = buildGraph(allEntities);
    }

    public ScriptEntitiesGraph(Integer srcid) throws SQLException, JSQLParserException {
        ArrayList<Entity> someEntities = EntitiesFactory.getEntities(srcid);
        entityGraph = buildGraph(someEntities);
    }

    public ScriptEntitiesGraph(ArrayList<Entity> entities) throws SQLException, JSQLParserException {
        entityGraph = buildGraph(entities);
    }

    private DefaultDirectedWeightedGraph<ScriptEntity, DefaultWeightedEdge> buildGraph(ArrayList<Entity> entities) throws SQLException, JSQLParserException {
        //entities.stream().peek(entityGraph::addVertex).forEach(entity -> entity.lookForTablesInRequest());
        logger.info("-------------------------------------------------");
        logger.info("Making graph for " + entities.size() + " entities");
        entities.stream().forEach(entity -> entity.lookForTablesInRequest());
        logger.info("Adding Vertices..");
        ScriptEntitiesFactory.getScriptEntities().forEach(entityGraph::addVertex);

        logger.info("Adding Edges..");
        for (ScriptEntity insertEntity : ScriptEntitiesFactory.getScriptEntities()) {
            insertEntity.getPredecessorsScriptEntities().stream()
                    .filter(scriptEntity -> scriptEntity!=insertEntity)
                    //.peek(se -> System.out.println(se + "->" + insertEntity))
                    .map(se -> entityGraph.addEdge(se,insertEntity))
                    .forEach(defaultWeightedEdge -> entityGraph.setEdgeWeight(defaultWeightedEdge,10D));

            //make dependencies STG_X -> X
            String scriptEntityTableNameNoPartitions = insertEntity.getScriptEntityTableNameNoPartitions().replaceAll("t_\\d{6}_","");
            String scriptEntityLayer = insertEntity.getScriptEntityLayer().replaceAll("_stg","");
            Integer src_id = insertEntity.getSrc_id();
            ScriptEntity relatedEntity = ScriptEntitiesFactory.getExistsingEntity(scriptEntityLayer,scriptEntityTableNameNoPartitions,src_id);
            if (relatedEntity!=null && relatedEntity!=insertEntity)
                entityGraph.addEdge(insertEntity,relatedEntity);
        }

//        for (Model.ScriptEntity insertEntity : Model.ScriptEntitiesFactory.getScriptEntities()) {
//
//
//
////            if (insertEntity.getRelatedEntity() != null && !insertEntity.getRelatedEntity().equals(entity)) {
////                entityGraph.addEdge(entity, insertEntity.getRelatedEntity());
////            } else {
////                entities.stream()
////                        .filter(e -> e.makesScriptEntities(insertEntity))
////                        .filter(e -> e.equals(entity))
////                        .filter(e -> entityGraph.containsEdge(entity, e))
////                        .forEach(e -> entityGraph.addEdge(entity, e));
////            }
//        }
        logger.info("Graph is ready.");
        return entityGraph;
    }

    public String getXmlGraph(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        logger.info("--------------------------------------");
        logger.info("Graph -> XML");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().newDocument();

            Element root = doc.createElement("data");
            //root.setAttribute("xmlns", "http://www.javacore.ru/schemas/");
            doc.appendChild(root);

            for (ScriptEntity scriptEntity:entityGraph.vertexSet()){
                Element item1 = doc.createElement("nodes");
                //<nodes ID="7839" LABEL="KING" COLORVALUE="10" SIZEVALUE="5000"/>
                item1.setAttribute("id", String.valueOf(scriptEntity.getIndex()));
                item1.setAttribute("label", scriptEntity.toString());
                item1.setAttribute("srcid", scriptEntity.getSrc_id().toString());
                item1.setAttribute("isroot", Graphs.vertexHasPredecessors(entityGraph ,scriptEntity)? "false" : "true");
                item1.setAttribute("originScript", scriptEntity.getOriginEntity()!=null?scriptEntity.getOriginEntity().toString():"null");
                root.appendChild(item1);
            }

            for (DefaultWeightedEdge edge:entityGraph.edgeSet()){
                Element item1 = doc.createElement("links");
                //<links FROMID="7839" TOID="7839" STYLE="solid"/>
                item1.setAttribute("fromid", String.valueOf(entityGraph.getEdgeSource(edge).getIndex()));
                item1.setAttribute("toid", String.valueOf(entityGraph.getEdgeTarget(edge).getIndex()));
                item1.setAttribute("style",entityGraph.getEdgeWeight(edge)>1?"solid":"dotted");
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

    public DefaultDirectedWeightedGraph<ScriptEntity, DefaultWeightedEdge> getEntityGraph() {
        return entityGraph;
    }
}
