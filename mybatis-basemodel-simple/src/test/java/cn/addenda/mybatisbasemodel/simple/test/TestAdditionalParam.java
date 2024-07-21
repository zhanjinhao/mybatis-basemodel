
package cn.addenda.mybatisbasemodel.simple.test;

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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class TestAdditionalParam {

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

    SimpleBaseModelSource.runWithUser("zhangsan", () -> {
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
          userMapper.insert5(user.getNickname(), user.getAge(), user.getBirthday());
        });

        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        List<User> users = mapper.queryByNickNameList();

        id1.set(users.get(0).getId());
        id2.set(users.get(1).getId());
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
