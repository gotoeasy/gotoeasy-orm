package top.gotoeasy.framework.orm.impl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import top.gotoeasy.framework.core.converter.ConvertUtil;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.util.CmnBean;
import top.gotoeasy.framework.core.util.CmnString;
import top.gotoeasy.framework.ioc.util.CmnIoc;
import top.gotoeasy.framework.orm.DbManager;
import top.gotoeasy.framework.orm.annotation.Id;
import top.gotoeasy.framework.orm.exception.OrmException;
import top.gotoeasy.framework.orm.strategy.OrmNamingStrategy;
import top.gotoeasy.framework.orm.transaction.TransactionManager;
import top.gotoeasy.framework.orm.util.CmnOrm;

/**
 * 数据库操作抽象类
 * 
 * @since 2018/05
 * @author 青松
 */
public abstract class AbstractDbManager implements DbManager {

    private static final Log      log       = LoggerFactory.getLogger(AbstractDbManager.class);

    /** 命名参数【:name】的正则表达式 */
    protected static final String REG_PARAM = "[:]{1,1}[\\w]{1,}";

    /**
     * 执行查询
     * 
     * @param namingSql 命名参数SQL
     * @param paramObject 参数
     * @param reader 结果集阅读器
     */
    protected void query(String namingSql, Object paramObject, ResultSetReader reader) {
        String sql = toPrepareSql(namingSql); // 命名参数替换为问号
        List<Object> listParams = getSqlParameters(namingSql, paramObject);
        log.debug(sql);
        log.debug("参数{}", listParams);

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            preparedStatement = CmnIoc.getBean(TransactionManager.class).prepareStatement(sql);
            for ( int i = 0; i < listParams.size(); i++ ) {
                preparedStatement.setObject(i + 1, listParams.get(i));
            }
            rs = preparedStatement.executeQuery();
            String[] propertyNames = getPropertyNames(rs);
            while ( rs.next() ) {
                reader.readRow(readToMap(rs, propertyNames));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new OrmException("执行命名参数SQL查询出错", e);
        } finally {
            CmnOrm.close(preparedStatement, rs);
        }
    }

    /**
     * 执行命名参数SQL查询
     * 
     * @param namingSql 命名参数SQL
     * @param paramObject 参数对象
     * @return 查询结果集游标
     */
    protected ResultSet executeQuery(PreparedStatement preparedStatement, String namingSql, Object paramObject) {
        String sql = toPrepareSql(namingSql); // 命名参数替换为问号
        List<Object> listParams = getSqlParameters(namingSql, paramObject);
        log.debug(sql);
        log.debug("参数{}", listParams);

        try {
            for ( int i = 0; i < listParams.size(); i++ ) {
                preparedStatement.setObject(i + 1, listParams.get(i));
            }
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new OrmException("执行预编译SQL查询出错", e);
        }
    }

    /**
     * 执行命名参数SQL取得查询结果数值
     * 
     * @param namingSql 命名参数SQL
     * @param paramObject 参数对象
     * @return 查询结果数值
     */
    protected int executeQueryInt(String namingSql, Object paramObject) {
        String sql = toPrepareSql(namingSql); // 命名参数替换为问号
        List<Object> listParams = getSqlParameters(namingSql, paramObject);
        log.debug(sql);
        log.debug("参数{}", listParams);

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            preparedStatement = CmnIoc.getBean(TransactionManager.class).prepareStatement(sql);
            for ( int i = 0; i < listParams.size(); i++ ) {
                preparedStatement.setObject(i + 1, listParams.get(i));
            }
            rs = preparedStatement.executeQuery();

            if ( rs.next() ) {
                BigDecimal bigDecimal = rs.getBigDecimal(1);
                return bigDecimal == null ? 0 : bigDecimal.intValue();
            }

            return 0;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new OrmException("执行命名参数SQL查询出错", e);
        } finally {
            CmnOrm.close(preparedStatement, rs);
        }

    }

    /**
     * 执行命名参数SQL(参数使用问号“:name”形式)
     * 
     * @param namingSql 命名参数SQL
     * @param paramObject 参数对象
     * @return
     */
    @Override
    public int executeUpdate(String namingSql, Object paramObject) {
        String sql = toPrepareSql(namingSql); // 命名参数替换为问号
        List<Object> listParams = getSqlParameters(namingSql, paramObject);
        return execute(sql, listParams);
    }

    /**
     * 执行SQL(参数使用问号“?”形式)
     * 
     * @param sql SQL
     * @param listParams 参数列表
     * @return 影响件数
     */
    @Override
    public int execute(String sql, List<Object> listParams) {
        log.debug(sql);
        log.debug("参数{}", listParams);

        TransactionManager transactionManager = CmnIoc.getBean(TransactionManager.class);
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = transactionManager.prepareStatement(sql);
            if ( listParams != null ) {
                for ( int i = 0; i < listParams.size(); i++ ) {
                    preparedStatement.setObject(i + 1, listParams.get(i));
                }
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new OrmException(e.getMessage(), e);
        } finally {
            CmnOrm.close(preparedStatement);
        }
    }

    /**
     * 按主键删除记录
     * 
     * @param <T> Entity类
     * @param entityClass 表定义的Entity类
     * @param id 主键值
     * @return 成功件数
     */
    @Override
    public <T> int deleteById(Class<T> entityClass, Object ... ids) {
        if ( ids == null || ids.length == 0 ) {
            throw new OrmException("必须设定主键参数进行操作");
        }

        String pk = null;
        Map<String, Field> mapField = CmnOrm.getEntityFields(entityClass);
        Iterator<String> it = mapField.keySet().iterator();
        String name;
        while ( it.hasNext() ) {
            name = it.next();
            if ( !mapField.get(name).isAnnotationPresent(Id.class) ) {
                continue;
            }
            if ( pk == null ) {
                pk = name;
            } else {
                throw new OrmException("复合主键不支持按单一主键操作");
            }
        }

        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String sql = "delete from {table} where {pk} in ({ids});";

        List<Object> listParams = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < ids.length; i++ ) {
            listParams.add(ids[i]);
            buf.append(",?");
        }
        buf.delete(0, 1); // 删除第一个逗号

        sql = CmnString.format(sql, strategy.tableName(entityClass), strategy.columnName(pk), buf.toString());
        return execute(sql, listParams);
    }

    /**
     * 命名参数转换成“?”形式参数
     * 
     * @param namingSql 命名SQL
     * @return 参数为“?”形式的SQL文
     */
    protected String toPrepareSql(String namingSql) {
        return namingSql.replaceAll(REG_PARAM, "?");
    }

    protected List<Object> getSqlParameters(String namingSql, Object paramObject) {
        List<Object> listParams = new ArrayList<>();
        if ( paramObject == null ) {
            return listParams;
        }
        Pattern pattern = Pattern.compile(REG_PARAM);
        Matcher matcher = pattern.matcher(namingSql);
        String name;
        while ( matcher.find() ) {
            name = matcher.group().substring(1);
            listParams.add(toSqlParameterObject(CmnBean.getPropertyValue(paramObject, name)));
        }
        return listParams;
    }

    protected Object toSqlParameterObject(Object obj) {
        if ( obj == null ) {
            return null;
        }
        if ( obj instanceof Date ) {
            return ConvertUtil.convert(obj, java.sql.Date.class);
        }
        return obj;
    }

    protected String[] getPropertyNames(ResultSet rs) throws SQLException {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] names = new String[columnCount];
        for ( int i = 0; i < columnCount; i++ ) {
            names[i] = strategy.fieldName(metaData.getColumnLabel(i + 1).toLowerCase());
        }

        return names;
    }

    protected Map<String, Object> readToMap(ResultSet rs, String[] propertyNames) throws SQLException {
        Map<String, Object> map = new HashMap<>();
        for ( int i = 0; i < propertyNames.length; i++ ) {
            map.put(propertyNames[i], rs.getObject(i + 1));
        }
        return map;
    }

    protected <T> String getUpdateSql(T entity, boolean includeNullValue) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String table = strategy.tableName(entity.getClass());
        String sql = "update {table} set {columns} where {where};";

        Map<String, Field> mapField = CmnOrm.getEntityFields(entity.getClass());
        StringBuilder columns = new StringBuilder();
        StringBuilder where = new StringBuilder();

        Iterator<String> it = mapField.keySet().iterator();
        String name;
        while ( it.hasNext() ) {
            name = it.next();
            if ( !includeNullValue && CmnBean.getFieldValue(entity, name) == null ) {
                continue;
            }

            if ( mapField.get(name).isAnnotationPresent(Id.class) ) {
                // where of id
                if ( where.length() > 0 ) {
                    where.append(" ").append("and ");
                }
                where.append(strategy.columnName(name)).append("=:").append(name);
            } else {
                // set
                if ( columns.length() > 0 ) {
                    columns.append(",");
                }
                columns.append(strategy.columnName(name)).append("=:").append(name);
            }

        }

        if ( columns.length() == 0 ) {
            // 没有字段需要更新
            return null;
        }
        return CmnString.format(sql, table, columns.toString(), where.toString());
    }

    protected <T> String getInsertSql(T entity) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String table = strategy.tableName(entity.getClass());
        String sql = "insert into {table}({columns}) values({values});";
        List<String> listProps = CmnOrm.getEntityFieldNames(entity.getClass());

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for ( String name : listProps ) {
            if ( columns.length() > 0 ) {
                columns.append(",");
                values.append(",");
            }
            columns.append(name);
            values.append(":").append(name);
        }

        return CmnString.format(sql, table, strategy.columnName(columns.toString()), values.toString());
    }

    protected <T> String getCountByIdSql(Class<T> entityClass) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String table = strategy.tableName(entityClass);
        String sql = "select count(*) from {table} where {where};";
        return CmnString.format(sql, table, getIdConditionSql(entityClass));
    }

    protected <T> String getIdConditionSql(Class<T> entityClass) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        Map<String, Field> mapField = CmnOrm.getEntityFields(entityClass);
        StringBuilder where = new StringBuilder();

        Iterator<String> it = mapField.keySet().iterator();
        String name;
        while ( it.hasNext() ) {
            name = it.next();
            if ( !mapField.get(name).isAnnotationPresent(Id.class) ) {
                continue;
            }

            if ( where.length() > 0 ) {
                where.append(" and ");
            }
            where.append(strategy.columnName(name)).append("=:").append(name);
        }

        return where.toString();
    }

    protected <T> String getConditionSql(T entity) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        Map<String, Field> mapField = CmnOrm.getEntityFields(entity.getClass());
        StringBuilder where = new StringBuilder();
        Field field;

        Iterator<String> it = mapField.keySet().iterator();
        String name;
        while ( it.hasNext() ) {
            name = it.next();
            field = mapField.get(name);
            if ( CmnString.isEmpty(CmnBean.getFieldValue(field, entity)) ) {
                continue;
            }

            if ( where.length() > 0 ) {
                where.append(" and ");
            }
            where.append(strategy.columnName(name)).append("=:").append(name);
        }

        return where.toString();
    }

}
