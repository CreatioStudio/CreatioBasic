package vip.creatio.basic.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Executor {

    /** Command line */
    String value();

    /** Permission to use this command node, useless to Executor with Fallback type */
    String permission() default "";

    /** Command sender will not be able to see this section entirely if no permission */
    boolean restricted() default true;

}
