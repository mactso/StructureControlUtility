package common.logic;

import java.util.List;
import java.util.ListIterator;

import common.command.utilities.MyUtilities;
import common.config.MyConfig;
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
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;

public class BlockEventsLogic {

	// client side variables.
	public static long cGameTime = 0;
	
	public static void handleBlockPlacement(EntityPlaceEvent event) {
	    LevelAccessor level = event.getLevel();
	    BlockPos pos = event.getPos();
	    Block block = event.getPlacedBlock().getBlock();
	
	    if (event.getEntity() instanceof Player p && p.isCreative()) return;
	
	    // Fire protection
	    if (block == Blocks.FIRE && MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_FIRE)) {
	        if (event.isCancelable()) event.setCanceled(true);
	    }
	
	    // General block protection
	    if (MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_BREAKING)) {
	        if (MyUtilities.isProtectableBlock(event.getState())) {
	            if (event.isCancelable()) {
	                if (event.getEntity() instanceof ServerPlayer sp) {
	                    MyUtilities.updateHands(sp);
	                }
	                SpecialEffects.doFailureEffects(event.getEntity(), pos);
	                event.setCanceled(true);
	            }
	        }
	    }
	}

	public static void handleBlockBreak(ServerPlayer sp, BlockPos pos, BreakEvent event) {
	    ServerLevel level = (ServerLevel) sp.level();
	
	    if (MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_BREAKING) && event.isCancelable()) {
	        SpecialEffects.doFailureEffects(sp, pos);
	        event.setCanceled(true);
	    }
	}

	public static void handleBreakingSpeed(ServerPlayer sp, BlockPos pos) {
	    LevelAccessor level = sp.level();
	    if (level.getChunk(pos).getInhabitedTime() > MyConfig.getStopBreakingTicks()) return;
	
	    RandomSource rand = level.getRandom();
	    long gameTime = ((Level) level).getGameTime();
	
	    if (MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_BREAKING)) {
	        if (BlockEventsLogic.cGameTime < gameTime) {
	            BlockEventsLogic.cGameTime = gameTime + 10 + rand.nextInt(20);
	            SpecialEffects.doFailureEffects(sp, pos);
	        }
	    }
	}

	public static void handleExplosionDetonate(Level level, List<BlockPos> affectedBlocks) {
	    for (ListIterator<BlockPos> iter = affectedBlocks.listIterator(affectedBlocks.size()); iter.hasPrevious();) {
	        BlockPos pos = iter.previous();
	        if (MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_EXPLODING)) {
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
	            if (MyUtilities.insideProtectedStructure(level, mPos, MyUtilities.DAMAGE_FIRE)) {
	                MyUtilities.debugMsg(2, d.getName() + ", and is protected.");
	                ScheduledBlockCleanup.scheduleBlockCleanup(mPos);
	                ScheduledBlockCleanup.scheduleBlockCleanup(dpos);
	                return;
	            }
	        }
	    }
	}

}
