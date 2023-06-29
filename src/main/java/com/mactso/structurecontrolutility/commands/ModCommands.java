package com.mactso.structurecontrolutility.commands;


import com.mactso.structurecontrolutility.config.MyConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;



public class ModCommands {
	String subcommand = "";
	String value = "";

	
	public static void register(CommandDispatcher<CommandSource> dispatcher)
	{
		dispatcher.register(Commands.literal("happytrails").requires((source) -> 
			{
				return source.hasPermission(2);
			}
		)
		.then(Commands.literal("debugLevel").then(
				Commands.argument("debugLevel", IntegerArgumentType.integer(0,2)).executes(ctx -> {
					return setDebugLevel(IntegerArgumentType.getInteger(ctx, "debugLevel"));
			}
			)
			)
			)
		);

	}
	
	public static int setDebugLevel (int newDebugLevel) {
		MyConfig.setDebugLevel(newDebugLevel);
		MyConfig.pushDebugValue();
		return 1;
	}
}
