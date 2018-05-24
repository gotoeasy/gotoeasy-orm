package top.gotoeasy.framework.orm.support.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.ioc.util.CmnIoc;
import top.gotoeasy.framework.orm.annotation.Column;
import top.gotoeasy.framework.orm.annotation.Id;
import top.gotoeasy.framework.orm.exception.OrmException;
import top.gotoeasy.framework.orm.strategy.OrmNamingStrategy;
import top.gotoeasy.framework.orm.support.SqlDialect;
import top.gotoeasy.framework.orm.util.CmnOrm;

public class DefaultSqlDialect implements SqlDialect {

    private static final Log log = LoggerFactory.getLogger(DefaultSqlDialect.class);

    @Override
    public String getCreateTableDdl(Class<?> claz) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String ddl = "create table {table} ( {columns} {pk} )";
        String table = strategy.tableName(claz);

        StringBuilder columns = new StringBuilder();
        Map<String, Field> mapField = CmnOrm.getEntityFields(claz);
        for ( String name : mapField.keySet() ) {
            columns.append(strategy.columnName(name)).append(" ").append(getColumnDefine(mapField.get(name))).append(",");
        }

        StringBuilder pk = new StringBuilder();
        for ( String name : mapField.keySet() ) {
            if ( mapField.get(name).isAnnotationPresent(Id.class) ) {
                pk.append(",").append(strategy.columnName(mapField.get(name).getName()));
            }
        }
        if ( pk.length() > 0 ) {
            pk.deleteCharAt(0).insert(0, "(").append(")");
            pk.insert(0, " PRIMARY KEY ").insert(0, table).insert(0, "CONSTRAINT pk_"); // CONSTRAINT pk_tablename PRIMARY KEY (key1, key2, key3)
        }

        // CONSTRAINT mvc_mst_config_pkey PRIMARY KEY (key1, key2, key3)
        return ddl.replace("{table}", table).replace("{columns}", columns.toString()).replace("{pk}", pk.toString());
    }

    @Override
    public String getColumnDefine(Field field) {
        String rs;
        if ( field.getType().equals(String.class) ) {
            if ( field.isAnnotationPresent(Column.class) ) {
                Column column = field.getAnnotation(Column.class);
                rs = column.value();
            } else {
                rs = "character varying(255)";
            }
        } else if ( field.getType().equals(int.class) || field.getType().equals(Integer.class) || field.getType().equals(Long.class) ) {
            rs = "integer";
        } else if ( field.getType().equals(BigDecimal.class) || field.getType().equals(Double.class) ) {
            rs = "numeric(20,2)";
        } else if ( field.getType().equals(Date.class) ) {
            rs = "timestamp";
        } else {
            log.error("待对应的字段类型{}", field);
            throw new OrmException("TODO 待对应的字段类型：" + field);
        }

        if ( field.isAnnotationPresent(Id.class) ) {
            rs += " NOT NULL";
        }
        return rs;
    }

}
