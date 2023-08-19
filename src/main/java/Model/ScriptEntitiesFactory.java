package Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ScriptEntitiesFactory {

    private static final Logger logger = LoggerFactory.getLogger(ScriptEntitiesFactory.class);
    private static ArrayList<ScriptEntity> scriptEntities = new ArrayList<ScriptEntity>();

    public static ScriptEntity getScriptEntityForSources(String layer, String tableName, Integer src_id){
        ScriptEntity existsingEntity = getExistsingEntity(layer, tableName, src_id);
        if (existsingEntity!=null)
            return existsingEntity;

        ScriptEntity scriptEntity = new ScriptEntity(layer,tableName,src_id);
        scriptEntities.add(scriptEntity);
        return scriptEntity;
    }

    public static ScriptEntity getExistsingEntity(String layer, String tableName, Integer src_id){
        for (ScriptEntity scriptEntity : scriptEntities){
            if(scriptEntity.getScriptEntityLayer().toLowerCase().equals(layer.toLowerCase()) &&
                    scriptEntity.getSrc_id().equals(src_id) &&
                    (scriptEntity.getScriptEntityTableName().toLowerCase().equals(tableName.toLowerCase()) ||
                            scriptEntity.getScriptEntityTableNameNoPartitions().toLowerCase().equals(tableName.toLowerCase())
                    )
            ){

                return scriptEntity;
            }
        }
        return null;
    }

    public static ArrayList<ScriptEntity> getScriptEntities() {
        return scriptEntities;
    }
}
