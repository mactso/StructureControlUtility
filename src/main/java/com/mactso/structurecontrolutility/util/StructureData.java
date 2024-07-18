package com.mactso.structurecontrolutility.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mactso.structurecontrolutility.config.MyConfig;
import com.mactso.structurecontrolutility.utility.Utility;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo.StructureInfo.Builder;
import net.minecraftforge.event.server.ServerStartingEvent;

public class StructureData {
	private static final Logger LOGGER = LogManager.getLogger();

	static int reportlinenumber = 0;

	static {
		initReports();
	}

	public static void initReports() {
		File fd = new File("config/structurecontrolutility");
		if (!fd.exists())
			fd.mkdir();
		File fs = new File("config/spawnbalanceutility/structures.rpt");
		if (fs.exists())
			fs.delete();
	}

	public static void onStructure(Holder<Structure> struct, Builder builder) {

		String threadname = Thread.currentThread().getName();

		// no processing at load time.

	}

	public static void generateStructuresReport(ServerStartingEvent event) {
		PrintStream p = null;
		try {
			p = new PrintStream(new FileOutputStream("config/structurecontrolutility/structures.rpt", false));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (p == null) {
			p = System.out;
		}

		p.println("* This is the Structures report file that is output every time the server starts.");
		p.println("* ");
		p.println("* Structure Control Utility will use this file ONLY if it is renamed to structures.csv.");
		p.println("* Lines starting with '*' are comments and ignored");
		p.println("* ");
		p.println(
				"* Line Number, Structure Name, EffectsString, effects Minutes, Stop Fire Minutes, Stop Block Breaking Minutes, Stop Explosions Minutes ");
		p.println(
				"* Supported Structure Effects are : Jump Boost, Movement Slowness, Regeneration, Slow Falling, Water Breathing, and Weakness");
		p.println(
				"* Each digit of the Effects string is an effect power from 0 (off) to 9");
		p.println(
				"* Example Line : Minecraft:Mansion, Effects:110002, 721, 1440, 720, 999999 ");
		p.println(
				"* The above line sets Jump Boost 1, Movement Slowness 1, Weakness 2 for 721 minutes.");
		p.println(
				"* It also prevents fire for 1440 minutes (12 hours), digging for 720 minutes, and explosions for 999999 minutes ");
		

		int linenumber = 0;
		MinecraftServer server = event.getServer();
		RegistryAccess dynreg = server.registryAccess();
		Registry<Structure> structRegistry = dynreg.registryOrThrow(Registries.STRUCTURE);
		
		int effectsMinutes = MyConfig.getEffectsMinutes();
		int stopFireMinutes = MyConfig.getStopFireMinutes();
		int stopBreakingMinutes = MyConfig.getStopBreakingMinutes();
		int stopExplosionsMinutes = MyConfig.getStopExplosionMinutes();
		
		for (Structure struct : structRegistry) {
			String modAndStructure = structRegistry.getKey(struct).toString();
			// if minecraft mines, protect for 0 seconds.
			// otherwise use the default protection time.

			String effectFlags = "Effects:000000";
			if (Utility.unprotectedStructures.contains(modAndStructure)) {
				stopFireMinutes = 0;
				stopBreakingMinutes = 0;
				stopExplosionsMinutes = 0;

			}
			p.println(++linenumber + ", " + modAndStructure + ", " + effectFlags + ", " + effectsMinutes + ", " + stopFireMinutes + ", " + stopBreakingMinutes
					+ ", " + stopExplosionsMinutes);

		}

		if (p != System.out) {
			p.close();
		}
	}

}
