package top.gotoeasy.framework.orm.transaction.impl;

import java.util.ArrayDeque;
import java.util.Deque;

import javax.sql.DataSource;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.core.util.Assert;
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

    private DataSource                      dataSource;

    /**
     * 取得DataSource
     * 
     * @return DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 设定DataSource
     * 
     * @param dataSource DataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 判断是否要开启新事务
     * 
     * @param transaction 事务注解
     * @return true:是/false:否
     */
    @Override
    public boolean isNewTransaction(Transaction transaction) {
        Assert.notNull(transaction, "参数transaction不能为null");
        log.trace("有事务注解[{}]", transaction);

        if ( localTransaction.get() == null || localTransaction.get().isEmpty() ) {
            log.debug("当前环境无事务，开启新事务[{}]", transaction);
        } else if ( transaction.isNewTransaction() ) {
            log.debug("当前环境有事务，注解指定需新事务，开启新事务[{}]", transaction);
        } else {
            if ( localTransaction.get().peekFirst().readOnly() && !transaction.readOnly() ) {
                log.warn("已声明的事务为只读，当前声明的可写事务无效，保持当前事务为只读");
            }
            log.trace("沿用当前环境事务不变");
            return false;
        }

        return true;
    }

    /**
     * 添加事务
     * 
     * @param transaction 事务注解
     */
    protected void push(Transaction transaction) {
        Deque<Transaction> stack = localTransaction.get();
        if ( stack == null ) {
            stack = new ArrayDeque<>();
            localTransaction.set(stack);
        }

        stack.push(transaction);
    }

    /**
     * 弹出事务
     * 
     * @param transaction 事务注解
     */
    public void pop(Transaction transaction) {
        localTransaction.get().pop();

        if ( localTransaction.get().isEmpty() ) {
            localTransaction.remove();
        }
    }

}
