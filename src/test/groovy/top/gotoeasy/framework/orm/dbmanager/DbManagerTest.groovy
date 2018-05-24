package top.gotoeasy.framework.orm.dbmanager

import static org.junit.Assert.*

import org.junit.Test

import spock.lang.Specification
import top.gotoeasy.framework.core.config.DefaultConfig
import top.gotoeasy.framework.ioc.util.CmnIoc
import top.gotoeasy.framework.orm.DbManager
import top.gotoeasy.framework.orm.dbmanager.config.MyBus
import top.gotoeasy.framework.orm.dbmanager.config.MyCar
import top.gotoeasy.framework.orm.dbmanager.config.MyService
import top.gotoeasy.framework.orm.dbmanager.config.MyTable
import top.gotoeasy.framework.orm.exception.OrmException
import top.gotoeasy.framework.orm.util.CmnOrm


class DbManagerTest  extends Specification {

    def setup() {
        DefaultConfig.getInstance().set("ioc.scan", "top.gotoeasy.framework.orm.dbmanager,top.gotoeasy.framework.orm.transaction.aop");
        DefaultConfig.getInstance().set("ioc.config.file", "h2.xml");
        // DefaultConfig.getInstance().set("log.level.trace", "true");
        DefaultConfig.getInstance().set("ioc.lazyload", "false");

        CmnOrm.initDatabaseTables()
    }

    @Test
    def void "利用h2db测试"() {
        expect:
        MyService myService = CmnIoc.getBean(MyService.class)
        DbManager dbManager =  CmnIoc.getBean(DbManager.class)

        // DB操作测试
        def entity = new MyCar()
        entity.setId("BMW2017")
        entity.setName("宝马2017")
        entity.setColor("红色")
        dbManager.countByCondition("select count(*) from my_car where id=:id", entity) == 0
        dbManager.findById(MyCar.class, null) == null
        dbManager.isExist(entity) == false
        myService.save(entity) == 1
        myService.save(entity) == 1
        def entity2 = new MyCar()
        entity2.setId("BMW2018")
        entity2.setName("宝马2018")
        myService.save(entity2) == 1

        def car = myService.getCar("BMW2018")
        car.getName() == "宝马2018"

        dbManager.countAll(MyCar.class) == 2
        dbManager.count(car) == 1

        car.setName("宝马2018-new")
        myService.update(car)
        def tmp = myService.getCar("BMW2018")
        tmp.getName() == "宝马2018-new"

        tmp.setName(null)
        myService.update(tmp, false) == 0
        myService.update(tmp) == 1
        def tmp2 = myService.getCar("BMW2018")
        tmp2.getName() == null

        myService.delete(tmp)
        dbManager.countAll(MyCar.class) == 1
        myService.update(tmp) == 0

        myService.save(entity2) == 1
        def tmp3 = dbManager.findById(entity2)
        tmp3.getName() == "宝马2018"
        def tmp4 = dbManager.findById(MyCar.class, entity2.getId())
        tmp4.getName() == "宝马2018"

        myService.deleteById(MyCar.class, "BMW2017", "BMW2018") == 2
        dbManager.countAll(MyCar.class) == 0
        dbManager.isExist(entity2) == false

        myService.save(entity) == 1
        myService.save(entity2) == 1
        dbManager.findOne("select * from my_car where id='xxxx'", null) == null
        dbManager.findOne("select * from my_car where id='xxxx'", null, MyCar.class) == null
        List<MyCar> list1 = dbManager.find(entity)
        list1.get(0).getName() == entity.getName()
        List<MyCar> list2 = dbManager.find(entity, "name desc")
        list2.get(0).getName() == entity.getName()
        List<MyCar> list3 = dbManager.find("select * from my_car where id=:id", entity2, MyCar.class)
        list3.get(0).getName() == entity2.getName()
        List<MyCar> list4 = dbManager.findAll(MyCar.class)
        list4.size() == 2
        List<MyCar> list5 = dbManager.findAll(MyCar.class, "id")
        list5.size() == 2
        List<Map<String, Object>> list6 = dbManager.findByCondtion("select * from my_car where id=:id", entity2)
        list6.size() == 1
        MyCar tmp5 = dbManager.findOne("select * from my_car where id=:id", entity2)
        tmp5.getName() == entity2.getName()
        MyCar tmp6 = dbManager.findOne("select * from my_car where id=:id", entity2, MyCar.class)
        tmp6.getName() == tmp5.getName()
        dbManager.countByCondition("select count(*) from my_car where id=:id", tmp6) == 1
        List<MyCar> list7 = dbManager.find(new MyCar())
        list7.size() == 2

        MyBus bus = new MyBus();
        bus.setId(1)
        bus.setBusCd(1001)
        bus.setByDate(new Date())
        myService.save(bus) == 1
        dbManager.isExist(bus) == true


        when:
        dbManager.deleteById(MyBus.class, 1)
        then:
        thrown(OrmException)

        when:
        dbManager.deleteById(MyCar.class)
        then:
        thrown(OrmException)

        when:
        dbManager.countByCondition("select count(*) from my_table where id=:id", tmp6)
        then:
        thrown(OrmException)

        when:
        dbManager.findById(MyTable.class, "xxxxx")
        then:
        thrown(OrmException)

        when:
        dbManager.findByCondtion("select count(*) from my_table where id=:id", entity)
        then:
        thrown(OrmException)

        when:
        dbManager.find("select count(*) from my_table where id=:id", entity, MyTable.class)
        then:
        thrown(OrmException)

        when:
        dbManager.find(entity, "xxxxxx")
        then:
        thrown(OrmException)

        when:
        dbManager.findOne("select count(*) from my_table where id=:id", entity)
        then:
        thrown(OrmException)

        when:
        dbManager.findOne("select count(*) from my_table where id=:id", entity)
        then:
        thrown(OrmException)

    }
}
