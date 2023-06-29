package com.mactso.structurecontrolutility.utility;

import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.structurecontrolutility.config.MyConfig;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GravelBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

public class Utility {
	
	private static final Logger LOGGER = LogManager.getLogger();
	

	public static void dbgChatln(PlayerEntity p, String msg, int level) {
		if (MyConfig.getDebugLevel() > level - 1) {
			sendChat(p, msg, TextFormatting.YELLOW);
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
			LOGGER.info("L" + level + " (" 
					+ le.blockPosition().getX() + "," 
					+ le.blockPosition().getY() + ","
					+ le.blockPosition().getZ() + "): " + dMsg);
		}

	}

	public static void sendBoldChat(PlayerEntity p, String chatMessage, TextFormatting textColor) {

		StringTextComponent component = new StringTextComponent (chatMessage);
		component.getStyle().withBold(true);
		component.getStyle().withColor(Color.fromLegacyFormat(textColor));

		p.sendMessage(component, p.getUUID());


	}

	public static void sendChat(PlayerEntity p, String chatMessage) {
		sendChat (p, chatMessage, TextFormatting.DARK_GREEN);
	}
	
	public static void sendChat(PlayerEntity p, String chatMessage, TextFormatting textColor) {
		StringTextComponent component = new StringTextComponent (chatMessage);
		component.getStyle().withColor(Color.fromLegacyFormat(TextFormatting.GRAY));
		p.sendMessage(component, p.getUUID());
	}
	
	// True: BlockPos is inside a structure bounding box in a fairly new chunk.
	// False: BlockPos is not inside a structure or the chunk is old.
	public static boolean isAreaProtected(IWorld level, BlockPos pos) {
		IChunk chunk = level.getChunk(pos);
		long ageInTicks = chunk.getInhabitedTime();

		BlockState bs = level.getBlockState(pos);
		if ((bs.is(BlockTags.BAMBOO_PLANTABLE_ON))) {
			return false;
		}
		if ((bs.is(BlockTags.WOOL))) {
			return false;
		}		
		if ((bs.is(BlockTags.CARPETS))) {
			return false;
		}		

		if ((bs.getBlock() instanceof GravelBlock)) {
			return false;
		}

		Set<Entry<Structure<?>, LongSet>> structureReferences = chunk.getAllReferences().entrySet();
		for (Entry<Structure<?>, LongSet> entry : structureReferences) {
			LongIterator longiterator = entry.getValue().iterator();
			while (longiterator.hasNext()) {
				long packedChunkCoordinates = longiterator.nextLong();
				IChunk istructurereader = level.getChunk(ChunkPos.getX(packedChunkCoordinates), ChunkPos.getZ(packedChunkCoordinates),
						ChunkStatus.STRUCTURE_STARTS);
				StructureStart<?> structurestart = istructurereader.getStartForFeature(entry.getKey());
				if (structurestart.getBoundingBox().isInside(pos)) {
						return true;
				}

				// handle structure roofs right on the bounding box
				if (level.getBlockState(pos).getBlock() == Blocks.FIRE) {
					if (structurestart.getBoundingBox().isInside(pos.below())) {
						return true;
					}
				}
			}
		}
		return false;
	}


	
}
	



