//import net.sf.jsqlparser.JSQLParserException;
//import net.sf.jsqlparser.parser.CCJSqlParserUtil;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.insert.Insert;
//import net.sf.jsqlparser.util.TablesNamesFinder;
//
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.*;
//
//public class RequestUtils {
//
//    Model.Entity entity;
//    Date dateStart;
//    Date dateEnd;
//    private static Connection connection = Model.RtlcConnection.getConnection();
//
//
//    public static RequestTablesInfo getTablesFromRequest(String initialRequest) throws JSQLParserException {
//        //Pattern commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
//        List<String> sqlRequests = Arrays.asList(initialRequest
//                .toLowerCase()
//                //.replaceAll("\\/\\*(?:.|[\\n\\r])*?\\*\\/", "") //supress block comments
//                //.replaceAll("\\/\\*\\n*?\\r*?(?:.\\n?\\r?)*?\\n*?\\r*?\\*\\/", "") //supress block comments
//                .replaceAll("(?s)\\/\\*.*?\\*\\/", "") //supress block comments
//                .replaceAll("\\-\\-.*\n", " ") //supress comments
//                .replaceAll("\\n\\n\\n*", "\n") //supress comments
//                .split(";\n?"));
//        // \/\*\s*\*\/
//        // \/\*(?:\s*)*?\*\/
//        // \/\*(?:.\n?\r?)*?\*\/
//        // \/\*.*?\*\/(?s)
//
//        Statement statement;
//        TablesNamesFinder tablesNamesFinder;
//        List<String> tableList = new ArrayList<>();
//        List<String> insertTableList = new ArrayList<>();
//        Iterator<String> iterator = sqlRequests.iterator();
//        String previousSQLPart = "";
//
//        while (iterator.hasNext()) {
//            String request = iterator.next();
//            String refinedRequest = requestReplaceKnownParseErrors(request.trim());
//            refinedRequest = previousSQLPart + refinedRequest;
//
//            if ((refinedRequest.contains("analyse")
//                    || refinedRequest.contains("analyze")
//                    || refinedRequest.contains("optimizer")
//                    || refinedRequest.contains("edw_stg_dds.f_")
//                    || refinedRequest.contains("truncate")
//                    || refinedRequest.equals("begin")
//                    || refinedRequest.matches("begin")
//                    || refinedRequest.trim().equals("")
//                    || refinedRequest.contains("delete") && refinedRequest.contains("using"))
//                    && previousSQLPart.equals(""))
//                continue;
//
//            try {
//                statement = CCJSqlParserUtil.parse(refinedRequest);
//
//                if (statement instanceof Insert) {
//                    //System.out.println(((Insert) statement).getTable());
//                    tablesNamesFinder = new TablesNamesFinder();
//                    tableList.addAll(tablesNamesFinder.getTableList(statement));
//                    insertTableList.add(((Insert) statement).getTable().getFullyQualifiedName());
//                }
//                previousSQLPart = "";
//
//            } catch (JSQLParserException e) {
//                if (iterator.hasNext())
//                    previousSQLPart = refinedRequest;
//                else
//                    throw e;
//            }
//        }
//
//        if (!tableList.isEmpty())
//            //return new HashSet<>(tableList);
//            return  new RequestTablesInfo(insertTableList,tableList);
//        else
//            return null;
////        return entity.getSelectScriptEntities().stream()
////                .map(Model.Entity::getTableName)
////                .collect(Collectors.toCollection(HashSet::new));
//    }
//
//    private static String requestReplaceKnownParseErrors(String initialRequest) {
//        String fixedRequest = initialRequest
//                .replaceAll("current_timestamp\\(\\d?\\)", "current_timestamp")
//                .replaceAll("unnest\\(.*\\)", "'unnest()'")
//                .replaceAll("::numeric(\\(\\d\\d\\s*,?\\s*\\d?\\s*\\))?", " ")
//                .replaceAll("::text", " ")
//                .replaceAll("similar to","like")
//                .replaceAll("~.*?'.*?'.*?=.*?true","like \"%\" ")
//                //.replaceAll("not.*?\\w.*?in.*?\\(.*?\\)","true") //coalesce(subs_dwh.soo_src_id) NOT IN (140,1014)
//                .replaceAll("\\(38\\,0\\)", " ") //81, TFCT_TELEPHONY_CONSUMPTION, STG_DDS
//                .replaceAll("filter\\s*\\(.*?\\)", " ") //151, tfct_iptv_pack, STG_DMCM
//                ;
//
//        return fixedRequest;
//    }
//
//    public static String getRequestForEntity(Model.Entity entity) throws SQLException {
//        if (connection == null)
//            connection = Model.RtlcConnection.getConnection();
//        //System.out.println("looking for request for " + entity + " src " + entity.getSrc_id());
//        String query = "select RTLC_META.PKG_CHECK.GEN_ETL_SCRIPT_PY(p_src_id  => LPAD(" + entity.getSrc_id() + ",6,'0'),\n" +
//                "                        p_schema_name                             => '" + entity.getLayer().toUpperCase() + "',\n" +
//                "                        p_table_name                              => '" + entity.getTableName().toUpperCase() + "',\n" +
//                "                        p_start_dt                                => '20190601',\n" +
//                "                        p_end_dt                                  => '20190630') from dual ";
//
//        java.sql.Statement statement = connection.createStatement();
//        ResultSet resultSet = statement.executeQuery(query);
//        resultSet.next();
//        String request = resultSet.getString(1);
//        resultSet.close();
//        statement.close();
//        return request;
//    }
//}
