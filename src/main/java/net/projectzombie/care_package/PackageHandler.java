/*
 * CarePackage
 *
 * Version:     0.5
 * MC Build:    1.8.3
 * Date:        05-03-2015
 *
 * Authur:      Jesse Bannon
 * Server:      Project Zombie
 * Website:     www.projectzombie.net
 * 
 * Initiates random care package drops by combining an alternate state of the
 * map with a base state on the actual player map. Stores the base state blocks
 * within a text buffer and pastes the alt state to the location of the base
 * state. Finds single chest within the pasted alt state and sets a randomly
 * define set of items made by the administrator.  Restores the state on a
 * timer.
 *
 */
package net.projectzombie.care_package;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import net.projectzombie.care_package.files.StateFile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Jesse Bannon
 * 
 * This class is to handle everything related to chest configurations
 * within the care package drop.
 * 
 */
public class PackageHandler {
    
    private static final Random RAND = new Random();
    private static final String CONFIG_FILE_NAME = "chest_configs.yml";
    private static final String ROOT_PATH = "chest_configs";
    private File chestFile;
    private FileConfiguration chestConfig;
    
    /**
     * Initializes config file.
     * @param plugin Bukkit plugin.
     */
    public PackageHandler()
    {
        this.loadConfig();
    }
    
    /**
     * Returns random package defined within the configuration file.
     * @return Random ItemStack from serialized string in config.
     */
    public ArrayList<ItemStack> getRandPackage()
    {   
        final String chestName;
        ArrayList<String> chestList = new ArrayList<>();
        
        if (!chestConfig.contains(ROOT_PATH))
            return null;
        
        for (String key : chestConfig.getConfigurationSection(ROOT_PATH).getKeys(false))
        {
            chestList.add(key);
        }
      
        if (chestList.isEmpty())
        {
            Bukkit.getServer().getLogger().info("[CarePackage] No chests exist. Cannot initate drop.");
            return null;
        }
        
        chestName = chestList.get(RAND.nextInt(chestList.size()));
        chestList.clear();
        return this.getPackage(chestName);
    }
    
    private ArrayList<ItemStack> getPackage(final String packageName)
    {
        if (!chestConfig.contains(ROOT_PATH + "." + packageName))
            return null;
        else
            return (ArrayList<ItemStack>)chestConfig.getList(ROOT_PATH + "." + packageName);
    }
    
    /**
     * Creates a package based on the command sender's inventory (excluding
     * their hot-bar!) and stores it serialized within the config.
     * @param sender Command sender.
     * @param packageName Name of the package.
     */
    public void createPackage(final Player sender,
                              final String packageName) 
    {
        if (chestConfig == null)
        {
            sender.sendMessage("The file is null! Please contact the server administrator.");
            return;
        }
        
        final ArrayList<ItemStack> inventoryItems = new ArrayList<>();
        final PlayerInventory inventory = sender.getInventory();
        ItemStack tempItem;
        
        for (int i = 9; i <= 35; i++)
        {
            tempItem = inventory.getItem(i);
            if (tempItem != null && tempItem.getType() != Material.AIR)
                inventoryItems.add(inventory.getItem(i));
        }

        chestConfig.set(ROOT_PATH + "." + packageName, inventoryItems);
        
        this.saveConfig();
        sender.sendMessage("Your inventory has been saved as " + packageName);
    }

    /**
     * Removes a package of the given name called by the command sender.
     * @param sender Command sender.
     * @param packageName Name of the package.
     */
    public void removePackage(final Player sender,
                              final String packageName)
    {
        if (chestConfig == null)
        {
            sender.sendMessage("The file is null! Please contact the server administrator.");
            return;
        }
        
        if (chestConfig.contains(ROOT_PATH + "."  + packageName))
        {
            chestConfig.set(ROOT_PATH + "."  + packageName, null);
            this.saveConfig();
            sender.sendMessage(packageName + " has been deleted.");
        } else
            sender.sendMessage(packageName + " does not exist");
    }
    
    public void getPlayerPackage(final Player sender,
                                 final String packageName)
    {
        if (!chestConfig.contains(ROOT_PATH + "." + packageName))
        {
            sender.sendMessage("Chest does not exist.");
            return;
        }
        
        ArrayList<ItemStack> items = this.getPackage(packageName);
        if (items == null)
        {
            sender.sendMessage("An error has occured.");
            return;
        }
            
        for (int i = 0; i < items.size(); i++)
            sender.getInventory().setItem(i+9, items.get(i));
        
        sender.sendMessage("Package " + packageName + " recieved.");
    }
    
    public void listPackages(final Player sender)
    {
        sender.sendMessage("Packages:");
        for (String key : chestConfig.getConfigurationSection(ROOT_PATH).getKeys(false))
        {
            sender.sendMessage(" - " + key);
        }
    }
    
    /**
     * Loads configuration file from plugin data folder.
     */
    private void loadConfig()
    {
        if (chestFile == null) 
            chestFile = new File(StateFile.getFolder(), CONFIG_FILE_NAME);

        chestConfig = new YamlConfiguration();
        chestConfig = YamlConfiguration.loadConfiguration(chestFile);
        this.saveConfig();
    }

    /**
     * Saves all changes to file and file configuration.
     */
    private void saveConfig()
    {
        if (chestFile == null || chestConfig == null) 
            return;
        try {
            chestConfig.save(chestFile);
        } catch (IOException e) {
            StateFile.getPlugin().getLogger().log(Level.SEVERE, "Could not save config to "
                    + chestConfig, e);
        }
    }
    
    public void onEnable()
    {
        this.loadConfig();
    }
    
    public void onDisable()
    {
        this.saveConfig();
    }
}
