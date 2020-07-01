package com.aimango.robot.server.utils;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

public class NacosClient {
    private static final Logger logger= LoggerFactory.getLogger(NacosClient.class);

    public NacosClient() throws Exception {
        initConfigProperties();
        initConfigYaml();
    }

    private static NacosClient INSTANCE;
    /**
     * 从nacos读取的配置的原字符串
     */
    private String config;
    private ConfigService configService;
    private NamingService namingService;
    /**
     * 从nacos读取的配置文件
     */
    private Properties configProperties;
    /**
     * 从namcos读取的yaml配置
     */
    private Map configYaml;
    /**
     * nacos元信息配置
     */
    private NacosConfig nacosConfig;

    private void initConfigYaml(){

    }

    private void initConfigProperties() throws Exception {
        if (StringUtils.isEmpty(config)) {
            initConfig();
        }
        Properties res = new Properties();
        BufferedReader reader = new BufferedReader(new StringReader(config));
        res.load(reader);
        configProperties = res;
    }

    private void initNacosConfig() throws Exception {
        Properties properties = new Properties();
        InputStream inputStream = NacosClient.class.getClassLoader().getResourceAsStream("bootstrap.properties");
        try {
            configProperties.load(inputStream);
        } catch (IOException e) {
            logger.error("加载nacos bootstrap.properties文件失败",e);
        }
        NacosConfig nacosConfig = new NacosConfig(properties);
        this.nacosConfig=nacosConfig;
    }


    private void initNamingService(String serverAddr, String namespace) throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        NamingService namingService = NamingFactory.createNamingService(properties);
        this.namingService = namingService;
    }

    private void initConfig() throws Exception {
        if (nacosConfig==null){
            initNacosConfig();
        }
        String serverAddr = nacosConfig.getServerAddr();
        String namespace = nacosConfig.getNamespace();
        String dataId = nacosConfig.getDataId();
        String group = nacosConfig.getGroup();
        long timeoutMs = nacosConfig.getTimeoutMs();
        initConfig(serverAddr,namespace,dataId,group,timeoutMs);
    }

    private void initConfig(String serverAddr,String namespace,String dataId, String group, long timeoutMs) throws NacosException {
        if (configService == null) {
            initConfigService(serverAddr, namespace);
        }
        String config = configService.getConfig(dataId, group, timeoutMs);
        this.config = config;
    }

    private String getPropertiesValue(String key) throws Exception {
        if (configProperties==null){
            initConfigProperties();
        }
        String value = configProperties.getProperty(key);
        return value;
    }

    private void initConfigService(String serverAddr, String namespace) throws NacosException {
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        ConfigService configService = NacosFactory.createConfigService(properties);
        this.configService = configService;

    }

    public static void regist(){

    }

    public static void unregist(){

    }

    public static String propertiesValue(String key) throws Exception {
        if (StringUtils.isEmpty(key)){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (NacosClient.INSTANCE==null){
            NacosClient nacosClient=new NacosClient();
            NacosClient.INSTANCE=nacosClient;
        }
        String propertiesValue = NacosClient.INSTANCE.getPropertiesValue(key);
        return propertiesValue;
    }

    public static String yamlValue(String key) {
        return null;
    }

    class NacosConfig{
        private String serverAddr;
        private String namespace;
        private String dataId;
        private String group;
        private long timeoutMs=3000;

        public NacosConfig(Properties properties) throws Exception {
            String name = properties.getProperty("spring.application.name");
            String after = properties.getProperty("spring.cloud.nacos.config.file-extension");
            String dataId = name + "." + after;
            String group = properties.getProperty("spring.cloud.nacos.config.group");
            String serverAddr = properties.getProperty("spring.cloud.nacos.discovery.server-addr");
            String namespace = properties.getProperty("spring.cloud.nacos.config.namespace");
            if (StringUtils.isEmpty(name)||StringUtils.isEmpty(after)||StringUtils.isEmpty(group)||StringUtils.isEmpty(serverAddr)||StringUtils.isEmpty(namespace)){
                throw new Exception("请检查nacos元信息配置文件");
            }
            this.serverAddr=serverAddr;
            this.namespace=namespace;
            this.dataId=dataId;
            this.group=group;
        }

        public String getServerAddr() {
            return serverAddr;
        }

        public void setServerAddr(String serverAddr) {
            this.serverAddr = serverAddr;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getDataId() {
            return dataId;
        }

        public void setDataId(String dataId) {
            this.dataId = dataId;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }
}
