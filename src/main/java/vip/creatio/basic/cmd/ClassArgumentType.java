package vip.creatio.basic.cmd;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.NotNull;
import vip.creatio.basic.SharedConstants;
import vip.creatio.basic.chat.Component;
import vip.creatio.common.util.ReflectUtil;
import vip.creatio.common.util.StringUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class ClassArgumentType extends ExternArgumentType<Class[]> {
    private static final Collection<String> EXAMPLES = Arrays.asList("lang.java.*", "com.example.Class", "vip.creatio.basic.**");

    public static final DynamicCommandExceptionType CLASS_NOT_FOUND = new DynamicCommandExceptionType(o -> Component.of("Cannot find class '" + o + "'"));
    public static final SimpleCommandExceptionType INVALID_NAME = new SimpleCommandExceptionType(Component.of("Class name contains illegal character"));
    public static final SimpleCommandExceptionType SINGLE_ONLY = new SimpleCommandExceptionType(Component.of("Only one class allowed here"));

    private final Collection<Class<?>> range;
    private final boolean selectMultiple;
    private final boolean getAssociated;

    public ClassArgumentType(@NotNull Collection<Class<?>> range, boolean selectMultiple, boolean getAssociated) {
        super(ArgumentTypes.ofWord());
        this.range = range;
        this.selectMultiple = selectMultiple;
        this.getAssociated = getAssociated;
    }

    public ClassArgumentType(@NotNull Collection<Class<?>> range, boolean getAssociated) {
        this(range, true, getAssociated);
    }

    public ClassArgumentType(@NotNull Collection<Class<?>> range) {
        this(range, true, true);
    }


    @Override
    public Class<?>[] parse(StringReader reader) throws CommandSyntaxException {

        String str = StringUtil.readString(reader.getRemaining());

        if (StringUtil.containsAny(str, SharedConstants.CLASSFILE_ILLEGAL_CHARS))
            throw createException(reader, INVALID_NAME);

        Set<Class<?>> f = new HashSet<>();
        if (str.equals("*")) {
            if (!selectMultiple) throw createException(reader, SINGLE_ONLY);
            f.addAll(range);
        } else if (str.endsWith(".*")) {
            if (!selectMultiple) throw createException(reader, SINGLE_ONLY);
            int lastIndex = str.lastIndexOf('.');
            String base = str.substring(0, lastIndex + 1);
            f.addAll(range.stream().filter(c -> c.getTypeName().startsWith(base) && c.getTypeName().lastIndexOf('.') == lastIndex).collect(Collectors.toList()));
        } else if (str.endsWith(".**")) {
            if (!selectMultiple) throw createException(reader, SINGLE_ONLY);
            String base = str.substring(0, str.length() - 2);
            f.addAll(range.stream().filter(c -> c.getTypeName().startsWith(base)).collect(Collectors.toList()));
        } else {
            final String finalStr = str;
            if (getAssociated) {
                f.addAll(range.stream().filter(c -> c.getTypeName().equals(finalStr)).collect(Collectors.toList()));
            } else {
                f.addAll(range.stream().filter(c -> !c.getTypeName().contains("$") && c.getTypeName().equals(finalStr)).collect(Collectors.toList()));
            }
            if (f.size() == 0) throw createException(reader, CLASS_NOT_FOUND, str);
        }
        reader.setCursor(reader.getString().length());
        return f.toArray(new Class[0]);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        String string = StringUtil.readString(builder.getRemaining());
        if (string.startsWith("\"")) {
            builder = builder.createOffset(builder.getStart() + 1);
            string = string.substring(1);
        }
        List<String> cache = range.stream().map(Class::getTypeName).collect(Collectors.toList());

        if (string.isEmpty()) {
            cache.stream().filter(s -> !s.contains("$")).forEach(builder::suggest);
            builder.suggest("*");
        } else if (string.charAt(string.length() - 1) == ' ') {
            builder = builder.createOffset(builder.getInput().length());
            cache.stream().filter(s -> !s.contains("$")).forEach(builder::suggest);
            builder.suggest("*");
        } else {
            final String temp = string;
            cache.stream().filter(s -> s.startsWith(temp) && !s.contains("$")).forEach(builder::suggest);

            if (selectMultiple && temp.endsWith(".")) {
                builder.suggest(temp + "*");
                builder.suggest(temp + "**");
            }
        }
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
