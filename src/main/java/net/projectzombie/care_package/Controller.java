package net.projectzombie.care_package;

import net.projectzombie.region_rotation.modules.StateController;
import net.projectzombie.region_rotation.modules.StateControllers;
import org.bukkit.Bukkit;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by jb on 9/5/16.
 */
public class Controller {

    static Controller INSTANCE = null;

    static protected Controller instance() {
        if (INSTANCE == null) {
            INSTANCE = new Controller();
        }
        return INSTANCE;
    }

    static protected void reload() {
        INSTANCE = new Controller();
    }

    static private Random RNG = new Random();
    static private String DEFAULT_NAME = "cp";

    private final StateController controller;
    private HashMap<String, ArrayList<String>> states;
    private ArrayList<String> baseStates;
    private String activeDrop;
    private boolean isValid;

    private Controller()
    {
        if (!StateControllers.contains(DEFAULT_NAME)) {
            StateControllers.create(DEFAULT_NAME);
        }

        this.controller = StateControllers.get(DEFAULT_NAME);
        this.isValid = (controller != null && controller.isValid());
        if (this.isValid) {
            baseStates = controller.getBaseStateRegionNames();
            states = controller.getBaseStateStringMap();

            activeDrop = null;
            for (String baseStateRegionName : baseStates) {
                if (controller.isRotated(baseStateRegionName)) {
                    activeDrop = baseStateRegionName;
                    break;
                }
            }
        }
    }

    public boolean resetCurrentDrop()
    {
        boolean toRet = true;
        if (activeDrop != null) {
            toRet = controller.resetBaseState(activeDrop, false);
            activeDrop = null;
        }
        return toRet;
    }

    public boolean initiateDrop()
    {
        final String baseStateDrop;
        final String altStateDrop;
        final ArrayList<String> altStates;

        if (!this.resetCurrentDrop()) {
            Bukkit.getLogger().info("Current Drop failed to reset");
            return false;
        }

        baseStateDrop = baseStates.get(RNG.nextInt(baseStates.size()));
        if (baseStateDrop == null) {
            Bukkit.getLogger().info("BaseState name is null");
            return false;
        }

        altStates = states.get(baseStateDrop);
        if (altStates == null || altStates.isEmpty()) {
            Bukkit.getLogger().info("BaseState does not contain any AltStates");
            return false;
        }

        altStateDrop = altStates.get(RNG.nextInt(altStates.size()));
        if (altStateDrop == null) {
            Bukkit.getLogger().info("AltState name is null");
            return false;
        }

        final ItemStack[] chestItems = PackageHandler.instance().getRandPackage();
        if (chestItems == null) {
            Bukkit.getLogger().info("Random package is null");
            return false;
        }

        if (!controller.rotateBaseStateBroadcast(baseStateDrop, altStateDrop, true, false)) {
            Bukkit.getLogger().info("Failed to rotate BaseState with AltState");
            return false;
        }

        final ArrayList<Chest> chests = controller.getCurrentStateChests(baseStateDrop);
        if (chests == null || chests.isEmpty()) {
            Bukkit.getLogger().info("Could not find any chests in BaseState");
            controller.resetBaseState(baseStateDrop, false);
            return false;
        }

        final Chest chest = chests.get(RNG.nextInt(chests.size()));
        chest.getInventory().setContents(chestItems);
        chest.update(true);
        this.activeDrop = baseStateDrop;

        return true;
    }

    public String getActiveDrop() {
        return activeDrop;
    }





}
