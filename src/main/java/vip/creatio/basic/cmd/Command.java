package vip.creatio.basic.cmd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Command {

    String name();

    String[] aliases() default {};

    String description() default "";

    /** Permission for player to use this command */
    String permission() default "";

    /** If there's an existing command registered, will it be overridden? */
    boolean override() default true;

    /** Hide command in /help list, but can still be seen in tab complete */
    boolean hide() default false;

}
