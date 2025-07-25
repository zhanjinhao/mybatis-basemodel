package cn.addenda.mybatisbasemodel.simple.test;

import cn.addenda.mybatisbasemodel.core.BaseModelException;
import cn.addenda.mybatisbasemodel.simple.SimpleBaseModel;
import cn.addenda.mybatisbasemodel.simple.User;
import cn.addenda.mybatisbasemodel.simple.UserMapper;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

class TestBaseModel {

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
        User param = new User();
        param.setId(id.get());
        param.setAge(3);
        param.setBirthday(LocalDateTime.now());
        mapper.updateById(param);
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


    SimpleBaseModel.runWithUser("lisi", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User param = new User();
        param.setId(id.get());
        User user1 = mapper.queryByIdAndModifier2(param);
        Assertions.assertNotNull(user1);

        Map<String, Long> mapParam = new HashMap<>();
        mapParam.put("id", id.get());
        User user11 = mapper.queryByMap(mapParam);
        Assertions.assertNotNull(user11);
      }
    });


    SimpleBaseModel.runWithUser("zhangsan", () -> {
      try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User param = new User();
        param.setId(id.get());
        User user1 = mapper.queryByIdAndModifier2(param);
        Assertions.assertNull(user1);
      }
    });

    PersistenceException zhangsan = Assertions.assertThrows(PersistenceException.class, () -> {
      SimpleBaseModel.runWithUser("zhangsan", () -> {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          User param = new User();
          param.setId(id.get());
          User user1 = mapper.queryByIdAndModifier3(param);
          Assertions.assertNull(user1);
        }
      });
    });

    Assertions.assertThrows(BaseModelException.class, () -> {
      throw zhangsan.getCause();
    });

  }

}
