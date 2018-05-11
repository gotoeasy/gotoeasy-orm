package top.gotoeasy.framework.orm.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库事务注解
 * 
 * @since 2018/05
 * @author 青松
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Transaction {

    /**
     * 指定数据库
     * <p>
     * 未指定时代表默认数据库
     * </p>
     * 
     * @return 数据库
     */
    String value() default "";

    /**
     * 是否只读
     * 
     * @return true:只读/false:可读写
     */
    boolean readOnly() default false;

    /**
     * 是否要开启新事务
     * <p>
     * 未指定时，如果已开启事务则使用原事务，否则开启新事务使用
     * </p>
     * 
     * @return true:要/false:不要
     */
    boolean isNewTransaction() default false;

    /**
     * 捕获指定异常时回滚事务
     * <p>
     * 未指定时按noRollbackForException规则处理<br>
     * rollbackForException和noRollbackForException同时指定时，以rollbackForException为准<br>
     * 两者都未指定时，捕获任意异常都将回滚事务
     * </p>
     * 
     * @return 异常类数组
     */
    Class<?>[] rollbackForException() default {};

    /**
     * 捕获指定异常时不回滚事务
     * <p>
     * 未指定时按rollbackForException规则处理<br>
     * rollbackForException和noRollbackForException同时指定时，以rollbackForException为准<br>
     * 两者都未指定时，捕获任意异常都将回滚事务
     * </p>
     * 
     * @return 异常类数组
     */
    Class<?>[] noRollbackForException() default {};

}