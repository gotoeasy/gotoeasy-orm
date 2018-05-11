package top.gotoeasy.framework.orm.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import top.gotoeasy.framework.orm.annotation.Transaction;

/**
 * 事务管理接口
 * 
 * @since 2018/05
 * @author 青松
 */
public interface TransactionManager {

    /**
     * 判断是否要开启新事务
     * 
     * @param transaction 事务注解
     * @return true:是/false:否
     */
    public boolean isNewTransaction(Transaction transaction);

    /**
     * 取得数据库连接
     * 
     * @return 数据库连接
     */
    public Connection getConnection();

    /**
     * 创建SQL语句
     * 
     * @return SQL语句
     */
    public Statement createStatement();

    /**
     * 创建预编译SQL语句
     * 
     * @param sql SQL文
     * @return 预编译SQL语句
     */
    public PreparedStatement prepareStatement(String sql);

    /**
     * 开始事务
     * 
     * @param transaction 事务注解
     */
    public void beginTransaction(Transaction transaction);

    /**
     * 提交事务
     * 
     * @param transaction 事务注解
     */
    public void commitTransaction(Transaction transaction);

    /**
     * 回滚事务
     * 
     * @param transaction 事务注解
     */
    public void rollbackTransaction(Transaction transaction);
}
