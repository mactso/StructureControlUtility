package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.logic.BlockEventsLogic;
import com.mactso.structurecontrolutility.common.logic.ScheduledBlockCleanup;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Handles block placement events for players.
 * Cancels placement if the block is not allowed in protected structures.
 * 
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class BlockPlacementEvents {

	static boolean CANCEL_EVENT = true;
	static boolean CONTINUE_EVENT = false;
	
    @SubscribeEvent
    public static boolean onBlockPlacement(EntityPlaceEvent event) {
    	
        if (!(event.getEntity() instanceof Player p))
			return CONTINUE_EVENT;

        Block block = event.getPlacedBlock().getBlock();      
        if (p.isCreative() && (block != Blocks.FIRE))
			return CONTINUE_EVENT;

        if (event.getEntity().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
			return CONTINUE_EVENT;

        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();


        if (!BlockEventsLogic.isBlockPlacable(level, pos, p, block)) {
        	if (block == Blocks.FIRE)
        		ScheduledBlockCleanup.scheduleBlockCleanup(pos);
			return CANCEL_EVENT; 
        }
        
		return CONTINUE_EVENT;
        
    }
}
