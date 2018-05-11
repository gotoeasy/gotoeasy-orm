package top.gotoeasy.framework.orm.transaction.impl;

import java.util.ArrayDeque;
import java.util.Deque;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.orm.annotation.Transaction;
import top.gotoeasy.framework.orm.transaction.TransactionManager;

/**
 * 事务管理抽象类
 * 
 * @since 2018/05
 * @author 青松
 */
public abstract class AbstractTransactionManager implements TransactionManager {

    private static final Log                log              = LoggerFactory.getLogger(AbstractTransactionManager.class);

    private ThreadLocal<Deque<Transaction>> localTransaction = new ThreadLocal<>();

    /**
     * 判断是否要开启新事务
     * 
     * @param transaction 事务注解
     * @return true:是/false:否
     */
    @Override
    public boolean isNewTransaction(Transaction transaction) {
        if ( transaction == null ) {
            log.trace("无事务注解，不需事务");
            return false;
        }
        log.trace("有事务注解[{}]", transaction);

        if ( localTransaction.get() == null || localTransaction.get().isEmpty() ) {
            log.debug("当前环境无事务，开启新事务[{}]", transaction);
        } else if ( transaction.isNewTransaction() ) {
            log.debug("当前环境有事务，注解指定需新事务，开启新事务[{}]", transaction);
        } else {
            log.trace("沿用当前环境事务不变");
            return false;
        }

        return true;
    }

    /**
     * 开始事务
     * 
     * @param transaction 事务注解
     */
    @Override
    public void beginTransaction(Transaction transaction) {
        Deque<Transaction> stack = localTransaction.get();
        if ( stack == null ) {
            stack = new ArrayDeque<>();
            localTransaction.set(stack);
        }

        stack.add(transaction); // 添加元素到尾部
    }

    /**
     * 提交事务
     * 
     * @param transaction 事务注解
     */
    @Override
    public void commitTransaction(Transaction transaction) {
        if ( localTransaction.get() == null || localTransaction.get().isEmpty() ) {
            log.warn("当前无事务，提交无效");
        } else {
            localTransaction.get().pollLast(); // 弹出尾部元素
        }

        if ( localTransaction.get().isEmpty() ) {
            localTransaction.remove();
        }
    }

    /**
     * 回滚事务
     * 
     * @param transaction 事务注解
     */
    @Override
    public void rollbackTransaction(Transaction transaction) {
        if ( localTransaction.get() == null || localTransaction.get().isEmpty() ) {
            log.warn("当前无事务，回滚无效");
        } else {
            localTransaction.get().pollLast(); // 弹出尾部元素
        }

        if ( localTransaction.get().isEmpty() ) {
            localTransaction.remove();
        }
    }

}
