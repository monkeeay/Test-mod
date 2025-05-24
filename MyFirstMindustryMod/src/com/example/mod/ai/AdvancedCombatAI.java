package com.example.mod.ai;

import arc.math.geom.Vec2;
import mindustry.ai.types.CommandAI;
import mindustry.entities.Units;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.Vars;
import arc.util.Log;
import arc.util.Nullable;
import mindustry.gen.Healthc; // For checking .dead()

public class AdvancedCombatAI extends CommandAI {

    public float retreatHealthThreshold = 0.4f; 
    public float engageHealthThreshold = 0.75f; 
    private boolean isRetreating = false;
    private Teamc currentFocusTargetInternal; // Renamed to avoid conflict with protected 'target'

    public AdvancedCombatAI() {
        super();
    }

    @Override
    public void unit(Unit unit) {
        super.unit(unit);
        if (unit != null) {
            isRetreating = false;
            currentFocusTargetInternal = null;
        }
    }
    
    public void setFocusTarget(@Nullable Teamc newFocusTarget){
        this.currentFocusTargetInternal = newFocusTarget;
        // If a focus target is set, make it the primary target for CommandAI's logic
        // by using the commandTarget method.
        if(newFocusTarget != null){
            this.commandTarget(newFocusTarget); 
        } else {
            // If null, perhaps clear existing command or let it be.
            // For now, setting CommandAI's direct target to null if focus is cleared.
            this.target = null; 
        }
    }

    private boolean isTargetDead(@Nullable Teamc targetToCheck) {
        if (targetToCheck == null) return true;
        if (targetToCheck instanceof Healthc) {
            return ((Healthc)targetToCheck).dead();
        }
        return !targetToCheck.isAdded(); // Fallback for non-Healthc types, assume gone if not added
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead()) {
            isRetreating = false; 
            return;
        }

        // --- Smart Retreat Logic ---
        // unit.isAttacked() was problematic, using a simpler health check for now
        boolean currentlyUnderAttack = unit.health < unit.maxHealth * 0.99f && unit.hitTime > 0; // Heuristic for being "under attack"

        if (isRetreating) {
            if (unit.healthf() >= engageHealthThreshold && !currentlyUnderAttack) {
                isRetreating = false;
                Log.info("[AdvAI] Unit " + unit.id + " recovered, stopping retreat.");
                targetPos = null; 
            } else {
                Building core = unit.closestCore();
                if (core != null) {
                    moveTo(core, unit.type.range * 0.8f); 
                    // Log.info("[AdvAI] Unit " + unit.id + " retreating to core. Health: " + unit.healthf());
                } else {
                    Teamc closestEnemy = Units.closestEnemy(unit.team, unit.x, unit.y, unit.type.range * 2, u -> true);
                    if (closestEnemy != null) {
                        vec.set(unit.x - closestEnemy.x(), unit.y - closestEnemy.y()).setLength(unit.speed());
                        unit.movePref(vec);
                        // Log.info("[AdvAI] Unit " + unit.id + " retreating from enemy (no core). Health: " + unit.healthf());
                    } else {
                        if (!currentlyUnderAttack) isRetreating = false; 
                    }
                }
                unit.controlWeapons(false, false); 
                return; 
            }
        } else {
            if (unit.healthf() < retreatHealthThreshold && currentlyUnderAttack) {
                isRetreating = true;
                Log.info("[AdvAI] Unit " + unit.id + " initiating retreat. Health: " + unit.healthf());
                unit.controlWeapons(false, false);
                return; 
            }
        }

        // --- Basic Focus Fire Logic --- 
        if (currentFocusTargetInternal != null && !isTargetDead(currentFocusTargetInternal) && currentFocusTargetInternal.isAdded()) {
            // If a focus target is set, ensure CommandAI's main 'target' is this focus target.
            // The 'commandTarget' method in CommandAI is designed to set this up.
            if(this.target != currentFocusTargetInternal){ // only issue command if target changed
                commandTarget(currentFocusTargetInternal);
            }
        } else {
            currentFocusTargetInternal = null; // Clear if dead or removed
            // If no explicit focus target, CommandAI's regular targeting (findTarget in super.updateUnit()) will take over.
            // However, if we had a focus target and it just died, CommandAI's 'target' might still be set to it.
            // Clearing it ensures retargeting.
            if (this.target != null && isTargetDead(this.target)) {
                 this.target = null;
            }
        }
        
        super.updateUnit(); 
    }

    @Override
    public boolean isLogicControllable() {
        return false; 
    }
}
