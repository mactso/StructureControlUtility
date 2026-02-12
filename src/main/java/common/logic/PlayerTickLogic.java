package common.logic;

import common.command.utilities.MyUtilities;
import common.config.MyConfig;
import common.managers.StructureManager;
import common.managers.StructureManager.StructureItem;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class PlayerTickLogic {

	/**
	 * Applies structure-related effects to the player, including mining fatigue and
	 * other effects. Determines the structure key, retrieves the StructureItem, and
	 * performs all checks.
	 */
	public static void applyStructureEffects(ServerPlayer sp) {
		
		ServerLevel serverLevel = sp.serverLevel();

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
		if (si.isMiningFatigue() && si.getMiningFatigueTicks() > 
		ageInTicks) {
			PlayerTickLogic.helperUpdateEffect(sp, si.getMiningFatigueLevel() - 1, MobEffects.DIG_SLOWDOWN);
		}

		// Other effects
		PlayerTickLogic.helperUpdateEffect(sp, si.getJumpBoostIntensity(), MobEffects.JUMP);
		PlayerTickLogic.helperUpdateEffect(sp, si.getNightVisionIntensity(), MobEffects.NIGHT_VISION);
		PlayerTickLogic.helperUpdateEffect(sp, si.getRegenerationIntensity(), MobEffects.REGENERATION);
		PlayerTickLogic.helperUpdateEffect(sp, si.getSlowFallingIntensity(), MobEffects.SLOW_FALLING);
		PlayerTickLogic.helperUpdateEffect(sp, si.getWaterBreathingIntensity(), MobEffects.WATER_BREATHING);
		PlayerTickLogic.helperUpdateEffect(sp, si.getWeaknessIntensity(), MobEffects.WEAKNESS);
	}

	public static void helperUpdateEffect(ServerPlayer sp, int intensity, Holder<MobEffect> effect) {
		if (intensity == 0)
			return;
		if (effect.equals(MobEffects.NIGHT_VISION)) {
			MobEffectInstance inst = sp.getEffect(MobEffects.NIGHT_VISION);
			if (inst == null || inst.getDuration() < 220) {
				sp.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0, false, false, true));
			}

		} else {
			MyUtilities.updateEffect(sp, intensity - 1, effect, MyUtilities.FOUR_SECONDS);
		}
	}

}
