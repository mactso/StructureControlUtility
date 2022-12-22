package com.mactso.structurecontrolutility.config;

import org.apache.commons.lang3.tuple.Pair;

import com.mactso.structurecontrolutility.Main;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig {
	
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	public static int TICKS_PER_MINUTE;

	static
	{
		final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
		COMMON_SPEC = specPair.getRight();
		COMMON = specPair.getLeft();
	}	
	
	public static int getDebugLevel() {
		return debugLevel;
	}
	
	public static void setDebugLevel(int i) {
		debugLevel = i; // note: doesn't save to config file- intentionally temporary
	}
	
	public static long getStopBreakTicks() {
		return (long)stopBreakMinutes*TICKS_PER_MINUTE;
	}

	public static long getStopFireTicks() {
		return (long)stopFireMinutes*TICKS_PER_MINUTE;
	}
	public static long getStopExplosionTicks() {
		return (long)stopExplosionMinutes*TICKS_PER_MINUTE;
	}

	private static int    debugLevel;
	private static int    stopFireMinutes;
	private static int    stopBreakMinutes;
	private static int    stopExplosionMinutes;		
	
	@SubscribeEvent
	public static void onModConfigEvent(final ModConfigEvent configEvent)
	{
		if (configEvent.getConfig().getSpec() == MyConfig.COMMON_SPEC)
		{
			bakeConfig();
		}
	}	

	public static void pushDebugValue() {
		if (debugLevel > 0) {
			System.out.println("Happy Trails Debug Level:"+MyConfig.debugLevel);
		}
		COMMON.debugLevel.set( MyConfig.debugLevel);
	}

	public static void bakeConfig()
	{
		debugLevel = COMMON.debugLevel.get();
		stopBreakMinutes = COMMON.stopBreakMinutes.get();
		stopFireMinutes = COMMON.stopFireMinutes.get();
		stopExplosionMinutes = COMMON.stopExplosionMinutes.get();
		debugLevel = COMMON.debugLevel.get();
		if (debugLevel > 0) {
			System.out.println("Happy Trails Debug: " + debugLevel );
		}
	}
	
	public static class Common {

		public final IntValue     debugLevel;
		public final IntValue     stopFireMinutes;
		public final IntValue     stopBreakMinutes;
		public final IntValue     stopExplosionMinutes;		
		
		public Common(ForgeConfigSpec.Builder builder) {
			builder.push("Happy Trail Control Values");
			
			debugLevel = builder
					.comment("Debug Level: 0 = Off, 1 = Log, 2 = Chat+Log")
					.translation(Main.MODID + ".config." + "debugLevel")
					.defineInRange("debugLevel", () -> 0, 0, 2);

			stopFireMinutes = builder
					.comment("how many minutes before structures can burn")
					.translation(Main.MODID + ".config." + "stopFireMinutes ")
					.defineInRange("stopFireMinutes ", () -> 10080, 0, Integer.MAX_VALUE);
			
			stopBreakMinutes = builder
					.comment("how many minutes before entities can break blocks")
					.translation(Main.MODID + ".config." + "stopBreakMinutes")
					.defineInRange("stopBreakMinutes", () -> 720, 0, Integer.MAX_VALUE);

			stopExplosionMinutes = builder
					.comment("how many minutes before explosions can break blocks")
					.translation(Main.MODID + ".config." + "stopExplosionMinutes")
					.defineInRange("stopExplosionMinutes", () -> 720, 0, Integer.MAX_VALUE);
			
			builder.pop();			
		}
	}
}
