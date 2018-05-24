package top.gotoeasy.framework.orm.dbmanager.config;

import top.gotoeasy.framework.ioc.annotation.Autowired;
import top.gotoeasy.framework.ioc.annotation.Component;
import top.gotoeasy.framework.orm.DbManager;
import top.gotoeasy.framework.orm.annotation.Transaction;

@Component
public class CarService {

    @Autowired
    private DbManager dbManager;

    @Transaction
    public int save(Car car) {
        return dbManager.insert(car);
    }

    public Car getCar(String id) {
        return dbManager.findById(Car.class, id);
    }

}
