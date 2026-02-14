//package com.mactso.structurecontrolutility.modloader.events;
//
//import java.util.List;
//
//import com.mactso.structurecontrolutility.common.config.MyConfig;
//import com.mactso.structurecontrolutility.common.logic.BlockEventsLogic;
//import com.mactso.structurecontrolutility.modloader.main.Main;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraftforge.common.util.Result;
//import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
//import net.minecraftforge.event.level.BlockEvent;
//import net.minecraftforge.event.level.BlockEvent.BreakEvent;
//import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
//import net.minecraftforge.event.level.ExplosionEvent.Detonate;
//import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
//
//@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
//public class BlockEvents {
//
//	static boolean CANCEL_EVENT = true;
//	static boolean CONTINUE_EVENT = false;
//	
//
//	@SubscribeEvent
//	public static boolean onBlockPlacement(EntityPlaceEvent event) {
//
//		if (!(event.getEntity() instanceof Player p))
//			return CONTINUE_EVENT;
//			
//		if ( p.isCreative())
//			return CONTINUE_EVENT;
//
//		if (event.getEntity().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
//			return CONTINUE_EVENT;
//
//		LevelAccessor level = event.getLevel();
//		BlockPos pos = event.getPos();
//		Block block = event.getPlacedBlock().getBlock();
//
//		if (!BlockEventsLogic.isBlockPlacable(level, pos, p, block)) {
//			return CANCEL_EVENT;
//		}
//		return CONTINUE_EVENT;
//	}
//
//	@SubscribeEvent
//	public static void onBreakBlock(BreakEvent event) {
//		
//		if (event.getPlayer().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
//			return;
//		
//		if (!(event.getPlayer() instanceof ServerPlayer sp))
//			return;
//		
//		if (sp.isCreative())
//			return;
//		
//		BlockPos pos = event.getPos();
//
//		if (BlockEventsLogic.isProtectBlock(sp, pos, event)) {
//			event.setResult(Result.DENY);
//		}
//		
//	}
//
//	@SubscribeEvent
//	public static void onBreakingSpeed(BreakSpeed event) {
//
//		if (!(event.getEntity() instanceof ServerPlayer sp))
//			return;
//		if (sp == null || sp.isCreative() || !event.getPosition().isPresent())
//			return;
//		BlockPos pos = event.getPosition().get();
//
//		BlockEventsLogic.handleBreakingSpeed(sp, pos);
//		
//	}
//
//	@SubscribeEvent
//	public static void onExplosionDetonate(Detonate event) {
//		
//		Level level = event.getLevel();
//		List<BlockPos> affectedBlocks = event.getAffectedBlocks();
//		BlockEventsLogic.handleExplosionDetonate(level, affectedBlocks);
//		
//	}
//
//	@SubscribeEvent
//	public static void onNeighborNotifyEvent(BlockEvent.NeighborNotifyEvent event) {
//		
//		if (event.getState().getBlock() != Blocks.FIRE)
//			return;
//		if (event.getLevel().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopFireTicks())
//			return;
//
//		LevelAccessor level = event.getLevel();
//		BlockPos pos = event.getPos();
//		Iterable<net.minecraft.core.Direction> notifiedSides = event.getNotifiedSides();
//
//		BlockEventsLogic.handleNeighborNotify(level, pos, notifiedSides);
//		
//	}
//}
//
//
