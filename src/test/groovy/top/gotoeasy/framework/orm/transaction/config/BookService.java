package top.gotoeasy.framework.orm.transaction.config;

import top.gotoeasy.framework.core.log.Log;
import top.gotoeasy.framework.core.log.LoggerFactory;
import top.gotoeasy.framework.ioc.annotation.Autowired;
import top.gotoeasy.framework.ioc.annotation.Component;
import top.gotoeasy.framework.orm.annotation.Transaction;
import top.gotoeasy.framework.orm.exception.OrmException;

@Component
public class BookService {

    private static final Log log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookLogService   bookLogService;

    @Transaction
    public int update(BookEntity bookEntity) {
        log.info("执行 BookService.update");

        bookLogService.log(bookEntity);
        return bookLogService.save(bookEntity);
    }

    @Transaction(rollbackForException = {UnsupportedOperationException.class, RuntimeException.class})
    public int delete(BookEntity bookEntity) {
        log.info("执行 BookService.delete 后回滚");

        throw new UnsupportedOperationException();
    }

    @Transaction(noRollbackForException = {UnsupportedOperationException.class})
    public int remove(BookEntity bookEntity) {
        log.info("执行 BookService.remove 后提交");

        throw new UnsupportedOperationException();
    }

    @Transaction(rollbackForException = {UnsupportedOperationException.class, OrmException.class})
    public int del(BookEntity bookEntity) {
        log.info("执行 BookService.remove 后回滚");

        throw new RuntimeException();
    }

    @Transaction(readOnly = true)
    public BookEntity findOne(BookEntity bookEntity) {
        log.info("执行 BookService.findOne");
        bookEntity.name = "name";
        return bookEntity;
    }
}
