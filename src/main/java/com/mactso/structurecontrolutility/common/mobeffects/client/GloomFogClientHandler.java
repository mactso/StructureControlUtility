package com.mactso.structurecontrolutility.common.mobeffects.client;

import org.joml.Vector4f;

import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.mobeffects.MyMobEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
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
	private static final float DEFAULT_FOG_START = 8.0f;
	private static final float DEFAULT_FOG_END = 24.0f;
	private static final int RAMP_TICKS = 120; // 6 seconds

	/** Current darkness intensity (0 = no darkness, 1 = full darkness) */
	private static float currentVisualIntensity = 0f;

	@SubscribeEvent
	public static boolean onRenderFog(ViewportEvent.RenderFog event) {

	    // --- ENTRY LOG ---
//	    System.out.println("[GLOOM] onRenderFog: entered");

	    Minecraft mc = Minecraft.getInstance();
	    if (mc.player == null) {
//	        System.out.println("[GLOOM] onRenderFog: no player, exiting");
	        return false;
	    }

	    LivingEntity player = mc.player;
	    MobEffectInstance gloom = player.getEffect(MyMobEffects.getGloomHolder());
	    boolean hasGloom = gloom != null;

	    // --- Early exit if no gloom and effect has finished turning off  ---
	    if (!hasGloom && currentVisualIntensity <= 0.0f) {
//	        System.out.println("[GLOOM] onRenderFog: no gloom & fully faded, exiting");
	        return false;
	    }

	    // --- Determine target intensity ---
	    float targetIntensity;
	    if (hasGloom) {
	        targetIntensity = MAX_GLOOM; // Gloom is active → full effect
	    } else {
	        targetIntensity = MIN_GLOOM; // No gloom → fade out
	        if (currentVisualIntensity <= 0.0f) {
	            //System.out.println("[GLOOM] onRenderFog: finished ramped down and turned off, exiting");
	            return false;
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
	            return false;
	        }
	    }

	    // --- Base fog distances for Gloom effect ---
	    float maxDistance = mc.options.renderDistance().get() * 16f;
	    maxDistance = 64.0f;
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
	    baseStart = baseStart - ((amplifier+1)*1.5f);
	    baseEnd   = baseEnd - ((amplifier+1)*2.5f);
	    baseStart = Math.max(baseStart, MIN_FOG_START);
	    baseEnd   = Math.max(baseEnd, MIN_FOG_END);

//	    // --- INTERMEDIATE LOG ---
//	    System.out.println("[GLOOM] onRenderFog: hasGloom=" + hasGloom
//	        + ", amplifier=" + amplifier
//	        + ", currentDarknessIntensity=" + currentDarknessIntensity
//	        + ", baseStart=" + baseStart
//	        + ", baseEnd=" + baseEnd
//	        + ", maxDistance=" + maxDistance);

	    // --- Apply fog distances ---
	    FogData data = event.getData();
	    data.renderDistanceStart = maxDistance + (baseStart - maxDistance) * currentVisualIntensity;
	    data.renderDistanceEnd   = maxDistance + (baseEnd   - maxDistance) * currentVisualIntensity;
	    data.environmentalStart  = data.renderDistanceStart;
	    data.environmentalEnd    = data.renderDistanceEnd;
	    data.skyEnd              = data.environmentalEnd;
	    data.cloudEnd            = data.environmentalEnd;

	    // --- Fog color interpolation ---
	    Vector4f color = event.getColor();

	    float srvR = MyConfig.getGloomRed() ;
	    float srvG = MyConfig.getGloomGreen() ;
	    float srvB = MyConfig.getGloomBlue() ;
	    float srvA = MyConfig.getGloomAlpha() ; // transparency

	    // interpolate between vanilla color and server-defined gloom color
	    float r = color.x() * (1 - currentVisualIntensity) + srvR * currentVisualIntensity;
	    float g = color.y() * (1 - currentVisualIntensity) + srvG * currentVisualIntensity;
	    float b = color.z() * (1 - currentVisualIntensity) + srvB * currentVisualIntensity;
	    float a = color.w() * (1 - currentVisualIntensity) + srvA * currentVisualIntensity;
	    color.set(r, g, b, a);

	    // --- EXIT LOG ---
//	    System.out.println("[GLOOM] onRenderFog: applied fog, exiting");

	    return true; // insert our color changes- if false, our changes are ignored.
	}


//	@SubscribeEvent
//	public static void onColorFog(ViewportEvent.ComputeFogColor event) {
//		// note this event is ignored if ViewportEvent.RenderFog event returns true;
//	}



}