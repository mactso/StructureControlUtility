package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.logic.ScheduledBlockCleanup;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Handles scheduled block updates each world tick for protected structures.
 * 
 * Tracks fire and lava positions to be cleared at the end of each server tick.
 * Thread-safe via synchronized lists. Assumes event fires only for server
 * worlds.
 */

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class WorldTickHandler {



	@SubscribeEvent
	public static void onWorldTickEvent(LevelTickEvent.Post event) {
	    // --- Extract event fields ---
	    Level level = event.level();
	    if (level.isClientSide())
	        return; // server-only
	    ServerLevel serverLevel = (ServerLevel) level;
	    ScheduledBlockCleanup.handleScheduledBlockCleanup(serverLevel);
	}

}
