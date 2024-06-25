package cn.addenda.mybatisbasemodel.simple.test;

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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class TestAdditionalParam {

  static SqlSessionFactory sqlSessionFactory;

  static {
    String resource = "META-INF/mybatis-config-simple.xml";
    Reader reader = null;
    try {
      reader = Resources.getResourceAsReader(resource);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
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

    SimpleBaseModelSource.runWithUser("zhangsan", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        mapper.insert2("a", 2, LocalDateTime.now());
        sqlSession.commit();

        User param = new User();
        param.setNickname("a");
        List<User> users = mapper.queryByEntity(param);

        id.set(users.get(0).getId());
        User user = mapper.queryById(id.get());
        Assertions.assertEquals("zhangsan", user.getCreator());
        Assertions.assertEquals("zhangsan", user.getCreatorName());
        Assertions.assertEquals("zhangsan", user.getModifier());
        Assertions.assertEquals("zhangsan", user.getModifierName());
        Assertions.assertNotNull(user.getCreateTime());
        Assertions.assertNotNull(user.getModifyTime());
      }
    });


    SimpleBaseModelSource.runWithUser("lisi", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        mapper.updateById2(id.get(), "a", 3, LocalDateTime.now());
        sqlSession.commit();

        User user = mapper.queryById(id.get());
        Assertions.assertEquals("zhangsan", user.getCreator());
        Assertions.assertEquals("zhangsan", user.getCreatorName());
        Assertions.assertEquals(3, user.getAge());
        Assertions.assertEquals("lisi", user.getModifier());
        Assertions.assertEquals("lisi", user.getModifierName());
        Assertions.assertNotEquals(user.getCreateTime(), user.getModifyTime());
      }
    });


    SimpleBaseModelSource.runWithUser("lisi", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user1 = mapper.queryByIdAndModifier(id.get());
        Assertions.assertNotNull(user1);
      }
    });


    SimpleBaseModelSource.runWithUser("zhangsan", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user1 = mapper.queryByIdAndModifier(id.get());
        Assertions.assertNull(user1);
      }
    });

  }
}
