package com.aimango.robot.server.core.handler;

import com.aimango.robot.server.core.exception.NoneException;
import com.aimango.robot.server.core.exception.OutboundTargetValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class HeartbeatAttrbute {
    private static final Logger logger= LoggerFactory.getLogger(HeartbeatAttrbute.class);
    private static volatile ConcurrentHashMap<String,Integer> heartbeatAttrBute;
    private static volatile Object lock=new Object();
    private static void init(){
        if (heartbeatAttrBute==null){
            synchronized (HeartbeatAttrbute.class){
                if (heartbeatAttrBute==null){
                    heartbeatAttrBute=new ConcurrentHashMap<>(1024);
                }
            }
        }
    }

    public static void put(String key,int value){
        if (heartbeatAttrBute==null){
            init();
        }
            heartbeatAttrBute.put(key,value);
    }

    public static boolean exists(String key){
        if (heartbeatAttrBute==null){
            return false;
        }else {
            boolean b = heartbeatAttrBute.containsKey(key);
            return b;
        }
    }

    public static int get(String key) throws NoneException {
        if (heartbeatAttrBute==null){
            throw new NoneException("未找到key");
        }else {
            Integer integer = heartbeatAttrBute.get(key);
            if (integer!=null){
                return integer;
            }else {
                throw new NoneException("未找到key");
            }
        }
    }

    /**
     * 获取key值 先减一再返回
     * @param key
     */
    public static int decrementAndGet(String key) throws NoneException {
        synchronized (lock){
            int i = get(key);
            logger.debug(String.format("获取key:[%s]的值:[%s]",key,i));
            i--;
            HeartbeatAttrbute.put(key,i);
            return i;
        }
    }

    public static void remove(String key) {
        if (heartbeatAttrBute!=null){
            heartbeatAttrBute.remove(key);
        }
    }

    /**
     * 获取key的值判断值是否大于i
     * 如果大于i
     * 如果不大于i 就把i值加1
     * 如果大于i 就抛出异常
     * @param key
     * @param i
     */
    public static void validateAndAdd(String key, int i) throws NoneException, OutboundTargetValueException {
        synchronized (lock){
            int value = get(key);
            logger.debug(String.format("获取key:[%s]的值:[%s]",key,value));
            if (value<i){
                value++;
                put(key,value);
            }else {
                throw new OutboundTargetValueException(String.format("key:[%s]的值:[%s]不小于目标值:[%s]",key,value,i));
            }
        }
    }
}
