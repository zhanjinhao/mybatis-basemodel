package cn.addenda.mybatisbasemodel.simple;

import cn.addenda.mybatisbasemodel.core.annotation.AdditionalParam;
import cn.addenda.mybatisbasemodel.simple.test.UserWithHost;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;
import java.util.List;

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
   * 按ID删除数据
   */
  int deleteById(@Param("id") Long id);

  /**
   * 按实体类删除数据
   */
  @AdditionalParam(name = "a", expression = "1", jdbcType = JdbcType.BIGINT, ifValue = true)
  @AdditionalParam(name = "b", expression = "2", jdbcType = JdbcType.BIGINT, ifValue = true)
  int deleteByEntity(User user);

  /**
   * 按实体类删除数据
   */
  @AdditionalParam(name = "a", expression = "1", jdbcType = JdbcType.BIGINT, ifValue = true)
  @AdditionalParam(name = "b", expression = "2", jdbcType = JdbcType.BIGINT, ifValue = true)
  int deleteByEntity2(User user);

  /**
   * 按实体类查询数据
   */
  List<User> queryByEntity(User user);

  /**
   * 按ID查询数据
   */
  UserWithHost queryById(@Param("id") Long id);

  /**
   * 按ID集合查询数据
   */
  List<User> queryByIdList(@Param("idList") List<Long> idList);

  /**
   * 按实体类计数数据
   */
  List<Long> countByEntity(User user);


  int update(@Param("id") Long id, @Param("nickName") String nickName);

  @AdditionalParam(name = SimpleBaseModel.F_MODIFY_TIME, expression = "T(java.time.LocalDateTime).now()", jdbcType = JdbcType.TIMESTAMP, ifValue = true)
  @AdditionalParam(name = SimpleBaseModel.F_MODIFY_TIME, expression = "test21", jdbcType = JdbcType.VARCHAR, ifValue = true)
  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER_NAME, expression = "test22", jdbcType = JdbcType.VARCHAR, ifValue = true)
  int increment(@Param("nickName") String nickName);

  @AdditionalParam(name = SimpleBaseModel.F_MODIFY_TIME, expression = "now(3)", jdbcType = JdbcType.TIMESTAMP, ifValue = false)
  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER_NAME, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  @AdditionalParam(name = SimpleBaseModel.F_CREATE_TIME, expression = "now(3)", jdbcType = JdbcType.TIMESTAMP, ifValue = false)
  @AdditionalParam(name = SimpleBaseModel.F_CREATOR, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  @AdditionalParam(name = SimpleBaseModel.F_CREATOR_NAME, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  void insert2(@Param("nickName") String nickName, @Param("age") Integer age, @Param("birthday") LocalDateTime birthday);

  @AdditionalParam(name = SimpleBaseModel.F_MODIFY_TIME, expression = "now(3)", jdbcType = JdbcType.TIMESTAMP, ifValue = false)
  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER_NAME, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  int updateById2(@Param("id") Long id, @Param("nickName") String nickName, @Param("age") Integer age, @Param("birthday") LocalDateTime birthday);

  /**
   * 新增数据
   */
  @AdditionalParam(name = "host", expression = SimpleBaseModelSource.HOST_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  int insert3(User user);

  /**
   * 按ID更新数据
   */
  @AdditionalParam(name = "host", expression = SimpleBaseModelSource.HOST_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  int updateById3(User user);

  /**
   * 新增数据
   */
  @AdditionalParam(name = "host", expression = SimpleBaseModelSource.HOST_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  int insert4(SimpleUser user);

  /**
   * 按ID更新数据
   */
  @AdditionalParam(name = "host", expression = SimpleBaseModelSource.HOST_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  int updateById4(SimpleUser user);

  // todo 注入列和注入参数应该分为两个注解
  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  User queryByIdAndModifier(@Param("id") Long id);

  @AdditionalParam(name = "modifier2", expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  User queryByIdAndModifier2(User user);

  @AdditionalParam(name = SimpleBaseModel.F_MODIFIER, expression = SimpleBaseModelSource.USER_EL, jdbcType = JdbcType.VARCHAR, ifValue = true)
  User queryByIdAndModifier(User user);

}

