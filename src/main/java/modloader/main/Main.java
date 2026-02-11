// 
package modloader.main;

import common.command.MyCommands;
import common.command.utilities.MyUtilities;
import common.config.MyConfig;
import common.managers.StructureData;
import common.managers.StructureManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("structurecontrolutility")
public class Main {

	    public static final String MODID = "structurecontrolutility"; 
	    
	    public Main()
	    {
	    	MyUtilities.debugMsg(0,MODID + ": Registering Mod.");
	  		FMLJavaModLoadingContext.get().getModEventBus().register(this);
 	        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON,MyConfig.COMMON_SPEC );
			
	    }

	    // Register ourselves for server and other game events we are interested in
		@SubscribeEvent 
		public void preInit (final FMLCommonSetupEvent event) {
			System.out.println("structurecontrolutility: Registering Handlers");
		}       

		@Mod.EventBusSubscriber(bus = Bus.FORGE)
	    public static class ForgeEvents
	    {
			@SubscribeEvent 		
			public static void onCommandsRegistry(final RegisterCommandsEvent event) {
				MyUtilities.debugMsg(0,MODID+": Registering Command Dispatcher");
				MyCommands.register(event.getDispatcher());			
			}
			
			@SubscribeEvent
			public static void onServerStarting(ServerStartingEvent event) {

					StructureData.generateStructuresReport(event);
					StructureManager.structureInit();

			}

	    }

}


