package com.mactso.structurecontrolutility.common.mobeffects;

import com.mactso.structurecontrolutility.modloader.main.Main;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Central registry for all mod MobEffects.
 * Call register() from the main mod constructor to hook into Forge's mod event bus.
 */
public class MyMobEffects {

    // Deferred register for MobEffects
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Main.MODID);

    // RegistryObject for Gloom
    public static final RegistryObject<MobEffect> GLOOM_EFFECT =
            MOB_EFFECTS.register("gloom", GloomEffect::new);

    // Holder for convenient access anywhere in common code
    public static Holder<MobEffect> GLOOM;

    /**
     * Hook the DeferredRegister to the mod event bus.
     * Call from your Main constructor.
     */
    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }

    /**
     * Initialize holders after registries are frozen (CommonSetupEvent).
     */
    /** Initialize holders after registries are frozen */
    public static void init() {
        // Convert the RegistryObject into a Holder<MobEffect>
        GLOOM = GLOOM_EFFECT.getHolder()
                .orElseThrow(() -> new IllegalStateException("GLOOM holder not present!"));
    }

    /** Getter for client-side code (fog environment, etc.) */
    public static Holder<MobEffect> getGloomHolder() {
        return GLOOM;
    }
}
