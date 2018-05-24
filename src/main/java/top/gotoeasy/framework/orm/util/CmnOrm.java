package top.gotoeasy.framework.orm.util;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import top.gotoeasy.framework.core.config.DefaultConfig;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.util.CmnClass;
import top.gotoeasy.framework.core.util.CmnMath;
import top.gotoeasy.framework.core.util.CmnString;
import top.gotoeasy.framework.ioc.util.CmnIoc;
import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.exception.OrmException;
import top.gotoeasy.framework.orm.strategy.OrmNamingStrategy;
import top.gotoeasy.framework.orm.support.SqlDialect;

public class CmnOrm {

    private static final Log log = LoggerFactory.getLogger(CmnOrm.class);

    public static void initDatabaseTables() {

        String auto = DefaultConfig.getInstance().getString("table.ddl.auto", "UPDATE");
        if ( "NONE".equalsIgnoreCase(auto) ) {
            log.trace("NONE模式不做数据库初始化处理");
            return;
        }

        String[] scanPackages = DefaultConfig.getInstance().getString("ioc.scan", "top.gotoeasy.framework.orm").split(",");
        List<Class<?>> listEntity = new ArrayList<>();
        for ( String pack : scanPackages ) {
            if ( CmnString.isBlank(pack) ) {
                continue;
            }

            List<Class<?>> listTmp = CmnClass.getClasses(pack.trim());
            for ( Class<?> claz : listTmp ) {
                if ( claz.isAnnotationPresent(Entity.class) && !listEntity.contains(claz) ) {
                    listEntity.add(claz);
                }
            }
        }
        log.trace("扫描对象Entity定义{}", listEntity);

        OrmNamingStrategy ormNamingStrategy = CmnIoc.getBean(OrmNamingStrategy.class);
        List<String> listTableNames = getTables();
        log.trace("数据库已有表{}", listTableNames);

        String table = null;
        for ( Class<?> claz : listEntity ) {
            table = ormNamingStrategy.tableName(claz);
            log.debug("检查表[{}]", table);

            if ( !listTableNames.contains(table) ) {
                log.debug("表不存在，新建表[{}]", table);
                createTable(claz);
                continue;
            }

            if ( !"UPDATE".equalsIgnoreCase(auto) ) {
                log.trace("表已存在，非UPDATE模式不做表结构检查处理[{}]", table);
                continue;
            }
            if ( checkTableColumns(claz) ) {
                log.trace("表已存在，结构一致[{}]", table);
                continue;
            }

            log.trace("表已存在，结构不一致[{}]", table);

            // 备份、删除、新建
            String newTable = table + CmnMath.random(1, 999);
            log.debug("备份表数据[{}->{}]", table, newTable);
            backupTable(table, newTable);
            log.debug("删除表[{}]", table);
            dropTable(claz);
            log.debug("新建表[{}]", table);
            createTable(claz);
        }

        log.debug("数据库初始化处理完成[{}]", auto);
    }

    public static void backupTable(String origTableName, String backupTableName) {
        execute("SELECT * INTO " + backupTableName + " FROM " + origTableName);
    }

    public static void dropTable(Class<?> claz) {
        OrmNamingStrategy namingStrategy = CmnIoc.getBean(OrmNamingStrategy.class);
        execute("DROP TABLE " + namingStrategy.tableName(claz));
    }

    public static void createTable(Class<?> claz) {
        SqlDialect sqlDialect = CmnIoc.getBean(SqlDialect.class);
        execute(sqlDialect.getCreateTableDdl(claz));
    }

    private static void execute(String sql) {
        DataSource dataSource = CmnIoc.getBean(DataSource.class);
        Connection connection = null;
        Statement statement = null;

        log.debug(sql);
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (SQLException e) {
            throw new OrmException(e);
        } finally {
            close(connection, statement);
        }
    }

    public static boolean checkTableColumns(Class<?> claz) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        List<String> listColumns = getTableColumns(claz);
        List<String> listProps = getEntityFieldNames(claz);

        if ( listColumns.size() == listProps.size() ) {
            for ( String prop : listProps ) {
                if ( !listColumns.contains(strategy.columnName(prop)) ) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static List<String> getTableColumns(Class<?> claz) {
        List<String> list = new ArrayList<>();
        DataSource dataSource = CmnIoc.getBean(DataSource.class);
        OrmNamingStrategy namingStrategy = CmnIoc.getBean(OrmNamingStrategy.class);
        Connection connection = null;
        ResultSet rs = null;

        try {
            connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            rs = metaData.getColumns(null, null, namingStrategy.tableName(claz), null);

            while ( rs.next() ) {
                list.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        } catch (SQLException e) {
            throw new OrmException("取得列名出错", e);
        } finally {
            close(connection, rs);
        }

        return list;
    }

    public static boolean isTableExist(String table) {
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = CmnIoc.getBean(DataSource.class).getConnection();
            DatabaseMetaData meta = connection.getMetaData();
            String type[] = {"TABLE", "VIEW"};
            rs = meta.getTables(null, null, table, type);
            return rs.next();
        } catch (SQLException e) {
            throw new OrmException("检查表是否存在时出错", e);
        } finally {
            CmnOrm.close(connection, null, rs);
        }
    }

    public static List<String> getTables() {
        List<String> list = new ArrayList<>();
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = CmnIoc.getBean(DataSource.class).getConnection();
            DatabaseMetaData dbmd = connection.getMetaData();
            String[] types = {"TABLE", "VIEW"};
            rs = dbmd.getTables(null, null, "%", types);
            while ( rs.next() ) {
                list.add(rs.getString("TABLE_NAME").toLowerCase());
            }
            return list;
        } catch (SQLException e) {
            throw new OrmException(e);
        } finally {
            CmnOrm.close(connection, null, rs);
        }

    }

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
            // 忽略Transient及final字段
            if ( Modifier.isTransient(field.getModifiers()) || field.isAnnotationPresent(Transient.class)
                    || Modifier.isFinal(field.getModifiers()) ) {
                continue;
            }

            // public字段、注解字段
            // if ( Modifier.isPublic(field.getModifiers()) || field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(Column.class) ) {
            field.setAccessible(true);
            map.put(field.getName(), field);
            // }
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
