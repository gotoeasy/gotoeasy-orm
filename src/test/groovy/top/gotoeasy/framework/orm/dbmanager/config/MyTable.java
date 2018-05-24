package top.gotoeasy.framework.orm.dbmanager.config;

import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.annotation.Id;

@Entity
public class MyTable {

    @Id
    private int    id;
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
