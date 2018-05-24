package top.gotoeasy.framework.orm.dbmanager.config;

import top.gotoeasy.framework.ioc.annotation.Bean;
import top.gotoeasy.framework.ioc.annotation.BeanConfig;
import top.gotoeasy.framework.orm.strategy.OrmNamingStrategy;
import top.gotoeasy.framework.orm.support.SqlDialect;
import top.gotoeasy.framework.orm.support.impl.DefaultSqlDialect;

@BeanConfig
public class BeanConfiguration {

    @Bean
    public OrmNamingStrategy ormNamingStrategy() {
        return new OrmNamingStrategy() {};
    }

    @Bean
    public SqlDialect sqlDialect() {
        return new DefaultSqlDialect();
    }
}
