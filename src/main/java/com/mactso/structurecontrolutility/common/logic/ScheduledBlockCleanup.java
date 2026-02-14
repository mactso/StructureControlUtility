package com.mactso.structurecontrolutility.common.logic;

import java.util.ArrayList;
import java.util.List;

import com.mactso.structurecontrolutility.common.utility.ModUtilities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

/**
 * ScheduledBlockCleanupLogic
 *
 * Handles cleanup of scheduled blocks in protected structures.
 * 
 * <p>
 * Fire and lava blocks may be placed even when the event is cancelled in Forge,
 * Fabric, and NeoForge, so scheduled removal is deferred to the next server
 * tick.
 * </p>
 *
 * <p>
 * Thread-safe: fire and lava positions are stored in synchronized lists. Fire
 * removal is batched with MAX_FIRE_CLEAR_PER_TICK per tick; lava removal
 * processes all scheduled blocks. Typical list sizes are expected to be small:
 * fire < 32, lava < 8.
 * </p>
 *
 * <p>
 * Provides helper methods to schedule blocks for removal and to handle special
 * cases such as lava buckets used in protected structures.
 * </p>
 */

public class ScheduledBlockCleanup {

	private static final int MAX_BLOCKS_CLEANUP_PER_TICK = 256;

	static List<BlockPos> blockPosList = new ArrayList<BlockPos>();
	static List<BlockPos> lavaPosList = new ArrayList<BlockPos>();
	
	public static void handleScheduledBlockCleanup(ServerLevel serverLevel) {
		if (blockPosList.isEmpty())
			return;

	    int batchSize = Math.min(MAX_BLOCKS_CLEANUP_PER_TICK, blockPosList.size());
	    List<BlockPos> batchList;

	    synchronized (blockPosList) {
	        batchList = new ArrayList<>(blockPosList.subList(0, batchSize));
	        blockPosList.subList(0, batchSize).clear();
	    }

	    for (BlockPos pos : batchList) {
        	serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
	    }
	}

	public static void scheduleBlockCleanup(BlockPos pos) {
		synchronized (blockPosList) {
			blockPosList.add(pos);
		}
	}

	public static boolean handleLavaBucketInProtectedStructure(ServerPlayer sp, InteractionHand hand, BlockPos pos,
			Direction face) {

		Item item = sp.getItemInHand(hand).getItem();
	    if (!(item instanceof BucketItem bucket)) 
			return false;
	    if (bucket.getFluid() != Fluids.LAVA) 
			return false;
	
		ServerLevel level = sp.level();
		if (!ModUtilities.insideProtectedStructure(level, pos, ModUtilities.DAMAGE_FIRE))
			return false;
	
	    BlockPos targetPos = (face != null) ? pos.relative(face) : pos;
	
	    scheduleBlockCleanup(targetPos);
	    SpecialEffects.doFailureEffects(sp, targetPos);
	    
		return true;
	    
	}
	
}
