package vip.creatio.basic.tools;

/**
 * Wrapper that does not contain original field, for the sake of efficiency.
 */
@SuppressWarnings("unchecked")
public interface ElasticWrapper<T> extends Wrapper<T> {

    @Override
    default T unwrap() {
        return (T) this;
    }

    @Override
    default Class<? extends T> wrappedClass() {
        return (Class<? extends T>) getClass();
    }
}
