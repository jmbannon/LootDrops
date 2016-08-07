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

import net.projectzombie.care_package.files.StateFile;
import net.projectzombie.care_package.utilities.BlockSerialize;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.projectzombie.care_package.PackageHandler;
import net.projectzombie.care_package.files.StateBuffer;
import net.projectzombie.care_package.state.AltState;
import net.projectzombie.care_package.state.BaseState;
import net.projectzombie.care_package.state.State;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Jesse Bannon
 * 
 * Handles switching between alternate and base states. 
 * - Sets Base to Alt
 * - Restores Alt to Base
 * - Gets chest relative location
 */
public class StateChange
{
    private static final int ALT_STATE_LENGTH = 30;
    private static final int ALT_STATE_WIDTH = 30;
    private static final int ALT_STATE_HEIGHT = 8;
    
    private final BaseState base;
    private final AltState alt;
    
    private final PackageHandler CONTENTS;
    private final Block altBlock;
    private final Block baseBlock;
    private final Block chestBlock; 
    private final String description;
    
    private final File   stateBuffer;
    
    /**
     * Initializes
     * 
     * @param baseName
     * @param altName
     */
    public StateChange(final String baseName,
                       final String altName)
    {
        this.base = new BaseState(baseName);
        this.alt  = new AltState(altName);
        
        this.altBlock    = alt.getLocationBlock();
        this.baseBlock   = base.getLocationBlock();
        this.chestBlock  = alt.getChestBlock(this.baseBlock);
        this.description = base.getDescription(altName);

        this.stateBuffer     = new File(StateFile.getFolder(), baseName + ".state_buffer");
        
        this.CONTENTS = new PackageHandler();
    }
    
    public String getBaseName()
    {
        return this.base.getName();
    }
    
    public boolean isValid()
    {
        return base.exists()
                && alt.exists()
                && base.containsAlt(alt)
                && altBlock != null
                && baseBlock != null
                && chestBlock != null
                && description != null;
    }
    
    /**
     * Sets an alternate state to a base state's location by serializing the 
     * base state's blocks into a text file and pasting the blocks with respect
     * to the offset location's coordinates.
     * 
     * @return Negative indicates error, positive success.
     */
    protected int setState() 
    {
        if (!isValid())
            return -1;
        
        final ArrayList<ItemStack> items = CONTENTS.getRandPackage();
        final ItemStack[] chestItems = new ItemStack[27];
        
        if (items == null)
        {
            Bukkit.getLogger().info("No packages within YML. Aborting state change");
            return -1;
        }
        else if (set(alt, base.getLocationBlock(), stateBuffer))
        {
            Bukkit.broadcastMessage("state set");
            final Chest chest = setChest();
            if (chest != null)
            {
                
                while (items.size() < 27)
                    items.add(new ItemStack(Material.AIR));

                Collections.shuffle(items);
                for (int i = 0; i < items.size(); i++)
                    chestItems[i] = items.get(i);

                chest.getInventory().setContents(chestItems);
                chest.update(true);
                StateFile.getPlugin().getServer().broadcastMessage(description);
                return 1;
            }
            else
            {
                Bukkit.broadcastMessage("chest not instanceof!");
                restoreState();
                return -1;
            }
        }
        else
            return -1;
    }
    
    public boolean restoreState()
    {
        return StateBuffer.removeAndRestore(this);
    }
    
    private Chest setChest()
    {
        //chestBlock.setType(Material.CHEST);
        return (chestBlock.getState() instanceof Chest) ? (Chest)chestBlock.getState() : null;
    }
    
    /**
     * Pastes a state to the set Location.
     * @param state State to paste at the setLocationBlock.
     * @param setLocationBlock Block to paste the state.
     * @param buffer Buffer to save the setLocationBlock contents at.
     * @return True if the paste was successful.
     */
    static public boolean set(final State state,
                              final Block setLocationBlock,
                              final File buffer)
    {
        final Block stateLocation = state.getLocationBlock();
        Block toPaste;
        
        if (stateLocation == null)
            return false;
        
        try (FileWriter stateWriter = new FileWriter(buffer))
        {
            for (int i = 0; i < ALT_STATE_LENGTH; i++)
            {
                for (int j = 0; j < ALT_STATE_WIDTH; j++)
                {
                    for (int k = 0; k < ALT_STATE_HEIGHT; k++)
                    {
                        toPaste = setLocationBlock.getRelative(i, k, j);
                        stateWriter.write(BlockSerialize.serialize(toPaste));
                        toPaste.setType(stateLocation.getRelative(i, k, j).getType());
                        toPaste.setData(stateLocation.getRelative(i, k, j).getData());
                    }
                }
            }   
            stateWriter.flush();
            stateWriter.close();
            return true;
        }
        catch (IOException ex)
        {
            Logger.getLogger(StateChange.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public Block getBaseLocationBlock()
    {
        return this.baseBlock;
    }
    
    public File getBuffer()
    {
        return this.stateBuffer;
    }
    
    /**
     * Returns Returns the state's length.
     * @return Length of all states.
     */
    static public int getStateLength()
    {
        return ALT_STATE_LENGTH;
    }
    
    /**
     * Returns Returns the state's width.
     * @return Width of all states.
     */
    static public int getStateWidth()
    {
        return ALT_STATE_WIDTH;
    }
    
    /**
     * Returns Returns the state's height.
     * @return Height of all states.
     */
    static public int getStateHeight()
    {
        return ALT_STATE_HEIGHT;
    }
    
}
