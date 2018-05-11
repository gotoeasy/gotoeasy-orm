package top.gotoeasy.framework.orm.strategy

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.orm.exception.OrmException
import top.gotoeasy.framework.orm.strategy.config.MyBook


class OrmNammingStrategyTest  extends Specification {

    @Test
    def void "正常测试"() {
        expect:
        OrmNammingStrategy strategy = new OrmNammingStrategy() {};

        strategy.tableName("MyBook") == "my_book"
        strategy.tableName("top.gotoeasy.framework.orm.strategy.config.MyBook") == "my_book"
        strategy.tableName(MyBook.class) == "my_book"
        strategy.tableName("top.gotoeasy.framework.orm.strategy.config.MyBook2") == "XXX_bbb_1321ffds"

        strategy.columnName("abcdEfg") == "abcd_efg"
        strategy.fieldName("abcd_efg") == "abcdEfg"
        strategy.fieldName("abcd__efg") == "abcdEfg"

        when:
        strategy.tableName(OrmNammingStrategyTest.class)

        then:
        thrown(OrmException)
    }
}
