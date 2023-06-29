package com.mactso.structurecontrolutility.events;

import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import com.mactso.structurecontrolutility.Main;
import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.utility.Utility;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.event.world.ExplosionEvent.Detonate;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(bus = Bus.FORGE, modid = Main.MODID)
public class BlockEvents {
	// client side variables.
	static long cGameTime = 0;

	@SubscribeEvent
	public static void blockBreakSpeed(PlayerEvent.BreakSpeed event) {
		// note: This is both server and clientside. client uses to display properly.
		if (event.getEntity() == null) 
			return;
			
		Entity entity = event.getEntity();
		if (!(entity instanceof PlayerEntity)) {
			return;
		}
		
		PlayerEntity p = (PlayerEntity) event.getEntity();
		if (p.isCreative()) 
			return;
		
		BlockPos ePos = event.getPos();
		if (ePos.getY() == -1) 
			return;
		
		World level = p.level;
		long gameTime = ((World) level).getGameTime();
		if (level.getChunk(ePos).getInhabitedTime() > MyConfig.getStopBreakTicks())
			return;

		Random rand = level.getRandom();

		Vector3d rfv = p.getForward().reverse().scale(0.6);

		float adjustY = 0;
		if (p.blockPosition().getY() < ePos.getY()) {
			adjustY = -0.5f;
		}

		if ((Utility.isAreaProtected(level, ePos))) {
			if (cGameTime < gameTime) {
				cGameTime = gameTime + 10 + rand.nextInt(20);
				level.playSound(null, ePos, SoundEvents.FIRE_EXTINGUISH, SoundCategory.AMBIENT, 0.11f, 0.6f);
				for (int j = 0; j < 7; ++j) {
					double x = 0.5d + (double) ePos.getX() + rand.nextDouble() * (double) 0.1F;
					double y = 0.5d + (double) ePos.getY() + rand.nextDouble() + adjustY;
					double z = 0.5d + (double) ePos.getZ() + rand.nextDouble();
					((ServerWorld) level).sendParticles(ParticleTypes.WITCH, x, y, z, 3, rfv.x, rfv.y, rfv.z, -0.04D);
				}
			}
		}

	}

	@SubscribeEvent
	public static void blockBreak(BlockEvent.BreakEvent event) {

		if (event.getPlayer().level.getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakTicks())
			return;

		ServerPlayerEntity sp = (ServerPlayerEntity) event.getPlayer();
		if (sp.isCreative())
			return;

		ServerWorld serverLevel = (ServerWorld) sp.level;

		if (Utility.isAreaProtected((World) serverLevel, event.getPos()) && event.isCancelable()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBlockPlacement(EntityPlaceEvent event) {

		if (event.getEntity().level.getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakTicks())
			return;

		World level = event.getEntity().level;
		BlockPos pos = event.getPos();
		Block block = event.getPlacedBlock().getBlock();

		if (event.getEntity() instanceof PlayerEntity) {
			PlayerEntity p = (PlayerEntity) event.getEntity();
			if (p.isCreative())
				return;
		}

		if (block == Blocks.FIRE) {
			if (Utility.isAreaProtected(level, pos)) {
				if (event.isCancelable()) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onExplosionDetonate(Detonate event) {

		if (event.getWorld().getChunk(new BlockPos(event.getExplosion().getPosition())).getInhabitedTime() > MyConfig
				.getStopExplosionTicks())
			return;
		World level = event.getWorld();
		List<BlockPos> list = event.getAffectedBlocks();

		for (ListIterator<BlockPos> iter = list.listIterator(list.size()); iter.hasPrevious();) {
			BlockPos tPos = iter.previous();
			// System.out.println ("Checking :" + tPos);
			if (Utility.isAreaProtected(level, tPos)) {
				iter.remove();
			}
		}
	}

	@SubscribeEvent
	public static void onNeighborNotifyEvent(BlockEvent.NeighborNotifyEvent event) {

		if (event.getState().getBlock() != Blocks.FIRE) {
			return;
		}

		if (event.getWorld().getChunk(new BlockPos(event.getPos())).getInhabitedTime() > MyConfig.getStopFireTicks())
			return;

		IWorld level = event.getWorld();
		BlockPos ePos = event.getPos();
		Mutable pos = new Mutable(ePos.getX(), ePos.getY(), ePos.getZ());

		Utility.debugMsg(1, pos, "Neighbor Notify Event");
		for (Direction d : event.getNotifiedSides()) {
			Utility.debugMsg(2, d.getName() + " " + d.getNormal() + ", ");
			BlockPos dpos = pos.east(d.getStepX()).south(d.getStepZ()).above(d.getStepY());
			if (level.getBlockState(dpos).isFlammable(level, pos, d.getOpposite())) {
				Utility.debugMsg(2, ", is flammable");
				if (Utility.isAreaProtected(level, pos)) {
					Utility.debugMsg(2, d.getName() + ", and is protected.");
					WorldTickHandler.addFirePos(pos); // TODO: by dimension later
					return;
				}
			}
		}
	}
}
