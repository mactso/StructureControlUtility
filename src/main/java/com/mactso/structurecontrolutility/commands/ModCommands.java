package com.mactso.structurecontrolutility.commands;

import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.managers.StructureManager;
import com.mactso.structurecontrolutility.managers.StructureManager.StructureItem;
import com.mactso.structurecontrolutility.utility.Utility;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ModCommands {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("structurecontrolutility").requires((source) -> {
			return source.hasPermission(2);
		}).then(Commands.literal("setDebugLevel")
				.then(Commands.argument("debugLevel", IntegerArgumentType.integer(0, 2)).executes(ctx -> {
					return setDebugLevel(IntegerArgumentType.getInteger(ctx, "debugLevel"));
				}))).then(Commands.literal("info").executes(ctx -> {
					ServerPlayer p = ctx.getSource().getPlayerOrException();
					doReport(p);
					return 1;
				})));

	}

	public static int setDebugLevel(int newDebugLevel) {
		MyConfig.setDebugLevel(newDebugLevel);
		MyConfig.pushDebugValue();
		return 1;
	}

	public static int doReport(ServerPlayer sp) {

		Utility.sendChat( sp, "\nStructure Control Info\n", ChatFormatting.DARK_GREEN);

		String key = StructureManager.insideStructure(sp.level(), sp.blockPosition());
		if (key == null) {
			Utility.sendChat( sp, "You are not inside a Structure.", ChatFormatting.GREEN);
			return 1;
		}

		ChunkAccess chunk = sp.level().getChunk(sp.blockPosition());

		long ageInTicks = chunk.getInhabitedTime();
		long ageInMinutes = ageInTicks / Utility.TICKS_PER_MINUTE;
		String chatMessage = "";

		if (key != null) {
			StructureItem si = StructureManager.getStructureItemOrDefault(key);

			chatMessage = "You are inside a " + key + " structure which is " + ageInMinutes + " minutes old.\n";
			int len = chatMessage.length();
			
			if (si.getStopFireMinutes() - ageInMinutes > 0) {
				chatMessage += "\n It is Protected From Burning for " + (si.getStopFireMinutes() - ageInMinutes)
						+ " more minutes.";
			}
			
			if (si.getStopBreakingMinutes() - ageInMinutes > 0) {
				chatMessage += "\n It is Protected From Digging for " + (si.getStopBreakingMinutes() - ageInMinutes)
						+ " minutes.";
			}
			
			if (si.getStopExplosionsMinutes() - ageInMinutes > 0) {
				chatMessage += "\n It is Protected From Exploding for " + (si.getStopExplosionsMinutes() - ageInMinutes)
						+ " minutes.";
			}
			
			if (chatMessage.length() == len) {
				chatMessage += "\nIt is not protected from destruction.";
			}

		}
		Utility.sendChat( sp, chatMessage, ChatFormatting.GREEN);
		return 1;
	}
}
