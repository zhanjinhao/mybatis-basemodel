package cn.addenda.mybatisbasemodel.simple;

import cn.addenda.mybatisbasemodel.core.annotation.AdditionalBaseModel;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalParam;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalValue;
import cn.addenda.mybatisbasemodel.simple.test.UserWithHost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * todo 需要测试list参数、参数作为条件
 *
 * @author addenda
 * @since 2023-07-07 09:19:52
 */
public interface UserMapper {

  void createTable();

  void dropTable();

  /**
   * 新增数据
   */
  int insert(User user);

  /**
   * 按ID更新数据
   */
  int updateById(User user);

  /**
   * 按实体类查询数据
   */
  List<User> queryByEntity(User user);

  /**
   * 按ID查询数据
   */
  UserWithHost queryById(@Param("id") Long id);

  @AdditionalValue(name = SimpleBaseModel.F_MODIFY_TIME, expression = "now(3)", jdbcType = JdbcType.TIMESTAMP, ifObj = false)
  @AdditionalValue(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModel.USER_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  @AdditionalValue(name = SimpleBaseModel.F_MODIFIER_NAME, expression = SimpleBaseModel.USER_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  @AdditionalValue(name = SimpleBaseModel.F_CREATE_TIME, expression = "now(3)", jdbcType = JdbcType.TIMESTAMP, ifObj = false)
  @AdditionalValue(name = SimpleBaseModel.F_CREATOR, expression = SimpleBaseModel.USER_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  @AdditionalValue(name = SimpleBaseModel.F_CREATOR_NAME, expression = SimpleBaseModel.USER_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  void insert2(@Param("nickName") String nickName, @Param("age") Integer age, @Param("birthday") LocalDateTime birthday);

  @AdditionalValue(name = SimpleBaseModel.F_MODIFY_TIME, expression = "now(3)", jdbcType = JdbcType.TIMESTAMP, ifObj = false)
  @AdditionalValue(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModel.USER_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  @AdditionalValue(name = SimpleBaseModel.F_MODIFIER_NAME, expression = SimpleBaseModel.USER_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  int updateById2(@Param("id") Long id, @Param("nickName") String nickName, @Param("age") Integer age, @Param("birthday") LocalDateTime birthday);

  /**
   * 新增数据
   */
  @AdditionalValue(name = "host", expression = SimpleBaseModel.HOST_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  int insert3(User user);

  /**
   * 按ID更新数据
   */
  @AdditionalValue(name = "host", expression = SimpleBaseModel.HOST_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  int updateById3(User user);

  /**
   * 新增数据
   */
  @AdditionalValue(name = "host", expression = SimpleBaseModel.HOST_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  int insert4(SimpleUser user);

  /**
   * 按ID更新数据
   */
  @AdditionalValue(name = "host", expression = SimpleBaseModel.HOST_EL, jdbcType = JdbcType.VARCHAR, ifObj = true)
  int updateById4(SimpleUser user);

  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModel.USER_EL)
  User queryByIdAndModifier(@Param("id") Long id);

  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModel.USER_EL)
  User queryByIdAndModifier4(Long id);

  @AdditionalParam(name = "modifier2", expression = SimpleBaseModel.USER_EL)
  User queryByIdAndModifier2(User user);

  // 参数为Map的场景
  @AdditionalParam(name = "modifier2", expression = SimpleBaseModel.USER_EL)
  User queryByMap(@Param("map") Map<String, Long> idMap);

  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModel.USER_EL)
  User queryByIdAndModifier3(User user);

  @AdditionalBaseModel(SimpleBaseModel.class)
  void insert5(@Param("nickName") String nickName, @Param("age") Integer age, @Param("birthday") LocalDateTime birthday);

  @AdditionalBaseModel(SimpleBaseModel.class)
  int updateById5(@Param("id") Long id, @Param("nickName") String nickName, @Param("age") Integer age, @Param("birthday") LocalDateTime birthday);

  @AdditionalParam(name = "nicknameList", expression = {"a", "b"}, objPreEvaluate = false)
  List<User> queryByNickNameList();

}

