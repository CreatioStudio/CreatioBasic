package vip.creatio.basic.cmd;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.*;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import vip.creatio.accessor.Reflection;
import vip.creatio.accessor.Var;
import vip.creatio.basic.tools.Wrapper;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Any external custom argument type must inherit from this class.
 *
 * The field "raw" must be a registered type in {@see ArgumentRegistry}
 *
 * To make listSuggestions work, ExArgumentCommandNode will set suggestionProvider to this.
 */
@SuppressWarnings("unchecked")
public abstract class ExternArgumentType<T> implements ArgumentType<T>, Wrapper<ArgumentType<?>>, SuggestionProvider<T> {

    protected final ArgumentType<?> raw;

    protected ExternArgumentType(ArgumentType<?> raw) {
        this.raw = raw;
    }

    @Override
    public T parse(StringReader stringReader) throws CommandSyntaxException {
        return (T) raw.parse(stringReader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return raw.listSuggestions(context, builder);
    }

    @Override
    public final CompletableFuture<Suggestions> getSuggestions(CommandContext<T> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return listSuggestions(commandContext, suggestionsBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return raw.getExamples();
    }

    @Override
    public ArgumentType<?> unwrap() {
        return raw;
    }

    @Override
    public Class<? extends ArgumentType<?>> wrappedClass() {
        return (Class<? extends ArgumentType<?>>) raw.getClass();
    }

    protected static final Var<Message> SIMPLE_MESSAGE_VAR = Reflection.field(SimpleCommandExceptionType.class, "message");
    protected SyntaxException createException(ImmutableStringReader reader, SimpleCommandExceptionType type) {
        return new SyntaxException(type, SIMPLE_MESSAGE_VAR.get(type), reader.getString(), reader.getCursor());
    }

    protected static final Var<Function<Object, Message>> DYNAMIC_MESSAGE_VAR = Reflection.field(DynamicCommandExceptionType.class, "function");
    protected SyntaxException createException(ImmutableStringReader reader, DynamicCommandExceptionType type, Object o1) {
        return new SyntaxException(type, DYNAMIC_MESSAGE_VAR.get(type).apply(o1), reader.getString(), reader.getCursor(), o1);
    }

    protected static final Var<Dynamic2CommandExceptionType.Function> DYNAMIC_2_MESSAGE_VAR = Reflection.field(Dynamic2CommandExceptionType.class, "function");
    protected SyntaxException createException(ImmutableStringReader reader, Dynamic2CommandExceptionType type, Object o1, Object o2) {
        return new SyntaxException(type, DYNAMIC_2_MESSAGE_VAR.get(type).apply(o1, o2), reader.getString(), reader.getCursor(), o1, o2);
    }

    protected static final Var<Dynamic3CommandExceptionType.Function> DYNAMIC_3_MESSAGE_VAR = Reflection.field(Dynamic3CommandExceptionType.class, "function");
    protected SyntaxException createException(ImmutableStringReader reader, Dynamic3CommandExceptionType type, Object o1, Object o2, Object o3) {
        return new SyntaxException(type, DYNAMIC_3_MESSAGE_VAR.get(type).apply(o1, o2, o3), reader.getString(), reader.getCursor(), o1, o2, o3);
    }

    protected static final Var<Dynamic4CommandExceptionType.Function> DYNAMIC_4_MESSAGE_VAR = Reflection.field(Dynamic4CommandExceptionType.class, "function");
    protected SyntaxException createException(ImmutableStringReader reader, Dynamic4CommandExceptionType type, Object o1, Object o2, Object o3, Object o4) {
        return new SyntaxException(type, DYNAMIC_4_MESSAGE_VAR.get(type).apply(o1, o2, o3, o4), reader.getString(), reader.getCursor(), o1, o2, o3, o4);
    }

    protected static final Var<DynamicNCommandExceptionType.Function> DYNAMIC_N_MESSAGE_VAR = Reflection.field(DynamicNCommandExceptionType.class, "function");
    protected SyntaxException createException(ImmutableStringReader reader, DynamicNCommandExceptionType type, Object... objects) {
        return new SyntaxException(type, DYNAMIC_N_MESSAGE_VAR.get(type).apply(objects), reader.getString(), reader.getCursor(), objects);
    }

}
