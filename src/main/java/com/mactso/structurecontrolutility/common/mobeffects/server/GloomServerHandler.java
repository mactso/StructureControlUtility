package com.mactso.structurecontrolutility.common.mobeffects.server;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.mobeffects.MyMobEffects;
import com.mactso.structurecontrolutility.common.utility.MyUtilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Server-side authority for applying darkness-related effects.
 *
 * This class must NEVER reference client-only classes.
 */
public final class GloomServerHandler {

    // Prevent instantiation
    private GloomServerHandler() {}

    
    /**
     * Applies the Gloom or vanilla Darkness effect to the player depending on server mode.
     * If the effect is already present and has sufficient duration, it is not reapplied.
     *
     * @param sp        the server player
     * @param amplifier the effect strength (0 = off, 1–9 valid)
     */
    public static void applyGloomOrDarkness(ServerPlayer sp, int amplifier) {

        // Clamp amplifier to safe range 0–9
        amplifier = Math.max(0, Math.min(amplifier, 9));
        if (amplifier == 0) return; // 0 = effect disabled


        if (MyConfig.isOnlyServerMode()) {
            // --- Use vanilla Minecraft Darkness ---
            MobEffectInstance currentDarkness = sp.getEffect(MobEffects.DARKNESS);
            if (currentDarkness != null && currentDarkness.getDuration() > MyUtilities.FOUR_SECONDS) return;

            sp.addEffect(new MobEffectInstance(
                    MobEffects.DARKNESS,
                    MyUtilities.FOUR_SECONDS*3, // 240 ticks
                    amplifier - 1,            // Minecraft amplifiers start at 0
                    true,                     // ambient
                    false,                    // particles
                    false                     // icon
            ));
        } else {
            // --- Use custom Gloom effect that doesn't pulse---
            MobEffectInstance currentGloom = sp.getEffect(MyMobEffects.GLOOM);
            if (currentGloom != null && currentGloom.getDuration() > 20) return;

            sp.addEffect(new MobEffectInstance(
                    MyMobEffects.GLOOM,
                    MyUtilities.FOUR_SECONDS, // 80 ticks
                    amplifier - 1,            // Gloom amplifier starts at 0
                    true,                     // ambient
                    false,                    // particles
                    false                     // icon
            ));
        }
    }
    
}
