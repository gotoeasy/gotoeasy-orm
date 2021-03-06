package top.gotoeasy.framework.orm.transaction

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.core.config.DefaultConfig
import top.gotoeasy.framework.ioc.util.CmnIoc
import top.gotoeasy.framework.orm.transaction.config.BookEntity
import top.gotoeasy.framework.orm.transaction.config.BookService
import top.gotoeasy.framework.orm.transaction.config.StudentService


class TransactionManagerTest  extends Specification {

    @Test
    def void "方法事务声明测试"() {
        expect:
        DefaultConfig.getInstance().set("ioc.scan", "top.gotoeasy.framework.orm.transaction");

        def bookEntity = new BookEntity()
        bookEntity.putAt("id", "id1")

        BookService service = CmnIoc.getBean(BookService.class)

        service != null
        service.update(bookEntity) == 1
        service.findOne(bookEntity).name == "name"

        when:
        service.delete(bookEntity)
        then:
        thrown(Exception)

        when:
        service.del(bookEntity)
        then:
        thrown(Exception)

        when:
        service.remove(bookEntity)
        then:
        thrown(Exception)
    }

    @Test
    def void "类事务声明测试"() {
        expect:
        DefaultConfig.getInstance().set("ioc.scan", "top.gotoeasy.framework.orm.transaction");


        StudentService service = CmnIoc.getBean(StudentService.class)

        service.getBook() != null
    }
}
