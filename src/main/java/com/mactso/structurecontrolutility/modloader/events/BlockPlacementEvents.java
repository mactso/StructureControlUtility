package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.logic.BlockEventsLogic;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Handles block placement events for players.
 * Cancels placement if the block is not allowed in protected structures.
 * 
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class BlockPlacementEvents {

    @SubscribeEvent
    public static void onBlockPlacement(EntityPlaceEvent event) {
    	
        if (!(event.getEntity() instanceof Player p))
            return;

        Block block = event.getPlacedBlock().getBlock();      
        if (p.isCreative() && (block != Blocks.FIRE))
            return;

        if (event.getEntity().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
            return;

        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();


        if (!BlockEventsLogic.isBlockPlacable(level, pos, p, block)) {
            event.setCanceled(true);
        }
        
    }
}
