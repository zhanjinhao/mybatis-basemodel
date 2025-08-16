package cn.addenda.mybatisbasemodel.simple.test;

import cn.addenda.mybatisbasemodel.core.BaseModelContext;
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
import java.util.concurrent.atomic.AtomicReference;

class TestFillMode {

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
  void test1() {
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


    SimpleBaseModel.runWithUser("lisi", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

        BaseModelContext.pushFillMode(BaseModelContext.FILL_MODE_SKIP);
        try {
          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          User param = mapper.queryById(id.get());
          param.setId(null);
          mapper.insert(param);
          sqlSession.commit();

          User user = mapper.queryById(param.getId());
          Assertions.assertEquals("zhangsan", user.getCreator());
          Assertions.assertEquals("zhangsan", user.getCreatorName());
          Assertions.assertEquals("zhangsan", user.getModifier());
          Assertions.assertEquals("zhangsan", user.getModifierName());
          Assertions.assertEquals(user.getCreateTime(), param.getCreateTime());
          Assertions.assertEquals(user.getModifyTime(), param.getModifyTime());
        } finally {
          BaseModelContext.popFillMode();
        }
      }
    });

  }

  @Test
  void test2() {
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


    SimpleBaseModel.runWithUser("lisi", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User param = mapper.queryById(id.get());
        param.setId(null);
        mapper.insert(param);
        sqlSession.commit();

        User user = mapper.queryById(param.getId());
        Assertions.assertEquals("lisi", user.getCreator());
        Assertions.assertEquals("lisi", user.getCreatorName());
        Assertions.assertEquals("lisi", user.getModifier());
        Assertions.assertEquals("lisi", user.getModifierName());
        Assertions.assertTrue(user.getCreateTime().isAfter(param.getCreateTime()));
        Assertions.assertTrue(user.getModifyTime().isAfter(param.getModifyTime()));
      }
    });

  }

}
