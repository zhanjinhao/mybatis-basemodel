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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class TestBaseModelRepeatedInsert {

  static SqlSessionFactory sqlSessionFactory;

  static BatchDmlHelper batchDmlHelper;

  static {
    String resource = "META-INF/mybatis-config-simple.xml";
    Reader reader;
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
    AtomicReference<Long> id1 = new AtomicReference<>();
    AtomicReference<Long> id2 = new AtomicReference<>();

    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

      UserMapper mapper = sqlSession.getMapper(UserMapper.class);
      User param1 = new User();
      param1.setNickname("a");
      param1.setAge(2);
      param1.setBirthday(LocalDateTime.now());

      AtomicInteger count = new AtomicInteger(1);
      batchDmlHelper.batch(UserMapper.class, Arrays.asList(param1, param1), (userMapper, user) -> {
        if (count.getAndIncrement() == 1) {
          SimpleBaseModel.runWithUser("zhangsan", () -> {
            userMapper.insert(user);
          });
        }

        if (count.getAndIncrement() == 2) {
          SimpleBaseModel.runWithUser("list", () -> {
            userMapper.insert(user);
          });
        }
      });

      sqlSession.commit();

      id1.set(param1.getId() - 1);
      User user1 = mapper.queryById(id1.get());
      Assertions.assertEquals("zhangsan", user1.getCreator());
      Assertions.assertEquals("zhangsan", user1.getCreatorName());
      Assertions.assertEquals("zhangsan", user1.getModifier());
      Assertions.assertEquals("zhangsan", user1.getModifierName());
      Assertions.assertNotNull(user1.getCreateTime());
      Assertions.assertNotNull(user1.getModifyTime());

      id2.set(param1.getId());
      User user2 = mapper.queryById(id2.get());
      Assertions.assertEquals("list", user2.getCreator());
      Assertions.assertEquals("list", user2.getCreatorName());
      Assertions.assertEquals("list", user2.getModifier());
      Assertions.assertEquals("list", user2.getModifierName());
      Assertions.assertNotNull(user2.getCreateTime());
      Assertions.assertNotNull(user2.getModifyTime());
    }

  }

}
