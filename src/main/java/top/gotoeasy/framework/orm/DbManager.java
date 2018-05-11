package top.gotoeasy.framework.orm;

import java.util.List;
import java.util.Map;

public interface DbManager {

    /**
     * 执行命名参数SQL(参数使用问号“:name”形式)
     * 
     * @param namingSql 命名参数SQL
     * @param paramObject 参数对象
     * @return 影响件数
     */
    public int executeUpdate(String namingSql, Object paramObject);

    /**
     * 执行SQL(参数使用问号“?”形式)
     * 
     * @param sql SQL
     * @param listParams 参数列表
     * @return 影响件数
     */
    public int execute(String sql, List<Object> listParams);

    /**
     * 插入记录 <br/>
     * 主键为空或主键重复时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     */
    public <T> int insert(T entity);

    /**
     * 按主键更新记录(值为null的字段也更新) <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @return 成功件数
     */
    public <T> int update(T entity);

    /**
     * 按主键更新记录(值为null的字段也更新) <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @param includeNullValue true时null值也更新到数据库，否则不更新
     * @return 成功件数
     */
    public <T> int update(T entity, boolean includeNullValue);

    /**
     * 按主键判断，记录存在时做更新处理，不存在时插入记录 <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @param includeNullValue true时null值也更新到数据库，否则不更新
     * @return 成功件数
     */
    public <T> int insertOrUpdate(T entity, boolean includeNullValue);

    /**
     * 按主键删除记录 <br/>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录
     * @return 成功件数
     */
    public <T> int delete(T entity);

    /**
     * 按主键检索记录</br>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录（含主键值）
     * @return 记录
     */
    public <T> T findById(T entity);

    /**
     * 按主键检索记录</br>
     * 
     * @param <T> Entity类
     * @param entityClass 表定义的Entity类
     * @param id 主键
     * @return 记录
     */
    public <T> T findById(Class<T> entityClass, Object id);

    /**
     * 按主键删除记录
     * 
     * @param <T> Entity类
     * @param entityClass 表定义的Entity类
     * @param id 主键值
     * @return 成功件数
     */
    public <T> int deleteById(Class<T> entityClass, Object ... ids);

    /**
     * 按主键判断记录是否存在</br>
     * 主键为空时将抛出异常
     * 
     * @param <T> Entity类
     * @param entity 记录（含主键值）
     * @return true：存在， false：不存在
     */
    public <T> boolean isExist(T entity);

    /**
     * 检索全部记录
     * 
     * @param <T> Entity类
     * @param entityClass 表定义的Entity类
     * @param orders 排序
     * @return 检索结果List
     */
    public <T> List<T> findAll(Class<T> entityClass, String ... orders);

    /**
     * 按指定hql和条件进行检索 。
     * 
     * @param <T> Entity类
     * @param namingSql 检索用sql （自定义结果类时检索列需指定各列匿名，如 SELECT a.id as id FROM tbl a）
     * @param condition 检索条件 （Map、POJO）
     * @param recordClass 记录的类 （POJO）
     * @return 检索结果List
     */
    public <T> List<T> find(String namingSql, Object condition, Class<T> recordClass);

    /**
     * 按指定hql和条件进行检索 。
     * 
     * @param namingSql 检索用sql （自定义结果类时检索列需指定各列匿名，如 SELECT a.id as id FROM tbl a）
     * @param condition 检索条件 （Map、POJO）
     * @return 检索结果List
     */
    public List<Map<String, Object>> findByCondtion(String namingSql, Object condition);

    /**
     * 按Entity对象进行检索 。
     * 
     * @param <T> Entity类
     * @param entity Entity对象
     * @param orders 排序字段
     * @return 检索结果List
     */
    public <T> List<T> find(T entity, String ... orders);

    /**
     * 按指定SQL和条件检索第一条记录 。
     * 
     * @param <T> Entity类
     * @param namingSql 检索用sql
     * @param condition 检索条件
     * @return 记录
     */
    public Map<String, Object> findOne(String namingSql, Object condition);

    /**
     * 按指定SQL和条件检索第一条记录 。
     * 
     * @param <T> Entity类
     * @param namingSql 检索用sql
     * @param condition 检索条件
     * @param recordClass 记录的类 （POJO）
     * @return 记录
     */
    public <T> T findOne(String namingSql, Object condition, Class<T> recordClass);

    /**
     * 检索全表件数
     * 
     * @param <T> Entity类
     * @param entity 表定义Entity类
     * @return 件数
     */
    public <T> int countAll(Class<T> entityClass);

    /**
     * 按匹配条件检索件数
     * 
     * @param <T> Entity类
     * @param entity 表定义Entity对象
     * @return 件数
     */
    public <T> int count(T entity);

    /**
     * 按条件检索件数
     * 
     * @param namingSql 检索用SQL
     * @param condition 检索条件
     * @return 件数
     */
    public int countByCondition(String namingSql, Object condition);

}
