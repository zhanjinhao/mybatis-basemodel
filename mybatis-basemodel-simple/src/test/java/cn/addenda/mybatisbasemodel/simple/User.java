package cn.addenda.mybatisbasemodel.simple;

import java.time.LocalDateTime;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author addenda
 * @since 2023-07-07 09:19:53
 */
@Setter
@Getter
@ToString(callSuper = true)
public class User extends SimpleBaseModel implements Serializable {

  private static final long serialVersionUID = 676662520236546432L;

  private Long id;
  /**
   * 昵称
   */
  private String nickname;
  /**
   * 年龄
   */
  private Integer age;
  /**
   * 生日
   */
  private LocalDateTime birthday;

}

