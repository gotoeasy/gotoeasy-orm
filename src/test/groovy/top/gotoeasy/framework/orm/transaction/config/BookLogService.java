package top.gotoeasy.framework.orm.transaction.config;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.ioc.annotation.Component;
import top.gotoeasy.framework.orm.annotation.Transaction;

@Component
public class BookLogService {

    private static final Log log = LoggerFactory.getLogger(BookLogService.class);

    @Transaction(isNewTransaction = true)
    public int log(BookEntity bookEntity) {
        log.info("执行 BookService.log");
        return 1;
    }

    @Transaction
    public int save(BookEntity bookEntity) {
        log.info("执行 BookService.save");
        return 1;
    }
}
