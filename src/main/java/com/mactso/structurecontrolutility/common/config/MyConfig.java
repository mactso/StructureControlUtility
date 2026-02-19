package com.mactso.structurecontrolutility.common.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.mactso.structurecontrolutility.common.utility.MyUtilities;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
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

	private static boolean onlyServerMode;
	private static int debugLevel;
	private static String unprotectedStructuresString;
	private static String unprotectedStructureEffects;
	private static String protectedStructureEffects;

	private static int structureEffectsMinutes;
	private static int stopFireMinutes;
	private static int stopBreakMinutes;
	private static int stopExplosionMinutes;
	private static int miningFatigueMinutes;
	private static int miningFatigueAmplifier;

	private static float gloomRed;
	private static float gloomGreen;
	private static float gloomBlue;
	private static float gloomAlpha;  // how opaque is it?  Transparency value

	// Default percentages (0�100)
	public static final int DEFAULT_GLOOM_RED_PERCENT    = 2;  
	public static final int DEFAULT_GLOOM_GREEN_PERCENT  = 3;  
	public static final int DEFAULT_GLOOM_BLUE_PERCENT   = 5;  
	public static final int DEFAULT_GLOOM_ALPHA_PERCENT  = 81;  // 81% opaque, 19% transparent

	// minimums (percentages)
	public static final int MIN_GLOOM_RED_PERCENT   = 0;
	public static final int MIN_GLOOM_GREEN_PERCENT = 0;
	public static final int MIN_GLOOM_BLUE_PERCENT  = 0;
	public static final int MIN_GLOOM_ALPHA_PERCENT = 70;
	public static final int MAX_GLOOM_ALPHA_PERCENT = 95;
	
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

	public static String getUnprotectedStructuresAsString() {
		return unprotectedStructuresString;
	}

	public static List<String> getUnprotectedStructuresAsList() {
		String raw = getUnprotectedStructuresAsString();
		List<String> list = new ArrayList<>();

		if (raw == null || raw.isEmpty()) {
			return list; // empty list if nothing is set
		}

		String[] parts = raw.split(";");
		for (String part : parts) {
			part = part.trim();
			if (!part.isEmpty()) {
				list.add(part);
			}
		}
		return list;
	}

	public static String getProtectedStructuresEffects() {
		if (!protectedStructureEffects.matches("[0-9]{7}")) {
			MyUtilities.debugMsg(0, "Invalid protectedStructureEffects string '" + protectedStructureEffects
					+ "'. Reset to default 0000001.");
			protectedStructureEffects = "0000000";
		}
		return protectedStructureEffects;
	}

	public static String getUnprotectedStructuresEffects() {
		if (!unprotectedStructureEffects.matches("[0-9]{7}")) {
			MyUtilities.debugMsg(0, "Invalid unprotectedStructureEffects string '" + unprotectedStructureEffects
					+ "'. Reset to default 0000000.");
			unprotectedStructureEffects = "0000000";
		}
		return unprotectedStructureEffects;
	}

	public static int getStructureEffectsMinutes() {
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

		onlyServerMode = COMMON.onlyServerMode.get();
		debugLevel = COMMON.debugLevel.get();
		if (debugLevel > 0) {
			System.out.println("Structure Control Utility Debug: " + debugLevel);
		}

		unprotectedStructuresString = COMMON.unprotectedStructuresString.get();
		structureEffectsMinutes = COMMON.structureEffectsMinutes.get();
		protectedStructureEffects = COMMON.protectedStructureEffects.get();
		unprotectedStructureEffects = COMMON.UnprotectedStructureEffects.get();

		stopBreakMinutes = COMMON.stopBreakMinutes.get();
		stopFireMinutes = COMMON.stopFireMinutes.get();
		stopExplosionMinutes = COMMON.stopExplosionMinutes.get();
		
		miningFatigueMinutes = COMMON.miningFatigueMinutes.get();
		miningFatigueAmplifier = COMMON.miningFatigueAmplifier.get();
	
	
	    // --- Gloom fog color percentages (0�100) ---
	    gloomRed   = COMMON.gloomRed.get() / 100.0f;
	    gloomGreen = COMMON.gloomGreen.get() / 100.0f;
	    gloomBlue  = COMMON.gloomBlue.get() / 100.0f;
	    gloomAlpha = COMMON.gloomAlpha.get() / 100.0f;

	}



	// getters
	public static boolean isOnlyServerMode() {
		return onlyServerMode;
	}
	
	
	public static float getGloomRed() {
		return gloomRed;
	}

	public static float getGloomGreen() {
		return gloomGreen;
	}

	public static float getGloomBlue() {
		return gloomBlue;
		}

	public static float getGloomAlpha() {
		return gloomAlpha;
	}


	public static class Common {

		public final ForgeConfigSpec.BooleanValue onlyServerMode;
		public final IntValue debugLevel;
		public final ForgeConfigSpec.ConfigValue<String> unprotectedStructuresString;
		public final String defaultUnprotectedStructuresString = "minecraft:mineshaft;minecraft:mineshaft_mesa;minecraft:trail_ruins;"
				+ "minecraft:village_desert;minecraft:village_plains;minecraft:village_savanna;"
				+ "minecraft:village_snowy;minecraft:village_taiga";
		public final ForgeConfigSpec.ConfigValue<String> protectedStructureEffects;
		public final ForgeConfigSpec.ConfigValue<String> UnprotectedStructureEffects;
		public final IntValue structureEffectsMinutes;
		public final IntValue stopFireMinutes;
		public final IntValue stopBreakMinutes;
		public final IntValue stopExplosionMinutes;
		public final IntValue     miningFatigueMinutes;
		public final IntValue     miningFatigueAmplifier;
		public final IntValue gloomRed;
		public final IntValue gloomGreen;
		public final IntValue gloomBlue;
		public final IntValue gloomAlpha;

		public Common(ForgeConfigSpec.Builder builder) {
			builder.push("Structure Control Utility"); // outer level

			onlyServerMode = builder.comment(
					"If true, server Only (no client, use Minecraft Darkness effect).  If false, ( server+client, use SCU.Gloom effect")
					.define("onlyServerMode", false);
			
			builder.push("Debug Settings");
			debugLevel = builder.comment("Debug Level: 0 = Off, 1 = Log, 2 = Chat+Log")
					.translation(Main.MODID + ".config." + "debugLevel").defineInRange("debugLevel", () -> 0, 0, 2);
			builder.pop(); // debug settings

			builder.push("Structures which should not be protected");
			unprotectedStructuresString = builder
					.comment("List of structures that do not have protection by default, separated by semicolons")
					.translation(Main.MODID + ".config.unprotectedStructuresString")
					.define("unprotectedStructuresString", defaultUnprotectedStructuresString);
			builder.pop(); // Structures not protected

			builder.push("Structure Protection Values"); // Structure Protections
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
			builder.pop(); // Structure protection.

			builder.push(
					"Potion Effects: JumpBoost, MovementSlowness, Regeneration, SlowFalling, WaterBreathing, Weakness, Darkness ");

			protectedStructureEffects = builder
					.comment("Protected Structures Potion Effect Amplifiers: 0 = off, 1-9 = amplifier 0 to 8")
					.translation(Main.MODID + ".config.protectedStructuresEffects")
					.define("protectedStructuresEffects", "0000001");

			UnprotectedStructureEffects = builder
					.comment("Unprotected Structures Potion Effect Amplifiers: 0 = off, 1-9 = amplifier 0 to 8")
					.translation(Main.MODID + ".config.unProtectedStructuresEffects")
					.define("unProtectedStructuresEffects", "0000000");

			structureEffectsMinutes = builder.comment("Structure Potion Effects Duration in Minutes")
					.translation(Main.MODID + ".config." + "structureEffectsMinutes")
					.defineInRange("structureEffectsMinutes", () -> 10080, 0, Integer.MAX_VALUE);

			builder.pop(); // Potion Effects

			builder.push("Gloom color and transparency settings.  Default is dark blue black haze");
			
			gloomRed = builder
				    .comment("Gloom fog RED component as a percentage (0�100%).")
				    .translation(Main.MODID + ".config.gloomRed")
				    .defineInRange("gloomRed", () -> DEFAULT_GLOOM_RED_PERCENT, MIN_GLOOM_RED_PERCENT, 100);

				gloomGreen = builder
				    .comment("Gloom fog GREEN component as a percentage (0�100%).")
				    .translation(Main.MODID + ".config.gloomGreen")
				    .defineInRange("gloomGreen", () -> DEFAULT_GLOOM_GREEN_PERCENT, MIN_GLOOM_GREEN_PERCENT, 100);

				gloomBlue = builder
				    .comment("Gloom fog BLUE component as a percentage (0�100%).")
				    .translation(Main.MODID + ".config.gloomBlue")
				    .defineInRange("gloomBlue", () -> DEFAULT_GLOOM_BLUE_PERCENT, MIN_GLOOM_BLUE_PERCENT, 100);

				gloomAlpha = builder
				    .comment("Gloom fog ALPHA/opacity component as a percentage (70�95%).")
				    .translation(Main.MODID + ".config.gloomAlpha")
				    .defineInRange("gloomAlpha", () -> DEFAULT_GLOOM_ALPHA_PERCENT, MIN_GLOOM_ALPHA_PERCENT, 95);



			builder.pop(); // gloom color and transparency

			builder.pop(); // outer level
		}
	}
}
