/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.projectzombie.care_package.controller.StateChange;
import net.projectzombie.care_package.utilities.BlockSerialize;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

/**
 *
 * @author com.gmail.jbann1994
 */
public class StateBuffer
{
    private static final HashMap<String, StateChange> activeStateChanges = new HashMap<>();
    
    private StateBuffer() { /* Do nothing. */ }
    
    static public void put(final StateChange stateChange)
    {
        activeStateChanges.put(stateChange.getBaseName(), stateChange);
    }
    
    static public StateChange remove(final String baseName)
    {
        return activeStateChanges.remove(baseName);
    }
    
    static public boolean removeAndRestore(final StateChange toRestore)
    {
        if (toRestore != null)
            return restore(toRestore.getBaseLocationBlock(), toRestore.getBuffer());
        else
            return false;
    }
    
    static public boolean removeAndRestore(final String baseName)
    {
        return(removeAndRestore(activeStateChanges.remove(baseName)));
    }
    
    static public Set<String> active()
    {
        return activeStateChanges.keySet();
    }
    
    static public boolean contains(final String baseName)
    {
        return activeStateChanges.containsKey(baseName);
    }
    
    static public boolean contains(final StateChange stateChange)
    {
        return activeStateChanges.containsKey(stateChange.getBaseName());
    }
    
    static public void onDisable()
    {
        for (StateChange stateChange : activeStateChanges.values())
            stateChange.restoreState();
    }
    
    
    static protected boolean restore(final Block setLocationBlock,
                                     final File buffer)
    {
        if (buffer.exists())
        {
            try
            {
                BufferedReader reader = new BufferedReader(new FileReader(buffer));
                final String[] blocks = reader.readLine().split("#");

                for (String block : blocks)
                {
                    BlockSerialize.deserializeAndSet(block);
                }
                buffer.delete();
                removeDroppedEntities(setLocationBlock);
                return true;
            }
            catch (IOException ex)
            {
                Logger.getLogger(StateChange.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        else
            return false;
    }
    
    static private void removeDroppedEntities(final Block baseBlock)
    {
        final int width = StateChange.getStateWidth();
        final int height = StateChange.getStateHeight();
        final int length = StateChange.getStateLength();
        final Location centerLoc 
                = baseBlock.getRelative(width/2, 
                                        height/2, 
                                        length/2).getLocation();
        
        final Entity tempEntity = centerLoc.getWorld().spawnEntity(centerLoc, EntityType.ARROW);
        
        for (Entity entity : tempEntity.getNearbyEntities(width/2, 
                                                          height/2, 
                                                          length/2))
        {
            if (entity.getType() == EntityType.DROPPED_ITEM)
                entity.remove();            
        }
        tempEntity.remove();
    }
}
