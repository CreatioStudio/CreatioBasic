package vip.creatio.basic.cmd;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;
import vip.creatio.basic.chat.Component;
import vip.creatio.common.util.FileUtil;
import vip.creatio.common.util.StringUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static vip.creatio.basic.SharedConstants.CLASSFILE_ILLEGAL_CHARS;

/**
 * Select class file using class path
 * The name of selected file must be ended with a specified suffix, such as ".class", ".java"
 */
public class ClassPathArgumentType extends ExternArgumentType<File[]> {
    private static final String[] EMPTY = new String[0];

    private static final Collection<String> EXAMPLES = Arrays.asList("*", "com.example.Test", "java.lang.Class");

    public static final DynamicCommandExceptionType NOT_EXIST = new DynamicCommandExceptionType(o -> Component.of("Directory '" + o + "' not exist"));
    public static final SimpleCommandExceptionType INVALID_NAME = new SimpleCommandExceptionType(Component.of("Class name contains illegal character"));

    private final File root;
    private final String suffix;
    private final FileFilter filter;
    private final FileFilter classFilter;

    public ClassPathArgumentType(@NotNull File root, @NotNull String suffix, @NotNull FileFilter filter) {
        super(ArgumentTypes.ofWord());
        this.root = root;
        this.suffix = suffix;
        this.filter = f -> filter.accept(f) && validate(f.getName()) && (f.isDirectory() || f.getName().endsWith(suffix));
        this.classFilter = f -> !f.isDirectory() && f.getName().endsWith(suffix);
    }

    public ClassPathArgumentType(@NotNull File root, @NotNull String suffix) {
        this(root, suffix, f -> true);
    }

    public ClassPathArgumentType(@NotNull File root) {
        this(root, ".class", f -> true);
    }


    @Override
    public File[] parse(StringReader reader) throws CommandSyntaxException {
        if (!root.exists()) return new File[0];

        String source = StringUtil.readString(reader.getRemaining());

        if (!validate(source)) throw createException(reader, INVALID_NAME);

        String str = source.replace('.', '/');

        Set<File> f = new HashSet<>();
        if (str.equals("*")) {
            File[] files = root.listFiles(ff -> classFilter.accept(ff) && filter.accept(ff));
            if (files != null) f.addAll(Arrays.asList(files));
        } else if (str.equals("**")) {
            File[] files = FileUtil.listFilesRecursively(root, ff -> classFilter.accept(ff) && filter.accept(ff), false);
            f.addAll(Arrays.asList(files));
        } else if (str.endsWith("/*")) {
            File file = new File(root, str.substring(0, str.length() - 1));

            if (!file.exists()) throw createException(reader, NOT_EXIST, source);

            File[] files = file.listFiles(ff -> classFilter.accept(ff) && filter.accept(ff));

            if (files != null)
                f.addAll(Arrays.asList(files));
        } else if (str.endsWith("/**")) {
            File file = new File(root, str.substring(0, str.length() - 2));

            if (!file.exists()) throw createException(reader, NOT_EXIST, str);

            File[] files = FileUtil.listFilesRecursively(file, ff -> classFilter.accept(ff) && filter.accept(ff), false);
            f.addAll(Arrays.asList(files));
        } else {
            str = str + suffix;
            File file = new File(root, str);

            if (!file.exists()) throw createException(reader, NOT_EXIST, str);

            if (classFilter.accept(file) && filter.accept(file)) f.add(file);
            else throw createException(reader, INVALID_NAME);
        }
        reader.setCursor(reader.getString().length());
        return f.toArray(new File[0]);
    }

    //Note: mess logic, easily broken
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!root.exists()) return builder.buildFuture();

        String string = StringUtil.readString(builder.getRemaining());

        String[] files = EMPTY;

        if (string.isEmpty() || string.charAt(string.length() - 1) == ' ') {
            File[] f = root.listFiles(filter);
            if (f != null) files = Arrays
                    .stream(f)
                    .map(this::toStringName)
                    .toArray(String[]::new);
            builder.suggest("*");
            builder.suggest("**");
        } else {
            int lastIndex = string.lastIndexOf('.');

            // string end with '.'
            if (lastIndex == string.length() - 1) {
                builder = builder.createOffset(builder.getInput().length());

                File sub = new File(root, string.substring(0, string.length() - 1).replace('.', '/'));
                if (sub.exists() && sub.isDirectory()) {
                    File[] f = sub.listFiles(filter);
                    if (f != null) {
                        files = Arrays
                                .stream(f)
                                .map(this::toStringName)
                                .toArray(String[]::new);
                        builder.suggest("*");
                        builder.suggest("**");
                    }
                } else {
                    return builder.buildFuture();
                }
            } else {
                // string without '.'
                if (lastIndex == -1) {
                    builder = builder.createOffset(builder.getStart());

                    String str = string.replace('.', '/');

                    File[] f = root.listFiles(ff -> ff.getName().startsWith(str) && filter.accept(ff));
                    if (f != null) files = Arrays
                            .stream(f)
                            .map(this::toStringName)
                            .toArray(String[]::new);
                } else {        // string with '.'
                    builder = builder.createOffset(builder.getStart() + lastIndex + 1);
                    String str = string.substring(lastIndex + 1).replace('.', '/');

                    File sub = new File(root, string.substring(0, lastIndex));

                    if (sub.exists() && sub.isDirectory()) {
                        File[] f = sub.listFiles(ff -> ff.getName().startsWith(str) && filter.accept(ff));
                        if (f != null) files = Arrays
                                .stream(f)
                                .map(this::toStringName)
                                .toArray(String[]::new);
                    }
                }
            }
        }

        for (String f : files) {
            builder.suggest(f);
        }

        return builder.buildFuture();
    }

    private static boolean validate(String clsName) {
        return StringUtil.containsNone(clsName, CLASSFILE_ILLEGAL_CHARS);
    }

    private String toStringName(File f) {
        String name = f.getName();
        return name.endsWith(suffix) ? name.substring(0, name.length() - suffix.length()) : name;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
