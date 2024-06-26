package cn.addenda.mybatisbasemodel.simple.test;

import cn.addenda.mybatisbasemodel.simple.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString(callSuper = true)
public class UserWithHost extends User {

  private String host;

}
