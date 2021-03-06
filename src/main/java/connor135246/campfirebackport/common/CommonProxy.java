package connor135246.campfirebackport.common;

import java.io.File;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import com.google.common.io.Files;

import connor135246.campfirebackport.CampfireBackport;
import connor135246.campfirebackport.CampfireBackportConfig;
import connor135246.campfirebackport.common.blocks.CampfireBackportBlocks;
import connor135246.campfirebackport.common.dispenser.BehaviourShovel;
import connor135246.campfirebackport.common.dispenser.BehaviourSword;
import connor135246.campfirebackport.common.tileentity.TileEntityCampfire;
import connor135246.campfirebackport.util.CampfireBackportEventHandler;
import connor135246.campfirebackport.util.Reference;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

public class CommonProxy
{

    public static Configuration config;
    public static boolean useDefaultConfig = false;

    public static CampfireBackportEventHandler handler = new CampfireBackportEventHandler();

    public static Logger modlog;

    public void preInit(FMLPreInitializationEvent event)
    {
        config = new Configuration(event.getSuggestedConfigurationFile());

        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);

        modlog = event.getModLog();

        // old (pre-1.4) config is very different
        if (config.hasKey(Configuration.CATEGORY_GENERAL, "Regen Level"))
        {
            try
            {
                modlog.info("Renaming old (v1.3 or earlier) config file, and creating a new one!");
                Files.move(config.getConfigFile(), new File(config.getConfigFile().getCanonicalPath() + "_1.3"));
                config = new Configuration(event.getSuggestedConfigurationFile());
            }
            catch (Exception e)
            {
                modlog.catching(e);
                modlog.error("Failed to rename old (v1.3 or earlier) config file. Config will be loaded from default settings instead.");
                modlog.error("Delete or rename the old config file, then restart minecraft to get a new one.");
                modlog.error("No settings will be saved from the in-game config screen.");
                useDefaultConfig = true;
            }
        }

        CampfireBackportBlocks.preInit();
        GameRegistry.registerTileEntity(TileEntityCampfire.class, Reference.MODID + ":" + "campfire");
    }

    public void init(FMLInitializationEvent event)
    {
        FMLInterModComms.sendMessage("Waila", "register", "connor135246.campfirebackport.client.compat.CampfireBackportWailaDataProvider.register");
    }

    public void postInit(FMLPostInitializationEvent event)
    {
        syncConfig();

        if (CampfireBackportConfig.dispenserBehaviours)
            registerShovelsAndSwordsInDispenser();
    }

    public static void syncConfig()
    {
        try
        {
            if (!useDefaultConfig)
            {
                FMLCommonHandler.instance().bus().register(CampfireBackport.instance);
                CampfireBackportConfig.doConfig(config);
                config.save();
            }
            else
            {
                CampfireBackportConfig.doDefaultConfig();
            }
        }
        catch (Exception excep)
        {
            modlog.catching(excep);
        }
    }

    public static void registerShovelsAndSwordsInDispenser()
    {
        ArrayList<Item> bitemlist = CampfireBackportConfig.dispenserBehavioursBlacklistItems;
        boolean print = CampfireBackportConfig.printDispenserBehaviours;
        FMLControlledNamespacedRegistry<Item> itemreg = GameData.getItemRegistry();
        FMLControlledNamespacedRegistry<Block> blockreg = GameData.getBlockRegistry();

        iteratorLoop: for (Item item : itemreg.typeSafeIterable())
        {
            if (item instanceof ItemSpade)
            {
                for (Item bitem : bitemlist)
                {
                    if (item == bitem)
                        continue iteratorLoop;
                }
                BlockDispenser.dispenseBehaviorRegistry.putObject(item, new BehaviourShovel());

                if (print)
                    modlog.info("Dispenser Behaviour (Shovel) added to: " + new ItemStack(item).getDisplayName());

            }
            else if (item instanceof ItemSword)
            {
                for (Item bitem : bitemlist)
                {
                    if (item == bitem)
                        continue iteratorLoop;
                }
                BlockDispenser.dispenseBehaviorRegistry.putObject(item, new BehaviourSword());

                if (print)
                    modlog.info("Dispenser Behaviour (Sword)  added to: " + new ItemStack(item).getDisplayName());

            }
        }

        for (String witem : CampfireBackportConfig.dispenserBehavioursWhitelist)
        {
            String[] segment = witem.split("/");
            Item item = itemreg.getObject(segment[0]);

            if (item != null)
            {
                if (segment[1].equals("shovel"))
                {
                    BlockDispenser.dispenseBehaviorRegistry.putObject(item, new BehaviourShovel());
                    if (print)
                        modlog.info("Dispenser Behaviour (Shovel) added to: " + new ItemStack(item).getDisplayName());
                }
                else // if (segment[1].equals("sword"))
                {
                    BlockDispenser.dispenseBehaviorRegistry.putObject(item, new BehaviourSword());
                    if (print)
                        modlog.info("Dispenser Behaviour (Sword)  added to: " + new ItemStack(item).getDisplayName());
                }
                continue;
            }

            Block block = blockreg.getObject(segment[0]);

            if (block != Blocks.air)
            {
                if (segment[1].equals("shovel"))
                {
                    BlockDispenser.dispenseBehaviorRegistry.putObject(block, new BehaviourShovel());
                    if (print)
                        modlog.info("Dispenser Behaviour (Shovel) added to: " + new ItemStack(block).getDisplayName());
                }
                else // if (segment[1].equals("sword"))
                {
                    BlockDispenser.dispenseBehaviorRegistry.putObject(block, new BehaviourSword());
                    if (print)
                        modlog.info("Dispenser Behaviour (Sword)  added to: " + new ItemStack(block).getDisplayName());
                }
                continue;
            }
            if (!CampfireBackportConfig.suppressInputErrors)
                modlog.warn("Dispenser Behaviour Whitelist entry " + witem + " was invalid!");
        }
    }

    public void generateBigSmokeParticles(World world, int x, int y, int z, boolean signalFire)
    {
        ;
    }

}