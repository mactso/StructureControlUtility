package modloader.events;

import common.logic.ScheduledBlockCleanup;
import modloader.main.Main;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class FluidEvents {

	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event) {
		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		if (sp.isCreative())
			return;
	
		boolean cancel = ScheduledBlockCleanup.handleLavaBucketInProtectedStructure(sp, event.getHand(), event.getPos(), event.getFace(),
				event);
		if ((cancel) && event.isCancelable()) event.setCanceled(true); // modloaders may not obey this cancel

	}

}
