package com.mactso.structurecontrolutility.events;

import java.util.ArrayList;
import java.util.List;

import com.mactso.structurecontrolutility.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class WorldTickHandler {
	static List<BlockPos> firePosList = new ArrayList<BlockPos>();

	// assumes this event only raised for server worlds. TODO verify.
	@SubscribeEvent
	public static void onWorldTickEvent(LevelTickEvent event) {
		if (event.phase == Phase.START)
			return;
		for (BlockPos pos : firePosList) {
			if (event.level.getBlockState(pos).getBlock() == Blocks.FIRE) {
				event.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
//				System.out.println("put out fire at " + pos);
			}
		}
		firePosList.clear();
	}
	
	public static void addFirePos (BlockPos firePos) {
//		System.out.println ("Saving fire to extinguish at: " + firePos);
		firePosList.add(firePos);
	}

}
