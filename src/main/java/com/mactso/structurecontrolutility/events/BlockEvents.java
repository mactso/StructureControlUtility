package com.mactso.structurecontrolutility.events;

import java.util.List;
import java.util.ListIterator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mactso.structurecontrolutility.Main;
import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.utility.Utility;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
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

	private static void doFailureEffects(Entity e) {
		doFailureEffects(e, e.blockPosition());
	}

	private static void doFailureEffects(Entity e, BlockPos pos) {

		LevelAccessor level = e.level();
		RandomSource rand = level.getRandom();
		Vec3 rfv = e.getForward().reverse().scale(0.6);

		float adjustY = 0;
		if (e.blockPosition().getY() < pos.getY()) {
			adjustY = -0.5f;
		}

		if (level instanceof ServerLevel) {
			level.playSound(null, pos, SoundEvents.DISPENSER_FAIL, SoundSource.AMBIENT, 0.51f, 0.6f);
		}

		for (int j = 0; j < 7; ++j) {
			double x = 0.5d + (double) pos.getX() + rand.nextDouble() * (double) 0.1F;
			double y = 0.5d + (double) pos.getY() + rand.nextDouble() + adjustY;
			double z = 0.5d + (double) pos.getZ() + rand.nextDouble();
			((ServerLevel) level).sendParticles(ParticleTypes.WITCH, x, y, z, 3, rfv.x, rfv.y, rfv.z, -0.04D);
		}
	}

	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event) {

		Player e = event.getEntity();

		if (!(e instanceof Player))
			return;

		if (!(event.getEntity() instanceof ServerPlayer))
			return;
		ServerPlayer sp = (ServerPlayer) e;

		@NotNull
		InteractionHand hand = event.getHand();
		Item item = sp.getItemInHand(hand).getItem();

		if (!(item instanceof BucketItem)) {
			return;
		}

		BucketItem bucket = (BucketItem) item;

		if (bucket.getFluid() != Fluids.LAVA) {
			return;
		}

		if (!(Utility.insideProtectedStructure(sp.serverLevel(), sp.blockPosition(), Utility.DAMAGE_FIRE))) {
			return;
		}

		if (event.isCancelable()) {
			event.setCanceled(true);
		}

		@NotNull
		BlockPos pos = event.getPos();

		@Nullable
		Direction d = event.getFace();

		BlockPos dpos = pos;
		if (d != null) {
			dpos = pos.relative(d);
		}

		WorldTickHandler.addLavaPos(dpos);
		doFailureEffects(sp, dpos);

	}

//	@SubscribeEvent
//	public static void onFluidPlacement(FluidPlaceBlockEvent event) {
//
//		LevelAccessor level = event.getLevel();
//		BlockPos pos = event.getPos();
//
//		if (event.getState() == Blocks.LAVA.defaultBlockState()) {
//			if (Utility.insideProtectedStructure(level, pos, Utility.DAMAGE_FIRE)) {
//				if (event.isCancelable()) {
//					event.setCanceled(true);
//				}
//			}
//		}
//
//	}

	@SubscribeEvent
	public static void onBlockPlacement(EntityPlaceEvent event) {

		if (event.getEntity().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
			return;

		LevelAccessor level = event.getLevel();
		RandomSource rand = level.getRandom();
		BlockPos pos = event.getPos();
		Block block = event.getPlacedBlock().getBlock();

		if (event.getEntity() instanceof Player p) {
			if (p.isCreative())
				return;
		}

		if (block == Blocks.FIRE) {
			if (Utility.insideProtectedStructure(level, pos, Utility.DAMAGE_FIRE)) {
				if (event.isCancelable()) {
					event.setCanceled(true);
				}
			}
		}

		if (Utility.insideProtectedStructure(level, pos, Utility.DAMAGE_BREAKING)) {
			if (Utility.isProtectableBlock(event.getState())) {
				if (event.isCancelable()) {
					if (event.getEntity() instanceof ServerPlayer sp) {
						Utility.updateHands( sp);
					}
					doFailureEffects(event.getEntity(), event.getPos());
					event.setCanceled(true);
				}
			}
		}

	}

	@SubscribeEvent
	public static void onBreakBlock(BreakEvent event) {

		if (event.getPlayer().level().getChunk(event.getPos()).getInhabitedTime() > MyConfig.getStopBreakingTicks())
			return;

		ServerPlayer sp = (ServerPlayer) event.getPlayer();
		if (sp.isCreative())
			return;

		ServerLevel serverLevel = (ServerLevel) sp.level();

		if (Utility.insideProtectedStructure((LevelAccessor) serverLevel, event.getPos(), Utility.DAMAGE_BREAKING)
				&& event.isCancelable()) {
			doFailureEffects(sp, event.getPos());
			event.setCanceled(true);
		}
	}

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
		BlockPos ePos = event.getPosition().get();
		Player p = event.getEntity();
		LevelAccessor level = p.level();
		RandomSource rand = level.getRandom();

		// Temp Hack
		if (level.isClientSide()) {
			return;
		}

		long gameTime = ((Level) level).getGameTime();
		if (level.getChunk(ePos).getInhabitedTime() > MyConfig.getStopBreakingTicks())
			return;

		if ((Utility.insideProtectedStructure(level, ePos, Utility.DAMAGE_BREAKING))) {
			if (cGameTime < gameTime) {
				cGameTime = gameTime + 10 + rand.nextInt(20);
				doFailureEffects(p, ePos);
			}
		}

	}

	@SubscribeEvent
	public static void onExplosionDetonate(Detonate event) {

		Level level = event.getLevel();
		List<BlockPos> list = event.getAffectedBlocks();

		for (ListIterator<BlockPos> iter = list.listIterator(list.size()); iter.hasPrevious();) {
			BlockPos tPos = iter.previous();
			// System.out.println ("Checking :" + tPos);
			if (Utility.insideProtectedStructure(level, tPos, Utility.DAMAGE_EXPLODING)) {
				iter.remove();
			}
		}
	}

	@SubscribeEvent
	public static void onNeighborNotifyEvent(BlockEvent.NeighborNotifyEvent event) {

		if (event.getState().getBlock() != Blocks.FIRE) {
			return;
		}
		if (event.getLevel().getChunk(new BlockPos(event.getPos())).getInhabitedTime() > MyConfig.getStopFireTicks())
			return;

		LevelAccessor level = event.getLevel();
		BlockPos ePos = event.getPos();
		MutableBlockPos pos = new MutableBlockPos(ePos.getX(), ePos.getY(), ePos.getZ());

		Utility.debugMsg(1, pos, "Neighbor Notify Event");
		for (Direction d : event.getNotifiedSides()) {
			Utility.debugMsg(2, d.getName() + " " + d.getUnitVec3i()+ ", ");
			BlockPos dpos = pos.relative(d);
			if (level.getBlockState(dpos).isFlammable(level, pos, d.getOpposite())) {
				Utility.debugMsg(2, ", is flammable");
				if (Utility.insideProtectedStructure(level, pos, Utility.DAMAGE_FIRE)) {
					Utility.debugMsg(2, d.getName() + ", and is protected.");
					WorldTickHandler.addFirePos(pos); // TODO: by dimension later, TODO: Should this be dpos ?
					WorldTickHandler.addFirePos(dpos);
					return;
				}
			}
		}
	}
}
