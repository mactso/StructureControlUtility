package com.mactso.structurecontrolutility.events;

import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.managers.StructureManager;
import com.mactso.structurecontrolutility.managers.StructureManager.StructureItem;
import com.mactso.structurecontrolutility.utility.Utility;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber()
public class LivingUpdateHandler {
	
	@SubscribeEvent
	public static void onLivingUpdate(LivingTickEvent event) {
		
		LivingEntity e = event.getEntity();
		
		if (!(e instanceof ServerPlayer)) return;
		
		ServerPlayer sp = (ServerPlayer) e;
		if (sp.isCreative()) return;
		
		ServerLevel sl = sp.serverLevel();
		String key = StructureManager.insideStructure(sl, sp.blockPosition());
		
		if (key == null) return;

		StructureItem si = StructureManager.getStructureItemOrDefault(key);
		int debug = 4;
		ChunkAccess chunk = sl.getChunk(sp.blockPosition());
		long ageInTicks = chunk.getInhabitedTime();

		if (!si.hasEffects()) return;  
		
		if (ageInTicks > MyConfig.getStructureEffectsTicks()) return;
		
		helperUpdateEffect ((LivingEntity) sp, si.getJumpBoostIntensity(), MobEffects.JUMP);		
		helperUpdateEffect ((LivingEntity) sp, si.getNightVisionIntensity(), MobEffects.MOVEMENT_SLOWDOWN);		
		helperUpdateEffect ((LivingEntity) sp, si.getRegenerationIntensity() , MobEffects.REGENERATION);		
		helperUpdateEffect ((LivingEntity) sp, si.getSlowFallingIntensity(), MobEffects.SLOW_FALLING);	
		helperUpdateEffect ((LivingEntity) sp, si.getWaterBreathingIntensity(), MobEffects.WATER_BREATHING);		
		helperUpdateEffect ((LivingEntity) sp, si.getWeaknessIntensity(), MobEffects.WEAKNESS);		
		
	}


	public static void helperUpdateEffect (LivingEntity e, int intensity, MobEffect me) {
		
		if (intensity == 0) return;
		
		Utility.updateEffect(e, intensity - 1, me,	Utility.FOUR_SECONDS);	
	}
	
}

