package top.gotoeasy.framework.orm.util;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.orm.annotation.Column;
import top.gotoeasy.framework.orm.annotation.Id;

public class CmnOrm {

    private static final Log log = LoggerFactory.getLogger(CmnOrm.class);

    public static void close(Statement statement) {
        try {
            if ( statement != null ) {
                statement.close();
            }
        } catch (SQLException e) {
            log.error("关闭Statement出错", e);
        }
    }

    public static void close(ResultSet rs) {
        try {
            if ( rs != null ) {
                rs.close();
            }
        } catch (SQLException e) {
            log.error("关闭ResultSet出错", e);
        }
    }

    public static void close(Connection connection) {
        try {
            if ( connection != null ) {
                connection.close();
            }
        } catch (SQLException e) {
            log.error("关闭Connection出错", e);
        }
    }

    public static void close(Statement statement, ResultSet rs) {
        close(null, statement, rs);
    }

    public static void close(Connection connection, Statement statement) {
        close(connection, statement, null);
    }

    public static void close(Connection connection, ResultSet rs) {
        close(connection, null, rs);
    }

    public static void close(Connection connection, Statement statement, ResultSet rs) {
        close(rs);
        close(statement);
        close(connection);
    }

    public static Map<String, Field> getEntityFields(Class<?> clas) {
        Map<String, Field> map = new HashMap<>();

        Field[] fields = clas.getDeclaredFields();
        for ( Field field : fields ) {
            // 忽略@Transient及final字段
            if ( field.isAnnotationPresent(Transient.class) || Modifier.isFinal(field.getModifiers()) ) {
                continue;
            }

            // public字段、注解字段
            if ( Modifier.isPublic(field.getModifiers()) || field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class) ) {
                field.setAccessible(true);
                map.put(field.getName(), field);
            }
        }

        return map;
    }

    public static List<String> getEntityFieldNames(Class<?> clas) {
        List<String> list = new ArrayList<>();
        Map<String, Field> map = getEntityFields(clas);
        for ( String name : map.keySet() ) {
            list.add(name);
        }
        return list;
    }

}
