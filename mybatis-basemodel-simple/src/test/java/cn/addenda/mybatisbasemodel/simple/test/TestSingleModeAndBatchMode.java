package cn.addenda.mybatisbasemodel.simple.test;

import cn.addenda.component.mybatis.helper.BatchDmlHelper;
import cn.addenda.mybatisbasemodel.simple.SimpleBaseModel;
import cn.addenda.mybatisbasemodel.simple.User;
import cn.addenda.mybatisbasemodel.simple.UserMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

class TestSingleModeAndBatchMode {

  static SqlSessionFactory sqlSessionFactory;
  static BatchDmlHelper batchDmlHelper;

  static {
    String resource = "META-INF/mybatis-config-simple.xml";
    Reader reader = null;
    try {
      reader = Resources.getResourceAsReader(resource);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    batchDmlHelper = new BatchDmlHelper(sqlSessionFactory);
    batchDmlHelper.setAutoCommit(true);
    createTable();
  }

  static void createTable() {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      UserMapper mapper = sqlSession.getMapper(UserMapper.class);
      mapper.dropTable();
      mapper.createTable();
      sqlSession.commit();
    }
  }

  @Test
  void test() {
    AtomicReference<Long> id = new AtomicReference<>();

    SimpleBaseModel.runWithUser("zhangsan", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User param = new User();
        param.setNickname("a");
        param.setAge(2);
        param.setBirthday(LocalDateTime.now());
        mapper.insert(param);
        sqlSession.commit();

        id.set(param.getId());
        User user = mapper.queryById(id.get());
        Assertions.assertEquals("zhangsan", user.getCreator());
        Assertions.assertEquals("zhangsan", user.getCreatorName());
        Assertions.assertEquals("zhangsan", user.getModifier());
        Assertions.assertEquals("zhangsan", user.getModifierName());
        Assertions.assertNotNull(user.getCreateTime());
        Assertions.assertNotNull(user.getModifyTime());
      }
    });


    AtomicReference<Long> id1 = new AtomicReference<>();
    AtomicReference<Long> id2 = new AtomicReference<>();

    SimpleBaseModel.runWithUser("zhangsan", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

        User param1 = new User();
        param1.setNickname("a");
        param1.setAge(2);
        param1.setBirthday(LocalDateTime.now());

        User param2 = new User();
        param2.setNickname("a");
        param2.setAge(2);
        param2.setBirthday(LocalDateTime.now());
        batchDmlHelper.batch(UserMapper.class, Arrays.asList(param1, param2), (userMapper, user) -> {
          userMapper.insert(user);
        });

        sqlSession.commit();
        id1.set(param1.getId());
        id2.set(param2.getId());

      }
    });

    SimpleBaseModel.runWithUser("zhangsan", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        User user = mapper.queryById(id.get());
        Assertions.assertEquals("zhangsan", user.getCreator());
        Assertions.assertEquals("zhangsan", user.getCreatorName());
        Assertions.assertEquals("zhangsan", user.getModifier());
        Assertions.assertEquals("zhangsan", user.getModifierName());
        Assertions.assertNotNull(user.getCreateTime());
        Assertions.assertNotNull(user.getModifyTime());

        User user1 = mapper.queryById(id1.get());
        Assertions.assertEquals("zhangsan", user1.getCreator());
        Assertions.assertEquals("zhangsan", user1.getCreatorName());
        Assertions.assertEquals("zhangsan", user1.getModifier());
        Assertions.assertEquals("zhangsan", user1.getModifierName());
        Assertions.assertNotNull(user1.getCreateTime());
        Assertions.assertNotNull(user1.getModifyTime());

        User user2 = mapper.queryById(id2.get());
        Assertions.assertEquals("zhangsan", user2.getCreator());
        Assertions.assertEquals("zhangsan", user2.getCreatorName());
        Assertions.assertEquals("zhangsan", user2.getModifier());
        Assertions.assertEquals("zhangsan", user2.getModifierName());
        Assertions.assertNotNull(user2.getCreateTime());
        Assertions.assertNotNull(user2.getModifyTime());
      }
    });

  }

}
