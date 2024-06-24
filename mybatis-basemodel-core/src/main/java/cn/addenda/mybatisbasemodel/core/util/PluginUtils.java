package cn.addenda.mybatisbasemodel.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.ibatis.executor.BatchExecutor;
import org.apache.ibatis.executor.CachingExecutor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginUtils {

  public static boolean isBatchMode(Executor executor) {
    if (executor instanceof BatchExecutor) {
      return true;
    }
    if (!(executor instanceof CachingExecutor)) {
      return false;
    }
    CachingExecutor cachingExecutor = (CachingExecutor) executor;
    MetaObject metaObject = SystemMetaObject.forObject(cachingExecutor);
    Object delegate = metaObject.getValue("delegate");
    if (delegate instanceof BatchExecutor) {
      return true;
    }
    return false;
  }

}
