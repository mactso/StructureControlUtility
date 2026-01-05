// 
package com.mactso.structurecontrolutility;

import com.mactso.structurecontrolutility.commands.MyCommands;
import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.managers.StructureManager;
import com.mactso.structurecontrolutility.util.StructureData;
import com.mactso.structurecontrolutility.utility.MyUtility;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("structurecontrolutility")
public class Main {

	    public static final String MODID = "structurecontrolutility"; 
		// private static final Logger LOGGER = LogManager.getLogger();
	    
	    public Main(FMLJavaModLoadingContext context)
	    {
			context.registerConfig(ModConfig.Type.COMMON, MyConfig.COMMON_SPEC);
	        FMLCommonSetupEvent.getBus(context.getModBusGroup()).addListener(this::handleCommonSetup);
	    	MyUtility.debugMsg(0,MODID + ": Registering Mod.");
			
	    }

	    // Register ourselves for server and other game events we are interested in
		@SubscribeEvent 
		public void handleCommonSetup (final FMLCommonSetupEvent event) {
			// nothing happens in here any more.
		}       

		@Mod.EventBusSubscriber(bus = Bus.FORGE)
	    public static class ForgeEvents
	    {
			@SubscribeEvent 		
			public static void onCommandsRegistry(final RegisterCommandsEvent event) {
				MyUtility.debugMsg(0,MODID+": Registering Command Dispatcher");
				MyCommands.register(event.getDispatcher());			
			}
			
			@SubscribeEvent
			public static void onServerStarting(ServerStartingEvent event) {

					StructureData.generateStructuresReport(event);
					StructureManager.structureInit();

			}

	    }

}


