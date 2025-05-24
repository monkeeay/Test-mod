package com.example.mod.ai;

import arc.math.geom.Vec2;
import mindustry.ai.types.CommandAI;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.Vars;
import mindustry.entities.Units;
import com.example.mod.ai.util.EvasiveManeuvers;
import com.example.mod.MyExampleMod;
import arc.util.Log;
import com.example.mod.ai.AdvancedCombatAI; 
import mindustry.gen.Healthc; // For .dead()
import arc.util.Nullable; // Added for @Nullable

public class PatrolPointAI extends CommandAI {

    public Vec2 patrolTargetPos;
    private Teamc currentEnemyTarget; // This is CommandAI's 'target' field essentially.
    private float previousHealth;
    private boolean advancedTacticsLogged = false;

    private AdvancedCombatAI advancedCombatDelegate; 

    public PatrolPointAI() {
        super();
        this.advancedCombatDelegate = new AdvancedCombatAI();
    }

    @Override
    public void unit(Unit unit) {
        super.unit(unit); 
        this.advancedCombatDelegate.unit(unit); 

        this.advancedTacticsLogged = false;
        if (unit != null) {
            this.previousHealth = unit.health;
            if (this.patrolTargetPos == null) {
                if (this.targetPos != null && !this.targetPos.isZero()) { // targetPos is from CommandAI (superclass)
                    this.patrolTargetPos = new Vec2(this.targetPos);
                } else {
                    this.patrolTargetPos = new Vec2(unit.x, unit.y);
                }
            }
            this.advancedCombatDelegate.targetPos = this.patrolTargetPos != null ? new Vec2(this.patrolTargetPos) : null;
        }
    }
    
    public void setPatrolLocation(Vec2 location) {
        this.patrolTargetPos = new Vec2(location);
        this.targetPos = new Vec2(location); 
        if(unit != null) {
             this.previousHealth = unit.health;
        }
        this.advancedTacticsLogged = false;
        if (this.advancedCombatDelegate != null) {
            this.advancedCombatDelegate.targetPos = new Vec2(this.patrolTargetPos);
        }
    }
    
    private boolean isTargetDead(@Nullable Teamc targetToCheck) {
        if (targetToCheck == null) return true;
        if (targetToCheck instanceof Healthc) {
            return ((Healthc)targetToCheck).dead();
        }
        return !targetToCheck.isAdded();
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead()) {
            if (unit != null && unit.userData() instanceof EvasiveManeuvers.DodgeState) { 
                unit.userData(null);
            }
            return; 
        }
        if (patrolTargetPos == null) { 
             if (this.targetPos != null && !this.targetPos.isZero()){
                 this.patrolTargetPos = new Vec2(this.targetPos);
             } else {
                 this.patrolTargetPos = new Vec2(unit.x, unit.y);
             }
             this.previousHealth = unit.health; 
             this.advancedTacticsLogged = false; 
             if (this.advancedCombatDelegate != null && this.advancedCombatDelegate.unit() == unit) { 
                this.advancedCombatDelegate.targetPos = new Vec2(this.patrolTargetPos);
             }
        }

        boolean advancedTacticsUnlocked = MyExampleMod.advancedTacticsCenter != null && unit.team.rules().isUnlocked(MyExampleMod.advancedTacticsCenter);

        if (advancedTacticsUnlocked) {
            if (!advancedTacticsLogged) {
                Log.info("[PTL_AI] Unit " + unit.id + ": Advanced Tactics Unlocked! Delegating to AdvancedCombatAI.");
                advancedTacticsLogged = true;
            }
            
            if (this.currentEnemyTarget != null && !isTargetDead(this.currentEnemyTarget) && this.currentEnemyTarget.isAdded()) {
                 advancedCombatDelegate.commandTarget(this.currentEnemyTarget);
            } else {
                 advancedCombatDelegate.commandTarget(null); 
                 advancedCombatDelegate.targetPos = this.patrolTargetPos;
            }
            
            advancedCombatDelegate.updateUnit();
            this.previousHealth = unit.health; 
            return; 
        }

        boolean isTakingFire = unit.health < previousHealth;
        if (EvasiveManeuvers.attemptDodge(unit, currentEnemyTarget, isTakingFire)) {
            this.previousHealth = unit.health;
            return;
        }
        this.previousHealth = unit.health;

        if (currentEnemyTarget != null && !isTargetDead(currentEnemyTarget) && currentEnemyTarget.isAdded() && unit.within(currentEnemyTarget, unit.type().range * 0.9f)) {
            unit.lookAt(currentEnemyTarget);
            if (shouldShoot()) {
                 unit.controlWeapons(true, true);
            }
            if (isTargetDead(currentEnemyTarget) || !currentEnemyTarget.isAdded() || !unit.within(currentEnemyTarget, unit.type().range * 1.5f)) {
                currentEnemyTarget = null;
            }
            return;
        } else {
            currentEnemyTarget = null;
        }

        if (currentEnemyTarget == null) {
            Teamc closestEnemy = Units.closestTarget(unit.team, unit.x, unit.y, unit.type().range, 
                                                    u -> u.team != unit.team && u.checkTarget(unit.type().targetAir, unit.type().targetGround), 
                                                    t -> t.team != unit.team && unit.type().targetGround);
            if (closestEnemy != null) {
                this.target = closestEnemy; // Set CommandAI's target
                this.currentEnemyTarget = closestEnemy; // Also set local field
                unit.lookAt(currentEnemyTarget);
                 if (shouldShoot()) {
                    unit.controlWeapons(true, true);
                }
                return;
            }
        }
        
        if (unit.isShooting()) {
             unit.controlWeapons(false, false);
        }

        if (!unit.within(patrolTargetPos, unit.type().hitSize * 1.5f)) {
            moveTo(patrolTargetPos, unit.type().hitSize * 0.5f, 20f); 
        } else {
            unit.movePref(new Vec2(0,0));
        }
    }
    
    @Override
    public boolean isLogicControllable(){
        return false;
    }
}
