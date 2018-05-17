package top.gotoeasy.framework.orm.transaction.aop;

import java.lang.reflect.Method;

import top.gotoeasy.framework.aop.Enhance;
import top.gotoeasy.framework.aop.SuperInvoker;
import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Around;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.ioc.annotation.Autowired;
import top.gotoeasy.framework.orm.annotation.Transaction;
import top.gotoeasy.framework.orm.transaction.TransactionManager;

/**
 * 数据库事务拦截处理
 * 
 * @since 2018/05
 * @author 青松
 */
@Aop
public class TransactionAround {

    private static final Log   log = LoggerFactory.getLogger(TransactionAround.class);

    @Autowired
    private TransactionManager transactionManager;

    /**
     * 对@Transaction注解的方法进行拦截处理
     * 
     * @param enhance 增强对象
     * @param method 被拦截方法
     * @param superInvoker 父类方法调用器
     * @param args 被拦截方法的参数
     * @return 结果
     */
    @Around(annotations = Transaction.class)
    public Object around(Enhance enhance, Method method, SuperInvoker superInvoker, Object ... args) {

        Transaction transaction = method.getAnnotation(Transaction.class);
        if ( !transactionManager.isNewTransaction(transaction) ) {
            log.trace("不是新开事物，直接返回调用结果");
            return superInvoker.invoke(args);
        }

        transactionManager.beginTransaction(transaction);
        boolean isRollback = false;

        try {
            return superInvoker.invoke(args);
        } catch (Exception ex) {
            isRollback = isRollbackException(transaction, ex);
            throw ex;
        } finally {
            if ( transaction.readOnly() || isRollback ) {
                transactionManager.rollbackTransaction(transaction);
            } else {
                transactionManager.commitTransaction(transaction);
            }
        }

    }

    private boolean isRollbackException(Transaction transaction, Exception ex) {

        if ( transaction.rollbackForException().length > 0 ) {
            // 优先判断rollbackForException
            if ( matchClass(transaction.rollbackForException(), ex) ) {
                log.trace("有指定要回滚的异常类范围，当前异常在该范围内，将被回滚");
                return true;
            }
        } else {
            // 未指定rollbackForException时,判断noRollbackForException
            if ( matchClass(transaction.noRollbackForException(), ex) ) {
                log.trace("有指定不回滚的异常类范围，当前异常在该范围内，将被提交");
                return false;
            }
        }

        log.trace("判断属于需回滚异常，将被回滚");
        return true;

    }

    private boolean matchClass(Class<?>[] classes, Exception ex) {
        for ( Class<?> clas : classes ) {
            if ( clas.isInstance(ex) ) {
                return true;
            }
        }
        return false;
    }
}
