package com.mactso.structurecontrolutility.modloader.events;

import java.util.List;

import com.mactso.structurecontrolutility.common.logic.BlockEventsLogic;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Handles explosions.
 * Removes protected blocks from explosion affected blocks.
 */
@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class ExplosionEvents {

    @SubscribeEvent
    public static void onExplosionDetonate(Detonate event) {
        Level level = event.getLevel();
        List<BlockPos> affectedBlocks = event.getAffectedBlocks();
        BlockEventsLogic.handleExplosionDetonate(level, affectedBlocks);
    }
}

