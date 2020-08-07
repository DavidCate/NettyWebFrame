package com.aimango.robot.server.core.mybatis.druid;



import com.aimango.robot.server.core.component.Nacos;
import com.aimango.robot.server.core.component.PropertiesUtils;
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
        String url = PropertiesUtils.getProperty("db.url");
        logger.info("DB连接url:" + url);
        String userName =   PropertiesUtils.getProperty("db.username");
        String password = PropertiesUtils.getProperty("db.password");
        String connectionInitSqls = PropertiesUtils.getProperty("db.connection-init-sqls");
        dataSource.setUrl(url);
        dataSource.setUsername(userName);
        dataSource.setPassword(password);
        logger.info("配置连接池大小，超时等待");
        Integer initialSize =Integer.parseInt(PropertiesUtils.getProperty("db.initialSize"));
        dataSource.setInitialSize(initialSize);
        Integer minIdle =Integer.parseInt(PropertiesUtils.getProperty("db.minIdle"));
        dataSource.setMinIdle(minIdle);
        Integer maxActive =Integer.parseInt(PropertiesUtils.getProperty("db.maxActive"));
        dataSource.setMaxActive(maxActive);
        Integer maxWait =Integer.parseInt(PropertiesUtils.getProperty("db.maxWait"));
        dataSource.setMaxWait(maxWait);
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
