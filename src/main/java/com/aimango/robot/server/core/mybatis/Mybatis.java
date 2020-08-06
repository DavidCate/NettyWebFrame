package com.aimango.robot.server.core.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class Mybatis {
    private static final Logger logger= LoggerFactory.getLogger(Mybatis.class);
    private final static String RESOURCE= "mybatis/mybatis-config.xml";
    private volatile static SqlSessionFactory sqlSessionFactory;
    private Mybatis(){}

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
        return sqlSessionFactory.openSession();
    }

    public static SqlSession getTransactionalSqlSession(){
        if (sqlSessionFactory==null){
            init();
        }
        return sqlSessionFactory.openSession(false);
    }

    public static <T> T getMapper(Class<T> mapperClass){
        SqlSession sqlSession=getSqlSession();
        T mapper = sqlSession.getMapper(mapperClass);
        return mapper;
    }


    public static void main(String[] args) {
        SqlSession sqlSession= Mybatis.getSqlSession();
    }
}
