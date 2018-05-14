package top.gotoeasy.framework.orm.transaction.config;

import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.annotation.Id;

@Entity
public class BookEntity {

    @Id
    public String id;
    public String name;

    @Override
    public String toString() {
        return "BookEntity [id=" + id + ", name=" + name + "]";
    }

}
