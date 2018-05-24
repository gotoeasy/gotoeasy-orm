package top.gotoeasy.framework.orm.dbmanager

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.core.config.DefaultConfig
import top.gotoeasy.framework.ioc.util.CmnIoc
import top.gotoeasy.framework.orm.dbmanager.config.Car
import top.gotoeasy.framework.orm.dbmanager.config.CarService
import top.gotoeasy.framework.orm.util.CmnOrm


class DbManagerTest  extends Specification {

    @Test
    def void "h2测试环境检查"() {
        expect:
        DefaultConfig.getInstance().set("ioc.scan", "top.gotoeasy.framework.orm.dbmanager,top.gotoeasy.framework.orm.transaction.aop");
        DefaultConfig.getInstance().set("ioc.config.file", "h2.xml");
        // DefaultConfig.getInstance().set("log.level.trace", "true");
        DefaultConfig.getInstance().remove("ioc.lazyload"); // 默认懒加载

        CmnOrm.initDatabaseTables()

        CarService carService = CmnIoc.getBean(CarService.class)

        Car entity = new Car()
        entity.setId("BMW2018")
        entity.setName("宝马2018")
        carService.save(entity) == 1
        def car = carService.getCar("BMW2018")
        car.getName() == "宝马2018"
    }
}
