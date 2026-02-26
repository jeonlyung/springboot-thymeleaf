package com.project.springboot_thymeleaf.global.batch;

import jakarta.annotation.Resource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BatchExecutor {

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    public <M, T> void executeBatch(Class<M> mapperClass, List<T> dsList){
        int batchSize = 1000;
        int count = 0;
        try(SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false)){
            M mapper = sqlSession.getMapper(mapperClass);

            for (T data : dsList){
                if (++count % batchSize == 0) {
                    sqlSession.flushStatements(); // 중간 커밋 효과 (메모리 관리)
                    sqlSession.clearCache();
                }
            }

            sqlSession.flushStatements();
            sqlSession.clearCache();
            sqlSession.commit();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
