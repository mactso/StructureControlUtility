package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.logic.BlockEventsLogic;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Handles block breaking events for players.
 * Cancels breaking if the block is inside a protected structure.
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class BlockBreakEvents {

    @SubscribeEvent
    public static void onBreakBlock(BreakEvent event) {

		
        if (event.getPlayer().level().getChunk(event.getPos()).getInhabitedTime() > com.mactso.structurecontrolutility.common.config.MyConfig.getStopBreakingTicks())
            return;

        if (!(event.getPlayer() instanceof ServerPlayer sp))
            return;

        if (sp.isCreative())
            return;

        BlockPos pos = event.getPos();

        if (!BlockEventsLogic.canBreakBlock(sp, pos))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBreakingSpeed(BreakSpeed event) {

        if (!(event.getEntity() instanceof ServerPlayer sp))
            return;

        if (sp == null || sp.isCreative() || !event.getPosition().isPresent())
            return;

        BlockPos pos = event.getPosition().get();
        BlockEventsLogic.handleBreakingSpeed(sp, pos);
    }
}
