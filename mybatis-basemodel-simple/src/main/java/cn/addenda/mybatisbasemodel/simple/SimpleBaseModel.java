package cn.addenda.mybatisbasemodel.simple;

import cn.addenda.mybatisbasemodel.core.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author addenda
 * @since 2022/8/16 20:40
 */
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

  private static final Map<String, Field> allFieldNameMap;
  private static final List<String> allFieldNameList;
  private static final List<String> updateFieldNameList;

  static {
    allFieldNameMap = new HashMap<>();
    allFieldNameList = new ArrayList<>();
    Field[] declaredFields = SimpleBaseModel.class.getDeclaredFields();
    for (Field field : declaredFields) {
      if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
        continue;
      }
      allFieldNameMap.put(field.getName(), field);
      allFieldNameList.add(field.getName());
    }

    updateFieldNameList = new ArrayList<>();
    for (Field field : declaredFields) {
      if (Modifier.isFinal(field.getModifiers()) || Modifier.isStatic(field.getModifiers())
              || !field.isAnnotationPresent(UpdateField.class)) {
        continue;
      }
      updateFieldNameList.add(field.getName());
    }
  }

  @Override
  public List<String> getAllFieldNameList() {
    return allFieldNameList;
  }

  @Override
  public List<String> getUpdateFieldNameList() {
    return updateFieldNameList;
  }

  @Override
  public Field getFieldByFieldName(String fieldName) {
    return allFieldNameMap.get(fieldName);
  }

  @Getter
  @Setter
  private String creator;

  @Setter
  @Getter
  private String creatorName;

  @Getter
  @Setter
  @JsonSerialize(using = LocalDateTimeStrSerializer.class)
  @JsonDeserialize(using = LocalDateTimeStrDeSerializer.class)
  private LocalDateTime createTime;

  @Getter
  @Setter
  @UpdateField
  private String modifier;

  @Getter
  @Setter
  @UpdateField
  private String modifierName;

  @Getter
  @Setter
  @UpdateField
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
