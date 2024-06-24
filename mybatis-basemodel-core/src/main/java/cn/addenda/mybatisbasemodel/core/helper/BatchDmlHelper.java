package cn.addenda.mybatisbasemodel.core.helper;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.sql.Statement;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntBinaryOperator;

/**
 * @author addenda
 * @since 2022/10/9 16:57
 */
@Slf4j
public class BatchDmlHelper {

  @Setter
  private boolean autoCommit = false;

  @Setter
  private int defaultBatchSize = 100;

  private final SqlSessionFactory sqlSessionFactory;

  public BatchDmlHelper(SqlSessionFactory sqlSessionFactory) {
    this.sqlSessionFactory = sqlSessionFactory;
  }

  public <T, U> Integer batch(Class<T> mapperClass, Iterable<U> data, BiConsumer<T, U> consumer) {
    return batch(mapperClass, data, consumer, getIntAccumulator(), defaultBatchSize, null);
  }

  public <T, U> Integer batch(Class<T> mapperClass, Iterable<U> data, BiConsumer<T, U> consumer, int batchSize) {
    return batch(mapperClass, data, consumer, getIntAccumulator(), batchSize, null);
  }

  public <T, U> Integer batch(Class<T> mapperClass, Iterable<U> data, BiConsumer<T, U> consumer, String name) {
    return batch(mapperClass, data, consumer, getIntAccumulator(), defaultBatchSize, name);
  }

  public <T, U> Integer batch(Class<T> mapperClass, Iterable<U> data, BiConsumer<T, U> consumer, IntBinaryOperator merger) {
    return batch(mapperClass, data, consumer, merger, defaultBatchSize, null);
  }

  public <T, U> Integer batch(Class<T> mapperClass, Iterable<U> data, BiConsumer<T, U> consumer, int batchSize, String name) {
    return batch(mapperClass, data, consumer, getIntAccumulator(), batchSize, name);
  }

  /**
   * 批量处理 DML 语句
   *
   * @param data        需要被处理的数据
   * @param mapperClass Mybatis的Mapper类
   * @param consumer    自定义处理逻辑
   * @param merger      合并单次function的接口
   * @param batchSize   flush到db的最大值
   * @param name        给batch操作起个名，方便排查问题
   */
  public <T, U> Integer batch(Class<T> mapperClass, Iterable<U> data, BiConsumer<T, U> consumer, IntBinaryOperator merger, int batchSize, String name) {
    if (data == null) {
      return null;
    }
    if (batchSize <= 0) {
      throw new IllegalArgumentException("batchSize must be greater than 0!");
    }
    long start = System.currentTimeMillis();
    int result = Integer.MIN_VALUE;
    try (SqlSession batchSqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, autoCommit)) {
      int i = 1;
      T mapper = batchSqlSession.getMapper(mapperClass);
      for (U element : data) {
        consumer.accept(mapper, element);
        if (i != 0 && i % batchSize == 0) {
          List<BatchResult> batchResults = batchSqlSession.flushStatements();
          result = mergeResult(result, batchResults, merger);
        }
        i++;
      }
      List<BatchResult> batchResults = batchSqlSession.flushStatements();
      result = mergeResult(result, batchResults, merger);
    }
    if (name == null) {
      log.info("Mybatis Batch Dml execute [{}] ms. ", System.currentTimeMillis() - start);
    } else {
      log.info("Mybatis Batch [{}] Dml execute [{}] ms. ", name, System.currentTimeMillis() - start);
    }
    return result;
  }

  private Integer mergeResult(int result, Iterable<BatchResult> batchResults, IntBinaryOperator merger) {
    for (BatchResult batchResult : batchResults) {
      int[] updateCounts = batchResult.getUpdateCounts();
      for (int updateCount : updateCounts) {
        result = merger.applyAsInt(result, updateCount);
      }
    }
    return result;
  }

  // -------------------------------------------------------------------------
  //  即使Mapper层传入的是BiFunction，也不会应用其返回值。
  //  所有的batch方法返回的都是影响的行数。
  //  影响行数由org.apache.ibatis.session.SqlSession#flushStatements的返回值计算得到。
  // -------------------------------------------------------------------------

  public <T, U, R> Integer batch(Class<T> mapperClass, Iterable<U> data, BiFunction<T, U, R> function) {
    return batch(mapperClass, data, toBiConsumer(function), getIntAccumulator(), defaultBatchSize, null);
  }

  public <T, U, R> Integer batch(Class<T> mapperClass, Iterable<U> data, BiFunction<T, U, R> function, int batchSize) {
    return batch(mapperClass, data, toBiConsumer(function), getIntAccumulator(), batchSize, null);
  }

  public <T, U, R> Integer batch(Class<T> mapperClass, Iterable<U> data, BiFunction<T, U, R> function, String name) {
    return batch(mapperClass, data, toBiConsumer(function), getIntAccumulator(), defaultBatchSize, name);
  }

  public <T, U, R> Integer batch(Class<T> mapperClass, Iterable<U> data, BiFunction<T, U, R> function, IntBinaryOperator merger) {
    return batch(mapperClass, data, toBiConsumer(function), merger, defaultBatchSize, null);
  }

  public <T, U, R> Integer batch(Class<T> mapperClass, Iterable<U> data, BiFunction<T, U, R> function, int batchSize, String name) {
    return batch(mapperClass, data, toBiConsumer(function), getIntAccumulator(), batchSize, name);
  }

  public <T, U, R> Integer batch(Class<T> mapperClass, Iterable<U> data, BiFunction<T, U, R> function, IntBinaryOperator merger, int batchSize, String name) {
    return batch(mapperClass, data, toBiConsumer(function), merger, batchSize, name);
  }

  private static <T, U, R> BiConsumer<T, U> toBiConsumer(BiFunction<T, U, R> function) {
    return function::apply;
  }

  public static IntBinaryOperator getIntAccumulator(Integer base) {
    return (left, right) -> {
      if (left == Integer.MIN_VALUE) {
        left = base;
      }
      if (Statement.SUCCESS_NO_INFO == left || Statement.SUCCESS_NO_INFO == right) {
        return Statement.SUCCESS_NO_INFO;
      }
      return left + right;
    };
  }

  public static IntBinaryOperator getIntAccumulator() {
    return getIntAccumulator(0);
  }

}
