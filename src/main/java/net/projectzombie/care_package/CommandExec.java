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

package net.projectzombie.care_package;

import net.projectzombie.care_package.state.StateType;
import net.projectzombie.care_package.controller.StateController;
import net.projectzombie.care_package.files.StateFile;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author jbannon
 */
public class CommandExec implements CommandExecutor
{
    private final PackageHandler chest;
    
    public CommandExec()
    {
        chest = new PackageHandler();
    }
    
    /**
     * TODO
     * -initiate drop
     * -clean player interface
     * 
     * @param cs
     * @param cmd
     * @param label
     * @param args
     * @return
     */
    @Override
    public boolean onCommand(final CommandSender cs,
                             final Command cmd,
                             final String label,
                             final String[] args)
    {
        final Player player = (Player) cs;

        if (!player.isOp())
            return false;

        if (args.length == 0)
        {
            this.listCommands(player);
        } 
        else if (args[0].equalsIgnoreCase("list") && args.length == 2)
        {
            if (args[1].equalsIgnoreCase("alt"))
                StateController.listStates(player, StateType.ALT);
            else if (args[1].equalsIgnoreCase("base"))
                StateController.listStates(player, StateType.BASE);
            else if (args[1].equalsIgnoreCase("package"))
                chest.listPackages(player);
            else
                this.listCommands(player);   
        }
        else if (args[0].equalsIgnoreCase("create") && args.length == 3)
        {
            if (args[1].equalsIgnoreCase("alt"))
                StateController.createState(player, args[2], StateType.ALT);
            else if (args[1].equalsIgnoreCase("base"))
                StateController.createState(player, args[2], StateType.BASE);
            else if (args[1].equalsIgnoreCase("package"))
                chest.createPackage(player, args[2]);
            else
                this.listCommands(player);
        }
        else if (args[0].equalsIgnoreCase("remove") && args.length == 3)
        {
            if (args[1].equalsIgnoreCase("base"))
                StateController.removeState(player, args[2], StateType.BASE);
            else if (args[1].equalsIgnoreCase("alt"))
                StateController.removeState(player, args[2], StateType.ALT);
            else if (args[1].equalsIgnoreCase("package"))
                chest.removePackage(player, args[2]);
            else
                this.listCommands(player);
                
        }
        else if (args[0].equalsIgnoreCase("link") && args.length >= 3)
        {
            StringBuilder temp = new StringBuilder("");
            for (int i = 3; i < args.length; i++) {
                temp.append(args[i]);
                temp.append(' ');
            }
            StateController.linkState(player, args[1], args[2], temp.toString());
            
        }
        else if (args[0].equalsIgnoreCase("package") && args.length == 2)
            chest.getPlayerPackage(player, args[1]);
        
        else if (args[0].equalsIgnoreCase("set") && args.length == 3)
            StateController.executeStateChange(args[1], args[2]);
            
        else if (args[0].equalsIgnoreCase("paste") && args.length == 2)
                StateController.pasteAltState(player, args[1]);
        
        else if (args[0].equalsIgnoreCase("unpaste") && args.length == 1) 
            StateController.undoPaste(player);
        
        else if (args[0].equalsIgnoreCase("tele") && args.length == 3)
        {
            if (args[1].equalsIgnoreCase("base"))
                StateController.teleportToState(player, args[2], StateType.BASE);
            else if (args[1].equalsIgnoreCase("alt"))
                StateController.teleportToState(player, args[2], StateType.ALT);
            else
                this.listCommands(player);
        }
        
        else if (args[0].equalsIgnoreCase("restore") && args.length == 2)
        {
            StateController.restoreState(player, args[1]);
        }
        
        else if (args[0].equalsIgnoreCase("initiate"))
            StateController.initiateDrop();
        
        else if (args[0].equalsIgnoreCase("active"))
            StateController.listActive(player);
        
        else if (args[0].equalsIgnoreCase("check"))
            StateController.checkYaw(player);
        
        else if (args[0].equalsIgnoreCase("reload"))
            StateController.reloadConfig(player);
            
        else
            this.listCommands(player);
        
        return true;
    }
    
        /**
     * Lists all commands to the sender.
     * @param sender Command sender.
     */
    private void listCommands(final Player sender) 
    {
        sender.sendMessage("/cp create <base/alt/package> <name>");
        sender.sendMessage("/cp remove <base/alt/package> <name>");
        sender.sendMessage("/cp list <base/alt/package>");
        sender.sendMessage("/cp link <base> <alt> <desc>");
        sender.sendMessage("/cp tele <base/alt> <name>");
        sender.sendMessage("/cp set <base name> <alt name>");
        sender.sendMessage("/cp paste <base name>");
        sender.sendMessage("/cp unpaste");
        sender.sendMessage("/cp package <package name>");
        sender.sendMessage("/cp initiate");
        sender.sendMessage("/cp active");
        sender.sendMessage("/cp restore <base_name>");
        sender.sendMessage("/cp check");
        sender.sendMessage("/cp reload");
    }
    
    public void onEnable()
    {
        chest.onEnable();
    }
    
    public void onDisable()
    {
        StateFile.onDisable();
    }
}
