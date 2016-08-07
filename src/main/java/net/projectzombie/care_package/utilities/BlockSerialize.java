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

package net.projectzombie.care_package.utilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 *
 * @author jbannon
 */
public class BlockSerialize implements Listener {

    private static final Server server = Bukkit.getServer();

    public BlockSerialize() { /* Do nothing */ }
    
    /**
     * Serializes blocks in the form of "world_name,x,y,z,type,data,\n"
     *
     * @param block
     * @return 
     */
    public static String serialize(final Block block) {
        final StringBuilder temp = new StringBuilder();
        
        temp.append(block.getWorld().getName());
        temp.append(',');
        temp.append(block.getX());
        temp.append(',');
        temp.append(block.getY());
        temp.append(',');
        temp.append(block.getZ());
        temp.append(',');
        temp.append(block.getType().toString());
        temp.append(',');
        temp.append(block.getData());
        temp.append('#');

        return temp.toString();
    }
    
    /**
     * Deserializes the string and sets the block in the specified world.
     * @param serializedString
     */
    public static void deserializeAndSet(final String serializedString) {
        String[] parts = serializedString.split(",");
        
        final Block theBlock = server.getWorld(parts[0]).getBlockAt(
                Integer.valueOf(parts[1]), 
                Integer.valueOf(parts[2]), 
                Integer.valueOf(parts[3]));
        
        BlockBreakEvent event = new BlockBreakEvent(theBlock, null);
        theBlock.setType(Material.valueOf(parts[4]));
        theBlock.setData(Byte.valueOf(parts[5]));
    }
}
