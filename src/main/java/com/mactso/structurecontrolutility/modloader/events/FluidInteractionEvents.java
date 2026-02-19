package com.mactso.structurecontrolutility.modloader.events;

import com.mactso.structurecontrolutility.common.logic.ScheduledBlockCleanup;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class FluidInteractionEvents {

	

	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event) {
		
        if (event.getCancellationResult() != InteractionResult.PASS) return;
		
		if (!(event.getEntity() instanceof ServerPlayer sp))
			return ;
		if (sp.isCreative())
			return ;
		
	    ItemStack stack = event.getItemStack();
	    if (stack.getItem() != Items.LAVA_BUCKET) return;
	    
		boolean handled = ScheduledBlockCleanup.handleLavaBucketInProtectedStructure(sp, event.getHand(), event.getPos(), event.getFace());
		
	    if (handled) {
	        event.setCancellationResult(InteractionResult.FAIL);
	    }

	}
	
}
