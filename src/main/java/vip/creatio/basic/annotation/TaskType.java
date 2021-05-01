package vip.creatio.basic.annotation;

public enum TaskType {

    SYNC_TICK,

    ASYNC_TICK,

    ON_LOAD,

    ON_UNLOAD,

    /** Called when server finished loading, after ON_LOAD */
    POST_WORLD;

}
