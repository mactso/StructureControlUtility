package com.mactso.structurecontrolutility.utility;

import java.util.Set;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.structurecontrolutility.config.MyConfig;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GravelBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

public class Utility {
	
	private static final Logger LOGGER = LogManager.getLogger();
	

	public static void dbgChatln(Player p, String msg, int level) {
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
			LOGGER.info("L" + level + " (" 
					+ le.blockPosition().getX() + "," 
					+ le.blockPosition().getY() + ","
					+ le.blockPosition().getZ() + "): " + dMsg);
		}

	}

	public static void sendBoldChat(Player p, String chatMessage, ChatFormatting textColor) {

		MutableComponent component = Component.literal(chatMessage);
		component.setStyle(component.getStyle().withBold(true));
		component.setStyle(component.getStyle().withColor(textColor));
		p.sendSystemMessage(component);


	}

	public static void sendChat(Player p, String chatMessage) {
		sendChat (p, chatMessage, ChatFormatting.DARK_GREEN);
	}
	
	public static void sendChat(Player p, String chatMessage, ChatFormatting textColor) {

		MutableComponent component = Component.literal(chatMessage);
		component.setStyle(component.getStyle().withColor(textColor));
		p.sendSystemMessage(component);
	}
	
	// True: BlockPos is inside a structure bounding box in a fairly new chunk.
	// False: BlockPos is not inside a structure or the chunk is old.
	public static boolean isAreaProtected(LevelAccessor level, BlockPos pos) {
		ChunkAccess chunk = level.getChunk(pos);
		long ageInTicks = chunk.getInhabitedTime();

		BlockState bs = level.getBlockState(pos);
		if ((bs.is(BlockTags.DIRT))) {
			return false;
		}
		if ((bs.is(BlockTags.WOOL))) {
			return false;
		}		
		if ((bs.is(BlockTags.WOOL_CARPETS))) {
			return false;
		}		

		if ((bs.getBlock() instanceof GravelBlock)) {
			return false;
		}

		Set<Entry<Structure, LongSet>> structureReferences = chunk.getAllReferences().entrySet();
		for (Entry<Structure, LongSet> entry : structureReferences) {
			LongIterator longiterator = entry.getValue().iterator();
			while (longiterator.hasNext()) {
				long packedChunkCoordinates = longiterator.nextLong();
				ChunkAccess istructurereader = level.getChunk(ChunkPos.getX(packedChunkCoordinates), ChunkPos.getZ(packedChunkCoordinates),
						ChunkStatus.STRUCTURE_STARTS);
				StructureStart structurestart = istructurereader.getStartForStructure(entry.getKey());
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
	



