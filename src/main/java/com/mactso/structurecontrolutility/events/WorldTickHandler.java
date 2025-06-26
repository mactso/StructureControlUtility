package com.mactso.structurecontrolutility.events;

import java.util.ArrayList;
import java.util.List;

import com.mactso.structurecontrolutility.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class WorldTickHandler {
	static List<BlockPos> firePosList = new ArrayList<BlockPos>();
	static List<BlockPos> lavaPosList = new ArrayList<BlockPos>();

	@SubscribeEvent
	public static void onWorldTickEvent(LevelTickEvent.Post event) {

		List<BlockPos> workFirePosList = new ArrayList<BlockPos>();

		synchronized (firePosList) {
			workFirePosList.addAll(firePosList);
			firePosList.clear();
		}
		for (BlockPos pos : workFirePosList) {
			if (event.level.getBlockState(pos).getBlock() == Blocks.FIRE) {
				event.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			}
		}

		synchronized (lavaPosList)
		{
			for (BlockPos pos : lavaPosList) {
				if (event.level.getBlockState(pos).getBlock() == Blocks.LAVA) {
					event.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
				}
			}
			lavaPosList.clear();
		}

	}

	public static void addFirePos(BlockPos firePos) {
		synchronized (firePosList) {
			firePosList.add(firePos);
		}
	}

	public static void addLavaPos(BlockPos lavaPos) {
		synchronized (lavaPosList) {
			lavaPosList.add(lavaPos);
		}
	}

}
