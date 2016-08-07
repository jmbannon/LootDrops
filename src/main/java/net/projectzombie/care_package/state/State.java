/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.state;

import net.projectzombie.care_package.files.StateFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 *
 * @author com.gmail.jbann1994
 */
public abstract class State
{
    
    private final String name;
    private final String pathState;
    private final String pathWorld;
    private final String pathVector;
    private final StateType type;
    
    public State(final StateType type,
                 final String stateName)
    {
        this.name           = stateName;
        this.type           = type;
        this.pathState      = type.getPath() + "." + stateName;
        this.pathWorld      = pathState + ".world";
        this.pathVector     = pathState + ".coords";
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getPath()
    {
        return this.pathState;
    }
    
    public String getPathWorld()
    {
        return this.pathWorld;
    }
    
    public String getPathVector()
    {
        return this.pathVector;
    }
    
    public boolean exists()
    {
        return StateFile.contains(pathState)
                && StateFile.contains(pathWorld)
                && StateFile.contains(pathVector);
    }
    
    public boolean isAlt()
    {
        return type.equals(StateType.ALT);
    }
    
    public boolean isBase()
    {
        return type.equals(StateType.BASE);
    }
    
    public BaseState toBaseState()
    {
        return new BaseState(this.name);
    }
    
    public AltState toAltState()
    {
        return new AltState(this.name);
    }
    
    public Block getLocationBlock()
    {
        if (StateFile.contains(pathWorld) && StateFile.contains(pathVector))
        {
            final World world = Bukkit.getWorld(StateFile.getString(pathWorld));
            final Vector vector;
            if (world != null)
            {
                vector = StateFile.getVector(pathVector);
                return new Location(world,
                                    vector.getX(),
                                    vector.getY(),
                                    vector.getZ()).getBlock();
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Creates a state based on the given type.
     * @param type Type of state to create.
     * @param stateName Name of the state.
     * @return The correct type of State.
     */
    static public State create(final StateType type,
                               final String stateName)
    {
        return (type.equals(StateType.BASE)) ? new BaseState(stateName) : new AltState(stateName);
    }
    
}
