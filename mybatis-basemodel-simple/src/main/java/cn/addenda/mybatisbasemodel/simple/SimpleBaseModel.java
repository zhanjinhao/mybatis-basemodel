package cn.addenda.mybatisbasemodel.simple;

import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.annotation.BaseModelExpression;
import cn.addenda.mybatisbasemodel.core.annotation.InsertField;
import cn.addenda.mybatisbasemodel.core.annotation.UpdateField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author addenda
 * @since 2022/8/16 20:40
 */
@ToString
@JsonIgnoreProperties({
        SimpleBaseModel.F_CREATOR, SimpleBaseModel.F_CREATOR_NAME, SimpleBaseModel.F_CREATE_TIME, SimpleBaseModel.F_MODIFIER,
        SimpleBaseModel.F_MODIFIER_NAME, SimpleBaseModel.F_MODIFY_TIME})
public abstract class SimpleBaseModel implements Serializable, BaseModel {

  private static final long serialVersionUID = 1L;

  public static final String F_CREATOR = "creator";

  public static final String F_CREATOR_NAME = "creatorName";

  public static final String F_CREATE_TIME = "createTime";

  public static final String F_MODIFIER = "modifier";

  public static final String F_MODIFIER_NAME = "modifierName";

  public static final String F_MODIFY_TIME = "modifyTime";

  @Getter
  @Setter
  @InsertField
  @BaseModelExpression(expression = SimpleBaseModelSource.USER_EL, ifObj = true)
  private String creator;

  @Setter
  @Getter
  @InsertField
  @BaseModelExpression(expression = SimpleBaseModelSource.USER_EL, ifObj = true)
  private String creatorName;

  @Getter
  @Setter
  @InsertField
  @JsonSerialize(using = LocalDateTimeStrSerializer.class)
  @JsonDeserialize(using = LocalDateTimeStrDeSerializer.class)
  @BaseModelExpression(expression = "now(3)", ifObj = false)
  private LocalDateTime createTime;

  @Getter
  @Setter
  @InsertField
  @UpdateField
  @BaseModelExpression(expression = SimpleBaseModelSource.USER_EL, ifObj = true)
  private String modifier;

  @Getter
  @Setter
  @InsertField
  @UpdateField
  @BaseModelExpression(expression = SimpleBaseModelSource.USER_EL, ifObj = true)
  private String modifierName;

  @Getter
  @Setter
  @InsertField
  @UpdateField
  @BaseModelExpression(expression = "now(3)", ifObj = false)
  @JsonSerialize(using = LocalDateTimeStrSerializer.class)
  @JsonDeserialize(using = LocalDateTimeStrDeSerializer.class)
  private LocalDateTime modifyTime;

  private static class LocalDateTimeStrDeSerializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode jsonNode = jp.getCodec().readTree(jp);
      final String s = jsonNode.asText();
      return DateUtils.parseLdt(s, DateUtils.FULL_FORMATTER);
    }
  }

  private static class LocalDateTimeStrSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeString(DateUtils.format(localDateTime, DateUtils.FULL_FORMATTER));
    }
  }

}
