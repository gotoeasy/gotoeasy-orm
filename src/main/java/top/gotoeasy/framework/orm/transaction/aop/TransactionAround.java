package top.gotoeasy.framework.orm.transaction.aop;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.orm.annotation.Transaction;

/**
 * 数据库事务拦截处理
 * 
 * @since 2018/05
 * @author 青松
 */
@Aop
public class TransactionAround {

    /**
     * 对@Transaction注解的方法进行拦截处理
     * 
     * @param enhance 增强对象
     * @param method 被拦截方法
     * @param superInvoker 父类方法调用器
     * @param args 被拦截方法的参数
     * @return 结果
     */
    @Around(annotation = Transaction.class)
    public Object around(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {

        return superInvoker.invoke(args);
    }
}
