package vip.creatio.basic.annotation;

import vip.creatio.basic.CreatioBasic;
import vip.creatio.basic.tools.GlobalTaskExecutor;
import vip.creatio.basic.tools.Task;
import vip.creatio.common.util.ReflectUtil;
import vip.creatio.accessor.annotation.AnnotationProcessor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TaskProcessor implements AnnotationProcessor<Task> {

    private final GlobalTaskExecutor executor;

    public TaskProcessor(GlobalTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void process(Task instance, Method mth) {
        if (Modifier.isStatic(mth.getModifiers())
                && mth.getParameterCount() == 0) {
            Runnable wrapper = ReflectUtil.createLambda(Runnable.class, mth);
            switch (instance.value()) {
                case SYNC_TICK:
                    executor.addSyncTask(wrapper, instance.period());
                    break;
                case ASYNC_TICK:
                    executor.addAsyncTask(wrapper, instance.period());
                    break;
                case ON_UNLOAD:
                    executor.addOnUnloadTask(wrapper);
                    break;
                case ON_LOAD:
                    executor.addOnLoadTask(wrapper);
                    break;
                case POST_WORLD:
                    executor.addPostWorldTask(wrapper);
            }
        } else {
            CreatioBasic.intern("[CLibBasic] Unable to register task for method " + mth);
        }
    }

    @Override
    public Class<Task> getTargetClass() {
        return Task.class;
    }
}
