package com.mactso.structurecontrolutility.common.logic;

import java.util.List;
import java.util.ListIterator;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.utility.ModUtilities;
import com.mactso.structurecontrolutility.common.utility.MyUtilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Centralized server-side logic for enforcing structure protection rules on
 * block placement, breaking, explosions, and fire propagation.
 * <p>
 * This class is stateless except for minimal client-rate-limiting data and
 * is intended to be called from Forge/Fabric/NeoForge event handlers.
 * </p>
 */

public class BlockEventsLogic {

	/** Client-side rate limiter for repeated failure effects. */
	public static long cGameTime = 0;

	/** Determines if block is being placed inside a protected structure is allowed */
	public static boolean isBlockPlacable(LevelAccessor level, BlockPos pos, Player p, Block block) {

		// Prevent fire placement inside protected structures.
		if (block == Blocks.FIRE && ModUtilities.insideProtectedStructure(level, pos, ModUtilities.DAMAGE_FIRE)) {
			SpecialEffects.doFireFailureEffects(p, pos);
			return false;
		}

		// Prevent general block placement inside protected structures.
		if (ModUtilities.insideProtectedStructure(level, pos, ModUtilities.DAMAGE_BREAKING)) {
			if (p instanceof ServerPlayer sp) {
				MyUtilities.updateHands(sp);
			}
			SpecialEffects.doFailureEffects(p, pos);
			return false;
		}

		return true;
	}


	public static boolean canBreakBlock(ServerPlayer sp, BlockPos pos) {
		ServerLevel serverLevel = (ServerLevel) sp.level();

		if (!(ModUtilities.insideProtectedStructure(serverLevel, pos, ModUtilities.DAMAGE_BREAKING)))
			return true;

		SpecialEffects.doFailureEffects(sp, pos);
		return false;
	}

	/** Applies throttled visual/audio feedback when breaking is blocked by protection. */
	public static void handleBreakingSpeed(ServerPlayer sp, BlockPos pos) {
		LevelAccessor level = sp.level();
		if (level.getChunk(pos).getInhabitedTime() > MyConfig.getStopBreakingTicks())
			return;

		RandomSource rand = level.getRandom();
		long gameTime = ((Level) level).getGameTime();

		if (ModUtilities.insideProtectedStructure(level, pos, ModUtilities.DAMAGE_BREAKING)) {
			if (BlockEventsLogic.cGameTime < gameTime) {
				BlockEventsLogic.cGameTime = gameTime + 10 + rand.nextInt(20);
				SpecialEffects.doFailureEffects(sp, pos);
			}
		}
	}

	/** Prevents explosions from damaging hard blocks that are part of a protected Structures. */
	public static void handleExplosionDetonate(Level level, List<BlockPos> affectedBlocks) {
		for (ListIterator<BlockPos> iter = affectedBlocks.listIterator(affectedBlocks.size()); iter.hasPrevious();) {
			BlockPos pos = iter.previous();
			if (ModUtilities.insideProtectedStructure(level, pos, ModUtilities.DAMAGE_EXPLODING)) {
				iter.remove();
			}
		}
	}


	/** Prevents fire spread into protected structures during neighbor updates. */
	public static void handleNeighborNotify(LevelAccessor level, BlockPos pos, Iterable<Direction> notifiedSides) {
		MutableBlockPos mPos = new MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());

		MyUtilities.debugMsg(1, mPos, "Neighbor Notify Event");
		for (Direction d : notifiedSides) {
			MyUtilities.debugMsg(2, d.getName() + " " + d.getNormal() + ", ");
			BlockPos dpos = mPos.relative(d);
			if (level.getBlockState(dpos).isFlammable(level, mPos, d.getOpposite())) {
				MyUtilities.debugMsg(2, ", is flammable");
				if (ModUtilities.insideProtectedStructure(level, mPos, ModUtilities.DAMAGE_FIRE)) {
					MyUtilities.debugMsg(2, d.getName() + ", and is protected.");
					ScheduledBlockCleanup.scheduleBlockCleanup(mPos);
					ScheduledBlockCleanup.scheduleBlockCleanup(dpos);
					return;
				}
			}
		}
	}

}
