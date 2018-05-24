package top.gotoeasy.framework.orm.dbmanager

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.core.config.DefaultConfig
import top.gotoeasy.framework.ioc.util.CmnIoc
import top.gotoeasy.framework.orm.DbManager
import top.gotoeasy.framework.orm.dbmanager.config.CarService
import top.gotoeasy.framework.orm.dbmanager.config.MyCar
import top.gotoeasy.framework.orm.util.CmnOrm


class DbManagerTest  extends Specification {

    @Test
    def void "利用h2db测试"() {
        expect:
        DefaultConfig.getInstance().set("ioc.scan", "top.gotoeasy.framework.orm.dbmanager,top.gotoeasy.framework.orm.transaction.aop");
        DefaultConfig.getInstance().set("ioc.config.file", "h2.xml");
        // DefaultConfig.getInstance().set("log.level.trace", "true");
        DefaultConfig.getInstance().remove("ioc.lazyload"); // 默认懒加载

        CmnOrm.initDatabaseTables()

        CarService carService = CmnIoc.getBean(CarService.class)
        DbManager dbManager =  CmnIoc.getBean(DbManager.class)

        // DB操作测试
        def entity = new MyCar()
        entity.setId("BMW2017")
        entity.setName("宝马2017")
        carService.save(entity) == 1
        def entity2 = new MyCar()
        entity2.setId("BMW2018")
        entity2.setName("宝马2018")
        carService.save(entity2) == 1

        def car = carService.getCar("BMW2018")
        car.getName() == "宝马2018"

        dbManager.countAll(MyCar.class) == 2
        dbManager.count(car) == 1

        car.setName("宝马2018-new")
        carService.update(car)
        def tmp = carService.getCar("BMW2018")
        tmp.getName() == "宝马2018-new"

        tmp.setName(null)
        carService.update(tmp, false) == 0
        carService.update(tmp) == 1
        def tmp2 = carService.getCar("BMW2018")
        tmp2.getName() == null

        carService.delete(tmp)
        dbManager.countAll(MyCar.class) == 1
        carService.update(tmp) == 0

        carService.save(entity2) == 1
        def tmp3 = dbManager.findById(entity2)
        tmp3.getName() == "宝马2018"
        def tmp4 = dbManager.findById(MyCar.class, entity2.getId())
        tmp4.getName() == "宝马2018"

        carService.deleteById(MyCar.class, "BMW2017", "BMW2018") == 2
        dbManager.countAll(MyCar.class) == 0
        dbManager.isExist(entity2) == false

        carService.save(entity) == 1
        carService.save(entity2) == 1
        List<MyCar> list1 = dbManager.find(entity, "name desc")
        list1.get(0).getName() == entity.getName()
        List<MyCar> list2 = dbManager.find("select * from my_car where id=:id", entity2, MyCar.class)
        list2.get(0).getName() == entity2.getName()
        List<MyCar> list = dbManager.findAll(MyCar.class)
        list.size() == 2
        List<Map<String, Object>> list3 = dbManager.findByCondtion("select * from my_car where id=:id", entity2)
        list3.size() == 1
        MyCar tmp5 = dbManager.findOne("select * from my_car where id=:id", entity2)
        tmp5.getName() == entity2.getName()
        MyCar tmp6 = dbManager.findOne("select * from my_car where id=:id", entity2, MyCar.class)
        tmp6.getName() == tmp5.getName()
        dbManager.countByCondition("select count(*) from my_car where id=:id", tmp6) == 1
    }
}
