package com.mactso.structurecontrolutility.managers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.icu.util.StringTokenizer;
import com.mactso.structurecontrolutility.Main;
import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.utility.Utility;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureManager {

	public static Map<String, StructureItem> structureMap = new HashMap<>();
	static int lastgoodline = 0;
	static final int TICKS_PER_MINUTE = 1200;
	private static final Logger LOGGER = LogManager.getLogger();
	static String regex = "[0-9][0-9][0-9][0-9][0-9][0-9]";
	static Pattern pattern = Pattern.compile(regex);
	static StructureItem defaultUnprotectedStructureItem = new StructureItem(0, "000000", 0, 0, 0, 0);
	static StructureItem defaultProtectedStructureItem = new StructureItem(0, "000000", MyConfig.getEffectsMinutes(), MyConfig.getStopFireMinutes(), MyConfig.getStopBreakingMinutes(), MyConfig.getStopExplosionMinutes());	
	public static void structureInit() {
		int lineNumber;
		String modAndStructure = "";
		String effectFlags;
		int effectMinutes;
		int stopFireMinutes;
		int stopBreakingMinutes;
		int stopExplosionsMinutes;
		int addcount = 0;
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
				if (line.charAt(0) == '*') {
					continue;
				}
				StringTokenizer st = new StringTokenizer(line, ",");
				linecount++;
				try {
					errorField = "linenumber";
					lineNumber = Integer.parseInt(st.nextToken().trim());

					errorField = "modAndStructure";
					modAndStructure = st.nextToken().trim();
					String key = modAndStructure;

					errorField = "effectFlags";
					effectFlags = st.nextToken().trim();

					if (effectFlags.length() != 14) {
						LOGGER.error(modAndStructure + " effects string of '" + effectFlags
								+ "' in Structures.csv is too short or long.  It was set to Effects:000000.");
						effectFlags = "Effects:000000";
					}

					effectFlags = effectFlags.substring(8);
					
					if (!pattern.matcher(effectFlags).matches()) {
						LOGGER.error(modAndStructure + " effects string of '" + effectFlags
								+ "' in Structures.csv has non numeric digits.  It was set to 000000.");
						effectFlags = "000000";
					}
					errorField = "effectMinutes";
					String token = st.nextToken().trim();
					effectMinutes = Integer.parseInt(token);
					
					
					errorField = "stopFireMinutes";
					token = st.nextToken().trim();
					stopFireMinutes = Integer.parseInt(token);

					errorField = "stopBreakingMinutes";
					token = st.nextToken().trim();
					stopBreakingMinutes = Integer.parseInt(token);

					errorField = "stopExplosionsMinutes";
					token = st.nextToken().trim();
					stopExplosionsMinutes = Integer.parseInt(token);

					lastgoodline = lineNumber;

					Utility.debugMsg(1, lineNumber + ", " + lastgoodline + ", " + modAndStructure + ",  " + effectFlags
							+ ",  " + stopFireMinutes + ", " + stopBreakingMinutes + ", " + stopExplosionsMinutes);
					errorField = "get Structure Item";
					StructureItem si = new StructureItem(lineNumber, effectFlags, effectMinutes, stopFireMinutes,
							stopBreakingMinutes, stopExplosionsMinutes);
					errorField = "put Structure Item";
					structureMap.put(key, si);
					
					addcount++;

				} catch (Exception e) {
					Utility.debugMsg(0, Main.MODID + " Error reading field " + errorField + " on " + linecount
							+ "th line of Structures.csv.");
				}
			}
			input.close();
		} catch (Exception e) {
			Utility.debugMsg(0,
					"Warning Structures.csv not found in subdirectory config/structurecontrolutility.  Using default values");

		}

	}

	// Structure: BlockPos is inside a structure bounding box.
	// null: BlockPos is not inside a structure.
	// Possible issue:  If Structure boundaries can overlap will only get the 1st structure.
	public static String insideStructure(LevelAccessor level, BlockPos pos) {
		ChunkAccess chunk = level.getChunk(pos);
		BlockState bs = level.getBlockState(pos);

		Optional<Registry<Structure>> opt = level.registryAccess().registry(Registries.STRUCTURE);

		if (opt.isEmpty()) {
			return null;
		}

		Registry<Structure> structRegistry = opt.get();

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
			if (Utility.unprotectedStructures.contains(key)) {
				si = defaultUnprotectedStructureItem;
			} else {
				si = defaultProtectedStructureItem;
			}
			structureMap.put(key, si);
		}

		return si;
	}

	public static class StructureItem {
		int lineNumber;
		String effectFlags;
		int effectMinutes;
		int stopFireMinutes;
		int stopBreakingMinutes;
		int stopExplosionsMinutes;
		// derived values
		long effectTicks;
		long stopFireTicks;
		long stopBreakingTicks;
		long stopExplosionsTicks;

		int jumpBoost;
		int nightVision;
		int regeneration;
		int slowFalling;
		int waterBreathing;
		int weakness;

		public StructureItem(int lineNumber, String effectFlags, int effectMinutes, int stopFireMinutes,
				int stopBreakingMinutes, int stopExplosionsMinutes) {
			
			this.lineNumber = lineNumber;
			this.effectFlags = effectFlags;
			setEffectsValues(effectFlags);
			this.effectMinutes = effectMinutes;
			this.stopFireMinutes = stopFireMinutes;
			this.stopBreakingMinutes = stopBreakingMinutes;
			this.stopExplosionsMinutes = stopExplosionsMinutes;
			
			// derived values
			this.effectTicks = effectMinutes * Utility.TICKS_PER_MINUTE;
			this.stopFireTicks = stopFireMinutes * Utility.TICKS_PER_MINUTE;
			this.stopBreakingTicks = stopBreakingMinutes * Utility.TICKS_PER_MINUTE;
			this.stopExplosionsTicks = stopExplosionsMinutes * Utility.TICKS_PER_MINUTE;
			
		}


		private void setEffectsValues(String s) {
			jumpBoost = Integer.valueOf(s.substring(Utility.JUMP_BOOST,1));
			nightVision = Integer.valueOf(s.substring(Utility.MOVEMENT_SLOWNESS,1));
			if (nightVision > 1)
				nightVision = 1;
			regeneration = Integer.valueOf(s.substring(Utility.REGENERATION,1));
			slowFalling = Integer.valueOf(s.substring(Utility.SLOW_FALLING,1));
			waterBreathing = Integer.valueOf(s.substring(Utility.WATER_BREATHING,1));
			if (waterBreathing > 1)
				waterBreathing = 1;
			weakness = Integer.valueOf(s.substring(Utility.WEAKNESS,1));
		}

		public boolean hasEffects() {
			if (this.effectFlags.equals("000000")) {
				return false;
			}
			return true;
		}

		public int getJumpBoostIntensity() {
			return jumpBoost;
		}

		public int getNightVisionIntensity() {
			return nightVision;
		}

		public int getRegenerationIntensity() {
			return regeneration;
		}

		public int getSlowFallingIntensity() {
			return slowFalling;
		}

		public int getWaterBreathingIntensity() {
			return waterBreathing;
		}

		public int getWeaknessIntensity() {
			return weakness;
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

		// derived values
		public long getStopBreakingTicks() {
			return stopBreakingTicks;
		}

		// derived values
		public long getStopFireTicks() {
			return stopFireTicks;
		}

		// derived values
		public long getStopExplosionsTicks() {
			return stopExplosionsTicks;
		}

	}

}
