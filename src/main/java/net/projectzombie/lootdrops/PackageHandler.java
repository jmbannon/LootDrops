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
package net.projectzombie.lootdrops;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
public class PackageHandler
{
    private static PackageHandler INSTANCE = null;

    static protected PackageHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new PackageHandler();
        }
        return INSTANCE;
    }

    private static final Random RAND = new Random();
    private static final String CONFIG_FILE_NAME = "chest_configs.yml";
    private static final String ROOT_PATH = "chest_configs";
    private File chestFile;
    private FileConfiguration chestConfig;
    
    /**
     * Initializes config file.
     */
    public PackageHandler()
    {
        this.loadConfig();
    }
    
    /**
     * Returns random package defined within the configuration file.
     * @return Random ItemStack from serialized string in config.
     */
    public ItemStack[] getRandPackage()
    {   
        final String packageName;
        final ArrayList<String> packageNames = new ArrayList<>();
        final ArrayList<ItemStack> chestItems;
        final ItemStack[] toRet = new ItemStack[27];
        
        if (!chestConfig.contains(ROOT_PATH))
            return null;

        packageNames.addAll(chestConfig.getConfigurationSection(ROOT_PATH).getKeys(false));
        if (packageNames.isEmpty())
        {
            Bukkit.getServer().getLogger().info("[CarePackage] No chests exist. Cannot initate drop.");
            return null;
        }

        packageName = packageNames.get(RAND.nextInt(packageNames.size()));
        chestItems = this.getPackage(packageName);

        if (chestItems == null || chestItems.isEmpty()) {
            return null;
        }

        while (chestItems.size() < 27) {
            chestItems.add(new ItemStack(Material.AIR));
        }

        Collections.shuffle(chestItems);
        for (int i = 0; i < 27; i++) {
            toRet[i] = chestItems.get(i);
        }

        return toRet;
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
    public void createPackage(final CommandSender sender,
                              final String packageName) 
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be a player to recieve package.");
            return;
        }

        final Player player = (Player)sender;
        if (chestConfig == null)
        {
            sender.sendMessage("The file is null! Please contact the server administrator.");
            return;
        }
        
        final ArrayList<ItemStack> inventoryItems = new ArrayList<>();
        final PlayerInventory inventory = player.getInventory();
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
    public void removePackage(final CommandSender sender,
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
    
    public void getPlayerPackage(final CommandSender sender,
                                 final String packageName)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Must be a player to receive package.");
            return;
        }

        final Player player = (Player)sender;
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
            player.getInventory().setItem(i+9, items.get(i));

        sender.sendMessage("Package " + packageName + " recieved.");
    }
    
    public void listPackages(final CommandSender sender)
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
            chestFile = new File(Main.getPlugin().getDataFolder(), CONFIG_FILE_NAME);

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
            Main.getPlugin().getLogger().log(Level.SEVERE, "Could not save config to " + chestConfig, e);
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
