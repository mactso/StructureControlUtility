package com.mactso.structurecontrolutility.common.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class SpecialEffects {

	public static void doFailureEffects(Entity e, BlockPos pos) {
	
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

	public static void doFailureEffects(Entity e) {
		doFailureEffects(e, e.blockPosition());
	}

}
