import Model.ScriptEntitiesFactory;
import net.sf.jsqlparser.JSQLParserException;

import org.apache.log4j.PropertyConfigurator;
import org.jgrapht.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class AllScriptsTester {

    private static final Logger logger = LoggerFactory.getLogger(AllScriptsTester.class);

    public static void main(String[] args)  {

        PropertyConfigurator.configure("log4j.properties");
        //PropertyConfigurator.configure("C:\\Users\\V.D.Petrov\\IdeaProjects\\SqlParser2\\src\\main\\resources\\log4j.properties");
        String xmlGraph = "";
        ScriptEntitiesGraph scriptEntitiesGraph = null;

        String arg = "";
        if (args.length>0) {
            arg = args[0];
        }
        Integer src = -1;

        logger.info("Starting parsing. Source: " + (arg.isEmpty()?"all":arg));

        if (arg.isEmpty()){
            try {
                scriptEntitiesGraph = new ScriptEntitiesGraph();
            } catch (JSQLParserException e) {
                logger.error("Parse failed.", e);
                System.exit(-1);
            } catch (SQLException e) {
                logger.error("Sql exception.", e);
                System.exit(-1);
            }
        } else {
            try {
                src = Integer.parseInt(arg.toLowerCase().trim());
                scriptEntitiesGraph = new ScriptEntitiesGraph(src);
            } catch (Exception e){
                logger.error("Incorrect src_id or parse fail", e);
                System.exit(-1);
            }
        }
        //edw_stg_dm_feb.tfct_edo_statistic_detail: 102

        xmlGraph = scriptEntitiesGraph.getXmlGraph();
        Graphs.predecessorListOf(scriptEntitiesGraph.getEntityGraph()
                , ScriptEntitiesFactory.getScriptEntityForSources("edw_stg_dm_feb", "tfct_edo_statistic_detail", 102));

        Path path = Paths.get("parseResults.xml");
        if (path.toFile().exists())
            path.toFile().delete();

        try {
            Files.write(path, xmlGraph.getBytes());
        } catch (IOException e) {
            logger.error("Fail to write file", e);
        }

        logger.info("Success");

//
//        try {
//            logger.info("Starting parsing. Source:" + (arg.isEmpty()?"all":src));
//
//            if ()
//            scriptEntitiesGraph = new ScriptEntitiesGraph(12);
//            xmlGraph = scriptEntitiesGraph.getXmlGraph();
//            Path path = Paths.get("parseResults.xml");
//            if (path.toFile().exists())
//                path.toFile().delete();
//
//            Files.write(path, xmlGraph.getBytes());
//
//            //Model.ScriptEntity scriptEntityForSources = Model.ScriptEntitiesFactory.getScriptEntityForSources("edw_dds", "tfct_adjust_1_prt_p000045", 45);
//            //DefaultDirectedGraph<Model.ScriptEntity, DefaultWeightedEdge> entityGraph = scriptEntitiesGraph.getEntityGraph();
//
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        logger.warn(xmlGraph);

//        try {
//            EntitiesGraph entitiesGraph = new EntitiesGraph(1);
//            String xmlGraph = entitiesGraph.getXmlGraph();
//            System.out.println(xmlGraph);
//
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
//        parseAll();
//        //parseSome();
//        //parseSpecific();
//        Integer srcId;
//
//        if (args[0].startsWith("-SRC")) {
//            srcId = Integer.parseInt(args[0].substring(4));
//            buildXMLDependenciesForSrc(srcId);
//        }

    }

//    public static void parseSome() throws SQLException {
//        Model.Entity entity = Model.EntitiesFactory.getEntityForSources("STG_DMCM","tfct_asrv",151);
//        String request = entity.getRequest();
//        try {
//            RequestUtils.lookForTablesInRequest(request);
//
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void parseAll() {
//        ArrayList<Model.Entity> entities = new ArrayList<>();
//        try {
//            entities = Model.EntitiesFactory.getEntities();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(true);
//
//        Document doc = null;
//        try {
//            doc = factory.newDocumentBuilder().newDocument();
//        } catch (ParserConfigurationException e) {
//            e.printStackTrace();
//        }
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        Element root = doc.createElement("data");
//        //root.setAttribute("xmlns", "http://www.javacore.ru/schemas/");
//        doc.appendChild(root);
//
//
//
//        for (Model.Entity entity : entities) {
//            try {
//
//                for(Model.ScriptEntity scriptEntity:entity.getSelectScriptEntities()){
//                    Element xmlEntity = doc.createElement("nodes");
//                    xmlEntity.setAttribute("Target", entity.toString());
//                    xmlEntity.setAttribute("Source", scriptEntity.toString());
//                    root.appendChild(xmlEntity);
//                }
//                //StreamResult streamResult = new StreamResult(byteArrayOutputStream);
//                //StreamResult streamResult = new StreamResult(System.out);
//                StreamResult streamResult =  new StreamResult(new File(System.getProperty("user.dir") + "\\dependencies.xml"));
//                Transformer transformer = TransformerFactory.newInstance().newTransformer();
//                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//                transformer.transform(new DOMSource(doc), streamResult);
//
//                //System.out.println(entity.toString());
//                //String request = RequestUtils.getRequestForEntity(entity);
//
////                try {
////                    RequestUtils.lookForTablesInRequest(request);
////
////                    Element xmlEntity = doc.createElement("nodes");
////                    //<nodes ID="7839" LABEL="KING" COLORVALUE="10" SIZEVALUE="5000"/>
////                    xmlEntity.setAttribute("TargetSchema", entity.toString());
////
////                    xmlEntity.setAttribute("label", entity.toString());
////                    root.appendChild(xmlEntity);
////
////
////                    System.out.println("Ok " + entity);
////                } catch (JSQLParserException e) {
////                    System.out.println("parse failed:" + entity);
////                } catch (Exception e) {
////                    System.out.println("Fatal:" + entity + " src:" + entity.getSrc_id());
////                }
//            } catch (SQLException e) {
//                System.out.println("failed to get request for: " + entity);
//            } catch (JSQLParserException e) {
//                System.out.println("failed to parse script:" + entity);
////                File file = Paths.get("C:\\Users\\V.D.Petrov\\IdeaProjects\\SqlParser2\\src\\main\\resources\\FailedScripts\\" + entity.toString() + ".sql").toFile();
////                try {
////                    FileWriter fileWriter = new FileWriter(file);
////                    fileWriter.write(entity.getRequest());
////                    fileWriter.close();
////                } catch (IOException | SQLException e1) {
////                    e1.printStackTrace();
////                }
//            } catch (TransformerConfigurationException e) {
//                System.out.println("TransformerConfigurationException:" + entity);
//            } catch (TransformerException e) {
//                System.out.println("TransformerException:" + entity);
//            }
//
//        }
//    }
//
//    public static void parseSpecific (){
//        String request = "  select 1\n" +
//                "  from\n" +
//                "    (with sel as (select 1 )\n" +
//                "    select 1 from sel\n" +
//                "    ) first";
//
//        try {
//            RequestUtils.lookForTablesInRequest(request);
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void buildXMLDependenciesForSrc (Integer srcId){
//        File file = Paths.get(System.getProperty("user.dir") + "\\dependenciesGraph.xml").toFile();
//
//        if(file.exists())
//            file.delete();
//
//                try {
//                    FileWriter fileWriter = new FileWriter(file);
//
//                    EntitiesGraph entitiesGraph = new EntitiesGraph(srcId);
//                    fileWriter.write(entitiesGraph.getXmlGraph());
//                    fileWriter.close();
//                } catch (IOException e1) {
//                    e1.printStackTrace();
//                } catch (JSQLParserException e) {
//                    e.printStackTrace();
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                }
//    }

}
