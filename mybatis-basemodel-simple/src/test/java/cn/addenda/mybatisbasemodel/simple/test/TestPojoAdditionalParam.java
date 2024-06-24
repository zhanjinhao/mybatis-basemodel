package cn.addenda.mybatisbasemodel.simple.test;

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
import java.util.concurrent.atomic.AtomicReference;

class TestPojoAdditionalParam {

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
    SimpleBaseModelSource.runWithHost("ip1", () -> {

      SimpleBaseModelSource.runWithUser("zhangsan", () -> {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          SimpleUser param = new SimpleUser();
          param.setNickname("a");
          param.setAge(2);
          param.setBirthday(LocalDateTime.now());
          mapper.insert4(param);
          sqlSession.commit();

          id.set(param.getId());
          User user = mapper.queryById(id.get());
          Assertions.assertEquals(2, user.getAge());
          Assertions.assertEquals("a", user.getNickname());
          Assertions.assertNotNull(user.getBirthday());
          Assertions.assertEquals("ip1", user.getHost());

          Assertions.assertNull(user.getCreator());
          Assertions.assertNull(user.getCreatorName());
          Assertions.assertNull(user.getModifier());
          Assertions.assertNull(user.getModifierName());
          Assertions.assertNull(user.getCreateTime());
          Assertions.assertNull(user.getModifyTime());
        }
      });
    });

    SimpleBaseModelSource.runWithHost("ip2", () -> {

      SimpleBaseModelSource.runWithUser("lisi", () -> {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          SimpleUser param = new SimpleUser();
          param.setId(id.get());
          param.setAge(3);
          param.setBirthday(LocalDateTime.now());
          mapper.updateById4(param);
          sqlSession.commit();

          User user = mapper.queryById(id.get());
          Assertions.assertEquals(3, user.getAge());
          Assertions.assertEquals("a", user.getNickname());
          Assertions.assertNotNull(user.getBirthday());
          Assertions.assertEquals("ip2", user.getHost());

          Assertions.assertNull(user.getCreator());
          Assertions.assertNull(user.getCreatorName());
          Assertions.assertNull(user.getModifier());
          Assertions.assertNull(user.getModifierName());
          Assertions.assertNull(user.getCreateTime());
          Assertions.assertNull(user.getModifyTime());
        }
      });
    });
  }

}
