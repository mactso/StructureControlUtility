package com.mactso.structurecontrolutility.common.utility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

public class MyUtilities {

	public static int TICKS_PER_MINUTE = 1200;

	private static final Logger LOGGER = LogManager.getLogger();

	public static final int FOUR_SECONDS = 80;

	public static void dbgChatln(ServerPlayer p, String msg, int level) {
		if (MyConfig.getDebugLevel() > level - 1) {
			sendChat(p, msg, ChatFormatting.YELLOW);
		}
	}

	public static void debugMsg(int level, String dMsg) {

		if (MyConfig.getDebugLevel() > level - 1) {
			LOGGER.info(Main.MODID + " L" + level + ":" + dMsg);
		}

	}

	public static void debugMsg(int level, BlockPos pos, String dMsg) {
		
		if (MyConfig.getDebugLevel() > level - 1) {
			LOGGER.info("L" + level + " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "): " + dMsg);
		}

	}

	public static void debugMsg(int level, LivingEntity le, String dMsg) {

		if (MyConfig.getDebugLevel() > level - 1) {
			LOGGER.info("L" + level + " (" + le.blockPosition().getX() + "," + le.blockPosition().getY() + ","
					+ le.blockPosition().getZ() + "): " + dMsg);
		}

	}

	public static void sendBoldChat(ServerPlayer p, String chatMessage, ChatFormatting textColor) {

		MutableComponent component = Component.literal(chatMessage);
		component.setStyle(component.getStyle().withBold(true));
		component.setStyle(component.getStyle().withColor(textColor));
		p.sendSystemMessage(component);

	}

	public static void sendChat(ServerPlayer p, String chatMessage) {
		sendChat(p, chatMessage, ChatFormatting.DARK_GREEN);
	}

	public static void sendChat(ServerPlayer p, String chatMessage, ChatFormatting textColor) {

		MutableComponent component = Component.literal(chatMessage);
		component.setStyle(component.getStyle().withColor(textColor));
		p.sendSystemMessage(component);
	}

	/**
	 * fix client side view of the hotbar 
	 */
	public static void updateHands(ServerPlayer player)
	{
		
		final int OFF_HAND_SLOT = 45;
		final int HOT_BAR_SLOT = 36;
		
		if (player.connection == null)
			return;
		
		if (!player.getInventory().getSelectedItem().isEmpty()) {
			slotChanged(player, HOT_BAR_SLOT + player.getInventory().getSelectedSlot(), player.getInventory().getSelectedItem());
		}
		
		if (!player.getOffhandItem().isEmpty())
			slotChanged(player, OFF_HAND_SLOT, player.getOffhandItem());
	}
	/*
	 * tell the client that an inventory slot changed with a network packet. 
	 */
	public static void slotChanged(ServerPlayer player, int index, ItemStack itemstack)
	{
		InventoryMenu menu = player.inventoryMenu;
    	player.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), index, itemstack));
	}

	// generic utility to add potion effect to an entity

	public static void updateEffect(LivingEntity e, int amplifier, Holder<MobEffect> mobEffect, int duration) {

		MobEffectInstance ei = e.getEffect(mobEffect);
		if (amplifier == 10) {
			amplifier = 20; // player "plaid" speed.
		}
		if (ei != null) {
			if (amplifier > ei.getAmplifier()) {
				e.removeEffect(mobEffect);
			}
			if (amplifier == ei.getAmplifier() && ei.getDuration() > 10) {
				return;
			}
			if (ei.getDuration() > 10) {
				return;
			}
			e.removeEffect(mobEffect);
		}
		e.addEffect(new MobEffectInstance(mobEffect, duration, amplifier, true, true));
		return;
	}

}
