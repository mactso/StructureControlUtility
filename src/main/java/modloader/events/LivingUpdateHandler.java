package modloader.events;

import common.logic.PlayerTickLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
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
public class LivingUpdateHandler {

	@SubscribeEvent
	public static void onLivingUpdate(LivingTickEvent event) {

		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		if (sp.isCreative() || sp.isSpectator())
			return;
		if (sp.tickCount % 15 != 0)
			return;

		PlayerTickLogic.applyStructureEffects(sp);

	}
}
