package top.gotoeasy.framework.orm.dbmanager.config;

import java.math.BigDecimal;

import top.gotoeasy.framework.orm.annotation.Column;
import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.annotation.Id;

@Entity
public class MyTestTable {

    @Id
    private int        id;
    @Column("character varying(20)")
    private String     name;
    private BigDecimal price;

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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

}
