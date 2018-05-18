package top.gotoeasy.framework.orm.transaction.config;

import top.gotoeasy.framework.ioc.annotation.Component;
import top.gotoeasy.framework.orm.annotation.Transaction;

@Transaction(readOnly = true)
@Component
public class StudentService {

    public BookEntity getBook() {
        BookEntity bookEntity = new BookEntity();
        checkBook(bookEntity);
        return bookEntity;
    }

    @Transaction
    public void checkBook(BookEntity bookEntity) {
        //
    }

}
