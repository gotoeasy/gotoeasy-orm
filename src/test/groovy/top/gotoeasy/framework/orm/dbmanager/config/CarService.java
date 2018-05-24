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
    public int save(MyCar myCar) {
        return dbManager.insertOrUpdate(myCar, true);
    }

    @Transaction
    public int update(MyCar myCar) {
        return dbManager.update(myCar);
    }

    @Transaction
    public int update(MyCar myCar, boolean inclNull) {
        return dbManager.update(myCar, inclNull);
    }

    @Transaction
    public int delete(MyCar myCar) {
        return dbManager.delete(myCar);
    }

    @Transaction
    public int deleteById(Class<?> entityClass, Object ... ids) {
        return dbManager.deleteById(entityClass, ids);
    }

    public MyCar getCar(String id) {
        return dbManager.findById(MyCar.class, id);
    }

}
