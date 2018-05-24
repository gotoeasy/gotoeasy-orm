package top.gotoeasy.framework.orm.dbmanager.config;

import java.util.Date;

import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.annotation.Id;

@Entity
public class MyBus {

    @Id
    private int  id;
    @Id
    private int  busCd;
    private Date byDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBusCd() {
        return busCd;
    }

    public void setBusCd(int busCd) {
        this.busCd = busCd;
    }

    public Date getByDate() {
        return byDate;
    }

    public void setByDate(Date byDate) {
        this.byDate = byDate;
    }

}
