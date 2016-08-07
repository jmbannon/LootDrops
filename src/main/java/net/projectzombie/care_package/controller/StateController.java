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

package net.projectzombie.care_package.controller;

import net.projectzombie.care_package.files.StateBuffer;
import net.projectzombie.care_package.files.StateFile;
import java.util.Set;
import net.projectzombie.care_package.files.CopyBuffer;
import net.projectzombie.care_package.state.StateType;
import net.projectzombie.care_package.state.AltState;
import net.projectzombie.care_package.state.BaseState;
import net.projectzombie.care_package.state.State;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Handles all player controls for the states.
 * - Create
 * - Remove
 * - List
 * - LinkStates
 * - SwitchStates
 *
 * @author Jesse Bannon
 */
public class StateController
{
    
    private StateController() { /* Do nothing */ }
    
    
    /**
     * Initiates a random care package drop.
     * @return True if the drop was valid and initiated.
     */
    static public boolean initiateDrop() 
    {
        return executeStateChange(StateFile.getRandomStateChange());
    }
    
    /**
     * Creates a new state within the configuration file.
     *
     * @param sender Command sender.
     * @param stateName State name.
     * @param stateType Type of state (base/alt).
     */
    static public void createState(final Player sender,
                                   final String stateName,
                                   final StateType stateType) 
    {
        final State state = State.create(stateType, stateName);
        final Location senderLoc = sender.getLocation();
        Vector chestSerialized;
        
        if (stateType == StateType.ALT)
        {
            chestSerialized = getChestRelative(senderLoc);
            if (chestSerialized != null)
            {
                StateFile.set(state.toAltState().getPathChestVector(), chestSerialized);
            }
            else
            {
                sender.sendMessage("A chest error occured");
                return;
            }
        }
        
        StateFile.set(state.getPathWorld(), sender.getWorld().getName());
        StateFile.set(state.getPathVector(), senderLoc.toVector());
        sender.sendMessage(stateName + " created.");
        StateFile.saveConfig();
    }

    static public boolean executeStateChange(final String baseName,
                                             final String altName)
    {
        return executeStateChange(new StateChange(baseName, altName));
    }
    
    /**
     * Executes a StateChange and adds it to the StateBuffer if it is validated.
     * @param stateChange StateChange to execute.
     * @return True if it executed successfully.
     */
    static public boolean executeStateChange(final StateChange stateChange) 
    {
        if (!StateBuffer.contains(stateChange) && stateChange.isValid())
        {
            stateChange.setState();
            StateBuffer.put(stateChange);
            return true;
        }
        else
            return false;
    }

    /**
     * Lists all states of the specified StateType to the command sender.
     *
     * @param sender
     * @param stateType
     */
    static public void listStates(final Player sender,
                                  final StateType stateType) 
    {
        final String statePath = stateType.getPath();
        sender.sendMessage(statePath);
        for (String key : StateFile.getSection(statePath))
        {
            sender.sendMessage(" - " + key);
        }
    }

    /**
     * Restores a base state back to its original form.
     * @param sender
     * @param baseName
     */
    static public void restoreState(final Player sender,
                                    final String baseName)
    {
        StateBuffer.removeAndRestore(baseName);
    }
    
    /**
     * Links a state //WIP!!!!
     * @param player
     * @param baseStateName
     * @param altStateName 
     * @param description 
     */
    static public void linkState(final Player player,
                                 final String baseStateName,
                                 final String altStateName,
                                       String description)
    {
        final BaseState base = new BaseState(baseStateName);
        final AltState alt = new AltState(altStateName);
        
        if (description.isEmpty())
            description = "NEEDS DESCRIPTION";
        
        if (!StateFile.contains(base.getPath()))
            player.sendMessage("Base state " + baseStateName + " does not exist.");
        
        else if (!StateFile.contains(alt.getPath()))
            player.sendMessage("Alt state " + altStateName + " does not exist.");
        
        else if (StateFile.linkStates(baseStateName, altStateName, description))
            player.sendMessage(baseStateName + " linked to " + altStateName);

        else
            player.sendMessage("An error occured when linking states.");
    }
    
    /**
     * Removes state of the given name if it exists.
     * 
     * @param player
     * @param stateName
     * @param stateType
     */
    static public void removeState(final Player player,
                                   final String stateName,
                                   final StateType stateType)
    {
        final State state = State.create(stateType, stateName);
        
        if (!state.exists())
            player.sendMessage(stateName + " does not exists.");
        else if (StateFile.removeState(state))
            player.sendMessage(stateName + " deleted.");
        else
            player.sendMessage("An error occured when removing " + stateName + ".");
    }
    
    /**
     * Gets the vector of the AltState's single chest relative to the
     * player's location.
     * 
     * @param playerLoc Location of the player.
     * @return Vector of the offset relative to the player. Null if no chest
     * or more than chest exist.
     */
    static private Vector getChestRelative(final Location playerLoc)
    {
        final Block loc = playerLoc.getBlock();
        final int length = StateChange.getStateLength();
        final int width  = StateChange.getStateWidth();
        final int height = StateChange.getStateHeight();
        
        Vector chestRelative = null;
        boolean hasChest = false;

        for (int i = 0; i < length; i++)
        {
            for (int j = 0; j < width; j++)
            {
                for (int k = 0; k < height; k++)
                {
                    if (loc.getRelative(i, k, j).getType() == Material.CHEST)
                    {
                        if (!hasChest)
                        {
                            chestRelative = new Vector(i, k, j);
                            hasChest = true;
                        }
                        else
                        {
                            return null;
                        }
                    }
                }
            }
        }
        return chestRelative;
    }
    
    static public void teleportToState(final Player sender,
                                       final String stateName,
                                       final StateType type)
    {
        final State state = State.create(type, stateName);
        final Block locationBlock = state.getLocationBlock();
        
        if (locationBlock == null)
            sender.sendMessage(stateName + " does not exist.");
        else
            sender.teleport(locationBlock.getLocation());
    }
    
    static public void listActive(final Player sender)
    {
        Set<String> baseNames = StateBuffer.active();
        if (baseNames != null)
        {
            for (String baseName : StateBuffer.active())
                sender.sendMessage(baseName);
        }
        else
            sender.sendMessage("No state changes are active.");
    }
    
    static public void checkYaw(final Player sender)
    {
        final float yaw = sender.getLocation().getYaw();
        if (yaw >= -67.5 && yaw < -22.5)
            sender.sendMessage("Correct direction! States always point SE.");
        else
            sender.sendMessage("Wrong direction! States always point SE.");
    }
    
    static public void pasteAltState(final Player sender,
                                     final String altName)
    {
        final AltState altState = new AltState(altName);
        final Block playerBodyBlock = sender.getEyeLocation().getBlock().getRelative(BlockFace.DOWN);
        
        if (!altState.exists())
            sender.sendMessage(altName + " does not exist.");

        else if (CopyBuffer.inProgress())
            sender.sendMessage("A paste has already been placed.");

        else if (CopyBuffer.pasteAltState(altState, playerBodyBlock))
            sender.sendMessage("Pasted " + altName);

        else
            sender.sendMessage("An error occured");
    }
    
    static public void undoPaste(Player sender)
    {
        if (!CopyBuffer.inProgress())
            sender.sendMessage("Nothing pasted.");
        
        else if (CopyBuffer.restorePaste())
            sender.sendMessage("Undid paste.");

        else
            sender.sendMessage("An error occured.");   
    }
    
    static public void reloadConfig(final Player sender)
    {
        StateFile.loadConfig();
        sender.sendMessage("Care Package config reloaded.");
    }



}
