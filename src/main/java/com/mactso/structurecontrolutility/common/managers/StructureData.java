package com.mactso.structurecontrolutility.common.managers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import com.mactso.structurecontrolutility.common.config.MyConfig;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.world.ModifiableStructureInfo.StructureInfo.Builder;
import net.minecraftforge.event.server.ServerStartingEvent;

/**
 * Generates a server-startup report of all registered structures and their
 * effective protection and potion configuration values.
 * <p>
 * The report is intended for inspection and optional promotion to a CSV
 * configuration file by renaming it.
 * </p>
 */

public class StructureData {


    static int reportlinenumber = 0;

    /** Creates the report directory and removes any existing structures report. */
    static {
        initReports();
    }
    public static void initReports() {
        File fd = new File("config/structurecontrolutility");
        if (!fd.exists())
            fd.mkdir();
        File fs = new File("config/structurecontrolutility/structures.rpt");
        if (fs.exists())
            fs.delete();
    }

    public static void onStructure(Holder<Structure> struct, Builder builder) {
        // no processing at load time.
    }

    /** Writes the structures report file when the server finishes starting. */
    public static void generateStructuresReport(ServerStartingEvent event) {
        PrintStream p = null;
        try {
            p = new PrintStream(new FileOutputStream("config/structurecontrolutility/structures.rpt", false));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Fallback to console output if file creation fails.
        if (p == null) {
            p = System.out;
        }

        // Header and documentation block.
        p.println("* This is the Structures report file that is output every time the server starts.");
        p.println("* ");
        p.println("* Structure Control Utility will use this file ONLY if it is renamed to structures.csv.");
        p.println("* Lines starting with '*' are comments and ignored");
        p.println("* ");
        p.println("* Each digit of the 'Effects:00000000' string is an effect power from 0 (off) to 9");
        p.println("* The Structure Effects are :, Jump Boost, Movement Slowness, Regeneration, Slow Falling, Water Breathing, Weakness, and Darkness");
        p.println("* ");
        p.println("* Example Line of a Minecraft:Mansion");
        p.println("* 0, Minecraft:Mansion, Effects:1100021, 721, 1440, 719, 999999, 1500, 2");
        p.println("* ");
        p.println("* Players inside Mansion gain effects:, Jump Boost 1, Movement Slowness 1, Weakness 2, Darkness 1 for the first 721 minutes,");
        p.println("* This Mansion is:, fireproof 1440 minutes, will not break for 719 minutes, will not explode for 999999 minutes.");
        p.println("* This Mansion resists breaking for the first 1500 minutes with Mining Fatigue 2 .");
        p.println("* ");
        p.println("* Line Number, Structure Name, Effects String, Effects Minutes, Fireproof Minutes, Stop Block Breaking Minutes, Stop Explosions Minutes, Mining Fatigue Minutes, Mining Fatigue Level");
        p.println("* ");

        int linenumber = 0;
		MinecraftServer server = event.getServer();
        RegistryAccess dynreg = server.registryAccess();
        Registry<Structure> structRegistry = dynreg.registryOrThrow(Registries.STRUCTURE);

        int effectsMinutes = MyConfig.getStructureEffectsMinutes();
        int stopFireMinutes = MyConfig.getStopFireMinutes();
        int stopBreakingMinutes = MyConfig.getStopBreakingMinutes();
        int stopExplosionsMinutes = MyConfig.getStopExplosionMinutes();
        int miningFatigueMinutes = MyConfig.getMiningFatigueMinutes();
        int miningFatigueLevel = MyConfig.getMiningFatigueLevel();

        // Emit one CSV-compatible line per registered structure.        
        for (Structure struct : structRegistry) {
            String modAndStructure = structRegistry.getKey(struct).toString();

            String effectFlags = "Effects:" + MyConfig.getProtectedStructuresEffects();

            // Unprotected structures will default to no protection and configured effects.
            if (StructureManager.unprotectedStructures.contains(modAndStructure)) {
                stopFireMinutes = 0;
                stopBreakingMinutes = 0;
                stopExplosionsMinutes = 0;
                miningFatigueMinutes = 0;
                miningFatigueLevel = 0;
                effectFlags = "Effects:" + MyConfig.getUnprotectedStructuresEffects();
            }

            p.println(++linenumber + ", " + modAndStructure + ", " + effectFlags + ", "
                    + effectsMinutes + ", " + stopFireMinutes + ", " + stopBreakingMinutes + ", "
                    + stopExplosionsMinutes + ", " + miningFatigueMinutes + ", " + miningFatigueLevel);
        }

        if (p != System.out) {
            p.close();
        }
    }
}