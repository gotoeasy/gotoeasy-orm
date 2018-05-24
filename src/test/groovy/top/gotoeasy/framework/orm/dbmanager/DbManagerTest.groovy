package top.gotoeasy.framework.orm.dbmanager

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.core.config.DefaultConfig
import top.gotoeasy.framework.ioc.util.CmnIoc
import top.gotoeasy.framework.orm.DbManager
import top.gotoeasy.framework.orm.dbmanager.config.Car


class DbManagerTest  extends Specification {

    @Test
    def void "h2测试环境检查"() {
        expect:
        DefaultConfig.getInstance().set("ioc.scan", "top.gotoeasy.framework.orm.dbmanager,top.gotoeasy.framework.orm.transaction.aop");
        DefaultConfig.getInstance().set("ioc.config.file", "h2.xml");
        // DefaultConfig.getInstance().set("log.level.trace", "true");
        DefaultConfig.getInstance().remove("ioc.lazyload"); // 默认懒加载


        //     Ioc ioc = new DefaultIoc()
        DbManager dbManager = CmnIoc.getBean(DbManager.class)
        dbManager.findById(Car.class, "xxx")
    }
}
