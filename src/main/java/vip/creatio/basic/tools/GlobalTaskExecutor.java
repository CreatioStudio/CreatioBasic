package vip.creatio.basic.tools;

public interface GlobalTaskExecutor extends TaskExecutor {

    void addOnLoadTask(Runnable task);

    void addOnUnloadTask(Runnable task);

    void addPostWorldTask(Runnable task);

    default void removeOnLoadTask(Runnable task) {
        throw new UnsupportedOperationException("operation unsupported!");
    }

    default void removeOnUnloadTask(Runnable task) {
        throw new UnsupportedOperationException("operation unsupported!");
    }

    default void removePostWorldTask(Runnable task) {
        throw new UnsupportedOperationException("operation unsupported!");
    }

    void onLoad();

    void onUnload();

    void onPostWorld();

}
