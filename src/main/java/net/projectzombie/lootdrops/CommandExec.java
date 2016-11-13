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

package net.projectzombie.lootdrops;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author jbannon
 */
public class CommandExec implements CommandExecutor
{
    static private CommandExec INSTANCE = null;
    static protected CommandExec instance() {
        if (INSTANCE == null) {
            INSTANCE = new CommandExec();
        }
        return INSTANCE;
    }

    private CommandExec() { /* Do nothing */ }


    @Override
    public boolean onCommand(final CommandSender cs,
                             final Command cmd,
                             final String label,
                             final String[] args)
    {
        if (!cs.isOp())
            return false;

        if (args.length == 0)
        {
            this.listCommands(cs);
        } 
        else if (args[0].equalsIgnoreCase("list") && args.length == 1)
        {
            PackageHandler.instance().listPackages(cs);
        }
        else if (args[0].equalsIgnoreCase("create") && args.length == 2)
        {
            PackageHandler.instance().createPackage(cs, args[1]);
        }
        else if (args[0].equalsIgnoreCase("remove") && args.length == 2)
        {
            PackageHandler.instance().removePackage(cs, args[1]);
        }
        else if (args[0].equalsIgnoreCase("get") && args.length == 2)
        {
            PackageHandler.instance().getPlayerPackage(cs, args[1]);
        }
        else if (args[0].equalsIgnoreCase("initiate"))
        {
            if (Controller.instance().initiateDrop()) {
                cs.sendMessage("Drop initiated.");
            } else {
                cs.sendMessage("Drop failed to initiate.");
            }
        }
        else if (args[0].equalsIgnoreCase("reset"))
        {
            if (Controller.instance().resetCurrentDrop()) {
                cs.sendMessage("Drop reset");
            } else {
                cs.sendMessage("Drop failed to reset");
            }
        }
        else if (args[0].equalsIgnoreCase("active"))
        {
            final String activeDrop = Controller.instance().getActiveDrop();
            if (activeDrop != null) {
                cs.sendMessage("Active Drop: " + activeDrop);
            } else {
                cs.sendMessage("No active drops");
            }
        }
        else if (args[0].equalsIgnoreCase("reload"))
        {
            Controller.reload();
        }
        else
        {
            this.listCommands(cs);
        }
        return true;
    }
    
        /**
     * Lists all commands to the sender.
     * @param sender Command sender.
     */
    private void listCommands(final CommandSender sender)
    {
        sender.sendMessage("/cp create <package name>");
        sender.sendMessage("/cp remove <package name>");
        sender.sendMessage("/cp get <package name>");
        sender.sendMessage("/cp initiate");
        sender.sendMessage("/cp reset");
        sender.sendMessage("/cp active");
        sender.sendMessage("/cp list");
        sender.sendMessage("/cp reload");
    }
}
