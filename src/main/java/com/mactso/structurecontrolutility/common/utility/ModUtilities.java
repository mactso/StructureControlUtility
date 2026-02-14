package com.mactso.structurecontrolutility.common.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.mactso.structurecontrolutility.common.managers.StructureManager;
import com.mactso.structurecontrolutility.common.managers.StructureManager.StructureItem;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.Tags;

/*
 * ModUtilities contains general logic used by many Event and Logic methods.
 */

public class ModUtilities {
	
	private static final boolean ALLOW_BREAK = true;
	private static final boolean PREVENT_BREAK = false;

	/**
	 * This returns true if the User is inside a structure AND if the structure is
	 * still protected from the type of damage being tested.
	 * 
	 * Damage Types include: DAMAGE_FIRE DAMAGE_BREAKING DAMAGE_EXPLODING
	 * 
	 * True: Inside a structure with protection against that kind of damage. False:
	 * Not inside a structure with protection against that kind of damage.
	 * 
	 * This may need to be decomposed into "inside a structure" and protection logic
	 */
	public static boolean insideProtectedStructure(LevelAccessor level, BlockPos pos, int damageType) {

		ChunkAccess chunk = level.getChunk(pos);
		long ageInTicks = chunk.getInhabitedTime();

		BlockState bs = level.getBlockState(pos);
		Block block = bs.getBlock();

		boolean isFire = false;
		if (block == Blocks.FIRE)
			isFire = true;

		if (isBlockBreakable(bs))
			return false;

		Registry<Structure> structRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

		Set<Entry<Structure, LongSet>> structureReferences = chunk.getAllReferences().entrySet();
		if (structureReferences.isEmpty())
			return false;

		for (Entry<Structure, LongSet> structure : structureReferences) {

			Structure structureKey = structure.getKey();
			Identifier key = structRegistry.getKey(structureKey);

			StructureItem si = StructureManager.getStructureItemOrDefault(key.toString());

			LongIterator longiterator = structure.getValue().iterator();
			while (longiterator.hasNext()) {

				long packedChunkCoordinates = longiterator.nextLong();
				ChunkAccess iStructureReader = level.getChunk(ChunkPos.getX(packedChunkCoordinates),
						ChunkPos.getZ(packedChunkCoordinates), ChunkStatus.STRUCTURE_STARTS);
				StructureStart structurestart = iStructureReader.getStartForStructure(structureKey);
				
				BoundingBox boundingBox;
				if (!isFire) {
					boundingBox = structurestart.getBoundingBox();
				} else {
					boundingBox = structurestart.getBoundingBox().inflatedBy(1, 1, 1);
				}

				if (((boundingBox.isInside(pos)))) {
					if ((isDamageTypeProtectionInEffect(damageType, ageInTicks, si)))
						return true;
				}
			}
		}
		return false;
	}
	

	/**
	 * This method identifies which blocks can be broken in a structure protected
	 * from block breaking.
	 * It uses tags, instanceof checks, and block comparisons.
	 */
	public static boolean isBlockBreakable(BlockState bs) {

		Block block = bs.getBlock();

		if (isBreakAllowedForBlock(block)) return ALLOW_BREAK;
		if (isBreakAllowedByInstance(block)) return ALLOW_BREAK;
		if (isBreakAllowedByTag(bs)) return ALLOW_BREAK;

		return PREVENT_BREAK;
	}

	private static final Set<Block> BREAKABLE_BLOCKS = Set.of(Blocks.RAW_GOLD_BLOCK, Blocks.RAW_IRON_BLOCK,
			Blocks.RAW_COPPER_BLOCK, Blocks.GOLD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.COPPER_BLOCK, Blocks.IRON_BLOCK,
			Blocks.EMERALD_BLOCK, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.GRAVEL);

	private static boolean isBreakAllowedForBlock(Block block) {
		return BREAKABLE_BLOCKS.contains(block);
	}

	private static boolean isBreakAllowedByTag(BlockState bs) {
		return bs.is(BlockTags.DIRT) || bs.is(BlockTags.WOOL) || bs.is(BlockTags.WOOL_CARPETS)
				|| bs.is(BlockTags.LEAVES) || bs.is(Tags.Blocks.ORES) || bs.is(BlockTags.SAND)
				|| bs.is(BlockTags.FLOWERS) || bs.is(BlockTags.CROPS) || bs.is(BlockTags.ALL_SIGNS)
				|| bs.is(BlockTags.BANNERS);
	}

	private static boolean isBreakAllowedByInstance(Block block) {
		return block instanceof WebBlock || block instanceof TallGrassBlock || block instanceof DoublePlantBlock
				|| block instanceof TallSeagrassBlock || block instanceof SeagrassBlock || block instanceof KelpBlock
				|| block instanceof TorchBlock || block instanceof WallTorchBlock || block instanceof RedstoneTorchBlock
				|| block instanceof TripWireBlock || block instanceof VineBlock;
	}

	/**
	 * Returns true if fire protection, block breaking protection, or explosion
	 * projection is still in effect;
	 */
	static boolean isDamageTypeProtectionInEffect(int damageType, long ageInTicks, StructureItem si) {
		if (damageType == DAMAGE_FIRE) {
			if (ageInTicks >= si.getStopFireTicks()) {
				return false;
			}
		} else if (damageType == DAMAGE_BREAKING) {
			if (ageInTicks >= si.getStopBreakingTicks()) {
				return false;
			}
		} else if (damageType == DAMAGE_EXPLODING) {
			if (ageInTicks >= si.getStopExplosionsTicks()) {
				return false;
			}
		}
		return true;
	}

	public static int DAMAGE_FIRE = 0;
	public static int DAMAGE_BREAKING = 1;
	public static int DAMAGE_EXPLODING = 2;
	public static List<String> unprotectedStructures = Arrays.asList("minecraft:mineshaft", "minecraft:mineshaft_mesa",
			"minecraft:trail_ruins", "minecraft:village_desert", "minecraft:village_plains",
			"minecraft:village_savanna", "minecraft:village_snowy", "minecraft:village_taiga");
	public static int JUMP_BOOST = 0;
	public static int MOVEMENT_SLOWNESS = 1;
	public static int REGENERATION = 2;
	public static int SLOW_FALLING = 3;
	public static int WATER_BREATHING = 4;
	public static int WEAKNESS = 5;

}
