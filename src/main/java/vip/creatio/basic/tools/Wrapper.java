package vip.creatio.basic.tools;

public interface Wrapper<T> {

    T unwrap();

    Class<? extends T> wrappedClass();

}
