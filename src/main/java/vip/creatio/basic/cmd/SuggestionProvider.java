package vip.creatio.basic.cmd;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface SuggestionProvider {

    CompletableFuture<Suggestions> getSuggestions(Context context, SuggestionsBuilder builder);

}
