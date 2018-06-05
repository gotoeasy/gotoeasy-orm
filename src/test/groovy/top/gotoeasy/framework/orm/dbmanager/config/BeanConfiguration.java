package top.gotoeasy.framework.orm.dbmanager.config;

import top.gotoeasy.framework.core.util.CmnSpi;
import top.gotoeasy.framework.ioc.annotation.Bean;
import top.gotoeasy.framework.ioc.annotation.BeanConfig;
import top.gotoeasy.framework.orm.strategy.OrmNamingStrategy;
import top.gotoeasy.framework.orm.support.SqlDialect;

@BeanConfig
public class BeanConfiguration {

    @Bean
    public OrmNamingStrategy ormNamingStrategy() {
        return CmnSpi.loadSpiInstance(OrmNamingStrategy.class);
    }

    @Bean
    public SqlDialect sqlDialect() {
        return CmnSpi.loadSpiInstance(SqlDialect.class);
    }
}
