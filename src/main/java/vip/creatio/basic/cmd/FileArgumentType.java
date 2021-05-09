package vip.creatio.basic.cmd;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;
import vip.creatio.basic.chat.Component;
import vip.creatio.common.util.FileUtil;
import vip.creatio.common.util.IndexPair;
import vip.creatio.common.util.StringUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FileArgumentType extends ExternArgumentType<File[]> {
    private static final String[] EMPTY = new String[0];

    private static final Collection<String> EXAMPLES = Arrays.asList("*", "/abc.c", "/tRna/agct.hpp");

    public static final DynamicCommandExceptionType NOT_EXIST = new DynamicCommandExceptionType(o -> Component.of("File '" + o + "' not exist"));
    public static final SimpleCommandExceptionType SINGLE_ONLY = new SimpleCommandExceptionType(Component.of("Only one file allowed here"));

    private final File parent;
    private final FileFilter filter;
    private final boolean includeDir;
    private final boolean singleOnly;

    public FileArgumentType(@NotNull File parent, @NotNull FileFilter filter, boolean includeDir, boolean singleOnly) {
        super(ArgumentTypes.ofString());
        this.parent = parent;
        this.filter = filter;
        this.includeDir = includeDir;
        this.singleOnly = singleOnly;
    }

    public FileArgumentType(File parent, FileFilter filter, boolean includeDir) {
        this(parent, filter, includeDir, false);
    }

    public FileArgumentType(@NotNull File parent, boolean includeDir) {
        this(parent, f -> true, includeDir);
    }

    public FileArgumentType(@NotNull File parent) {
        this(parent, f -> true, false);
    }


    @Override
    public File[] parse(StringReader reader) throws CommandSyntaxException {
        if (!parent.exists()) return new File[0];

        String str = StringUtil.readString(reader.getRemaining());

        Set<File> f = new HashSet<>();
        if (str.equals("*")) {
            if (singleOnly) throw createException(reader, SINGLE_ONLY);
            File[] files = parent.listFiles(filter);
            if (files != null) f.addAll(Arrays.asList(files));
        } else if (str.endsWith("/")) {
            if (singleOnly) throw createException(reader, SINGLE_ONLY);
            File file = new File(parent, str.substring(0, str.length() - 1));

            if (!file.exists()) throw createException(reader, NOT_EXIST, str);

            File[] files;
            if (includeDir) {
                files = file.listFiles(filter);
            } else {
                files = file.listFiles(fl -> filter.accept(fl) && !fl.isDirectory());
            }

            if (files != null)
                f.addAll(Arrays.asList(files));
        } else if (str.endsWith("/*")) {
            if (singleOnly) throw createException(reader, SINGLE_ONLY);
            File file = new File(parent, str.substring(0, str.length() - 2));

            if (!file.exists()) throw createException(reader, NOT_EXIST, str);

            File[] files = FileUtil.listFilesRecursively(file, filter, includeDir);
            f.addAll(Arrays.asList(files));
        } else {
            File file = new File(parent, str);

            if (!file.exists()) throw createException(reader, NOT_EXIST, str);

            if (includeDir || !file.isDirectory()) f.add(file);
        }
        reader.setCursor(reader.getString().length());
        return f.toArray(new File[0]);
    }

    //Note: mess logic, easily broken
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!parent.exists()) return builder.buildFuture();

        String string = StringUtil.readString(builder.getRemaining());
        boolean inQuote = !string.isEmpty() && builder.getRemaining().startsWith("\"");

        String[] files = EMPTY;

        if (string.isEmpty() || (!inQuote && string.charAt(string.length() - 1) == ' ')) {
            File[] f = parent.listFiles(filter);
            if (f != null) files = Arrays.stream(f).map(File::getName).map(s -> s.contains(" ") ? "\"" + s : s).toArray(String[]::new);
        } else {
            int lastIndex = string.lastIndexOf('/');

            // string end with '/'
            if (lastIndex == string.length() - 1) {
                builder = builder.createOffset(builder.getInput().length());

                File sub = new File(parent, string.substring(0, string.length() - 1));
                if (sub.exists() && sub.isDirectory()) {
                    File[] f = sub.listFiles(filter);
                    if (f != null) {
                        files = Arrays.stream(f).map(File::getName).toArray(String[]::new);
                        if (!singleOnly) builder.suggest("*");
                    }
                } else {
                    return builder.buildFuture();
                }
            } else {
                // string without '/'
                if (lastIndex == -1) {
                    builder = builder.createOffset(builder.getStart());

                    File[] f = parent.listFiles(ff -> ff.getName().startsWith(string) && filter.accept(ff));
                    if (f != null) files = Arrays.stream(f).map(File::getName).map(s -> s.contains(" ") ? "\"" + s : s).toArray(String[]::new);
                } else {        // string with '/'
                    builder = builder.createOffset(builder.getStart() + lastIndex + (inQuote ? 2 : 1));
                    String post = string.substring(lastIndex + 1);

                    File sub = new File(parent, string.substring(0, lastIndex));

                    if (sub.exists() && sub.isDirectory()) {
                        File[] f = sub.listFiles(ff -> ff.getName().startsWith(post) && filter.accept(ff));
                        if (f != null) files = Arrays.stream(f).map(File::getName).toArray(String[]::new);
                    }
                }
            }
        }

        for (String f : files) {
            builder.suggest(f);
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
