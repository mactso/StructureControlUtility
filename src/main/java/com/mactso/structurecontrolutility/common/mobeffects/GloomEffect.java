package com.mactso.structurecontrolutility.common.mobeffects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Defines the GLOOM MobEffect.
 * Server-side effect definition only; no rendering logic.
 * Registered by ModMobEffects during mod initialization.
 */
public class GloomEffect extends MobEffect {

    /**
     * Construct GLOOM effect with category HARMFUL and base color (dark gray).
     */
    public GloomEffect() {
        super(MobEffectCategory.HARMFUL, 0x1F1F1F); // dark gray
    }

    
    /**
     * Apply effect each tick on server side.
     * We do not alter entity attributes here; ramp logic handled elsewhere.
     */
    @Override
    public boolean applyEffectTick(LivingEntity p_333541_, int p_333570_) {
        // No direct effect on entity; ramp handled by GloomEffectApplier and client-side code
        return true;
    }

    /**
     * Determines whether the effect should tick this game tick.
     * Always false; server logic will manually apply/remove effect.
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public void onEffectAdded(LivingEntity entity, int amplifier) {
        // Optionally play sound or trigger event when GLOOM is added
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        // Hook for future use if needed
    }
}