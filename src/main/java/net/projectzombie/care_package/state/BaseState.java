/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.state;

import net.projectzombie.care_package.files.StateFile;

/**
 *
 * @author com.gmail.jbann1994
 */
public class BaseState extends State
{
    
    private final String altPath;
    
    public BaseState(final String stateName)
    {
        super(StateType.BASE, stateName);
        this.altPath = super.getPath() + ".alts";
    }
    
    public String getAltPath()
    {
        return this.altPath;
    }
    
    public String getPathAltDescription(final String altStateName)
    {
        return altPath + "." + altStateName;
    }
    
    public String getDescription(final String altStateName)
    {
        return StateFile.getString(getPathAltDescription(altStateName));
    }
    
    public boolean containsAlt(final String altStateName)
    {
        return StateFile.contains(getPathAltDescription(altStateName));
    }
    
    public boolean containsAlt(final AltState alt)
    {
        return containsAlt(alt.getName());
    }
    
    @Override
    public boolean exists()
    {
        return super.exists()
            && StateFile.contains(altPath);
    }
    
    static public String path()
    {
        return StateType.BASE.getPath();
    }
    
}
