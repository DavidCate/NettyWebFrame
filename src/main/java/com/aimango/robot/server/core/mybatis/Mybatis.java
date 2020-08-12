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
    private volatile static ConcurrentHashMap<Object,SqlSession> executorSqlsession=new ConcurrentHashMap<>(256);

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

    public synchronized static <T> T getMapper(Class<T> mapperClass, Object executor){
        SqlSession sqlSession = openExecutorSqlSession(executor);
        T mapper = sqlSession.getMapper(mapperClass);
        return mapper;
    }

    public synchronized static SqlSession openExecutorSqlSession(Object executor){
        boolean containsKey = executorSqlsession.containsKey(executor);
        if (containsKey){
            return executorSqlsession.get(executor);
        }else {
            SqlSession sqlSession = getSqlSession();
            executorSqlsession.put(executor, sqlSession);
            return sqlSession;
        }
    }

    public synchronized static SqlSession commitAndCloseExecutorSqlSession(Object executor){
        SqlSession sqlSession=null;
        try {
            boolean containsKey = executorSqlsession.containsKey(executor);
            if (containsKey){
                sqlSession = executorSqlsession.get(executor);
                sqlSession.commit();
                return sqlSession;
            }else {
                return null;
            }
        }finally {
            if (sqlSession!=null){
                executorSqlsession.remove(executor);
                closeSession(sqlSession);
            }
        }

    }

    public synchronized static  <T> SqlSession callback(Object executor){
        SqlSession sqlSession=null;
        try {
            boolean containsKey = executorSqlsession.containsKey(executor);
            if (containsKey){
                sqlSession = executorSqlsession.get(executor);
                sqlSession.rollback();
                return sqlSession;
            }else {
                return null;
            }
        }finally {
            if (sqlSession!=null){
                executorSqlsession.remove(executor);
                closeSession(sqlSession);
            }
        }
    }


    public static void main(String[] args) {
        SqlSession sqlSession= Mybatis.getSqlSession();
    }

    public synchronized static void closeSession(SqlSession sqlSession) {
        sqlSession.close();
    }
}
