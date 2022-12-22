package com.mactso.structurecontrolutility.events;

import java.util.List;
import java.util.ListIterator;

import com.mactso.structurecontrolutility.Main;
import com.mactso.structurecontrolutility.utility.Utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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
	// client side variables.
	static long cGameTime = 0;

	@SubscribeEvent
	public static void onBreakingSpeed(BreakSpeed event) {
		// note: This is both server and clientside. client uses to display properly.
		if (event.getEntity() == null) {
			return;
		} else if (event.getEntity().isCreative()) {
			return;
		} else if (!(event.getPosition().isPresent())) {
			return;
		}

		Player p = event.getEntity();
		Vec3 rfv = p.getForward().reverse().scale(0.6);
		LevelAccessor level = p.level;
		long gameTime = ((Level) level).getGameTime();
		RandomSource rand = level.getRandom();
		BlockPos ePos = event.getPosition().get();

		float adjustY = 0;
		if (p.blockPosition().getY() < ePos.getY()) {
			adjustY = -0.5f;
		}
		

		if ((Utility.isAreaProtected(level, ePos))) {
			if (cGameTime < gameTime) {
				cGameTime = gameTime + 10 + rand.nextInt(20);
				level.playSound(null, ePos, SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 0.11f, 0.6f);
				for (int j = 0; j < 7; ++j) {
					double x = 0.5d + (double) ePos.getX() + rand.nextDouble() * (double) 0.1F;
					double y = 0.5d + (double) ePos.getY() + rand.nextDouble() + adjustY;
					double z = 0.5d + (double) ePos.getZ() + rand.nextDouble();
					((ServerLevel) level).sendParticles(ParticleTypes.WITCH, x, y, z , 3, rfv.x, rfv.y, rfv.z, -0.04D);
				}
			}
		}

	}

	@SubscribeEvent
	public static void onBreakBlock(BreakEvent event) {

		// server side only event.
		ServerPlayer sp = (ServerPlayer) event.getPlayer();
		LevelAccessor serverLevel = (ServerLevel) sp.level;
		BlockPos pos = event.getPos();
		BlockState bs = serverLevel.getBlockState(pos);
		Block b = bs.getBlock();

		if (sp.isCreative())
			return;

		if (Utility.isAreaProtected(serverLevel, pos) && event.isCancelable()) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onBlockPlacement(EntityPlaceEvent event) {

		LevelAccessor level = event.getLevel();
		BlockPos pos = event.getPos();
		Block block = event.getPlacedBlock().getBlock();

		if (event.getEntity() instanceof Player p) {
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
		Level level = event.getLevel();
		List<BlockPos> list = event.getAffectedBlocks();
		Vec3 vPos = event.getExplosion().getPosition();
		BlockPos pos = new BlockPos(vPos);

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

		LevelAccessor level = event.getLevel();
		BlockPos ePos = event.getPos();
		MutableBlockPos pos = new MutableBlockPos(ePos.getX(), ePos.getY(), ePos.getZ());

		System.out.println("Neighbor Notify Pos=" + pos + " : ");
		for (Direction d : event.getNotifiedSides()) {
			System.out.println(d.getName() + " " + d.getNormal() + ", ");
			BlockPos dpos = pos.east(d.getStepX()).south(d.getStepZ()).above(d.getStepY());
			if (level.getBlockState(dpos).isFlammable(level, pos, d.getOpposite())) {
				System.out.println(", is flammable");
				if (Utility.isAreaProtected(level, pos)) {
					System.out.println(d.getName() + ", and is protected.");
					WorldTickHandler.addFirePos(pos); // TODO: by dimension later
					return;
				}

			}
//			}
		}
	}

}
