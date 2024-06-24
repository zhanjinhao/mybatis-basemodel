package cn.addenda.mybatisbasemodel.simple.test;

import cn.addenda.mybatisbasemodel.core.helper.BatchDmlHelper;
import cn.addenda.mybatisbasemodel.simple.SimpleBaseModelSource;
import cn.addenda.mybatisbasemodel.simple.SimpleUser;
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

class TestPojoAdditionalParamBatch {

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
    AtomicReference<Long> id1 = new AtomicReference<>();
    AtomicReference<Long> id2 = new AtomicReference<>();

    SimpleBaseModelSource.runWithHost("ip1", () -> {

      SimpleBaseModelSource.runWithUser("zhangsan", () -> {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          SimpleUser param1 = new SimpleUser();
          param1.setNickname("a");
          param1.setAge(2);
          param1.setBirthday(LocalDateTime.now());

          SimpleUser param2 = new SimpleUser();
          param2.setNickname("a");
          param2.setAge(2);
          param2.setBirthday(LocalDateTime.now());
          batchDmlHelper.batch(UserMapper.class, Arrays.asList(param1, param2), (userMapper, user) -> {
            userMapper.insert4(user);
          });

          id1.set(param1.getId());
          id2.set(param2.getId());
          User user1 = mapper.queryById(id1.get());
          Assertions.assertEquals(2, user1.getAge());
          Assertions.assertEquals("a", user1.getNickname());
          Assertions.assertNotNull(user1.getBirthday());
          Assertions.assertEquals("ip1", user1.getHost());
          Assertions.assertNull(user1.getCreator());
          Assertions.assertNull(user1.getCreatorName());
          Assertions.assertNull(user1.getModifier());
          Assertions.assertNull(user1.getModifierName());
          Assertions.assertNull(user1.getCreateTime());
          Assertions.assertNull(user1.getModifyTime());
          User user2 = mapper.queryById(id2.get());
          Assertions.assertEquals(2, user2.getAge());
          Assertions.assertEquals("a", user2.getNickname());
          Assertions.assertNotNull(user2.getBirthday());
          Assertions.assertEquals("ip1", user2.getHost());
          Assertions.assertNull(user2.getCreator());
          Assertions.assertNull(user2.getCreatorName());
          Assertions.assertNull(user2.getModifier());
          Assertions.assertNull(user2.getModifierName());
          Assertions.assertNull(user2.getCreateTime());
          Assertions.assertNull(user2.getModifyTime());
        }
      });
    });

    SimpleBaseModelSource.runWithHost("ip2", () -> {

      SimpleBaseModelSource.runWithUser("lisi", () -> {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          SimpleUser param1 = new SimpleUser();
          param1.setId(id1.get());
          param1.setAge(3);
          param1.setBirthday(LocalDateTime.now());
          SimpleUser param2 = new SimpleUser();
          param2.setId(id2.get());
          param2.setAge(3);
          param2.setBirthday(LocalDateTime.now());

          batchDmlHelper.batch(UserMapper.class, Arrays.asList(param1, param2), (userMapper, simpleUser) -> {
            userMapper.updateById4(simpleUser);
          });

          User user1 = mapper.queryById(id1.get());
          Assertions.assertEquals(3, user1.getAge());
          Assertions.assertEquals("a", user1.getNickname());
          Assertions.assertNotNull(user1.getBirthday());
          Assertions.assertEquals("ip2", user1.getHost());
          Assertions.assertNull(user1.getCreator());
          Assertions.assertNull(user1.getCreatorName());
          Assertions.assertNull(user1.getModifier());
          Assertions.assertNull(user1.getModifierName());
          Assertions.assertNull(user1.getCreateTime());
          Assertions.assertNull(user1.getModifyTime());

          User user2 = mapper.queryById(id2.get());
          Assertions.assertEquals(3, user2.getAge());
          Assertions.assertEquals("a", user2.getNickname());
          Assertions.assertNotNull(user2.getBirthday());
          Assertions.assertEquals("ip2", user2.getHost());
          Assertions.assertNull(user2.getCreator());
          Assertions.assertNull(user2.getCreatorName());
          Assertions.assertNull(user2.getModifier());
          Assertions.assertNull(user2.getModifierName());
          Assertions.assertNull(user2.getCreateTime());
          Assertions.assertNull(user2.getModifyTime());
        }
      });
    });
  }

}
