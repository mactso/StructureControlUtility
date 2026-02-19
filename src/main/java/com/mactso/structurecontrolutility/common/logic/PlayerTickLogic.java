package com.mactso.structurecontrolutility.common.logic;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.managers.StructureManager;
import com.mactso.structurecontrolutility.common.managers.StructureManager.StructureItem;
import com.mactso.structurecontrolutility.common.mobeffects.server.GloomServerHandler;
import com.mactso.structurecontrolutility.common.utility.MyUtilities;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * Controls structure-related effects that apply to players.
 */

public class PlayerTickLogic {

	/**
	 * Applies structure-related effects to the player, including mining fatigue and
	 * other effects. Determines the structure key, retrieves the StructureItem, and
	 * performs all checks.
	 */
	public static void applyStructureEffects(ServerPlayer sp) {

		ServerLevel serverLevel = sp.level();

		String structureKey = StructureManager.insideStructure(serverLevel, sp.blockPosition());
		if (structureKey == null)
			return;

		StructureItem si = StructureManager.getStructureItemOrDefault(structureKey);

		// --- Grab chunk and age in ticks once ---
		long ageInTicks = serverLevel.getChunk(sp.blockPosition()).getInhabitedTime();

		if (ageInTicks > MyConfig.getStructureEffectsTicks())
			return;
		if (!si.hasEffects() && !si.isMiningFatigue())
			return;

		// Mining fatigue
		if (si.isMiningFatigue() && si.getMiningFatigueTicks() > ageInTicks) {
			PlayerTickLogic.helperUpdateEffect(sp, si.getMiningFatigueAmplifier() + 1, MobEffects.MINING_FATIGUE);
		}

		// Other effects
		PlayerTickLogic.helperUpdateEffect(sp, si.getJumpBoostAmplifier(), MobEffects.JUMP_BOOST);
		PlayerTickLogic.helperUpdateEffect(sp, si.getMoveSlownessAmplifier(), MobEffects.SLOWNESS);
		PlayerTickLogic.helperUpdateEffect(sp, si.getRegenerationAmplifier(), MobEffects.REGENERATION);
		PlayerTickLogic.helperUpdateEffect(sp, si.getSlowFallingAmplifier(), MobEffects.SLOW_FALLING);
		PlayerTickLogic.helperUpdateEffect(sp, si.getWaterBreathingAmplifier(), MobEffects.WATER_BREATHING);
		PlayerTickLogic.helperUpdateEffect(sp, si.getWeaknessAmplifier(), MobEffects.WEAKNESS);
		// Delegate to server adapter to decide: Gloom (client-side visual) or vanilla
		// Darkness
		GloomServerHandler.applyGloomOrDarkness(sp, si.getGloomOrDarknessAmplifier());
	}

	// Note, 0 is treated as "off" in this method.
	public static void helperUpdateEffect(ServerPlayer sp, int amplifier, Holder<MobEffect> effect) {

		if (amplifier == 0) // 0 == effect not applied.
			return;

		amplifier -= 1; // now rebase to start at 0

		MyUtilities.updateEffect(sp, amplifier, effect, MyUtilities.FOUR_SECONDS);

	}

}
