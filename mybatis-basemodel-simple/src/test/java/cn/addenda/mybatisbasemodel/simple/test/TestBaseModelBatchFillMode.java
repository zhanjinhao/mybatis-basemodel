package cn.addenda.mybatisbasemodel.simple.test;

import cn.addenda.mybatisbasemodel.core.BaseModelContext;
import cn.addenda.mybatisbasemodel.core.helper.BatchDmlHelper;
import cn.addenda.mybatisbasemodel.simple.SimpleBaseModelSource;
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

class TestBaseModelBatchFillMode {

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

    BaseModelContext.runWithFillMode(BaseModelContext.FILL_MODE_NULL, () -> {
      SimpleBaseModelSource.runWithUser("zhangsan", () -> {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          User param1 = new User();
          param1.setNickname("a");
          param1.setAge(2);
          param1.setBirthday(LocalDateTime.now());
          param1.setCreator("wangwu");

          User param2 = new User();
          param2.setNickname("a");
          param2.setAge(2);
          param2.setBirthday(LocalDateTime.now());
          batchDmlHelper.batch(UserMapper.class, Arrays.asList(param1, param2), (userMapper, user) -> {
            userMapper.insert(user);
          });

          sqlSession.commit();

          id1.set(param1.getId());
          User user1 = mapper.queryById(id1.get());
          Assertions.assertEquals("wangwu", user1.getCreator());
          Assertions.assertEquals("zhangsan", user1.getCreatorName());
          Assertions.assertEquals("zhangsan", user1.getModifier());
          Assertions.assertEquals("zhangsan", user1.getModifierName());
          Assertions.assertNotNull(user1.getCreateTime());
          Assertions.assertNotNull(user1.getModifyTime());

          id2.set(param2.getId());
          User user2 = mapper.queryById(id2.get());
          Assertions.assertEquals("zhangsan", user2.getCreator());
          Assertions.assertEquals("zhangsan", user2.getCreatorName());
          Assertions.assertEquals("zhangsan", user2.getModifier());
          Assertions.assertEquals("zhangsan", user2.getModifierName());
          Assertions.assertNotNull(user2.getCreateTime());
          Assertions.assertNotNull(user2.getModifyTime());
        }
      });
    });

    SimpleBaseModelSource.runWithUser("lisi", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User param1 = new User();
        param1.setId(id1.get());
        param1.setAge(3);
        param1.setBirthday(LocalDateTime.now());

        User param2 = new User();
        param2.setId(id2.get());
        param2.setAge(3);
        param2.setBirthday(LocalDateTime.now());

        batchDmlHelper.batch(UserMapper.class, Arrays.asList(param1, param2), (userMapper, user) -> {
          userMapper.updateById(user);
        });

        sqlSession.commit();

        User user1 = mapper.queryById(id1.get());
        Assertions.assertEquals("wangwu", user1.getCreator());
        Assertions.assertEquals("zhangsan", user1.getCreatorName());
        Assertions.assertEquals(3, user1.getAge());
        Assertions.assertEquals("lisi", user1.getModifier());
        Assertions.assertEquals("lisi", user1.getModifierName());
        Assertions.assertNotEquals(user1.getCreateTime(), user1.getModifyTime());

        User user2 = mapper.queryById(id2.get());
        Assertions.assertEquals("zhangsan", user2.getCreator());
        Assertions.assertEquals("zhangsan", user2.getCreatorName());
        Assertions.assertEquals(3, user2.getAge());
        Assertions.assertEquals("lisi", user2.getModifier());
        Assertions.assertEquals("lisi", user2.getModifierName());
        Assertions.assertNotEquals(user2.getCreateTime(), user2.getModifyTime());
      }
    });
  }

}
