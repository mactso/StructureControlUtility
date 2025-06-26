package com.mactso.structurecontrolutility.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.managers.StructureManager;
import com.mactso.structurecontrolutility.managers.StructureManager.StructureItem;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.Tags;

public class Utility {

	public static List<String> unprotectedStructures = Arrays.asList("minecraft:mineshaft", "minecraft:mineshaft_mesa",
			"minecraft:trail_ruins", "minecraft:village_desert", "minecraft:village_plains",
			"minecraft:village_savanna", "minecraft:village_snowy", "minecraft:village_taiga");

	public static int DAMAGE_FIRE = 0;
	public static int DAMAGE_BREAKING = 1;
	public static int DAMAGE_EXPLODING = 2;

	public static int JUMP_BOOST = 0;
	public static int MOVEMENT_SLOWNESS = 0;
	public static int REGENERATION = 0;
	public static int SLOW_FALLING = 0;
	public static int WATER_BREATHING = 0;
	public static int WEAKNESS = 0;

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
			LOGGER.info("L" + level + ":" + dMsg);
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

	// True: BlockPos is inside a structure bounding box within protection time.
	// False: BlockPos is not inside a structure or the structure is no longer
	// protected.
	public static boolean insideProtectedStructure(LevelAccessor level, BlockPos pos, int damageType) {

		ChunkAccess chunk = level.getChunk(pos);
		long ageInTicks = chunk.getInhabitedTime();

		BlockState bs = level.getBlockState(pos);
		boolean isFire = false;
		if (level.getBlockState(pos).getBlock() == Blocks.FIRE)
			isFire = true;
		if (damageType == DAMAGE_FIRE)
			isFire = true;
		if (!isProtectableBlock(bs)) {
			return false;
		}

		Registry<Structure> structRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);

		Set<Entry<Structure, LongSet>> structureReferences = chunk.getAllReferences().entrySet();
		for (Entry<Structure, LongSet> entry : structureReferences) {

			LongIterator longiterator = entry.getValue().iterator();
			while (longiterator.hasNext()) {

				long packedChunkCoordinates = longiterator.nextLong();
				ChunkAccess istructurereader = level.getChunk(ChunkPos.getX(packedChunkCoordinates),
						ChunkPos.getZ(packedChunkCoordinates), ChunkStatus.STRUCTURE_STARTS);

				StructureStart structurestart = istructurereader.getStartForStructure(entry.getKey());

				ResourceLocation key = structRegistry.getKey(entry.getKey());

				StructureItem si = StructureManager.getStructureItemOrDefault(key.toString());

				if (structurestart.getBoundingBox().isInside(pos)) {
					if (isDamageTypeProtectionInEffect(damageType, ageInTicks, si))
						return true;
				}

				// handle fire set just outside the bounding box
				if ((isFire) && (ageInTicks < si.getStopFireTicks())) {
					if (structurestart.getBoundingBox().isInside(pos.below()))
						return true;
					if (structurestart.getBoundingBox().isInside(pos.above()))
						return true;
					if (structurestart.getBoundingBox().isInside(pos.east()))
						return true;
					if (structurestart.getBoundingBox().isInside(pos.west()))
						return true;
					if (structurestart.getBoundingBox().isInside(pos.north()))
						return true;
					if (structurestart.getBoundingBox().isInside(pos.south()))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * fix client side view of the hotbar for non creative
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

	public static void slotChanged(ServerPlayer player, int index, ItemStack itemstack) {
		InventoryMenu menu = player.inventoryMenu;
		player.connection.send(
				new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), index, itemstack));
	}

	public static boolean isProtectableBlock(BlockState bs) {

		if ((bs.getBlock() == Blocks.RAW_GOLD_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.RAW_GOLD_BLOCK)) {
			return false;
		}
		if ((bs.getBlock() == Blocks.RAW_GOLD_BLOCK)) {
			return false;
		}
		if ((bs.getBlock() == Blocks.RAW_GOLD_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.RAW_IRON_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.RAW_COPPER_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.EMERALD_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.DIAMOND_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.COPPER_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.IRON_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.EMERALD_BLOCK)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.BROWN_MUSHROOM)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.RED_MUSHROOM)) {
			return false;
		}

		if ((bs.getBlock() == Blocks.EMERALD_BLOCK)) {
			return false;
		}

		if ((bs.is(BlockTags.DIRT))) {
			return false;
		}

		if ((bs.is(BlockTags.WOOL))) {
			return false;
		}

		if ((bs.is(BlockTags.WOOL_CARPETS))) {
			return false;
		}

		if ((bs.is(BlockTags.LEAVES))) {
			return false;
		}

		if ((bs.is(Tags.Blocks.ORES))) {
			return false;
		}

		if ((bs.is(BlockTags.SAND))) {
			return false;
		}

		if ((bs.is(BlockTags.FLOWERS))) {
			return false;
		}

		if ((bs.is(BlockTags.CROPS))) {
			return false;
		}

		if ((bs.is(BlockTags.ALL_SIGNS))) {
			return false;
		}

		if ((bs.is(BlockTags.BANNERS))) {
			return false;
		}

		if ((bs.getBlock() instanceof WebBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof TallGrassBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof DoublePlantBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof TallSeagrassBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof SeagrassBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof KelpBlock)) {
			return false;
		}

		// TODO: is this valid?
		if ((bs.getBlock() == Blocks.GRAVEL)) {
			return false;
		}

		if ((bs.getBlock() instanceof TorchBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof WallTorchBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof RedstoneTorchBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof TripWireBlock)) {
			return false;
		}

		if ((bs.getBlock() instanceof VineBlock)) {
			return false;
		}

		return true;
	}

	private static boolean isDamageTypeProtectionInEffect(int damageType, long ageInTicks, StructureItem si) {
		if (damageType == DAMAGE_FIRE) {
			if (ageInTicks >= si.getStopFireTicks()) {
				return false;
			}
		} else if (damageType == DAMAGE_BREAKING) {
			if (ageInTicks >= si.getStopBreakingTicks()) {
				return false;
			}
		} else if (damageType == DAMAGE_EXPLODING) {
			if (ageInTicks >= si.getStopExplosionsTicks()) {
				return false;
			}
		}
		return true;
	}

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
