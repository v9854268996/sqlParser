package Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

//Сущность, встречаемая в скипте
public class ScriptEntity {
    private static final Logger logger = LoggerFactory.getLogger(ScriptEntity.class);
    private String scriptEntityTableName;
    private String scriptEntityTableNameNoPartitions;
    private String scriptEntityLayer;
    private Integer src_id;

    private Entity relatedEntity;
    //скрипт сборки сущности, в котором наполняется таблица
    private Entity originEntity;

    private Set<ScriptEntity> predecessorsScriptEntities = new HashSet<>();

    public ScriptEntity(String scriptLayer, String scriptTableName, Integer src_id) {
        this.scriptEntityTableName = scriptTableName;
        this.scriptEntityTableNameNoPartitions = scriptTableName.replaceAll("_1_prt_p\\d{6}","");
        this.scriptEntityLayer = scriptLayer;
        this.src_id = src_id;
        //this.relatedEntity = findRelatedEntity();
    }

//    private Model.Entity findRelatedEntity(){
//
//        Model.Entity supposedEntity = Model.EntitiesFactory.getEntityForSources(scriptEntityLayer,scriptEntityTableName,src_id);
//        if(supposedEntity!=null)
//            return supposedEntity;
//
//        supposedEntity = Model.EntitiesFactory.getEntityForSources(scriptEntityLayer,scriptEntityTableName.replaceAll("t_\\d{6}_",""),src_id);
//        if(supposedEntity!=null)
//            return supposedEntity;
//
//        return null;
//    }

    public String getScriptEntityTableName() {
        return scriptEntityTableName;
    }

    public String getScriptEntityTableNameNoPartitions() {
        return scriptEntityTableNameNoPartitions;
    }

    public String getScriptEntityLayer() {
        return scriptEntityLayer;
    }

    public Integer getSrc_id() {
        return src_id;
    }

    public Entity getRelatedEntity(){
        return relatedEntity;
    }

    @Override
    public String toString() {
        return this.getScriptEntityLayer() + "." + this.getScriptEntityTableNameNoPartitions() + ": " + this.getSrc_id();
    }

    public Entity getOriginEntity() {
        return originEntity;
    }
    public void setOriginEntity(Entity originEntity) {
        this.originEntity = originEntity;
    }

    public Set<ScriptEntity> getPredecessorsScriptEntities() {
        return predecessorsScriptEntities;
    }

    public void setPredecessorsScriptEntities(Set<ScriptEntity> predecessorsScriptEntities) {
        this.predecessorsScriptEntities = predecessorsScriptEntities;
    }

    public Integer getIndex() throws SQLException {
        return ScriptEntitiesFactory.getScriptEntities().indexOf(this);
    }
}
