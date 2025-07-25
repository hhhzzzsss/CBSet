package com.github.hhhzzzsss.cbset;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ClientBlockPosArgument implements ArgumentType<BlockPos> {
    private static final Collection<String> EXAMPLES = List.of("0 0 0");

    public static ClientBlockPosArgument blockPos() {
        return new ClientBlockPosArgument();
    }

    public BlockPos parse(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();

        int x = stringReader.readInt();
        if (stringReader.canRead() && stringReader.peek() == ' ') {
            stringReader.skip();
        }
        else {
            stringReader.setCursor(i);
            CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument();
        }
        int y = stringReader.readInt();
        if (stringReader.canRead() && stringReader.peek() == ' ') {
            stringReader.skip();
        }
        else {
            stringReader.setCursor(i);
            CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument();
        }
        int z = stringReader.readInt();

        return new BlockPos(x, y, z);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        if (!(commandContext.getSource() instanceof SharedSuggestionProvider)) return Suggestions.empty();

        String string = suggestionsBuilder.getRemaining();
        Collection<SharedSuggestionProvider.TextCoordinates> collection = ((SharedSuggestionProvider) commandContext.getSource()).getRelevantCoordinates();
        return SharedSuggestionProvider.suggestCoordinates(string, collection, suggestionsBuilder, Commands.createValidator(this::parse));
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
