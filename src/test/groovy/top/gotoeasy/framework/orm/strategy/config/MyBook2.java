package top.gotoeasy.framework.orm.strategy.config;

import top.gotoeasy.framework.orm.annotation.Entity;
import top.gotoeasy.framework.orm.annotation.Id;

@Entity("XXX_bbb_1321ffds")
public class MyBook2 {

    @Id
    private String id;

    private String bookName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

}
