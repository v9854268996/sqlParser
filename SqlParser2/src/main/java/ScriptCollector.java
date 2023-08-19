import Model.EntitiesFactory;
import Model.Entity;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class ScriptCollector {
    public static void main(String[] args) throws IOException {

        ArrayList<Entity> allEntities = null;
        try {
            allEntities = EntitiesFactory.getEntities();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<String> testTables = new ArrayList<String>(Arrays.asList(   "DWH_AGGSUPLMNTHLYGIFT"
                ,"DWH_ETL_ETP_CPARTY_TMP"
                ,"DWH_AGGSUPLWEEKLYGIFT"
                ,"DWH_AGGSUPLYEARLYGIFT"
                ,"DWH_ETL_ETP_DATA_LOAD_TMP"
                ,"DWH_AGGSUPLDAILYGIFT"
                ,"DWH_AGG_SUPL_DAILY"
                ,"DWH_AGG_SUPL_MONTHLY"
                ,"DWH_AGG_SUPL_WEEKLY"
                ,"DWH_AGG_SUPL_YEARLY"
        )
        );


        for (Entity entity : allEntities){
            String s = null;
            try {
                s = entity.getRequest().toUpperCase();
            } catch (SQLException e) {

            }
            if (s==null)
                continue;

            for(String table : testTables)
                if (s.contains(table)) {
//                    try {
                        System.out.println(table + " influences " + entity.toString() /*+ "| " + entity.getRequest().substring(0,30)*/);
//                    } catch (SQLException e) {
//                        e.printStackTrace();
//                    }
                }
//            Path file = null;
//            Path path = Paths.get("C:\\Users\\V.D.Petrov\\IdeaProjects\\SqlParser2\\Scripts\\"
//                    + entity.getLayer() + "#"
//                    + entity.getTableName() + "#"
//                    + entity.getSrc_id()
//                    + ".sql");
//
//            if (Files.exists(path))
//                Files.delete(path);
//            else
//                file = Files.createFile(path);
//
//            try {
//                Files.write( file, entity.getRequest().getBytes());
//            } catch (SQLException e) {
//                System.out.println(entity.toString());
//            }
        }
        System.out.println("fin");
    }
}
