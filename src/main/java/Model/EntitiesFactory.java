package Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.stream.Collectors;

/*
Запрашивает у базы список доступных скриптов.
Гранулярность: схема - таблица - источник
 */
public class EntitiesFactory {
    private static ArrayList<Entity> entities;
    private static Connection connection = RtlcConnection.getConnection();
    private static final Logger logger = LoggerFactory.getLogger(EntitiesFactory.class);

    public static Entity getEntityForSources(String layer, String tableName, Integer src_id) {
        try {
            if (entities == null)
                makeAvailableEntities();

            for (Entity entity : entities) {
                String entityLayer = entity.getLayer().replace("stg_", "");

                if (entityLayer.equals(layer
                            .toLowerCase()
                            .replace("edw_", "")
                            .replace("stg_", "")) &&
                        entity.getTableName().equals(tableName.toLowerCase().replaceAll("_1_prt_.*","")) &&
                        entity.getSrc_id().equals(src_id)) {
                    return entity;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void makeAvailableEntities() throws SQLException {
        logger.debug("Requesting entities from DB..");
        Statement statement = connection.createStatement();
        String query = "SELECT DISTINCT s.SCHEMA_NAME, s.TABLE_NAME, TO_NUMBER(src.SRC_CODE) \n" +
                "FROM rtlc_meta.ETL_SCRIPT s\n" +
                "INNER JOIN RTLC_META.DICT_SRC_CODE src\n" +
                "\tON 1=1 \n" +
                "\tAND s.SRC_ID_LIST LIKE '%' || src.SRC_CODE || '%'\n" +
                "\tAND src.SRC_CODE != 0 ";;
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            try {


                if (entities == null)
                    entities = new ArrayList<Entity>();

                String sch = resultSet.getString(1).toLowerCase();
                String tbl = resultSet.getString(2).toLowerCase();
                Integer src = resultSet.getInt(3);
                Entity ent = new Entity(sch, tbl, src);
                entities.add(ent);

            } catch (Exception e){
                System.out.println("Failed to get ETL_SCRIPT for entity: schema " + resultSet.getString(1) + " table " + resultSet.getString(2) + " src " + resultSet.getInt(3));
            }
        }
        logger.debug("Recieved " + getEntities().size() + " entities");
    }

    public static ArrayList<Entity> getEntities() throws SQLException {
        if (entities == null)
            makeAvailableEntities();
        return entities;
    }

    public static ArrayList<Entity> getEntities(Integer srcid) throws SQLException {
        return getEntities()
                .stream()
                .filter(e -> e.getSrc_id().equals(srcid))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
