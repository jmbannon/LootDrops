/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import net.projectzombie.care_package.controller.StateChange;
import net.projectzombie.care_package.state.AltState;
import net.projectzombie.care_package.state.BaseState;
import net.projectzombie.care_package.state.State;
import net.projectzombie.care_package.state.StateType;
import net.projectzombie.care_package.utilities.Rand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

/**
 *
 * @author com.gmail.jbann1994
 */
public class StateFile
{
    static private Plugin plugin;
    static private File stateFile;
    static private FileConfiguration stateConfig;
    static private final String FILE_NAME = "drop_locations.yml";
    
    private StateFile() { /* Do nothing. */ }
    
    /**
     * Initializes configuration file on server start-up.
     * @param pluginOnEnable JavaPlugin in Main.
     */
    static public void onEnable(final Plugin pluginOnEnable)
    {
        plugin = pluginOnEnable;
        loadConfig();
    }
    
    /**
     * Restores any states changed and saves configuration file.
     */
    static public void onDisable()
    {
        StateBuffer.onDisable();
        saveConfig();
    }
    
    /**
     * Retrieves a StateChange that is not already within the StateBuffer.
     * The caller is responsible for placing this within the StateBuffer if it
     * is to be used.
     * 
     * @return StasteChange not already within the StateBuffer.
     */
    static public StateChange getRandomStateChange()
    {
        final ArrayList<String> baseNames = new ArrayList<>();
        final ArrayList<String> altNames = new ArrayList<>();
        int randIndex;
        BaseState base;
        
        
        if (!stateConfig.contains(StateType.ALT.getPath())
                || !stateConfig.contains(StateType.BASE.getPath()))
        {
            return null;
        }
            
        for (String baseName : StateFile.getSection(BaseState.path()))
        {
            if (!StateBuffer.contains(baseName))
                baseNames.add(baseName);
        }
        
        if (baseNames.isEmpty())
        {
            return null;
        }
        
        randIndex = Rand.nextInt(baseNames.size());
        base = new BaseState(baseNames.get(randIndex));
        
        while (!StateFile.contains(base.getAltPath()))
        {
            baseNames.remove(randIndex);
            if (baseNames.isEmpty())
                return null;
            else
            {
                randIndex = Rand.nextInt(baseNames.size());
                base = new BaseState(baseNames.get(randIndex));
            }
        }
        
        for (String altName : StateFile.getSection(base.getAltPath()))
        {
            altNames.add(altName);
        }
        
        if (altNames.isEmpty())
        {
            return null;
        }
        
        randIndex = Rand.nextInt(altNames.size());
        return new StateChange(base.getName(), altNames.get(randIndex));
    }
    
    static public boolean linkStates(final String baseName,
                                     final String altName,
                                     String description)
    {
        final BaseState base = new BaseState(baseName);
        final AltState alt = new AltState(altName);
        
        if (description.isEmpty())
            description = "NEEDS DESCRIPTION";
        
        if (!stateConfig.contains(base.getPath()) || !stateConfig.contains(alt.getPath()))
        {
            return false;
        }
        
        stateConfig.set(base.getPathAltDescription(altName), description);
        return StateFile.saveConfig();
    }
    
    static public boolean removeState(final State state)
    {
        final String stateName = state.getName();
        final String statePath = state.getPath();
        BaseState linkedBaseState;
        
        if (StateFile.contains(statePath))
        {
            StateFile.set(statePath, null);
            if (state.isAlt())
            {
                for (String baseStateName : StateFile.getSection(BaseState.path()))
                {
                    linkedBaseState = new BaseState(baseStateName);
                    StateFile.set(linkedBaseState.getPathAltDescription(stateName), null);
                }
            }
            return saveConfig();
        }
        else
            return false;
    }

    /**
     * Saves all changes to file and file configuration.
     * @return True if save was successful.
     */
    static public boolean saveConfig()
    {
        try
        {
            if (stateFile != null && stateConfig != null)
            {
                stateConfig.save(stateFile);
                return true;
            }
        }
        catch (IOException e)
        {
            StateFile.getPlugin().getLogger().log(Level.SEVERE, "Could not save config to " + stateConfig, e);
            return false;
        }
        return false;
    }

    /**
     * Loads file from plugin folder.
     */
    static public void loadConfig()
    {
        if (stateFile == null)
        {
            stateFile = new File(StateFile.getFolder(), FILE_NAME);
        }
        stateConfig = new YamlConfiguration();
        stateConfig = YamlConfiguration.loadConfiguration(stateFile);
        saveConfig();
    }
    
    static public void set(final String path,
                           final Object value)
    {
        stateConfig.set(path, value);
    }
    
    static public Vector getVector(final String path)
    {
        return stateConfig.getVector(path);
    }
    
    
    static public String getString(final String path)
    {
        return stateConfig.getString(path);
    }

    static public boolean contains(final String path)
    {
        return stateConfig.contains(path);
    }
    
    static public Set<String> getSection(final String path)
    {
        return stateConfig.getConfigurationSection(path).getKeys(false);
    }

    static public Plugin getPlugin()
    {
        return plugin;
    }
    
    static public File getFolder()
    {
        return plugin.getDataFolder();
    }
}
