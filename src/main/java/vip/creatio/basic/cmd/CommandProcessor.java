package vip.creatio.basic.cmd;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import vip.creatio.accessor.ReflectiveClassLoader;
import vip.creatio.accessor.annotation.AnnotationProcessor;
import vip.creatio.basic.cmd.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: waiting to be complete
public class CommandProcessor implements AnnotationProcessor<Command> {

    private final CommandRegister register;
    private final ExecutorProc executorProc = new ExecutorProc();
    private final CompleterProc completerProc = new CompleterProc();
    private final FallbackProc fallbackProc = new FallbackProc();

    private final Map<Class<?>, CommandData> workMap = new HashMap<>();

    static class CommandData {
        String name;
        List<String> aliases;
        LiteralCommandNode<?> node;
        String description;
        boolean overrides;
    }

    public CommandProcessor(CommandRegister register) {
        this.register = register;
    }

    @Override
    public void process(Command instance, Class<?> c) {
        CommandData data = new CommandData();
        data.name = instance.name();
        data.aliases.addAll(Arrays.asList(instance.aliases()));
        data.description = instance.description();
        data.overrides = instance.override();
    }

    @Override
    public void onProcessEnd(Class<?> c) {
        CommandData data = workMap.get(c);
        if (data != null) {
            if (data.node == null) {
                System.err.println("[@Command processor] class annotated @Command has no valid @Executor method!");
                return;
            }
            register.register(data.node, data.description, data.aliases);
        }
    }

    @Override
    public void onRegister(ReflectiveClassLoader classLoader) {
        // Register subProcessor
        classLoader.addProcessor(executorProc);
        classLoader.addProcessor(completerProc);
        classLoader.addProcessor(fallbackProc);
    }

    @Override
    public Class<Command> getTargetClass() {
        return Command.class;
    }

    private static final Pattern TOKEN = Pattern.compile("\\[(.*)]");

    private static List<Node> parseNodes(String str) {
        if (str.charAt(0) == '/') str = str.substring(1);
        StringTokenizer tokenizer = new StringTokenizer(str);
        List<Node> list = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            Matcher mth = TOKEN.matcher(token);
            if (mth.find()) {
                list.add(new Node(mth.group(1), true));
            } else {
                list.add(new Node(token, true));
            }
        }
        return list;
    }

    private static class Node {
        String node;
        boolean isLiteral;

        Node(String node, boolean isLiteral) {
            this.node = node;
            this.isLiteral = isLiteral;
        }
    }


    class ExecutorProc implements AnnotationProcessor<Executor> {

        @Override
        public void process(Executor instance, Method mth) {
            List<Node> tokens = parseNodes(instance.value());
            CommandData data = workMap.get(mth.getDeclaringClass());

        }

        @Override
        public Class<Executor> getTargetClass() {
            return Executor.class;
        }
    }

    class CompleterProc implements AnnotationProcessor<Completer> {

        @Override
        public void process(Completer instance, Method mth) {
            AnnotationProcessor.super.process(instance, mth);
        }

        @Override
        public Class<Completer> getTargetClass() {
            return Completer.class;
        }
    }

    class FallbackProc implements AnnotationProcessor<Fallback> {

        @Override
        public void process(Fallback instance, Method mth) {
            AnnotationProcessor.super.process(instance, mth);
        }

        @Override
        public Class<Fallback> getTargetClass() {
            return Fallback.class;
        }
    }
}
