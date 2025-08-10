package com.github.hhhzzzsss.cbset;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandBlockModeArgument implements ArgumentType<NamedMode> {
    private static final Collection<String> EXAMPLES = (Collection<String>) Stream.of(NamedMode.IMPULSE, NamedMode.CHAIN, NamedMode.REPEATING)
            .map(NamedMode::getName)
            .collect(Collectors.toList());
    private static final NamedMode[] VALUES = NamedMode.values();

    public NamedMode parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        NamedMode mode = NamedMode.getByName(string);
        if (mode == null) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        } else {
            return mode;
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        if (!(commandContext.getSource() instanceof SharedSuggestionProvider)) return Suggestions.empty();

        return SharedSuggestionProvider.suggest(Arrays.stream(VALUES).map(NamedMode::getName), suggestionsBuilder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static CommandBlockModeArgument commandBlockMode() {
        return new CommandBlockModeArgument();
    }
}
