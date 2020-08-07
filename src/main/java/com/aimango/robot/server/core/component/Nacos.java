package com.aimango.robot.server.core.component;

import com.aimango.robot.server.core.constant.ServerConfig;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class Nacos {
    private static final Logger logger = LoggerFactory.getLogger(Nacos.class);
    private volatile static Properties properties;
    private volatile static Properties nacosProperties;

    public static Map<String, String> getPropertiesMap() {
        if (propertiesMap == null) {
            synchronized (Nacos.class) {
                if (propertiesMap == null) {
                    initYamlProperteis();
                }
            }
        }
        return propertiesMap;
    }

    private static Map<String, String> propertiesMap;

    /**
     * 配置管理
     */
    public static class ConfigManager {
        public static String getConfig(String dataId, String group, long timeoutMs, String serverAddr, String namespace) {
            String res = null;
            try {
                Properties properties = new Properties();
                properties.put("serverAddr", serverAddr);
                properties.put("namespace", namespace);
                ConfigService configService = NacosFactory.createConfigService(properties);
                String content = configService.getConfig(dataId, group, timeoutMs);
                res = content;
            } catch (NacosException e) {
                logger.error("NACOS异常", e);
            }
            return res;
        }

        public void addListener(String dataId, String group, Listener listener) {
            try {
                String serverAddr = "{serverAddr}";
                Properties properties = new Properties();
                properties.put("serverAddr", serverAddr);
                ConfigService configService = NacosFactory.createConfigService(properties);
                String content = configService.getConfig(dataId, group, 5000);
                System.out.println(content);
                configService.addListener(dataId, group, new Listener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        System.out.println("recieve1:" + configInfo);
                    }

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                });
            } catch (NacosException e) {
                logger.error("context", e);
            }

        }

        public void removeListener(String dataId, String group, Listener listener) {
            try {
                String serverAddr = "{serverAddr}";
                Properties properties = new Properties();
                properties.put("serverAddr", serverAddr);
                ConfigService configService = NacosFactory.createConfigService(properties);
                configService.removeListener(dataId, group, listener);
            } catch (NacosException e) {
                logger.error("context", e);
            }
        }

        public void publishConfig(String dataId, String group, String content) {
            try {
                // 初始化配置服务，控制台通过示例代码自动获取下面参数
                String serverAddr = "{serverAddr}";
                Properties properties = new Properties();
                properties.put("serverAddr", serverAddr);
                ConfigService configService = NacosFactory.createConfigService(properties);
                boolean isPublishOk = configService.publishConfig(dataId, group, content);
                System.out.println(isPublishOk);
            } catch (NacosException e) {
                // TODO Auto-generated catch block
                logger.error("context", e);
            }
        }

        public void removeConfig(String dataId, String group) {
            try {
                // 初始化配置服务，控制台通过示例代码自动获取下面参数
                String serverAddr = "{serverAddr}";
                Properties properties = new Properties();
                properties.put("serverAddr", serverAddr);

                ConfigService configService = NacosFactory.createConfigService(properties);
                boolean isRemoveOk = configService.removeConfig(dataId, group);
                System.out.println(isRemoveOk);
            } catch (NacosException e) {
                // TODO Auto-generated catch block
                logger.error("context", e);
            }
        }
    }

    /**
     * 服务发现SDK
     */
    public static class ServiceDiscovery {
        public static void registerInstance(String serviceName, String ip, int port, String clusterName, String serverAddr, String namespace) throws NacosException {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            NamingService naming = NamingFactory.createNamingService(properties);
            naming.registerInstance(serviceName, ip, port, clusterName);
        }

        public static void deregisterInstance(String serviceName, String ip, int port, String serverAddr) throws NacosException {
            NamingService naming = NamingFactory.createNamingService(serverAddr);
            naming.deregisterInstance(serviceName, ip, port, "DEFAULT");
        }

        public static List<Instance> getAllInstances(String serviceName) {
            List<Instance> allInstances = null;
            try {
                if (nacosProperties == null) {
                    initNacosProperties();
                }
                String name = nacosProperties.getProperty("spring.application.name");
                logger.info("application name : " + name);
                String after = nacosProperties.getProperty("spring.cloud.nacos.config.file-extension");
                String dataId = name + "." + after;
                String group = nacosProperties.getProperty("spring.cloud.nacos.config.group");
                logger.info("application group : " + group);
                String serverAddr = nacosProperties.getProperty("spring.cloud.nacos.discovery.server-addr");
                logger.info("serverAddr:" + serverAddr);
                String namespace = nacosProperties.getProperty("spring.cloud.nacos.config.namespace");
                logger.info("namespace:" + namespace);

                Properties properties = new Properties();
                properties.put(PropertyKeyConst.NAMESPACE, namespace);
                properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);

                NamingService namingService = NamingFactory.createNamingService(properties);
                allInstances = namingService.getAllInstances(serviceName);
            } catch (NacosException e) {
                logger.error("Nacos getAllInstances异常", e);
            }
            return allInstances;
        }

        public static List<Instance> selectInstances(String serviceName, boolean healthy, String serverAddr) {
            List<Instance> instances = null;
            try {
                NamingService naming = NamingFactory.createNamingService(serverAddr);
                instances = naming.selectInstances(serviceName, healthy);
            } catch (NacosException e) {
                logger.error("context", e);
            }
            return instances;
        }

        public static Instance getOneHealthyInstance(String serviceName) throws NacosException {
            if (nacosProperties == null) {
                initNacosProperties();
            }
            String name = nacosProperties.getProperty("spring.application.name");
            logger.info("application name : " + name);
            String after = nacosProperties.getProperty("spring.cloud.nacos.config.file-extension");
            String dataId = name + "." + after;
            String group = nacosProperties.getProperty("spring.cloud.nacos.config.group");
            logger.info("application group : " + group);
            String serverAddr = nacosProperties.getProperty("spring.cloud.nacos.discovery.server-addr");
            logger.info("serverAddr:" + serverAddr);
            String namespace = nacosProperties.getProperty("spring.cloud.nacos.config.namespace");
            logger.info("namespace:" + namespace);

            Properties properties = new Properties();
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);

            NamingService namingService = NamingFactory.createNamingService(properties);
            Instance instance = namingService.selectOneHealthyInstance(serviceName);
            return instance;
        }
    }

    private static void initNacosProperties() {
        if (nacosProperties == null) {
            synchronized (Nacos.class) {
                if (nacosProperties == null) {
                    Properties configProperties = new Properties();
                    InputStream inputStream = Nacos.class.getClassLoader().getResourceAsStream("bootstrap.properties");
                    logger.info("读取bootstrap.properties");
                    try {
                        configProperties.load(inputStream);
                    } catch (IOException e) {
                        logger.error("context", e);
                    }
                    nacosProperties = configProperties;
                }
            }
        }
    }

    private static void setDefaultProperties() throws IOException {
        Properties configProperties = new Properties();
        InputStream inputStream = Nacos.class.getClassLoader().getResourceAsStream("bootstrap.properties");
        logger.info("读取bootstrap.properties");
        configProperties.load(inputStream);


        String name = configProperties.getProperty(ServerConfig.SERVER_NAME);
        logger.info("application name : " + name);
        String after = configProperties.getProperty(ServerConfig.Nacos.CONFIG.FILE_EXTENSION);
        String dataId = name + "." + after;
        String group = configProperties.getProperty(ServerConfig.Nacos.CONFIG.GROUP);
        logger.info("application group : " + group);
        String serverAddr = configProperties.getProperty(ServerConfig.Nacos.SERVER_ADDR);
        logger.info("serverAddr:" + serverAddr);
        String namespace = configProperties.getProperty(ServerConfig.Nacos.NAMESPACE);
        logger.info("namespace:" + namespace);
        String config = ConfigManager.getConfig(dataId, group, 3000, serverAddr, namespace);
        Properties res = new Properties();
        BufferedReader reader = new BufferedReader(new StringReader(config));
        try {
            res.load(reader);
        } catch (IOException e) {
            logger.error("context", e);
        }
        properties = res;
    }

    private static void setDefaultYamlProperties() {
        Properties configProperties = new Properties();
        InputStream inputStream = Nacos.class.getClassLoader().getResourceAsStream("bootstrap.properties");
        logger.info("读取bootstrap.properties");
        try {
            configProperties.load(inputStream);
        } catch (IOException e) {
            logger.error("context", e);
        }
        String name = configProperties.getProperty("spring.application.name");
        logger.info("application name : " + name);
        String after = configProperties.getProperty("spring.cloud.nacos.config.file-extension");
        String dataId = name + "." + after;
        String group = configProperties.getProperty("spring.cloud.nacos.config.group");
        logger.info("application group : " + group);
        String serverAddr = configProperties.getProperty("spring.cloud.nacos.discovery.server-addr");
        logger.info("serverAddr:" + serverAddr);
        String namespace = configProperties.getProperty("spring.cloud.nacos.config.namespace");
        logger.info("namespace:" + namespace);
        String config = ConfigManager.getConfig(dataId, group, 3000, serverAddr, namespace);
        Yaml yaml = new Yaml();
        Map map = yaml.load(config);
        propertiesMap = map;
    }

    private static void initProperteis() throws IOException {
        if (properties == null) {
            synchronized (Nacos.class) {
                if (properties == null) {
                    logger.info("初始化nacos默认配置");
                    Nacos.setDefaultProperties();
                    logger.info("初始化nacos默认配置完成");
                }
            }
        }
    }

    private static void initYamlProperteis() {
        if (properties == null) {
            synchronized (Nacos.class) {
                if (properties == null) {
                    logger.info("初始化nacos默认配置");
                    Nacos.setDefaultYamlProperties();
                    logger.info("初始化nacos默认配置完成");
                }
            }
        }
    }

    public static String getPropertiesField(String key) throws IOException {
        if (properties == null) {
            initProperteis();
        }
        String res = properties.getProperty(key);
        if (res == null) {
            logger.warn("读取属性：" + key + "的值为null,请检查配置文件");
            return null;
        } else {
            return res;
        }
    }

    public static void defaultServiceRegiste(String serverName) throws IOException, NacosException {
        logger.info("开始服务注册！！！！");
        Properties configProperties = new Properties();
        InputStream inputStream = Nacos.class.getClassLoader().getResourceAsStream("bootstrap.properties");
        logger.info("读取bootstrap.properties");
        configProperties.load(inputStream);
        String serverAddr = configProperties.getProperty(ServerConfig.Nacos.SERVER_ADDR);
        logger.info("serverAddr:" + serverAddr);
        String namespace = configProperties.getProperty(ServerConfig.Nacos.NAMESPACE);
        logger.info("namespace:" + namespace);
        String netCard = Nacos.getPropertiesField(ServerConfig.NETWORK_CARD);
        String ip = getLocalIp(netCard);
        ServiceDiscovery.registerInstance(serverName, ip, Integer.parseInt(Nacos.getPropertiesField(ServerConfig.SERVER_PORT)), "NettyCluster", serverAddr, namespace);
        logger.info("NettyServer服务注册成功");
    }

    public static void defaultServiceUnRegiste(String serverName) throws IOException, NacosException {
        Properties configProperties = new Properties();
        InputStream inputStream = Nacos.class.getClassLoader().getResourceAsStream("bootstrap.properties");
        logger.info("读取bootstrap.properties");
        configProperties.load(inputStream);
        String serverAddr = configProperties.getProperty("spring.cloud.nacos.discovery.server-addr");
        String netCard = Nacos.getPropertiesField("net.card");
        String ip = getLocalIp(netCard);
        ServiceDiscovery.deregisterInstance(serverName, ip, Integer.parseInt(Nacos.getPropertiesField("websocket.port")), serverAddr);
        logger.info("NettyServer服务移除成功");
    }

    public static void main(String[] args) {
        Properties properties = PropertiesUtils.getPropertiesByFile("bootstrap.properties");
        String name = properties.getProperty("spring.application.name");
        String after = properties.getProperty("spring.cloud.nacos.config.file-extension");
        String dataId = name + "." + after;
        String group = properties.getProperty("spring.cloud.nacos.config.group");
        String serverAddr = properties.getProperty("spring.cloud.nacos.discovery.server-addr");
        String namespace = properties.getProperty("spring.cloud.nacos.config.namespace");
        String config = ConfigManager.getConfig(dataId, group, 3000, serverAddr, namespace);
        Yaml yaml = new Yaml();
        Map<String, String> map = yaml.load(config);
        logger.info(map.get("redis.host"));
    }

    public static String getLocalIp(String netCard) {
        try {
            InetAddress inetAddress = null;

            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            //如果枚举对象里面还有内容(NetworkInterface)
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                if (netCard.equals(ni.getName())) {
                    Enumeration<InetAddress> ips = ni.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        inetAddress = ips.nextElement();
                        boolean siteLocalAddress = inetAddress.isSiteLocalAddress();
                        boolean loopbackAddress = inetAddress.isLoopbackAddress();
                        if (siteLocalAddress && (!loopbackAddress)) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
            return null;
        } catch (SocketException e) {
            logger.error("获取ip异常", e);
            return null;
        }
    }
}
