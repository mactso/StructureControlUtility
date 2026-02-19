// 
package com.mactso.structurecontrolutility.modloader.main;

import com.mactso.structurecontrolutility.common.command.MyCommands;
import com.mactso.structurecontrolutility.common.config.MyConfig;
import com.mactso.structurecontrolutility.common.managers.StructureData;
import com.mactso.structurecontrolutility.common.managers.StructureManager;
import com.mactso.structurecontrolutility.common.mobeffects.MyMobEffects;
import com.mactso.structurecontrolutility.common.utility.MyUtilities;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("structurecontrolutility")
public class Main {

	    public static final String MODID = "structurecontrolutility"; 
	    
	    public Main(FMLJavaModLoadingContext context)
	    {
			context.getModEventBus().register(this);
			context.registerConfig(ModConfig.Type.COMMON, MyConfig.COMMON_SPEC);
		// Register the MobEffect DeferredRegister to the mod event bus

		// if MyConfig.isOnlyServerMode() is true, then client is not required to have
		// this mod.
		// if MyConfig.isOnlyServerMode() is false, then client is required to have this
		// mod.

		context.registerDisplayTest(() -> "ANY", (remote, isServer) -> MyConfig.isOnlyServerMode());

		// register mob effects
		IEventBus modEventBus = context.getModEventBus();
		modEventBus.addListener(this::handleCommonSetup);

		MyMobEffects.register(modEventBus);

		MyUtilities.debugMsg(0, MODID + ": Registering Mod.");
			
	    }

	    // Register ourselves for server and other game events we are interested in
		@SubscribeEvent 
	public void handleCommonSetup(final FMLCommonSetupEvent event) {
		MyMobEffects.init();
		MyUtilities.debugMsg(0, MODID + ": MobEffect holders initialized.");

		}       

		@Mod.EventBusSubscriber(bus = Bus.FORGE)
	    public static class ForgeEvents
	    {
			@SubscribeEvent 		
			public static void onCommandsRegistry(final RegisterCommandsEvent event) {
			MyUtilities.debugMsg(0, MODID + ": Registering Command Dispatcher");
				MyCommands.register(event.getDispatcher());			
			}
			
			@SubscribeEvent
			public static void onServerStarting(ServerStartingEvent event) {

					StructureManager.structureInit();
			StructureData.generateStructuresReport(event);

			}

	    }

}
