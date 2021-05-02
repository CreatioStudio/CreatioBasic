package vip.creatio.basic.cmd;

public @interface Executor {

    Type type() default Type.BRIGADIER;

    String value();

    /** Permission to use this command node, useless to Executor with Fallback type */
    String permission() default "";

    enum Type {

        /** Default type */
        BRIGADIER,

        /** Fallback type, called when an fallback occurs */
        FALLBACK;

    }
}
