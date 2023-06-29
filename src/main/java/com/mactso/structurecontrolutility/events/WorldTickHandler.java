package com.mactso.structurecontrolutility.events;

import java.util.ArrayList;
import java.util.List;

import com.mactso.structurecontrolutility.Main;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class WorldTickHandler {
	static List<BlockPos> firePosList = new ArrayList<BlockPos>();

	// assumes this event only raised for server worlds. TODO verify.
	@SubscribeEvent
	public static void onWorldTickEvent(WorldTickEvent event) {
		if (event.phase == Phase.START)
			return;
		for (BlockPos pos : firePosList) {
			if (event.world.getBlockState(pos).getBlock() == Blocks.FIRE) {
				event.world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
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
