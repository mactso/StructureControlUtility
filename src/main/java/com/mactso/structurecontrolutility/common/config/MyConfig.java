package com.mactso.structurecontrolutility.common.config;

import org.apache.commons.lang3.tuple.Pair;

import com.mactso.structurecontrolutility.common.utility.MyUtilities;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * 
 * Forge COMMON config holder for Structure Control Utility.
 * 
 * <p>
 * Defines server-side configuration values and maintains a baked, static
 * 
 * snapshot of config state for fast runtime access. Values are synchronized
 * 
 * from disk via {@link ModConfigEvent} and exposed as cached primitives.
 * 
 * <p>
 * Note: Some fields (e.g. debug level) may be overridden at runtime without
 * 
 * persisting changes to the config file.
 */

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MyConfig {

	public static final Common COMMON;
	public static final ForgeConfigSpec COMMON_SPEC;
	public static final int TICKS_PER_MINUTE = 1200;

	private static int debugLevel;
	private static int structureEffectsMinutes;
	private static int stopFireMinutes;
	private static int stopBreakMinutes;
	private static int stopExplosionMinutes;
	private static int miningFatigueMinutes;
	private static int miningFatigueAmplifier;

	static {
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

	public static void pushDebugValue() {
		MyUtilities.debugMsg(1, "Structure Control Utility Debug Level:" + MyConfig.debugLevel);
		COMMON.debugLevel.set(MyConfig.debugLevel);
	}

	public static boolean isDebug() {
		if (debugLevel > 0)
			return true;
		return false;
	}

	public static int getEffectsMinutes() {
		return structureEffectsMinutes;
	}

	public static long getStructureEffectsTicks() {
		return (long) structureEffectsMinutes * TICKS_PER_MINUTE;
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
		return (long) stopBreakMinutes * TICKS_PER_MINUTE;
	}

	public static long getStopFireTicks() {
		return (long) stopFireMinutes * TICKS_PER_MINUTE;
	}

	public static long getStopExplosionTicks() {
		return (long) stopExplosionMinutes * TICKS_PER_MINUTE;
	}

	public static int getMiningFatigueMinutes() {
		return miningFatigueMinutes;
	}

	public static boolean isMiningFatigue() {
		if (miningFatigueMinutes > 0)
			return true;
		return false;
	}

	public static long getMiningFatigueTicks() {
		return (long) miningFatigueMinutes * TICKS_PER_MINUTE;
	}

	
	public static int getMiningFatigueLevel() {
		return miningFatigueAmplifier;
	}

	@SubscribeEvent
	public static void onModConfigEvent(final ModConfigEvent configEvent) {
		if (configEvent.getConfig().getSpec() == MyConfig.COMMON_SPEC) {
			bakeConfig();
		}
	}

	public static void bakeConfig() {

		structureEffectsMinutes = COMMON.structureEffectsMinutes.get();
		stopBreakMinutes = COMMON.stopBreakMinutes.get();
		stopFireMinutes = COMMON.stopFireMinutes.get();
		stopExplosionMinutes = COMMON.stopExplosionMinutes.get();
		miningFatigueMinutes = COMMON.miningFatigueMinutes.get();
		miningFatigueAmplifier = COMMON.miningFatigueAmplifier.get();
		debugLevel = COMMON.debugLevel.get();
		if (debugLevel > 0) {
			System.out.println("Structure Control Utility Debug: " + debugLevel);
		}

	}

	public static class Common {

		public final IntValue debugLevel;
		public final IntValue structureEffectsMinutes;
		public final IntValue stopFireMinutes;
		public final IntValue stopBreakMinutes;
		public final IntValue stopExplosionMinutes;
		public final IntValue     miningFatigueMinutes;
		public final IntValue     miningFatigueAmplifier;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.push("Structure Control Utility control Values");

			debugLevel = builder.comment("Debug Level: 0 = Off, 1 = Log, 2 = Chat+Log")
					.translation(Main.MODID + ".config." + "debugLevel").defineInRange("debugLevel", () -> 0, 0, 2);

			structureEffectsMinutes = builder.comment("Minutes before structure potion effects end.")
					.translation(Main.MODID + ".config." + "structureEffectsMinutes")
					.defineInRange("structureEffectsMinutes", () -> 10080, 0, Integer.MAX_VALUE);

			stopFireMinutes = builder.comment("Default minutes before structures can burn")
					.translation(Main.MODID + ".config." + "stopFireMinutes")
					.defineInRange("stopFireMinutes", () -> 10080, 0, Integer.MAX_VALUE);

			stopBreakMinutes = builder.comment("Default minutes before entities can break blocks")
					.translation(Main.MODID + ".config." + "stopBreakMinutes")
					.defineInRange("stopBreakMinutes", () -> 720, 0, Integer.MAX_VALUE);

			stopExplosionMinutes = builder.comment("Default minutes before explosions can break blocks")
					.translation(Main.MODID + ".config." + "stopExplosionMinutes")
					.defineInRange("stopExplosionMinutes", () -> 720, 0, Integer.MAX_VALUE);
			
            miningFatigueMinutes = builder.comment("Minutes Mining Fatigue (MINING_SLOWNESS) is applied. 0 = off.")
                    .translation(Main.MODID + ".config." + "miningFatigueMinutes")
                    .defineInRange("miningFatigueMinutes", () -> 10080, 0, Integer.MAX_VALUE);

            miningFatigueAmplifier = builder.comment("Mining Fatigue (MINING_SLOWNESS) amplifier. 1-5 valid.")
                    .translation(Main.MODID + ".config." + "miningFatigueAmplifier")
                    .defineInRange("miningFatigueAmplifier", () -> 1, 1, 5);

			builder.pop();
		}
	}
}
