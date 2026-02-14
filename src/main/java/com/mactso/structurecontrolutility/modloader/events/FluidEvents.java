package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.logic.ScheduledBlockCleanup;
import com.mactso.structurecontrolutility.modloader.main.Main;


import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class FluidEvents {

	public static final boolean CANCEL_EVENT = true;
	public static final boolean CONTINUE_EVENT = false;
	
	@SubscribeEvent
	public static boolean onRightClickBlock(RightClickBlock event) {
		if (!(event.getEntity() instanceof ServerPlayer sp))
			return CONTINUE_EVENT;
		if (sp.isCreative())
			return CONTINUE_EVENT;

	
		boolean returnValue= ScheduledBlockCleanup.handleLavaBucketInProtectedStructure(sp, event.getHand(), event.getPos(), event.getFace());

		
		return returnValue;

	}

}
