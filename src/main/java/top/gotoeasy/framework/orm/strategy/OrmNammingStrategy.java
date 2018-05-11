package top.gotoeasy.framework.orm.strategy;

import top.gotoeasy.framework.core.util.CmnClass;
import top.gotoeasy.framework.core.util.CmnString;
import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.exception.OrmException;

/**
 * ROM模块命名策略
 * 
 * @since 2018/05
 * @author 青松
 */
public interface OrmNammingStrategy {

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
    public default String tableName(String className) {
        if ( className.contains(".") ) {
            return tableName(CmnClass.loadClass(className));
        }

        char[] chars = CmnString.uncapitalize(className).toCharArray();
        StringBuilder buf = new StringBuilder();
        boolean isPreLowerCase = false;
        for ( int i = 0; i < chars.length; i++ ) {
            if ( Character.isUpperCase(chars[i]) ) {
                if ( isPreLowerCase ) {
                    buf.append('_');
                }
                buf.append(String.valueOf(chars[i]).toLowerCase());
                isPreLowerCase = false;
            } else {
                buf.append(chars[i]);
                isPreLowerCase = true;
            }
        }
        return buf.toString();
    }

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
    public default String tableName(Class<?> clas) {
        if ( !clas.isAnnotationPresent(Entity.class) ) {
            throw new OrmException("无@Entity注解的类不能映射表名：" + clas.getCanonicalName());
        }

        Entity anno = clas.getAnnotation(Entity.class);
        if ( CmnString.isNotBlank(anno.value()) ) {
            return anno.value();
        }

        return tableName(clas.getSimpleName());
    }

    /**
     * 字段物理名
     * 
     * @param fieldName Java字段名
     * @return 字段物理名
     */
    public default String columnName(String fieldName) {
        return tableName(fieldName);
    }

    /**
     * 按字段物理名取得Java字段名
     * 
     * @param columnName 字段物理名
     * @return Java字段名
     */
    public default String fieldName(String columnName) {
        char[] chars = CmnString.uncapitalize(columnName).toCharArray();
        StringBuilder buf = new StringBuilder();
        char chr = ' ';
        for ( int i = 0; i < chars.length; i++ ) {

            if ( chr == '_' ) {
                if ( chars[i] != '_' ) {
                    buf.append(String.valueOf(chars[i]).toUpperCase());
                }
            } else {
                if ( chars[i] != '_' ) {
                    buf.append(chars[i]);
                }
            }
            chr = chars[i];
        }
        return buf.toString();
    }

}
