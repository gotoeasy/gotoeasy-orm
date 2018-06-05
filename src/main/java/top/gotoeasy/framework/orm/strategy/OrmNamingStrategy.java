package top.gotoeasy.framework.orm.strategy;

/**
 * ROM模块命名策略
 * 
 * @since 2018/05
 * @author 青松
 */
public interface OrmNamingStrategy {

    /**
     * 表物理名
     * <p>
     * 类名不含包名时直接转换<br>
     * 类名含包名时，通过判断类的@Entity注解取得表名
     * 注解@Entity未指定表名时按类名称转换取得表名
     * </p>
     * 
     * @param className 类名
     * @return 表物理名
     */
    public String tableName(String className);

    /**
     * 表物理名
     * <p>
     * 通过判断类的@Entity注解取得表名<br>
     * 注解@Entity未指定表名时按类名称转换取得表名
     * </p>
     * 
     * @param clas 类
     * @return 表物理名
     */
    public String tableName(Class<?> clas);

    /**
     * 字段物理名
     * 
     * @param fieldName Java字段名
     * @return 字段物理名
     */
    public String columnName(String fieldName);

    /**
     * 按字段物理名取得Java字段名
     * 
     * @param columnName 字段物理名
     * @return Java字段名
     */
    public String fieldName(String columnName);

}
