package com.mactso.structurecontrolutility.common.managers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.utility.MyUtilities;
import com.mactso.structurecontrolutility.modloader.main.Main;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Manages structure definitions and their associated effect rules.
 *
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Load structure data from `Structures.csv`.</li>
 *     <li>Provide default protection values for unregistered structures.</li>
 * <li>Track per-structure effect durations, protections, and potion
 * amplifiers.</li>
 *     <li>Determine if a position is inside a structure.</li>
 * <li>Support multiple potion effects including Mining Fatigue (configurable
 * duration and amplifier).</li>
 * </ul>
 *
 * <p>
 * Notes:
 * <ul>
 * <li>Static defaults are initialized at startup using the current config
 * values.</li>
 * <li>Parsing is robust: malformed lines are logged but do not break
 * startup.</li>
 *     <li>Derived values (ticks) are precomputed for efficiency.</li>
 * </ul>
 */
public class StructureManager {

	public static Map<String, StructureItem> structureMap = new HashMap<>();
	static int lastgoodline = 0;
	static final int TICKS_PER_MINUTE = 1200;
	private static final Logger LOGGER = LogManager.getLogger();
	static String regex = "[0-9][0-9][0-9][0-9][0-9][0-9][0-9]";
	static Pattern pattern = Pattern.compile(regex);

	// Types of structures that don't have protection by default.
	public static List<String> unprotectedStructures;


	private static StructureItem defaultUnprotectedStructureItem = new StructureItem(0, MyConfig.getUnprotectedStructuresEffects(), MyConfig.getStructureEffectsMinutes(), 0, 0, 0, 0, 0);
	private static StructureItem defaultProtectedStructureItem = new StructureItem(0, MyConfig.getProtectedStructuresEffects(), MyConfig.getStructureEffectsMinutes(),
			MyConfig.getStopFireMinutes(), MyConfig.getStopBreakingMinutes(), MyConfig.getStopExplosionMinutes(),
			MyConfig.getMiningFatigueMinutes(), MyConfig.getMiningFatigueLevel());
	
	// types of effects a structure can have
	public static final int JUMP_BOOST = 0;
	public static final int MOVEMENT_SLOWNESS = 1;
	public static final int REGENERATION = 2;
	public static final int SLOW_FALLING = 3;
	public static final int WATER_BREATHING = 4;
	public static final int WEAKNESS = 5;
	public static final int GLOOM = 6; // Uses Minecraft.Darkness if server only.

	public static void structureInit() {
		
	    unprotectedStructures = MyConfig.getUnprotectedStructuresAsList();
	    
		int lineNumber;
		int loadedCount = 0;
		String modAndStructure = "";
		String effectFlags;
		int effectMinutes;
		int stopFireMinutes;
		int stopBreakingMinutes;
		int stopExplosionsMinutes;
		int miningFatigueMinutes;   // number of minutes the effect lasts
		int miningFatigueAmplifier;     // amplifier, 0 = off, max 4

		int linecount = 0;

		String errorField = "first";
		String line;

		if (structureMap.size() > 0) {
			return;
		}
		try (InputStreamReader input = new InputStreamReader(
				new FileInputStream("config/structurecontrolutility/structures.csv"))) {
			BufferedReader br = new BufferedReader(input);
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) {
					continue;
				}
				if (line.equals("")) {
					continue;
				}

				if (line.charAt(0) == '*') {
					continue;
				}
				
				linecount++;
				
				try {
					String[] parts = line.split(",", -1); // preserve empty fields

					// Check field count
					if (parts.length < 9) {
						MyUtilities.debugMsg(0,
								Main.MODID + "Line# " + linecount + " Bad line in Structures.csv (not enough fields): "
										+ line + "\". Skipping this line.");
						continue; // skip this line, process the rest
					}

					errorField = "linenumber";
					lineNumber = Integer.parseInt(parts[0].trim());

					errorField = "modAndStructure";
					modAndStructure = parts[1].trim();

					errorField = "effectFlags";
					effectFlags = parts[2].trim();

					if (effectFlags.length() != 15) {
						LOGGER.error(modAndStructure + " effects string of '" + effectFlags
								+ "' in Structures.csv is too short or long.  It was set to Effects:0000000.");
						effectFlags = "Effects:0000000";
					}

					effectFlags = effectFlags.substring(8);

					if (!pattern.matcher(effectFlags).matches()) {
						LOGGER.error(modAndStructure + " effects string of '" + effectFlags
								+ "' in Structures.csv has non numeric digits.  It was set to 000000.");
						effectFlags = "000000";
					}

					errorField = "effectMinutes";
					effectMinutes = Integer.parseInt(parts[3].trim());

					errorField = "stopFireMinutes";
					stopFireMinutes = Integer.parseInt(parts[4].trim());

					errorField = "stopBreakingMinutes";
					stopBreakingMinutes = Integer.parseInt(parts[5].trim());

					errorField = "stopExplosionsMinutes";
					stopExplosionsMinutes = Integer.parseInt(parts[6].trim());
					
					errorField = "miningFatigueMinutes";
					miningFatigueMinutes = Integer.parseInt(parts[7].trim());

					errorField = "miningFatigueAmplifier";
					miningFatigueAmplifier = Integer.parseInt(parts[8].trim());

					// clamp amplifier
					if (miningFatigueAmplifier < 1)
						miningFatigueAmplifier = 1;
					if (miningFatigueAmplifier > 5)
						miningFatigueAmplifier = 5;

					lastgoodline = lineNumber;

					MyUtilities.debugMsg(0, loadedCount + " Loaded: " + modAndStructure + " values successfully.");

					errorField = "get Structure Item";
					StructureItem si = new StructureItem(lineNumber, effectFlags, effectMinutes, stopFireMinutes,
							stopBreakingMinutes, stopExplosionsMinutes, miningFatigueMinutes, miningFatigueAmplifier);

					errorField = "put Structure Item";
					structureMap.put(modAndStructure, si);

					loadedCount ++;

				} catch (Exception e) {
					MyUtilities.debugMsg(0, Main.MODID + " Error reading field " + errorField + " on " + linecount
							+ "th line of Structures.csv.");
				}
			}
			
		} catch (Exception e) {
			MyUtilities.debugMsg(0,
					"Warning Structures.csv not found in subdirectory config/structurecontrolutility.  Using default values");

		}

	}

	// Structure: BlockPos is inside a structure bounding box.
	// null: BlockPos is not inside a structure.
	// Possible issue: If Structure boundaries can overlap will only get the 1st
	// structure.
	public static String insideStructure(LevelAccessor level, BlockPos pos) {
		ChunkAccess chunk = level.getChunk(pos);

		Registry<Structure> structRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

		Set<Entry<Structure, LongSet>> structureReferences = chunk.getAllReferences().entrySet();
		for (Entry<Structure, LongSet> entry : structureReferences) {

			LongIterator longiterator = entry.getValue().iterator();
			while (longiterator.hasNext()) {
				long packedChunkCoordinates = longiterator.nextLong();
				ChunkAccess istructurereader = level.getChunk(ChunkPos.getX(packedChunkCoordinates),
						ChunkPos.getZ(packedChunkCoordinates), ChunkStatus.STRUCTURE_STARTS);
				StructureStart structurestart = istructurereader.getStartForStructure(entry.getKey());
				if (structurestart.getBoundingBox().isInside(pos)) {
					ResourceLocation key = structRegistry.getKey(entry.getKey());
					return key.toString();
				}
			}
		}

		return null;

	}

	public static StructureItem getStructureItemOrDefault(String key) {

		StructureItem si = StructureManager.structureMap.get(key);

		// Issue: User accidentally deleted line with a structure.
		if (si == null) {
			if (unprotectedStructures.contains(key)) {
				si = defaultUnprotectedStructureItem;
			} else {
				si = defaultProtectedStructureItem;
			}
			structureMap.put(key, si);
		}

		return si;
	}

	/**
	 * Represents a single structure's configuration and derived effect values.
	 *
	 * <p>
	 * Stores per-structure information including:
	 * <ul>
	 *     <li>Line number from the CSV file</li>
	 *     <li>Effect flags string and individual potion effect intensities</li>
	 *     <li>Durations for structure protections (fire, breaking, explosions)</li>
	 *     <li>Mining Fatigue effect duration and amplifier</li>
	 *     <li>Derived tick counts for each duration for efficient runtime use</li>
	 * </ul>
	 *
	 * <p>
	 * Provides helper methods to check for active effects and to access both
	 * minute-based and tick-based durations.
	 */
	public static class StructureItem {
		int lineNumber;
		String effectFlags;
		int effectMinutes;
		int stopFireMinutes;
		int stopBreakingMinutes;
		int stopExplosionsMinutes;
		int miningFatigueMinutes;   // number of minutes the mining fatigue lasts
		int miningFatigueAmplifier;     // amplifier, 0 = off, max 4
		
		// derived values
		long effectTicks;
		long stopFireTicks;
		long stopBreakingTicks;
		long stopExplosionsTicks;
		long miningFatigueTicks;    

		int amplifierJumpBoost;
		int amplifierMovementSlowness;
		int amplifierRegeneration;
		int amplifierSlowFalling;
		int amplifierWaterBreathing;
		int amplifierWeakness;
		int amplifierGloomOrDarkness;

		public StructureItem(int lineNumber, String effectFlags, int effectMinutes, int stopFireMinutes,
				int stopBreakingMinutes, int stopExplosionsMinutes, int miningFatigueMinutes,
				int rawMiningFatigueAmplifier) {

			this.lineNumber = lineNumber;
			this.effectFlags = effectFlags;
			setEffectsValues(effectFlags);
			this.effectMinutes = effectMinutes;
			this.stopFireMinutes = stopFireMinutes;
			this.stopBreakingMinutes = stopBreakingMinutes;
			this.stopExplosionsMinutes = stopExplosionsMinutes;
			this.miningFatigueMinutes = miningFatigueMinutes;
			this.miningFatigueAmplifier = rawMiningFatigueAmplifier - 1;
			
			// derived values
			this.effectTicks = effectMinutes * MyUtilities.TICKS_PER_MINUTE;
			this.stopFireTicks = stopFireMinutes * MyUtilities.TICKS_PER_MINUTE;
			this.stopBreakingTicks = stopBreakingMinutes * MyUtilities.TICKS_PER_MINUTE;
			this.stopExplosionsTicks = stopExplosionsMinutes * MyUtilities.TICKS_PER_MINUTE;
			this.miningFatigueTicks = miningFatigueMinutes * MyUtilities.TICKS_PER_MINUTE;

		}
		
		/*
		 * Parse the Effects string to initilize the MobEffects amplifiers
		 */
		private void setEffectsValues(String s) {
		    
			amplifierJumpBoost = parseEffectDigitOrZero(s, JUMP_BOOST);

			amplifierMovementSlowness = parseEffectDigitOrZero(s, MOVEMENT_SLOWNESS);
			if (amplifierMovementSlowness > 1)
				amplifierMovementSlowness = 1;

			amplifierRegeneration = parseEffectDigitOrZero(s, REGENERATION);
			amplifierSlowFalling = parseEffectDigitOrZero(s, SLOW_FALLING);

			amplifierWaterBreathing = parseEffectDigitOrZero(s, WATER_BREATHING);
			if (amplifierWaterBreathing > 1)
				amplifierWaterBreathing = 1;

			amplifierWeakness = parseEffectDigitOrZero(s, WEAKNESS);
			amplifierGloomOrDarkness = parseEffectDigitOrZero(s, GLOOM);

		}

		/** 
		 * Returns the numeric value of the character at the given index, or 0 if the
		 * character is not a valid digit.
		 */
		private int parseEffectDigitOrZero(String s, int index) {
			if (index < 0 || index >= s.length())
				return 0;
		    int value = Character.getNumericValue(s.charAt(index));
		    if (value >= 0) {
		        return value;
		    } else {
		        return 0;
		    }
		}

		public boolean hasEffects() {

			if (!this.effectFlags.equals("0000000")) {
				return true;
			}

			return false;
		}

		/**
		 * Returns true if the structure has no remaining protection of any kind.
		 */
		public boolean isProtected() {
			if (this.stopBreakingMinutes > 0)
				return true;
			if (this.stopFireMinutes > 0)
				return true;
			if (this.stopExplosionsMinutes > 0)
				return true;
			return false;
		}

		public int getJumpBoostAmplifier() {
			return amplifierJumpBoost;
		}

		public int getMoveSlownessAmplifier() {
			return amplifierMovementSlowness;
		}

		public int getRegenerationAmplifier() {
			return amplifierRegeneration;
		}

		public int getSlowFallingAmplifier() {
			return amplifierSlowFalling;
		}

		public int getWaterBreathingAmplifier() {
			return amplifierWaterBreathing;
		}

		public int getWeaknessAmplifier() {
			return amplifierWeakness;
		}

		public int getGloomOrDarknessAmplifier() {
			return amplifierGloomOrDarkness;
		}
		
		public int getStopFireMinutes() {
			return stopFireMinutes;
		}

		public int getStopBreakingMinutes() {
			return stopBreakingMinutes;
		}

		public int getStopExplosionsMinutes() {
			return stopExplosionsMinutes;
		}

		public int getMiningFatigueMinutes() {
			return miningFatigueMinutes;
		}

		public boolean isMiningFatigue() {
		    if (miningFatigueMinutes > 0) 
		    	return true;
		    return false;
		}
		
		public int getMiningFatigueAmplifier() {
			return miningFatigueAmplifier;
		}
		
		// derived values
		public long getStopBreakingTicks() {
			return stopBreakingTicks;
		}

		public long getStopFireTicks() {
			return stopFireTicks;
		}

		public long getStopExplosionsTicks() {
			return stopExplosionsTicks;
		}

		public long getMiningFatigueTicks() {
			return miningFatigueTicks;
		}

	}

}
