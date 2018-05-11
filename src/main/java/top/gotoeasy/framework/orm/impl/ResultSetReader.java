package top.gotoeasy.framework.orm.impl;

import java.util.Map;

/**
 * 结果集阅读器
 * 
 * @since 2018/05
 * @author 青松
 */
public interface ResultSetReader {

    /**
     * 行读取
     * 
     * @param mapRow 行Map
     */
    public void readRow(Map<String, Object> mapRow);
}
