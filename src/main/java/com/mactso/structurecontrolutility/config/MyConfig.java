package com.mactso.structurecontrolutility.config;

import org.apache.commons.lang3.tuple.Pair;

import com.mactso.structurecontrolutility.Main;
import com.mactso.structurecontrolutility.utility.Utility;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Main.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig {
	
	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	public static int TICKS_PER_MINUTE = 1200;

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
	
	public static int getEffectsMinutes() {
		return structureEffectsMinutes;
	}

	public static long getStructureEffectsTicks() {
		return (long)structureEffectsMinutes*TICKS_PER_MINUTE;
	}
	
	public static int getStopFireMinutes() {
		return stopFireMinutes;
	}

	public static int getStopBreakingMinutes() {
		return stopBreakMinutes;
	}

	public static int getStopExplosionMinutes() {
		return stopExplosionMinutes;
	}

	public static long getStopBreakingTicks() {
		return (long)stopBreakMinutes*TICKS_PER_MINUTE;
	}

	public static long getStopFireTicks() {
		return (long)stopFireMinutes*TICKS_PER_MINUTE;
	}
	public static long getStopExplosionTicks() {
		return (long)stopExplosionMinutes*TICKS_PER_MINUTE;
	}

	private static int    debugLevel;
	private static int    structureEffectsMinutes;
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
		Utility.debugMsg(1, "Structure Control Utility Debug Level:"+MyConfig.debugLevel);
		COMMON.debugLevel.set( MyConfig.debugLevel);
	}

	public static void bakeConfig()
	{
		if (debugLevel > 0) {
			System.out.println("Structure Control Utility Debug: " + debugLevel );
		}
		debugLevel = COMMON.debugLevel.get();
		structureEffectsMinutes = COMMON.structureEffectsMinutes.get();
		stopBreakMinutes = COMMON.stopBreakMinutes.get();
		stopFireMinutes = COMMON.stopFireMinutes.get();
		stopExplosionMinutes = COMMON.stopExplosionMinutes.get();
		debugLevel = COMMON.debugLevel.get();
	}
	
	public static class Common {

		public final IntValue     debugLevel;
		public final IntValue     structureEffectsMinutes;
		public final IntValue     stopFireMinutes;
		public final IntValue     stopBreakMinutes;
		public final IntValue     stopExplosionMinutes;		
		
		public Common(ForgeConfigSpec.Builder builder) {
			builder.push("Structure Control Utility control Values");
			
			debugLevel = builder
					.comment("Debug Level: 0 = Off, 1 = Log, 2 = Chat+Log")
					.translation(Main.MODID + ".config." + "debugLevel")
					.defineInRange("debugLevel", () -> 0, 0, 2);
			
			structureEffectsMinutes = builder
					.comment("Minutes before structure potion effects end.")
					.translation(Main.MODID + ".config." + "structureEffectsMinutes")
					.defineInRange("structureEffectsMinutes", () -> 10080, 0, Integer.MAX_VALUE);
			
			stopFireMinutes = builder
					.comment("Default minutes before structures can burn")
					.translation(Main.MODID + ".config." + "stopFireMinutes ")
					.defineInRange("stopFireMinutes ", () -> 10080, 0, Integer.MAX_VALUE);
			
			stopBreakMinutes = builder
					.comment("Default minutes before entities can break blocks")
					.translation(Main.MODID + ".config." + "stopBreakMinutes")
					.defineInRange("stopBreakMinutes", () -> 720, 0, Integer.MAX_VALUE);

			stopExplosionMinutes = builder
					.comment("Default minutes before explosions can break blocks")
					.translation(Main.MODID + ".config." + "stopExplosionMinutes")
					.defineInRange("stopExplosionMinutes", () -> 720, 0, Integer.MAX_VALUE);
			
			builder.pop();			
		}
	}
}
