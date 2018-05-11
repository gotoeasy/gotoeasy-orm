package top.gotoeasy.framework.orm.transaction.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.sql.DataSource;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.ioc.util.CmnIoc;
import top.gotoeasy.framework.orm.annotation.Transaction;
import top.gotoeasy.framework.orm.exception.OrmException;
import top.gotoeasy.framework.orm.util.CmnOrm;

/**
 * 默认事务管理器
 * 
 * @since 2018/05
 * @author 青松
 */
public class DefalutTransactionManager extends AbstractTransactionManager {

    private static final Log               log             = LoggerFactory.getLogger(DefalutTransactionManager.class);

    private ThreadLocal<Deque<Connection>> localConnection = new ThreadLocal<>();

    /**
     * 取得数据库连接
     * 
     * @return 数据库连接
     */
    @Override
    public Connection getConnection() {
        Deque<Connection> stack = localConnection.get();
        if ( stack == null || stack.isEmpty() ) {
            throw new OrmException("当前无事务无法取得连接");
        }

        return stack.peekLast(); // 从尾部取得最近的一个连接
    }

    /**
     * 创建SQL语句
     * 
     * @return SQL语句
     */
    @Override
    public Statement createStatement() {
        try {
            return getConnection().createStatement();
        } catch (SQLException e) {
            throw new OrmException("创建SQL语句失败", e);
        }
    }

    /**
     * 创建预编译SQL语句
     * 
     * @param sql SQL文
     * @return 预编译SQL语句
     */
    @Override
    public PreparedStatement prepareStatement(String sql) {
        try {
            return getConnection().prepareStatement(sql);
        } catch (SQLException e) {
            throw new OrmException("创建预编译SQL语句失败", e);
        }
    }

    /**
     * 开始事务
     * 
     * @param transaction 事务注解
     */
    @Override
    public void beginTransaction(Transaction transaction) {
        if ( localConnection.get() == null ) {
            localConnection.set(new ArrayDeque<>());
        }

        DataSource ds = CmnIoc.getBean(DataSource.class);
        Connection connection = null;
        try {
            connection = ds.getConnection();
        } catch (SQLException e) {
            throw new OrmException("从DataSource取得Connection失败", e);
        }

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            CmnOrm.close(connection);
            throw new OrmException(e);
        }

        localConnection.get().add(connection); // 添加元素到尾部
        super.beginTransaction(transaction);
        log.info("■■■ 开始事务，只读={} ■■■", transaction.readOnly());
    }

    /**
     * 提交事务
     * 
     * @param transaction 事务注解
     */
    @Override
    public void commitTransaction(Transaction transaction) {
        if ( transaction.readOnly() ) {
            log.error("只读事务不能提交，按回滚处理[{}]", transaction);
            this.rollbackTransaction(transaction);
            throw new OrmException("只读事务不能提交");
        }

        Deque<Connection> stack = localConnection.get();
        if ( stack == null || stack.isEmpty() ) {
            log.warn("当前没有事务，提交无效");
            return;
        }

        Connection connection = stack.pollLast(); // 弹出尾部元素
        try {
            connection.commit();
        } catch (SQLException e) {
            log.error("事务提交失败", e);
            throw new OrmException("事务提交失败", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.error("connection.setAutoCommit(true)失败", e);
            }

            CmnOrm.close(connection);
        }

        super.commitTransaction(transaction);
        log.info("■■■ 提交事务 ■■■");
    }

    /**
     * 回滚事务
     * 
     * @param transaction 事务注解
     */
    @Override
    public void rollbackTransaction(Transaction transaction) {
        Deque<Connection> stack = localConnection.get();
        if ( stack == null || stack.isEmpty() ) {
            log.warn("当前没有事务，回滚无效");
            return;
        }

        Connection connection = stack.pollLast(); // 弹出尾部元素
        try {
            connection.commit();
        } catch (SQLException e) {
            log.error("事务回滚失败", e);
            throw new OrmException("事务回滚失败", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                log.error("connection.setAutoCommit(true)失败", e);
            }

            CmnOrm.close(connection);
        }

        super.rollbackTransaction(transaction);
        log.info("■■■ 回滚事务，只读={} ■■■", transaction.readOnly());
    }

}
