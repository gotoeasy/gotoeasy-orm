package top.gotoeasy.framework.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库表注解
 * 
 * @since 2018/05
 * @author 青松
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Entity {

    /**
     * 指定物理表名
     * <p>
     * 未指定时按策略映射表名
     * </p>
     * 
     * @return 物理表名
     */
    String value() default "";
}
