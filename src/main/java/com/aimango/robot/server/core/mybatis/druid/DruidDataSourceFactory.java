package com.aimango.robot.server.core.mybatis.druid;



import com.aimango.robot.server.core.component.Nacos;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.LogFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * 整合mybatis 和 druid数据源
 */
public class DruidDataSourceFactory implements DataSourceFactory {
    private static final Logger logger = LoggerFactory.getLogger(DruidDataSourceFactory.class);

    @Override
    public void setProperties(Properties props) {
    }

    @Override
    public DataSource getDataSource() {
        logger.info("druid监控配置");
        DruidDataSource dataSource = new DruidDataSource();
        List<Filter> filters = new ArrayList<>();
        LogFilter logFilter = new Slf4jLogFilter();
        logFilter.setStatementExecutableSqlLogEnable(true);
        StatFilter statFilter = new StatFilter();
        statFilter.setSlowSqlMillis(10000);
        statFilter.setLogSlowSql(true);
        statFilter.setMergeSql(true);
        filters.add(logFilter);
        filters.add(statFilter);
        dataSource.setProxyFilters(filters);
        dataSource.setTimeBetweenLogStatsMillis(300000);
        logger.info("druid基础配置");
        String url = Nacos.getPropertiesField("db.url");
        logger.info("DB连接url:" + url);
        String userName = Nacos.getPropertiesField("db.username");
        String password = Nacos.getPropertiesField("db.password");
        String connectionInitSqls = Nacos.getPropertiesField("db.connection-init-sqls");
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        logger.info("配置连接池大小，超时等待");
        dataSource.setInitialSize(5);
        dataSource.setMinIdle(5);
        dataSource.setMaxActive(50);
        dataSource.setMaxWait(10000);
        /** 配置间隔多久启动一次DestroyThread，对连接池内的连接才进行一次检测，单位是毫秒。
         检测时:
         1.如果连接空闲并且超过minIdle以外的连接，如果空闲时间超过minEvictableIdleTimeMillis设置的值则直接物理关闭。
         2.在minIdle以内的不处理。
         */
        dataSource.setTimeBetweenEvictionRunsMillis(600000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setValidationQuery("select 1 from dual");
        dataSource.setKeepAlive(true);
        dataSource.setRemoveAbandoned(true);
        dataSource.setRemoveAbandonedTimeout(80);
        dataSource.setLogAbandoned(true);


        try {
            StringTokenizer tokenizer = new StringTokenizer(connectionInitSqls, ";");
            dataSource.setConnectionInitSqls(Collections.list(tokenizer));
        } catch (NumberFormatException var5) {
            logger.error("illegal property 'druid.initConnectionSqls'", var5);
        }

        try {
            dataSource.init();
        } catch (SQLException e) {
           logger.error("druid数据源初始化失败",e);
        }
        return dataSource;
    }
}
