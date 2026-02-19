package com.mactso.structurecontrolutility.common.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * Utility methods for spawning server-side visual and audio effects
 * used to indicate failed actions (e.g., blocked interactions).
 * Witch Particle are used for preventing block breaking
 * Small Fire Particles are used for block fires 
 */

public class SpecialEffects {

	private static final int PARTICLE_ITERATIONS = 7;
	private static final int PARTICLE_COUNT = 3;
	private static final double PARTICLE_SPEED = -0.04D;
	private static final float SOUND_VOLUME = 0.51f;
	private static final float SOUND_PITCH = 0.6f;

	public static void doFireFailureEffects(Entity e, BlockPos pos) {
		doFailureEffectsInternal(e, pos, ParticleTypes.SMALL_FLAME);
	}

	public static void doFailureEffects(Entity e, BlockPos pos) {
		doFailureEffectsInternal(e, pos, ParticleTypes.WITCH);
	}

	public static void doFailureEffects(Entity e) {
		doFailureEffects(e, e.blockPosition());
	}

	/* --------------------------------------------------------------------- */

	private static void doFailureEffectsInternal(
			Entity e,
			BlockPos pos,
			ParticleOptions particle
	) {
		LevelAccessor level = e.level();
		if (!(level instanceof ServerLevel serverLevel))
			return;

		RandomSource rand = level.getRandom();
		Vec3 reverseForward = e.getForward().reverse().scale(0.6);

		double adjustY = e.blockPosition().getY() < pos.getY() ? -0.5D : 0.0D;

		serverLevel.playSound(
				null,
				pos,
				SoundEvents.DISPENSER_FAIL,
				SoundSource.AMBIENT,
				SOUND_VOLUME,
				SOUND_PITCH
		);

		for (int i = 0; i < PARTICLE_ITERATIONS; i++) {
		    double spread = 0.1D;
		    double x = pos.getX() + 0.5 + (rand.nextDouble() - 0.5) * 2 * spread;
		    double y = pos.getY() + 0.5 + rand.nextDouble() + adjustY;
		    double z = pos.getZ() + 0.5 + (rand.nextDouble() - 0.5) * 2 * spread;

			serverLevel.sendParticles(
					particle,
					x, y, z,
					PARTICLE_COUNT,
					reverseForward.x,
					reverseForward.y,
					reverseForward.z,
					PARTICLE_SPEED
			);
		}
	}
}
