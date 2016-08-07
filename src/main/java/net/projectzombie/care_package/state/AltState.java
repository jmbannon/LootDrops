/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.state;

import net.projectzombie.care_package.files.StateFile;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 *
 * @author com.gmail.jbann1994
 */
public class AltState extends State
{
    private final String pathChest;
    
    public AltState(final String stateName)
    {
        super(StateType.ALT, stateName);
        this.pathChest = super.getPath() + ".chest";
    }
    
    
    public String getPathChestVector()
    {
        return pathChest;
    }
    
   public Block getChestBlock(final Block baseLocationBlock)
   {
       final Vector relativeVec;

       if (baseLocationBlock != null && StateFile.contains(pathChest))
       {
           relativeVec = StateFile.getVector(pathChest);
           return baseLocationBlock.getRelative(relativeVec.getBlockX(),
                                                relativeVec.getBlockY(),
                                                relativeVec.getBlockZ());
       }
       else
       {
           return null;
       }
   }
    
    @Override
    public boolean exists()
    {
        return super.exists()
            && StateFile.contains(pathChest);
    }
    
    static public String path()
    {
        return StateType.ALT.getPath();
    }
    
}
