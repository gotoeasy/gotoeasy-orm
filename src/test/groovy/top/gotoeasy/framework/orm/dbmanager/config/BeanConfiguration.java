package top.gotoeasy.framework.orm.dbmanager.config;

import top.gotoeasy.framework.ioc.annotation.Bean;
import top.gotoeasy.framework.ioc.annotation.BeanConfig;
import top.gotoeasy.framework.orm.strategy.OrmNamingStrategy;

@BeanConfig
public class BeanConfiguration {

    @Bean
    public OrmNamingStrategy ormNamingStrategy() {
        return new OrmNamingStrategy() {};
    }
}
