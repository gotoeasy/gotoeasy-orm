package top.gotoeasy.framework.orm.dbmanager.config;

import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.annotation.Id;

@Entity
public class MyCar {

    @Id
    private String id;
    private String name;
    private String color;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
