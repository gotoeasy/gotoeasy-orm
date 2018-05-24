package top.gotoeasy.framework.orm.support;

import java.lang.reflect.Field;

public interface SqlDialect {

    public String getCreateTableDdl(Class<?> claz);

    public String getColumnDefine(Field field);

}
