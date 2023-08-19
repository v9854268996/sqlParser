package Model;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//entity - сущность, для которой можно найти скрипт в RTLC_META
public class Entity {
    private static final Logger logger = LoggerFactory.getLogger(Entity.class);
    private String layer;
    private String tableName;
    private String request;
    private Integer src_id;

    private boolean isProcessed = false;

    List<Exception> exceptions = new ArrayList<>();

    private static Connection connection = RtlcConnection.getConnection();

//    private HashSet<Model.ScriptEntity> selectScriptEntities;
//    private HashSet<Model.ScriptEntity> insertScriptEntities;

//    RequestTablesInfo requestTablesInfo;

    public Entity(String layer, String tableName, Integer src_id) {
        this.layer = layer;
        this.tableName = tableName;
        this.src_id = src_id;
    }

//    public HashSet<Model.ScriptEntity> getInsertScriptEntities() throws SQLException, JSQLParserException {
////        if (insertScriptEntities!=null)
////            return insertScriptEntities;
////
////            insertScriptEntities = getRequestTablesInfo()
////                    .getInsertTables()
////                    .stream()
////                    .map(a -> Model.ScriptEntitiesFactory.getScriptEntityForSources(
////                            a.split("\\.")[0]
////                            , a.split("\\.")[1]
////                            , src_id))
////                    .collect(Collectors.toCollection(HashSet::new));
//        return insertScriptEntities;
//    }
//
//    public HashSet<Model.ScriptEntity> getSelectScriptEntities() throws SQLException, JSQLParserException {
////        if (selectScriptEntities !=null)
////            return selectScriptEntities;
////
////        selectScriptEntities = getRequestTablesInfo()
////                    .getAllTables()
////                    .stream()
////                    .map(a -> Model.ScriptEntitiesFactory.getScriptEntityForSources(
////                            a.split("\\.")[0]
////                            , a.split("\\.")[1]
////                            , src_id))
////                    .collect(Collectors.toCollection(HashSet::new));
//        return selectScriptEntities;
//    }

//    public RequestTablesInfo getRequestTablesInfo() throws JSQLParserException, SQLException {
//        if (requestTablesInfo!=null)
//            return requestTablesInfo;
//
//        requestTablesInfo = RequestUtils.lookForTablesInRequest(getRequest());
//        return requestTablesInfo;
//    }

    public String getRequest() throws SQLException {
        if (request!=null)
            return request;

        connection = RtlcConnection.getConnection();
        //System.out.println("looking for request for " + entity + " src " + entity.getSrc_id());
        String query = "select RTLC_META.PKG_CHECK.GEN_ETL_SCRIPT_PY(i_src_id  => LPAD(" + getSrc_id() + ",6,'0'),\n" +
                "                        i_schema_name                             => '" + getLayer().toUpperCase() + "',\n" +
                "                        i_table_name                              => '" + getTableName().toUpperCase() + "',\n" +
                "                        i_start_dt                                => '20190601',\n" +
                "                        i_end_dt                                  => '20190630') from dual ";

        java.sql.Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        request = resultSet.getString(1);
        resultSet.close();
        statement.close();

        return request;
    }


//
//    public boolean makesScriptEntities(Model.ScriptEntity scriptEntity){
//        boolean result = false;
//        try {
//            result = getInsertScriptEntities().contains(scriptEntity);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
//        return result;
//    }

    //-----------------------------------------------------------------------

    public void lookForTablesInRequest() {

        try {
        String initialRequest = getRequest();

//        replace all fields in select query
        Pattern regex = Pattern.compile("select(.*)from", Pattern.DOTALL);
        Matcher regexMatcher = regex.matcher(initialRequest);
        String preparedRequest = initialRequest;
        if (regexMatcher.find()) {
            preparedRequest = regexMatcher.replaceAll("select 1 from ");
        }
            //Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        List<String> sqlRequests = Arrays.asList(preparedRequest
                .toLowerCase()
                .replaceAll("(?s)\\/\\*.*?\\*\\/", "") //supress block comments
                .replaceAll("\\-\\-.*\n", " ") //supress comments
                .replaceAll("\\n\\n\\n*", "\n") //supress comments
                .split(";\n?"));


        Statement statement;
        TablesNamesFinder tablesNamesFinder;

        List<String> insertTableList = new ArrayList<>();
        Iterator<String> iterator = sqlRequests.iterator();
        String previousSQLPart = "";

        while (iterator.hasNext()) {
            String request = iterator.next();
            String refinedRequest = requestReplaceKnownParseErrors(request.trim());
            refinedRequest = previousSQLPart + refinedRequest;

            if ((refinedRequest.contains("analyse")
                    || refinedRequest.contains("analyze")
                    || refinedRequest.contains("optimizer")
                    || refinedRequest.contains("edw_stg_dds.f_")
                    || refinedRequest.contains("truncate")
                    || refinedRequest.equals("begin")
                    || refinedRequest.matches("begin")
                    || refinedRequest.trim().equals("")
                    || refinedRequest.contains("delete") && refinedRequest.contains("using"))
                    && previousSQLPart.equals(""))
                continue;

            try {
                statement = CCJSqlParserUtil.parse(refinedRequest);

                if (statement instanceof Insert) {
                    String insertTable = ((Insert) statement).getTable().getFullyQualifiedName();
                    ScriptEntity insertScriptEntity = ScriptEntitiesFactory.getScriptEntityForSources(insertTable.split("\\.")[0]
                            , insertTable.split("\\.")[1]
                            , src_id);

                    List<String> selectScriptEntitiesList = new ArrayList<>(new TablesNamesFinder().getTableList(statement));
                    Set<ScriptEntity> selectScriptEntities = selectScriptEntitiesList.stream()
                            .map(e -> ScriptEntitiesFactory.getScriptEntityForSources(e.split("\\.")[0], e.split("\\.")[1], src_id))
                            .filter(e -> e!=insertScriptEntity)
                            .collect(Collectors.toCollection(HashSet::new));

                    insertScriptEntity.setPredecessorsScriptEntities(selectScriptEntities);
                    insertScriptEntity.setOriginEntity(this);

////                    .getInsertTables()
////                    .stream()
////                    .map(a -> Model.ScriptEntitiesFactory.getScriptEntityForSources(
////                            a.split("\\.")[0]
////                            , a.split("\\.")[1]
////                            , src_id))
////                    .collect(Collectors.toCollection(HashSet::new));

                }
                previousSQLPart = "";

            } catch (JSQLParserException e) {
                if (iterator.hasNext())
                    previousSQLPart = refinedRequest;
                else
                    throw e;
            }
        }
        } catch (Exception e) {
            exceptions.add(e);
            logger.warn("parse error:" + this.toString() + ". ", e.getMessage());
        }

        isProcessed = true;
//
//        if (!tableList.isEmpty())
//            return  new RequestTablesInfo(insertTableList,tableList);
//        else
//            return null;

    }

    private static String requestReplaceKnownParseErrors(String initialRequest) {
        String fixedRequest = initialRequest
                .replaceAll("current_timestamp\\(\\d?\\)", "current_timestamp")
                .replaceAll("unnest\\(.*\\)", "'unnest()'")
                .replaceAll("::numeric(\\(\\d\\d\\s*,?\\s*\\d?\\s*\\))?", " ")
                .replaceAll("::text", " ")
                .replaceAll("similar to","like")
                .replaceAll("~.*?'.*?'.*?=.*?true","like \"%\" ")
                //.replaceAll("not.*?\\w.*?in.*?\\(.*?\\)","true") //coalesce(subs_dwh.soo_src_id) NOT IN (140,1014)
                .replaceAll("\\(38\\,0\\)", " ") //81, TFCT_TELEPHONY_CONSUMPTION, STG_DDS
                .replaceAll("filter\\s*\\(.*?\\)", " ") //151, tfct_iptv_pack, STG_DMCM
                ;

        return fixedRequest;
    }

    public String getLayer() {
        return layer;
    }

    public String getTableName() {
        return tableName;
    }

    public Integer getSrc_id() {
        return src_id;
    }

    public void setSrc_id(Integer src_id) {
        this.src_id = src_id;
    }

    @Override
    public String toString() {
        return this.getLayer() + "." + this.getTableName() + "." + this.getSrc_id();
    }

    public Integer getIndex() throws SQLException {
        return EntitiesFactory.getEntities().indexOf(this);
    }

    public boolean isProcessed() {
        return isProcessed;
    }


}
