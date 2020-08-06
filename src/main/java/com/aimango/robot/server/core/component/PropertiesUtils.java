package com.aimango.robot.server.core.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Properties;


public class PropertiesUtils {
    private static final Logger logger= LoggerFactory.getLogger(PropertiesUtils.class);
    private volatile static Properties properties;

    public PropertiesUtils() {}

    static {
        String configPath = "config.properties";
        if (properties == null){
            synchronized (PropertiesUtils.class){
                if (properties == null){
                    properties = new Properties();
                    try {
                        InputStreamReader isr = new InputStreamReader(PropertiesUtils.class.getClassLoader().getResourceAsStream(configPath),"utf-8");
                        properties.load(isr);
                    } catch (NullPointerException e) {
                        logger.warn("默认配置文件config.properties不存在.");
                    } catch (UnsupportedEncodingException e) {
                        logger.error(MessageFormat.format("config.properties file load fail, err: {0}", e));
                       logger.error("context",e);
                    } catch (IOException e) {
                        logger.error(MessageFormat.format("config.properties file load fail, err: {0}", e));
                       logger.error("context",e);
                    }
                }
            }
        }
    }

    /**
     * 单独加载某个文件
     * @param filePath
     * @return
     */
    public static Properties getPropertiesByFile(String filePath){
        Properties configProperties = new Properties();
        InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(filePath);
        try {
            configProperties.load(inputStream);
        } catch (IOException e) {
           logger.error("context",e);
        }
        return configProperties;
    }

    public static String getProperty(String key){
        return properties.getProperty(key);
    }

    public static Properties getProperties(String pattern){
        Properties prop = new Properties();
        Enumeration<?> keys = properties.propertyNames();
        while (keys.hasMoreElements()){
            String k = (String) keys.nextElement();
            if (k.startsWith(pattern)){
                prop.setProperty(k.replace(pattern, ""), getProperty(k));
            }
        }
        return prop;
    }
}
