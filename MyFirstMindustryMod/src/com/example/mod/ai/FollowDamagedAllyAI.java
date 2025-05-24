package com.example.mod.ai;

import arc.math.geom.Vec2;
import mindustry.ai.types.CommandAI;
import mindustry.entities.units.AIController;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
import mindustry.Vars;
import mindustry.entities.Units;
import mindustry.game.Team;

public class FollowDamagedAllyAI extends CommandAI {

    private Unit targetAlly;
    private float followDistance = 5f * 8f; // 5 tiles, 8 pixels per tile

    public FollowDamagedAllyAI() {
        super();
    }

    @Override
    public void updateUnit() {
        if (unit == null || unit.dead()) {
            return;
        }

        // If currently following an ally, check their status
        if (targetAlly != null) {
            if (targetAlly.dead() || targetAlly.health >= targetAlly.maxHealth || !unit.within(targetAlly, unit.type().range * 2f)) { // Stop if ally is dead, healed, or too far
                targetAlly = null;
            } else {
                // Continue following
                moveTo(targetAlly, unit.type().speed / 60f * 0.7f, 0f, true, null, true); // Move towards ally, try to arrive
                 unit.lookAt(targetAlly); // Look at the ally being followed
                return;
            }
        }

        // Scan for a new damaged ally if not currently following one
        if (targetAlly == null) {
            // Find the closest damaged ally
            // Using a slightly larger range to find allies, then will move towards them.
            Teamc closestDamagedAlly = Units.closestTarget(unit.team, unit.x, unit.y, unit.type().range * 1.5f, 
                u -> u.team == unit.team && u != unit && u.health < u.maxHealth && u.checkTarget(unit.type().targetAir, unit.type().targetGround), // Criteria: same team, not self, damaged, valid target type
                t -> true); // Building check (not relevant for finding units)

            if (closestDamagedAlly instanceof Unit) {
                targetAlly = (Unit) closestDamagedAlly;
                // Move towards the target ally
                moveTo(targetAlly, unit.type().speed / 60f * 0.7f, 0f, true, null, true);
                unit.lookAt(targetAlly);
            } else {
                // No damaged ally found, revert to default behavior (e.g., idle or return to rally point if one was set)
                // For now, just stop moving and clear any attack targets.
                unit.clearCommand(); // Clears current command, unit might revert to its type's default AI (e.g. CoreAI if near core)
                unit.movePref(new Vec2(0,0));
                attackTarget = null; // Clear any previous attack target
            }
        }
         // Stop shooting if not specifically targeting an enemy (which this AI currently doesn't do)
        unit.controlWeapons(false, false);
    }
    
    @Override
    public boolean isLogicControllable(){
        return false; // Prevent logic processors from overriding
    }
}
