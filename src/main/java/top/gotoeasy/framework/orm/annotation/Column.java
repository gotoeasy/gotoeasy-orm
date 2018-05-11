package top.gotoeasy.framework.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库字段注解
 * 
 * @since 2018/05
 * @author 青松
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Column {

    /**
     * 指定物理字段名
     * <p>
     * 未指定时按策略映射字段名
     * </p>
     * 
     * @return 物理字段名
     */
    String value() default "";

}
