package com.example.mod;

import mindustry.mod.Mod;
import mindustry.Vars;
import mindustry.ai.UnitCommand;
import com.example.mod.ai.PatrolPointAI;
import com.example.mod.ai.FollowDamagedAllyAI;
import arc.util.Log;

// Imports for the new block
import com.example.mod.world.blocks.AdvancedTacticsCenter;
import mindustry.world.Block;
import mindustry.content.Blocks; // For TechTree parent
import mindustry.type.ItemStack; // For research cost
import mindustry.content.Items;   // For research cost items
import mindustry.game.TechTree.TechNode; // For TechTree node creation
import mindustry.game.Objectives; // For research objectives, if needed

public class MyExampleMod extends Mod {

    // Unit Commands
    public static UnitCommand patrolPointCommand;
    public static UnitCommand followDamagedAllyCommand;

    // Blocks
    public static Block advancedTacticsCenter;

    public MyExampleMod() {
        Log.info("MyExampleMod constructor called.");
    }

    @Override
    public void init() {
        Log.info("MyExampleMod initialized!");
    }

    @Override
    public void loadContent() {
        Log.info("MyExampleMod: Loading content...");

        // Initialize UnitCommands
        patrolPointCommand = new UnitCommand(
            "patrolPoint",
            "command-move", 
            unit -> new PatrolPointAI()
        );

        followDamagedAllyCommand = new UnitCommand(
            "followDamagedAlly",
            "command-rally", 
            unit -> new FollowDamagedAllyAI()
        );
        Log.info("MyExampleMod: Custom commands loaded.");

        // Initialize new Block
        advancedTacticsCenter = new AdvancedTacticsCenter("advanced-tactics-center");
        Log.info("MyExampleMod: Custom block '" + advancedTacticsCenter.name + "' loaded with display name: " + advancedTacticsCenter.localizedName);


        // Add to Tech Tree
        // Ensure vanilla content is loaded before trying to access Blocks.microProcessor
        // A common way is to do this in a Runnable posted to the event queue,
        // or simply ensure this mod loads after core Mindustry content.
        // For simplicity here, we'll assume Blocks.microProcessor is available.
        // If TechTree.get(Blocks.microProcessor) is null, this will error.
        // A more robust way might involve checking Vars.content.blocks().contains(...)
        
        // Using microProcessor as a parent. Adjust if needed.
        // Research cost can be different from build cost.
        TechNode parentNode = TechTree.get(Blocks.microProcessor);
        if (parentNode != null) {
            parentNode.children.add(new TechNode(advancedTacticsCenter, 
                ItemStack.with(
                    Items.copper, 200, 
                    Items.lead, 150, 
                    Items.silicon, 100
                ), 
                // Runnable for when research is completed (optional)
                () -> {
                    Log.info(advancedTacticsCenter.localizedName + " researched!");
                }
            ));
            Log.info("MyExampleMod: " + advancedTacticsCenter.localizedName + " added to tech tree after " + Blocks.microProcessor.localizedName);
        } else {
            Log.err("MyExampleMod: Could not find parent node " + Blocks.microProcessor.name + " for tech tree. " + advancedTacticsCenter.localizedName + " will not be researchable.");
            // Fallback: add to a more generic root or another available block if desired
            // TechTree.roots.add(new TechNode(advancedTacticsCenter, ItemStack.with(Items.copper, 200, Items.lead, 150, Items.silicon, 100), () -> {}));
        }


        if (Vars.ui != null) {
            // Vars.ui.showInfo("MyExampleMod: Content loaded!");
        }
    }
}
