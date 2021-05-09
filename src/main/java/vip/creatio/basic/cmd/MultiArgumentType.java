package vip.creatio.basic.cmd;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import vip.creatio.basic.chat.Component;
import vip.creatio.common.util.FileUtil;
import vip.creatio.common.util.IndexPair;
import vip.creatio.common.util.StringUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class MultiArgumentType<T> extends ExternArgumentType<List<T>> {
    private static final Collection<String> EXAMPLES = Arrays.asList("xxx xxx");

    private final ArgumentType<T> type;
    private final int maxCount;
    private final int minCount;

    /** @param maxCount set to -1 to give unlimited arguments */
    public MultiArgumentType(ArgumentType<T> type, int minCount, int maxCount) {
        super(ArgumentTypes.ofGreedyString());
        this.type = type;
        this.maxCount = maxCount;
        this.minCount = minCount;
    }

    public MultiArgumentType(ArgumentType<T> type) {
        this(type, -1, -1);
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<T> parse(StringReader reader) throws CommandSyntaxException {
        int baseIndex = reader.getCursor();
        IndexPair<String>[] split = StringUtil.indexSplit(reader.getRemaining(), true);
        reader.setCursor(reader.getString().length());

        if (maxCount > 0 && split.length > maxCount) throw new CommandSyntaxException(new CommandExceptionType(){}, Component.of("Too many arguments! Expected " + maxCount),
                reader.getString(), baseIndex);
        if (minCount > 0 && split.length < minCount) throw new CommandSyntaxException(new CommandExceptionType(){}, Component.of("Not enough arguments! Expected " + maxCount),
                reader.getString(), baseIndex);

        Set<T> set = new HashSet<>();

        for (IndexPair<String> pair : split) {
            reader.setCursor(baseIndex + pair.getIndex());
            set.add(type.parse(reader));
        }

        reader.setCursor(reader.getTotalLength());

        return new ArrayList<>(set);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        int baseOffset = builder.getStart();
        IndexPair<String>[] split = StringUtil.indexSplit(remaining, true);
        if (maxCount > 0 && split.length > maxCount) return builder.buildFuture();

        if (split.length == 0) {
            return type.listSuggestions(context, builder);
        } else if (remaining.charAt(remaining.length() - 1) == ' ') {
            builder = builder.createOffset(builder.getInput().length());
            return type.listSuggestions(context, builder);
        } else {
            IndexPair<String> pair = split[split.length - 1];
            int offset = pair.getIndex();

            builder = builder.createOffset(baseOffset + offset);

            return type.listSuggestions(context, builder);
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
