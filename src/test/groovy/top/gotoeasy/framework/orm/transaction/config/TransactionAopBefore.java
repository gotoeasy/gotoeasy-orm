package top.gotoeasy.framework.orm.transaction.config;

import top.gotoeasy.framework.aop.annotation.Aop;
import top.gotoeasy.framework.aop.annotation.Before;
import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.orm.annotation.Transaction;

/**
 * 数据库事务拦截处理
 * 
 * @since 2018/05
 * @author 青松
 */
@Aop
public class TransactionAopBefore {

    private static final Log log = LoggerFactory.getLogger(TransactionAopBefore.class);

    @Before(annotation = Transaction.class)
    public void before(BookEntity bookEntity) {
        log.info("前置拦截@Transaction方法，参数：{}", bookEntity);
    }

}
