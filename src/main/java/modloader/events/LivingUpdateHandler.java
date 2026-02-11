package modloader.events;

import common.logic.PlayerTickLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles per-tick updates for living entities, applying structure-based effects 
 * to players inside protected structures. 
 * 
 * Delegates effect application to PlayerTickLogic for maintainability.
 */

@Mod.EventBusSubscriber()
public class LivingUpdateHandler {

	@SubscribeEvent
	public static void onLivingUpdate(LivingTickEvent event) {
	    LivingEntity entity = event.getEntity();

	    // --- Fast type check first ---
	    if (!(entity instanceof ServerPlayer sp)) return;
	    if (sp.isCreative()) return;

	    // --- Grab server-level reference once ---
	    ServerLevel serverLevel = sp.serverLevel() instanceof ServerLevel sl ? sl : null;
	    if (serverLevel == null || serverLevel.isClientSide()) return;

	    // --- Grab chunk and age in ticks once ---
	    long ageInTicks = serverLevel.getChunk(sp.blockPosition()).getInhabitedTime();

	    // --- Delegate remaining logic ---
	    PlayerTickLogic.applyStructureEffects(sp, serverLevel, ageInTicks);
	}
}
