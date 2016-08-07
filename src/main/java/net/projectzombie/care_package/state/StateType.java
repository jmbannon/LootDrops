/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.state;

/**
 *
 * @author com.gmail.jbann1994
 */
public enum StateType
{
    ALT("alt_states"),
    BASE("base_states");
    
    private final String path;
    
    private StateType(final String rootPath)
    {
        this.path = rootPath;
    }
    
    public String getPath()
    {
        return path;
    }
}
