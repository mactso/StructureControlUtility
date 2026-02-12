package com.mactso.structurecontrolutility.modloader.events;

import java.util.List;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.logic.BlockEventsLogic;
import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.level.ExplosionEvent.Detonate;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class BlockEvents {

	@SubscribeEvent
	public static void onBlockPlacement(EntityPlaceEvent event) {
		if (event.getEntity().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
			return;

		BlockEventsLogic.handleBlockPlacement(event);
	}

	@SubscribeEvent
	public static void onBreakBlock(BreakEvent event) {
		if (event.getPlayer().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
			return;
		if (!(event.getPlayer() instanceof ServerPlayer sp))
			return;
		if (sp.isCreative())
			return;
		BlockPos pos = event.getPos();

		BlockEventsLogic.handleBlockBreak(sp, pos, event);
		
	}

	@SubscribeEvent
	public static void onBreakingSpeed(BreakSpeed event) {

		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		if (sp == null || sp.isCreative() || !event.getPosition().isPresent())
			return;
		BlockPos pos = event.getPosition().get();

		BlockEventsLogic.handleBreakingSpeed(sp, pos);
		
	}

	@SubscribeEvent
	public static void onExplosionDetonate(Detonate event) {
		
		Level level = event.getLevel();
		List<BlockPos> affectedBlocks = event.getAffectedBlocks();
		BlockEventsLogic.handleExplosionDetonate(level, affectedBlocks);
		
	}

	@SubscribeEvent
	public static void onNeighborNotifyEvent(BlockEvent.NeighborNotifyEvent event) {
		
		if (event.getState().getBlock() != Blocks.FIRE)
			return;
		if (event.getLevel().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopFireTicks())
			return;

		LevelAccessor level = event.getLevel();
		BlockPos pos = event.getPos();
		Iterable<net.minecraft.core.Direction> notifiedSides = event.getNotifiedSides();

		BlockEventsLogic.handleNeighborNotify(level, pos, notifiedSides);
		
	}
}

//package modloader.events;
//
//import java.util.List;
//import java.util.ListIterator;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import common.command.utilities.MyUtilities;
//import common.config.MyConfig;
//import common.logic.SpecialEffects;
//import modloader.main.Main;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.BlockPos.MutableBlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.util.RandomSource;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.BucketItem;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.material.Fluids;
//import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
//import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
//import net.minecraftforge.event.level.BlockEvent;
//import net.minecraftforge.event.level.BlockEvent.BreakEvent;
//import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
//import net.minecraftforge.event.level.ExplosionEvent.Detonate;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
//
//@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
//public class BlockEvents {
//	// client side variables.
//	static long cGameTime = 0;
//
//	@SubscribeEvent
//	public static void onRightClickBlock(RightClickBlock event) {
//
//		Player e = event.getEntity();
//
//		if (!(e instanceof Player))
//			return;
//
//		if (!(event.getEntity() instanceof ServerPlayer))
//			return;
//		ServerPlayer sp = (ServerPlayer) e;
//
//		@NotNull
//		InteractionHand hand = event.getHand();
//		Item item = sp.getItemInHand(hand).getItem();
//
//		if (!(item instanceof BucketItem)) {
//			return;
//		}
//
//		BucketItem bucket = (BucketItem) item;
//
//		if (bucket.getFluid() != Fluids.LAVA) {
//			return;
//		}
//
//		if (!(MyUtilities.insideProtectedStructure(sp.serverLevel(), sp.blockPosition(), MyUtilities.DAMAGE_FIRE))) {
//			return;
//		}
//
//		if (event.isCancelable()) {
//			event.setCanceled(true);
//		}
//
//		@NotNull
//		BlockPos pos = event.getPos();
//
//		@Nullable
//		Direction d = event.getFace();
//
//		BlockPos dpos = pos;
//		if (d != null) {
//			dpos = pos.relative(d);
//		}
//
//		WorldTickHandler.addLavaPos(dpos);
//		SpecialEffects.doFailureEffects(sp, dpos);
//
//	}
//
//	@SubscribeEvent
//	public static void onBlockPlacement(EntityPlaceEvent event) {
//
//		if (event.getEntity().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
//			return;
//
//		LevelAccessor level = event.getLevel();
//		RandomSource rand = level.getRandom();
//		BlockPos pos = event.getPos();
//		Block block = event.getPlacedBlock().getBlock();
//
//		if (event.getEntity() instanceof Player p) {
//			if (p.isCreative())
//				return;
//		}
//
//		if (block == Blocks.FIRE) {
//			if (MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_FIRE)) {
//				if (event.isCancelable()) {
//					event.setCanceled(true);
//				}
//			}
//		}
//
//		if (MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_BREAKING)) {
//			if (MyUtilities.isProtectableBlock(event.getState())) {
//				if (event.isCancelable()) {
//					if (event.getEntity() instanceof ServerPlayer sp) {
//						MyUtilities.updateHands(sp);
//					}
//					SpecialEffects.doFailureEffects(event.getEntity(), event.getPos());
//					event.setCanceled(true);
//				}
//			}
//		}
//
//	}
//
//	@SubscribeEvent
//	public static void onBreakBlock(BreakEvent event) {
//
//		if (event.getPlayer().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
//			return;
//
//		ServerPlayer sp = (ServerPlayer) event.getPlayer();
//		if (sp.isCreative())
//			return;
//
//		ServerLevel serverLevel = (ServerLevel) sp.level();
//
//		if (MyUtilities.insideProtectedStructure((LevelAccessor) serverLevel, event.getPos(), MyUtilities.DAMAGE_BREAKING)
//				&& event.isCancelable()) {
//			SpecialEffects.doFailureEffects(sp, event.getPos());
//			event.setCanceled(true);
//		}
//	}
//
//	@SubscribeEvent
//	public static void onBreakingSpeed(BreakSpeed event) {
//		// note: This is both server and clientside. client uses to display properly.
//		if (event.getEntity() == null) {
//			return;
//		} else if (event.getEntity().isCreative()) {
//			return;
//		} else if (!(event.getPosition().isPresent())) {
//			return;
//		}
//		BlockPos ePos = event.getPosition().get();
//		Player p = event.getEntity();
//		LevelAccessor level = p.level();
//		RandomSource rand = level.getRandom();
//
//		// Temp Hack
//		if (level.isClientSide()) {
//			return;
//		}
//
//		long gameTime = ((Level) level).getGameTime();
//		if (level.getChunk(ePos).getInhabitedTime() > MyConfig.getStopBreakingTicks())
//			return;
//
//		if ((MyUtilities.insideProtectedStructure(level, ePos, MyUtilities.DAMAGE_BREAKING))) {
//			if (cGameTime < gameTime) {
//				cGameTime = gameTime + 10 + rand.nextInt(20);
//				SpecialEffects.doFailureEffects(p, ePos);
//			}
//		}
//
//	}
//
//	@SubscribeEvent
//	public static void onExplosionDetonate(Detonate event) {
//
//		Level level = event.getLevel();
//		List<BlockPos> list = event.getAffectedBlocks();
//
//		for (ListIterator<BlockPos> iter = list.listIterator(list.size()); iter.hasPrevious();) {
//			BlockPos tPos = iter.previous();
//			// System.out.println ("Checking :" + tPos);
//			if (MyUtilities.insideProtectedStructure(level, tPos, MyUtilities.DAMAGE_EXPLODING)) {
//				iter.remove();
//			}
//		}
//	}
//
//	@SubscribeEvent
//	public static void onNeighborNotifyEvent(BlockEvent.NeighborNotifyEvent event) {
//
//		if (event.getState().getBlock() != Blocks.FIRE) {
//			return;
//		}
//		if (event.getLevel().getChunk(new BlockPos(event.getPos())).getInhabitedTime() > MyConfig.getStopFireTicks())
//			return;
//
//		LevelAccessor level = event.getLevel();
//		BlockPos ePos = event.getPos();
//		MutableBlockPos pos = new MutableBlockPos(ePos.getX(), ePos.getY(), ePos.getZ());
//
//		MyUtilities.debugMsg(1, pos, "Neighbor Notify Event");
//		for (Direction d : event.getNotifiedSides()) {
//			MyUtilities.debugMsg(2, d.getName() + " " + d.getNormal() + ", ");
//			BlockPos dpos = pos.relative(d);
//			if (level.getBlockState(dpos).isFlammable(level, pos, d.getOpposite())) {
//				MyUtilities.debugMsg(2, ", is flammable");
//				if (MyUtilities.insideProtectedStructure(level, pos, MyUtilities.DAMAGE_FIRE)) {
//					MyUtilities.debugMsg(2, d.getName() + ", and is protected.");
//					WorldTickHandler.addFirePos(pos); // TODO: by dimension later, TODO: Should this be dpos ?
//					WorldTickHandler.addFirePos(dpos);
//					return;
//				}
//			}
//		}
//	}
//}
