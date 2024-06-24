
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
import java.util.concurrent.atomic.AtomicReference;

class TestBaseModelAdditionalParam {

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
          User param = new User();
          param.setNickname("a");
          param.setAge(2);
          param.setBirthday(LocalDateTime.now());
          mapper.insert3(param);
          sqlSession.commit();

          id.set(param.getId());
          User user = mapper.queryById(id.get());
          Assertions.assertEquals("zhangsan", user.getCreator());
          Assertions.assertEquals("zhangsan", user.getCreatorName());
          Assertions.assertEquals("ip1", user.getHost());
          Assertions.assertEquals("zhangsan", user.getModifier());
          Assertions.assertEquals("zhangsan", user.getModifierName());
          Assertions.assertNotNull(user.getCreateTime());
          Assertions.assertNotNull(user.getModifyTime());
        }
      });
    });

    SimpleBaseModelSource.runWithHost("ip2", () -> {
      SimpleBaseModelSource.runWithUser("lisi", () -> {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {

          UserMapper mapper = sqlSession.getMapper(UserMapper.class);
          User param = new User();
          param.setId(id.get());
          param.setAge(3);
          param.setBirthday(LocalDateTime.now());
          mapper.updateById3(param);
          sqlSession.commit();

          User user = mapper.queryById(id.get());
          Assertions.assertEquals("zhangsan", user.getCreator());
          Assertions.assertEquals("zhangsan", user.getCreatorName());
          Assertions.assertEquals(3, user.getAge());
          Assertions.assertEquals("ip2", user.getHost());
          Assertions.assertEquals("lisi", user.getModifier());
          Assertions.assertEquals("lisi", user.getModifierName());
          Assertions.assertNotEquals(user.getCreateTime(), user.getModifyTime());
        }
      });
    });
  }

}
