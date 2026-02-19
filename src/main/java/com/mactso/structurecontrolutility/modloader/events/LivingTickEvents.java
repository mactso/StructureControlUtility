package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.logic.PlayerTickLogic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles per-tick updates for living entities, applying structure-based
 * effects to players inside protected structures.
 * 
 * Delegates effect application to PlayerTickLogic for maintainability.
 */

@Mod.EventBusSubscriber()
public class LivingTickEvents {

	@SubscribeEvent
	public static void onLivingUpdate(LivingTickEvent event) {

		//		boolean disable = true;
//		if (disable) return;

		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		if (sp.isCreative() || sp.isSpectator())
			return;
		if (sp.tickCount % 15 != 0)
			return;

		PlayerTickLogic.applyStructureEffects(sp);

	}
}
