package com.example.mod.world.blocks;

import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.BlockStats;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.ctype.UnlockedableContent; // For category
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.content.Items; // For build costs

public class AdvancedTacticsCenter extends Block {

    public AdvancedTacticsCenter(String name) {
        super(name);
        this.localizedName = "Advanced Tactics Center"; // Display name
        this.description = "Unlocks advanced combat routines and tactical decision-making for controlled units.";
        this.category = Category.effect; // Or Category.logic / Category.power
        this.group = BlockGroup.logic; // Group in the build menu

        // Build cost
        this.requirements(Category.effect, ItemStack.with(
            Items.copper, 100,
            Items.lead, 50,
            Items.silicon, 25
        ));

        this.health = 100;
        this.size = 1; // 1x1 block
        this.solid = true;
        this.update = false; // No per-frame update logic needed for this block itself
        this.hasPower = false; // Does not require power
        this.configurable = false; // No per-block configuration
        
        // Placeholder icon - this refers to a sprite name
        // Ensure this sprite exists in Mindustry's assets or your mod's assets
        this.uiIcon = Vars.ui.getIcon("command-attack"); 
        // Or use a more generic one if that specific one causes issues during loading:
        // this.uiIcon = Vars.ui.getIcon("logic-processor");

        // Research cost is defined in the tech tree
    }

    @Override
    public void setStats() {
        super.setStats();
        stats.add(Stat.description, description);
        // You can add more stats here if needed, e.g., what it unlocks
        // For now, the description covers its purpose.
    }
    
    // This block itself doesn't do much other than exist and be researchable
    // Its presence (being unlocked) will be checked by AI routines
}
