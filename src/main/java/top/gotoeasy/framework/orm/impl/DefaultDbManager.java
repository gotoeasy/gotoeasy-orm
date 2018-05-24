package top.gotoeasy.framework.orm.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.util.CmnBean;
import top.gotoeasy.framework.core.util.CmnString;
import top.gotoeasy.framework.ioc.util.CmnIoc;
import top.gotoeasy.framework.orm.annotation.Transaction;
import top.gotoeasy.framework.orm.exception.OrmException;
import top.gotoeasy.framework.orm.strategy.OrmNamingStrategy;
import top.gotoeasy.framework.orm.transaction.TransactionManager;
import top.gotoeasy.framework.orm.util.CmnOrm;

@Transaction(readOnly = true)
public class DefaultDbManager extends AbstractDbManager {

    private static final Log log = LoggerFactory.getLogger(DefaultDbManager.class);

    /**
     * 插入记录 <br/>
     * 主键为空或主键重复时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     */
    @Override
    public <T> int insert(T entity) {
        String namingSql = getInsertSql(entity);
        log.trace(namingSql);
        return executeUpdate(namingSql, entity);
    }

    /**
     * 按主键更新记录(值为null的字段也更新) <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @param includeNullValue true时null值也更新到数据库，否则不更新
     * @return 成功件数
     */
    @Override
    public <T> int update(T entity, boolean includeNullValue) {
        String namingSql = getUpdateSql(entity, includeNullValue);
        log.trace(namingSql);
        if ( CmnString.isBlank(namingSql) ) {
            log.trace("没有字段需要更新，直接返回");
            return 0;
        }
        return executeUpdate(namingSql, entity);
    }

    /**
     * 按主键更新记录(值为null的字段也更新) <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @return 成功件数
     */
    @Override
    public <T> int update(T entity) {
        return update(entity, true);
    }

    /**
     * 按主键判断记录是否存在</br>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录（含主键值）
     * @return true：存在， false：不存在
     */
    @Override
    public <T> boolean isExist(T entity) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String namingSql = "select count(*) from {table} where {where};";
        namingSql = CmnString.format(namingSql, strategy.tableName(entity.getClass()), getIdConditionSql(entity.getClass()));
        log.debug(namingSql);
        return executeQueryInt(namingSql, entity) > 0;
    }

    /**
     * 按主键判断，记录存在时做更新处理，不存在时插入记录 <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @param includeNullValue true时null值也更新到数据库，否则不更新
     * @return 成功件数
     */
    @Override
    public <T> int insertOrUpdate(T entity, boolean includeNullValue) {
        int cnt = update(entity, includeNullValue);
        if ( cnt < 1 ) {
            cnt = insert(entity);
        }
        return cnt;
    }

    /**
     * 按主键删除记录 <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @return 成功件数
     */
    @Override
    public <T> int delete(T entity) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String namingSql = "delete from {table} where {where};";
        namingSql = CmnString.format(namingSql, strategy.tableName(entity.getClass()), getIdConditionSql(entity.getClass()));
        log.debug(namingSql);
        return executeUpdate(namingSql, entity);
    }

    /**
     * 按主键检索记录</br>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录（含主键值）
     * @return 记录
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T findById(T entity) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String namingSql = "select * from {table} where {where};";
        namingSql = CmnString.format(namingSql, strategy.tableName(entity.getClass()), getIdConditionSql(entity.getClass()));
        log.debug(namingSql);
        return (T)findOne(namingSql, entity, entity.getClass());
    }

    /**
     * 按主键检索记录</br>
     * 
     * @param <T> Entity类
     * @param entityClass 表定义的Entity类
     * @param id 主键
     * @return 记录
     */
    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String sql = "select * from {table} where {where};";
        sql = CmnString.format(sql, strategy.tableName(entityClass), getIdConditionSql(entityClass)).replaceAll(REG_PARAM, "?");
        log.debug(sql);
        log.debug("参数[{}]", id);

        PreparedStatement preparedStatement = null;
        ResultSet rs = null;

        try {
            preparedStatement = CmnIoc.getBean(TransactionManager.class).prepareStatement(sql);
            preparedStatement.setObject(1, id);
            rs = preparedStatement.executeQuery();
            if ( rs.next() ) {
                return CmnBean.mapToBean(readToMap(rs, getPropertyNames(rs)), entityClass);
            }
            return null;
        } catch (SQLException e) {
            throw new OrmException("执行SQL查询出错了", e);
        } finally {
            CmnOrm.close(preparedStatement, rs);
        }
    }

    /**
     * 检索全部记录
     * 
     * @param <T> Entity类
     * @param entityClass 表定义的Entity类
     * @param orders 排序
     * @return 检索结果List
     */
    @Override
    public <T> List<T> findAll(Class<T> entityClass, String ... orders) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String namingSql = "select * from {table} {orderby};";
        String orderby = orders.length > 0 ? "order by " + strategy.columnName(CmnString.join(orders, ",")) : "";
        namingSql = CmnString.format(namingSql, strategy.tableName(entityClass), orderby);
        log.debug(namingSql);
        return find(namingSql, null, entityClass);
    }

    /**
     * 按指定SQL和条件进行检索 。
     * 
     * @param <T> Entity类
     * @param namingSql 检索用sql （自定义结果类时检索列需指定各列匿名，如 SELECT a.id as id FROM tbl a）
     * @param condition 检索条件 （Map、POJO）
     * @param recordClass 记录的类 （POJO）
     * @return 检索结果List
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> find(T entity, String ... orders) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String namingSql = "select * from {table} {where} {orderBy};";
        String orderby = orders.length > 0 ? "order by " + strategy.columnName(CmnString.join(orders, ",")) : "";
        String where = getConditionSql(entity);
        if ( !CmnString.isBlank(where) ) {
            where = "where " + where;
        }
        namingSql = CmnString.format(namingSql, strategy.tableName(entity.getClass()), where, orderby);

        List<T> list = new ArrayList<>();
        super.query(namingSql, entity, mapRow -> list.add((T)CmnBean.mapToBean(mapRow, entity.getClass())));

        return list;
    }

    /**
     * 按指定hql和条件进行检索 。
     * 
     * @param namingSql 检索用sql （自定义结果类时检索列需指定各列匿名，如 SELECT a.id as id FROM tbl a）
     * @param condition 检索条件 （Map、POJO）
     * @return 检索结果List
     */
    @Override
    public List<Map<String, Object>> findByCondtion(String namingSql, Object condition) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = toPrepareSql(namingSql); // 命名参数替换为问号
        List<Object> listParams = getSqlParameters(namingSql, condition);
        log.debug(namingSql);
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
                list.add(readToMap(rs, propertyNames));
            }
        } catch (SQLException e) {
            throw new OrmException("执行SQL查询出错", e);
        } finally {
            CmnOrm.close(preparedStatement, rs);
        }

        return list;
    }

    /**
     * 按指定hql和条件进行检索 。
     * 
     * @param <T> Entity类
     * @param namingSql 检索用sql （自定义结果类时检索列需指定各列匿名，如 SELECT a.id as id FROM tbl a）
     * @param condition 检索条件 （Map、POJO）
     * @param recordClass 记录的类 （POJO）
     * @return 检索结果List
     */
    @Override
    public <T> List<T> find(String namingSql, Object condition, Class<T> recordClass) {
        List<T> list = new ArrayList<>();
        String sql = toPrepareSql(namingSql); // 命名参数替换为问号
        List<Object> listParams = getSqlParameters(namingSql, condition);
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
            Map<String, Object> map;
            while ( rs.next() ) {
                map = readToMap(rs, propertyNames);
                list.add(CmnBean.mapToBean(map, recordClass));
            }
        } catch (SQLException e) {
            throw new OrmException(e.getMessage(), e);
        } finally {
            CmnOrm.close(preparedStatement, rs);
        }

        return list;
    }

    /**
     * 按指定SQL和条件检索第一条记录 。
     * 
     * @param <T> Entity类
     * @param namingSql 检索用sql
     * @param condition 检索条件
     * @return 记录
     */
    @Override
    public Map<String, Object> findOne(String namingSql, Object condition) {
        String sql = toPrepareSql(namingSql); // 命名参数替换为问号
        List<Object> listParams = getSqlParameters(namingSql, condition);
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
                return readToMap(rs, getPropertyNames(rs));
            }
            return null;
        } catch (SQLException e) {
            throw new OrmException("执行SQL查询出错", e);
        } finally {
            CmnOrm.close(preparedStatement, rs);
        }
    }

    /**
     * 按指定SQL和条件检索第一条记录 。
     * 
     * @param <T> Entity类
     * @param namingSql 检索用sql
     * @param condition 检索条件
     * @param recordClass 记录的类 （POJO）
     * @return 记录
     */
    @Override
    public <T> T findOne(String namingSql, Object condition, Class<T> recordClass) {
        Map<String, Object> map = findOne(namingSql, condition);
        return map == null ? null : CmnBean.mapToBean(map, recordClass);
    }

    /**
     * 检索全表件数
     * 
     * @param <T> Entity类
     * @param entity 表定义Entity类
     * @return 件数
     */
    @Override
    public <T> int countAll(Class<T> entityClass) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String sql = "select count(*) from " + strategy.tableName(entityClass);
        return executeQueryInt(sql, null);
    }

    /**
     * 按匹配条件检索件数
     * 
     * @param <T> Entity类
     * @param entity 表定义Entity对象
     * @return 件数
     */
    @Override
    public <T> int count(T entity) {
        OrmNamingStrategy strategy = CmnIoc.getBean(OrmNamingStrategy.class);
        String namingSql = "select count(*) from {table} where {where};";
        namingSql = CmnString.format(namingSql, strategy.tableName(entity.getClass()), getConditionSql(entity));
        log.debug(namingSql);
        return executeQueryInt(namingSql, entity);
    }

    /**
     * 按条件检索件数
     * 
     * @param namingSql 检索用SQL
     * @param condition 检索条件
     * @return 件数
     */
    @Override
    public int countByCondition(String namingSql, Object condition) {
        log.debug(namingSql);
        return executeQueryInt(namingSql, condition);
    }

}
