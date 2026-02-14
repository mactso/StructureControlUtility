package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.logic.BlockEventsLogic;
import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent.NeighborNotifyEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Handles NeighborNotifyEvents, specifically for fire.
 * Protects blocks from fire spread if inside a protected structure.
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class NeighborNotifyEvents {

    @SubscribeEvent
    public static void onNeighborNotifyEvent(NeighborNotifyEvent event) {

        if (event.getState().getBlock() != Blocks.FIRE)
            return;

        if (event.getLevel().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopFireTicks())
            return;

        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        Iterable<net.minecraft.core.Direction> notifiedSides = event.getNotifiedSides();

        BlockEventsLogic.handleNeighborNotify(level, pos, notifiedSides);
    }
}
