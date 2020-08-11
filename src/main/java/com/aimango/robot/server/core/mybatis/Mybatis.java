package com.aimango.robot.server.core.mybatis;

import com.aimango.robot.server.core.component.PropertiesUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class Mybatis {
    private static final Logger logger= LoggerFactory.getLogger(Mybatis.class);
    private final static String RESOURCE= PropertiesUtils.getProperty("mybatis.config.location");
    private volatile static SqlSessionFactory sqlSessionFactory;
    private Mybatis(){}
    private volatile static ConcurrentHashMap<Object,SqlSession> mapperSqlsession=new ConcurrentHashMap<>(256);

    private static void init(){
        if (sqlSessionFactory==null){
            synchronized (Mybatis.class){
                if (sqlSessionFactory==null){
                    InputStream inputStream= null;
                    try {
                        inputStream = Resources.getResourceAsStream(RESOURCE);
                    } catch (IOException e) {
                       logger.error("context",e);
                    }
                    sqlSessionFactory=new SqlSessionFactoryBuilder().build(inputStream);
                }
            }
        }
    }

    public static SqlSessionFactory getSqlSessionFactory(){
        if (sqlSessionFactory==null){
            init();
        }
        return sqlSessionFactory;
    }

    public static SqlSession getSqlSession(){
        if (sqlSessionFactory==null){
            init();
        }
        return sqlSessionFactory.openSession(false);
    }

    public static SqlSession getTransactionalSqlSession(){
        if (sqlSessionFactory==null){
            init();
        }
        return sqlSessionFactory.openSession(false);
    }

    public synchronized static <T> T getMapper(Class<T> mapperClass){
        SqlSession sqlSession=getSqlSession();
        T mapper = sqlSession.getMapper(mapperClass);
        mapperSqlsession.put(mapper,sqlSession);
        return mapper;
    }

    public synchronized static SqlSession getSqlSession(Object mapper){
        SqlSession sqlSession = mapperSqlsession.get(mapper);
        return sqlSession;
    }

    public synchronized static SqlSession remove(Object mapper){
        SqlSession remove = mapperSqlsession.remove(mapper);
        return remove;
    }


    public static void main(String[] args) {
        SqlSession sqlSession= Mybatis.getSqlSession();
    }
}
