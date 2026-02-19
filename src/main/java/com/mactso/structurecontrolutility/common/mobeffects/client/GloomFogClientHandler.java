package com.mactso.structurecontrolutility.common.mobeffects.client;

import org.joml.Vector4f;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.mobeffects.MyMobEffects;
import com.mojang.blaze3d.shaders.FogShape;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side handler for the GLOOM mob effect fog. Gradually applies steady
 * darkness without pulsing.  Gloom is not opaque.   It just makes things darker.
 * Gloom is intended to be atmospheric.
 * 
 * Note: that Minecraft default starting color is red so it's a bad value to test with
 * because if the code is failing, it will display red.
 * 
 * Note: gloom does not turn on and off instantly.  It ramps up over time and when the
 * gloom effect ends, it ramps off over time.
 * 
 */

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GloomFogClientHandler {

	private static final float MAX_GLOOM = 1.0f;  // Gloom is fully on
	private static final float MIN_GLOOM = 0.0f;  // Gloom is fully off
	
	// These provide a floor when gloom amplifier is maximum so the player isn't blinded.
	private static final float MIN_FOG_START = 1.0f; // Effect starts at 1 meter
	private static final float MIN_FOG_END = 3.0f; // Effect ends at 2 meters away

	// These are arbitrary values that make the gloom effect "look right".
	private static final float DEFAULT_FOG_START = 6.0f;
	private static final float DEFAULT_FOG_END = 16.0f;
	private static final int RAMP_TICKS = 120; // 6 seconds

	/** Current darkness intensity (0 = no darkness, 1 = full darkness) */
	private static float currentVisualIntensity = 0f;

	@SubscribeEvent
	public static void onRenderFog(ViewportEvent.RenderFog event) {

	    // --- ENTRY LOG ---
//	    System.out.println("[GLOOM] onRenderFog: entered");

	    Minecraft mc = Minecraft.getInstance();
	    if (mc.player == null) {
//	        System.out.println("[GLOOM] onRenderFog: no player, exiting");
	        return ;
	    }

	    LivingEntity player = mc.player;
	    MobEffectInstance gloom = player.getEffect(MyMobEffects.getGloomHolder());
	    boolean hasGloom = gloom != null;

	    // --- Early exit if no gloom and effect has finished turning off  ---
	    if (!hasGloom && currentVisualIntensity <= 0.0f) {
//	        System.out.println("[GLOOM] onRenderFog: no gloom & fully faded, exiting");
	        return ;
	    }

	    // --- Determine target intensity ---
	    float targetIntensity;
	    if (hasGloom) {
	        targetIntensity = MAX_GLOOM; // Gloom is active → full effect
	    } else {
	        targetIntensity = MIN_GLOOM; // No gloom → fade out
	        if (currentVisualIntensity <= 0.0f) {
	            //System.out.println("[GLOOM] onRenderFog: finished ramped down and turned off, exiting");
	            return ;
	        }
	    }

	    // --- Turn GLOOM on and off by smoothly ramping of darkness intensity over 100 ticks ---
	    float interpPerTick = 1.0f / RAMP_TICKS; 
	    if (currentVisualIntensity < targetIntensity) { // turning on
	        currentVisualIntensity += interpPerTick;
	        if (currentVisualIntensity > targetIntensity) currentVisualIntensity = targetIntensity;
	    } else if (currentVisualIntensity > targetIntensity) { // turning off
	        currentVisualIntensity -= interpPerTick;
	        if (currentVisualIntensity < targetIntensity) currentVisualIntensity = targetIntensity;
	        if (currentVisualIntensity == 0.0f) {
	            //System.out.println("[GLOOM] onRenderFog: fully faded out, exiting");
	            return ;
	        }
	    }

	    // --- Base fog distances for Gloom effect ---
	    float maxDistance = mc.options.renderDistance().get() ;

	    float baseStart =  DEFAULT_FOG_START;
	    float baseEnd   =  DEFAULT_FOG_END;
//
//
	    // --- Determine amplifier and apply scaling ---
	    int amplifier;
	    if (hasGloom) {
	        amplifier = gloom.getAmplifier();
	        if (amplifier > 8)
	        	amplifier = 8;
	    } else {
	        amplifier = 0;
	    }

	    // float ampFactor = 1.0f + ((float) amplifier / 9.0f) * 0.8f;
	    baseStart = baseStart - ((amplifier+1)*1.2f) * currentVisualIntensity;
	    baseEnd   = baseEnd - ((amplifier+1)*1.5f) *currentVisualIntensity;
	    baseStart = Math.max(baseStart, MIN_FOG_START);
	    baseEnd   = Math.max(baseEnd, MIN_FOG_END);

	    // --- Apply fog distances ---

	    // --- MODIFY EVENT FOG DATA ---
	    // NOTE: in 1.21.5, the transparency is calculated based on these two values.

	    float nearDistance = baseStart;
	    float farDistance = baseEnd;
	    // Apply spherical fog for both sky and terrain
	    switch (event.getMode()) {
	        case FOG_SKY -> {
	            event.setNearPlaneDistance(nearDistance);
	            event.setFarPlaneDistance(farDistance);
	            event.setFogShape(FogShape.SPHERE);
	        }
	        case FOG_TERRAIN -> {
	            event.setNearPlaneDistance(nearDistance);
	            event.setFarPlaneDistance(farDistance);
	            event.setFogShape(FogShape.SPHERE);
	        }
	        default -> {
	    	    event.setNearPlaneDistance(nearDistance);
	    	    event.setFarPlaneDistance(farDistance);	        	
	            event.setFogShape(FogShape.SPHERE);
	        }
	    }
	    

	    
	    // --- Fog color interpolation ---

	    // --- EXIT LOG ---
//	    System.out.println("[GLOOM] onRenderFog: applied fog, exiting");
	    event.setCanceled(true); // insert our color changes- if false, our changes are ignored.
	    return ; 
	}


	@SubscribeEvent
	public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
	    
	    event.setRed(MyConfig.getGloomRed());
	    event.setGreen(MyConfig.getGloomGreen());
	    event.setBlue(MyConfig.getGloomBlue());
    
//	    event.setRed(event.getRed() * (1 - currentVisualIntensity) + targetR * currentVisualIntensity);
//	    event.setGreen(event.getGreen() * (1 - currentVisualIntensity) + targetG * currentVisualIntensity);
//	    event.setBlue(event.getBlue() * (1 - currentVisualIntensity) + targetB * currentVisualIntensity);
	    
	}



}