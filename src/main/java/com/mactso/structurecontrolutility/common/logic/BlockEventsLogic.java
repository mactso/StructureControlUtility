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

public class BlockEventsLogic {

	// client side variables.
	public static long cGameTime = 0;
	
	public static boolean isBlockPlacable(LevelAccessor level, BlockPos pos, Player p, Block block) {
	
	    // Fire protection
		if (block == Blocks.FIRE && ModUtilities.insideProtectedStructure(level, pos, ModUtilities.DAMAGE_FIRE)) {
			SpecialEffects.doFireFailureEffects(p, pos);
			return false;
	    }
	
	    // General block protection
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

	public static void handleExplosionDetonate(Level level, List<BlockPos> affectedBlocks) {
	    for (ListIterator<BlockPos> iter = affectedBlocks.listIterator(affectedBlocks.size()); iter.hasPrevious();) {
	        BlockPos pos = iter.previous();
			if (ModUtilities.insideProtectedStructure(level, pos, ModUtilities.DAMAGE_EXPLODING)) {
	            iter.remove();
	        }
	    }
	}

	public static void handleNeighborNotify(LevelAccessor level, BlockPos pos, Iterable<Direction> notifiedSides) {
	    MutableBlockPos mPos = new MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
	
	    MyUtilities.debugMsg(1, mPos, "Neighbor Notify Event");
	    for (Direction d : notifiedSides) {
	        MyUtilities.debugMsg(2, d.getName() + " " + d.getUnitVec3i() + ", ");
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
