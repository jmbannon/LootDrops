/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.projectzombie.care_package.files;

import java.io.File;
import net.projectzombie.care_package.controller.StateChange;
import net.projectzombie.care_package.state.AltState;
import org.bukkit.block.Block;

/**
 *
 * @author com.gmail.jbann1994
 */
public class CopyBuffer
{
    static private File COPY_FILE;
    static private Block COPY_BLOCK;
    static private boolean COPY_IN_PROGRESS = false;
    
    static public boolean pasteAltState(final AltState altState,
                                        final Block playerLocationBlock)
    {
        if (COPY_IN_PROGRESS || !altState.exists())
        {
            return false;
        }
        COPY_IN_PROGRESS = true;
        COPY_BLOCK = playerLocationBlock;
        COPY_FILE = new File(StateFile.getFolder(), "buffer.copy_buffer");
        return StateChange.set(altState, COPY_BLOCK, COPY_FILE);
    }
    
    static public boolean restorePaste()
    {
        COPY_IN_PROGRESS = false;
        return StateBuffer.restore(COPY_BLOCK, COPY_FILE);
    }
    
    static public boolean inProgress()
    {
        return COPY_IN_PROGRESS;
    }
}
