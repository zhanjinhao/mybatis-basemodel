package cn.addenda.mybatisbasemodel.core.util;

import cn.addenda.mybatisbasemodel.core.AdditionAttr;
import cn.addenda.mybatisbasemodel.core.BaseModel;
import cn.addenda.mybatisbasemodel.core.BaseModelAdapter;
import cn.addenda.mybatisbasemodel.core.BaseModelException;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalBaseModel;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalParam;
import cn.addenda.mybatisbasemodel.core.annotation.AdditionalValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.type.JdbcType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.ibatis.executor.keygen.SelectKeyGenerator.SELECT_KEY_SUFFIX;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MsIdAnnotationUtils {

  private static final Map<String, AdditionalValue[]> ADDITINAL_VALUE_MAP = new ConcurrentHashMap<>();
  private static final Map<String, AdditionalParam[]> ADDITINAL_PARAM_MAP = new ConcurrentHashMap<>();
  private static final Map<String, AdditionalBaseModel[]> ADDITIONAL_BASE_MODEL_MAP = new ConcurrentHashMap<>();

  private static final Map<String, List<AdditionAttr>> ADDITION_MAP = new ConcurrentHashMap<>();

  public static List<AdditionAttr> extractAddition(String msId, SqlCommandType sqlCommandType) {
    List<AdditionAttr> result = ADDITION_MAP.computeIfAbsent(msId,
            id -> {
              // 合并AdditionalValue和AdditionalParam的注解
              AdditionalParam[] additionalParams = extractAdditionalParams(id);
              AdditionalValue[] additionalValues = extractAdditionalValues(id);
              List<AdditionAttr> additionAttrList = mergeAddition(additionalParams, additionalValues);
              // 补上由AdditionalBaseModel生产的Addition
              if (sqlCommandType == SqlCommandType.INSERT) {
                AdditionAttr[] additionAttrs = generateAdditionAttrs(id, BaseModelMetaDataUtils::getAllFieldMap);
                if (additionAttrs != null && additionAttrs.length != 0) {
                  additionAttrList.addAll(Arrays.stream(additionAttrs).collect(Collectors.toList()));
                }
              } else if (sqlCommandType == SqlCommandType.UPDATE) {
                AdditionAttr[] additionAttrs = generateAdditionAttrs(id, BaseModelMetaDataUtils::getUpdateFieldMap);
                if (additionAttrs != null && additionAttrs.length != 0) {
                  additionAttrList.addAll(Arrays.stream(additionAttrs).collect(Collectors.toList()));
                }
              }
              validRepeat(msId, additionAttrList);
              return additionAttrList;
            });

    return result.stream().map(AdditionAttr::new).collect(Collectors.toList());
  }

  public static AdditionAttr[] generateAdditionAttrs(
          String msId, Function<Class<? extends BaseModel>, Map<String, Field>> fieldMapFunction) {
    AdditionalBaseModel[] additionalBaseModels = extractAdditionalBaseModels(msId);
    if (additionalBaseModels == null || additionalBaseModels.length == 0) {
      return null;
    }
    AdditionalBaseModel additionalBaseModel = additionalBaseModels[0];
    Class<? extends BaseModel> baseModelClass = additionalBaseModel.value();
    if (BaseModelAdapter.class.isAssignableFrom(baseModelClass)) {
      throw new BaseModelException(String.format("@AdditionalBaseModel does not support BaseModelAdapter[%s]", baseModelClass));
    }
    return doGenerateAdditionAttrs(fieldMapFunction.apply(baseModelClass));
  }

  private static AdditionAttr[] doGenerateAdditionAttrs(Map<String, Field> fieldMap) {
    List<AdditionAttr> additionAttrList = new ArrayList<>();
    for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
      Field field = entry.getValue();
      AdditionAttr additionAttr = new AdditionAttr();
      additionAttr.setName(entry.getKey());
      additionAttr.setColumnName(BaseModelMetaDataUtils.getColumnName(field));
      additionAttr.setExpression(BaseModelMetaDataUtils.getExpression(field));
      additionAttr.setJdbcType(calculateJdbcType(field));
      additionAttr.setIfValue(BaseModelMetaDataUtils.getIfValue(field));
      additionAttr.setIfInjected(true);
      additionAttrList.add(additionAttr);
    }
    return additionAttrList.toArray(new AdditionAttr[0]);
  }

  public static AdditionalBaseModel[] extractAdditionalBaseModels(String msId) {
    return ADDITIONAL_BASE_MODEL_MAP.computeIfAbsent(msId, msId1 -> {
      AdditionalBaseModel[] additionalBaseModels = extractAddition(msId1, AdditionalBaseModel.class);
      if (additionalBaseModels == null) {
        return new AdditionalBaseModel[0];
      }
      return additionalBaseModels;
    });
  }

  private static List<AdditionAttr> mergeAddition(
          AdditionalParam[] additionalParams, AdditionalValue[] additionalValues) {
    // 每一次都必须是新的对象
    List<AdditionAttr> additionAttrList1 = Arrays.stream(additionalParams).map(AdditionAttr::new).collect(Collectors.toList());
    List<AdditionAttr> additionAttrList2 = Arrays.stream(additionalValues).map(AdditionAttr::new).collect(Collectors.toList());
    additionAttrList1.addAll(additionAttrList2);
    return additionAttrList1;
  }

  public static AdditionalValue[] extractAdditionalValues(String msId) {
    return ADDITINAL_VALUE_MAP.computeIfAbsent(msId, msId1 -> {
      AdditionalValue[] additionalValues = extractAddition(msId1, AdditionalValue.class);
      if (additionalValues == null) {
        return new AdditionalValue[0];
      }
      return additionalValues;
    });
  }

  public static AdditionalParam[] extractAdditionalParams(String msId) {
    return ADDITINAL_PARAM_MAP.computeIfAbsent(msId, msId1 -> {
      AdditionalParam[] additionalParams = extractAddition(msId1, AdditionalParam.class);
      if (additionalParams == null) {
        return new AdditionalParam[0];
      }
      return additionalParams;
    });
  }

  @SneakyThrows
  private static <T extends Annotation> T[] extractAddition(String msId, Class<T> clazz) {
    if (msId.endsWith(SELECT_KEY_SUFFIX)) {
      return null;
    }
    int end = msId.lastIndexOf(".");
    Class<?> aClass = Class.forName(msId.substring(0, end));
    String methodName = msId.substring(end + 1);
    Method[] methods = aClass.getMethods();
    for (Method method : methods) {
      // mybatis 动态代理模式不支持函数重载。用方法名匹配没问题。
      if (method.getName().equals(methodName)) {
        return method.getAnnotationsByType(clazz);
      }
    }
    throw new BaseModelException(String.format("Can not extract [%s] from MappedStatement[%s]！", clazz, msId));
  }

  public static void validRepeat(String msId, List<AdditionAttr> additionAttrList) {
    Set<String> additionNameSet = new HashSet<>();
    for (AdditionAttr additionAttr : additionAttrList) {
      String name = additionAttr.getName();
      if (additionNameSet.contains(name)) {
        throw new BaseModelException(String.format("Addition[%s] of MappedStatement[%s] repeat.", name, msId));
      }
      additionNameSet.add(name);
    }
  }

  public static JdbcType calculateJdbcType(Field field) {
    Class<?> propertyType = field.getType();
    JdbcType jdbcType = BaseModelMetaDataUtils.getJdbcType(field);
    if (jdbcType == null) {
      if (propertyType == boolean.class || propertyType == Boolean.class) {
        jdbcType = JdbcType.BOOLEAN;
      } else if (propertyType == Byte.class || propertyType == byte.class) {
        jdbcType = JdbcType.TINYINT;
      } else if (propertyType == Short.class || propertyType == short.class) {
        jdbcType = JdbcType.SMALLINT;
      } else if (propertyType == Integer.class || propertyType == int.class) {
        jdbcType = JdbcType.INTEGER;
      } else if (propertyType == Long.class || propertyType == long.class) {
        jdbcType = JdbcType.BIGINT;
      } else if (propertyType == Float.class || propertyType == float.class) {
        jdbcType = JdbcType.FLOAT;
      } else if (propertyType == Double.class || propertyType == double.class) {
        jdbcType = JdbcType.DOUBLE;
      } else if (propertyType == char.class || propertyType == Character.class) {
        jdbcType = JdbcType.CHAR;
      } else if (CharSequence.class.isAssignableFrom(propertyType)) {
        jdbcType = JdbcType.VARCHAR;
      } else if (propertyType == LocalDateTime.class) {
        jdbcType = JdbcType.TIMESTAMP;
      } else if (propertyType == LocalDate.class) {
        jdbcType = JdbcType.DATE;
      } else if (propertyType == LocalTime.class) {
        jdbcType = JdbcType.TIME;
      } else if (propertyType == BigInteger.class) {
        jdbcType = JdbcType.BIGINT;
      } else {
        throw new IllegalArgumentException(String.format("[%s] : Unsupported property type", propertyType.getCanonicalName()));
      }
    }
    return jdbcType;
  }

}
