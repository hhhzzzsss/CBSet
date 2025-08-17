package com.github.hhhzzzsss.cbset;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CBSet implements ModInitializer {
	public static final String MOD_ID = "cbset";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(getCBSetCommand());
			dispatcher.register(getCDialogCommand(registryAccess));
			dispatcher.register((getCSetBookCommand()));
		});

		ClientReceiveMessageEvents.ALLOW_GAME.register((component, overlay) -> filterMessage(component));
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> getCBSetCommand() {
		return ClientCommandManager.literal("cbset").then(
			ClientCommandManager.argument("position", ClientBlockPosArgument.blockPos()).then(
				ClientCommandManager.argument("mode", CommandBlockModeArgument.commandBlockMode()).then(
					ClientCommandManager.argument("auto", BoolArgumentType.bool()).then(
						ClientCommandManager.argument("conditional", BoolArgumentType.bool()).then(
							ClientCommandManager.argument("trackOutput", BoolArgumentType.bool()).then(
								ClientCommandManager.argument("command", StringArgumentType.greedyString()).executes( ctx -> {
									setCommandBlock(
										ctx.getArgument("position", BlockPos.class),
										ctx.getArgument("command", String.class),
										ctx.getArgument("mode", NamedMode.class).getMode(),
										ctx.getArgument("trackOutput", Boolean.class),
										ctx.getArgument("conditional", Boolean.class),
										ctx.getArgument("auto", Boolean.class)
									);
									return 1;
								})
							)
						)
					)
				)
			)
		);
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> getCSetBookCommand() {
		return ClientCommandManager.literal("csetbook").then(
				ClientCommandManager.argument("slot", IntegerArgumentType.integer(0, 8)).then(
						ClientCommandManager.argument("data", StringArgumentType.greedyString()).executes(ctx -> {
							setBook(
									ctx.getArgument("slot", Integer.class),
									ctx.getArgument("data", String.class)
							);
							return 1;
						})
				)
		);
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> getCDialogCommand(CommandBuildContext registryAccess) {
		return ClientCommandManager.literal("cdialog").then(
			ClientCommandManager.argument("dialog", ResourceOrIdArgument.dialog(registryAccess)).executes( ctx -> {
				Holder<Dialog> dialog = ctx.getArgument("dialog", Holder.class);
				sendDialog(dialog);
				return 1;
			})
		);
	}

	private void setCommandBlock(BlockPos bp, String command, CommandBlockEntity.Mode mode, boolean trackOutput, boolean conditional, boolean automatic) {
		Minecraft.getInstance().getConnection().send(
			new ServerboundSetCommandBlockPacket(bp, command, mode, trackOutput, conditional, automatic)
		);
	}

	private void setBook(int slot, String text) {
		text = escapeBookText(text);
		List<String> pages = Arrays.asList(text.split("\f"));
		Minecraft.getInstance().getConnection().send(
				new ServerboundEditBookPacket(slot, pages, Optional.empty())
		);

	}

	private String escapeBookText(String raw) {
		StringBuilder sb = new StringBuilder();
		boolean escape = false;

		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);
			if (escape) {
				switch (c) {
					case '&': sb.append('&'); break;
					case 'n': sb.append('\n'); break;
					case 'f': sb.append('\f'); break;
					case '\\': sb.append('\\'); break;
					default:
						sb.append('\\').append(c);
						break;
				}
				escape = false;
			} else {
				if (c == '\\') {
					escape = true;
				} else if (c == '&') {
					sb.append('§');
				} else {
					sb.append(c);
				}
			}
		}

		// Handle trailing backslash
		if (escape) {
			sb.append('\\');
		}

		return sb.toString();
	}

	private void sendDialog(Holder<Dialog> dialog) {
		Minecraft.getInstance().schedule(() -> Minecraft.getInstance().getConnection().showDialog(dialog, Minecraft.getInstance().screen));
	}

	private boolean filterMessage(Component component) {
		if (component.getContents() instanceof TranslatableContents contents) {
			return !contents.getKey().equals("advMode.setCommand.success");
		}
		return true;
	}
}