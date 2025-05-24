package com.example.mod.ai.util;

import arc.math.Mathf;
import arc.math.geom.Vec2;
import mindustry.gen.Teamc;
import mindustry.gen.Unit;
// import mindustry.Vars; // Not strictly needed
import arc.util.Nullable; 
import mindustry.gen.Healthc; 
// import mindustry.core.World; // Not needed for Tmp
import arc.util.Tmp; // For Tmp.v1

public class EvasiveManeuvers {

    private static final float DODGE_DURATION_FRAMES = 30f; 
    private static final float DODGE_SPEED_MULTIPLIER = 1.2f; 
    private static final float MIN_HEALTH_PERCENT_TO_DODGE = 0.25f; 

    public static class DodgeState {
        boolean isDodging = false;
        float dodgeTimerFrames = 0;
        Vec2 dodgeDirection = new Vec2(); 
    }

    public static boolean attemptDodge(Unit unit, @Nullable Teamc currentTarget, boolean isTakingFire) {
        if (unit == null || unit.type.speed <= 0 || unit.healthf() < MIN_HEALTH_PERCENT_TO_DODGE) {
            return false; 
        }

        Object existingUserData = unit.userData(); 
        DodgeState state;

        if (existingUserData instanceof DodgeState) {
            state = (DodgeState) existingUserData;
        } else {
            state = new DodgeState();
            unit.userData(state); 
        }

        if (state.isDodging) {
            state.dodgeTimerFrames--;
            if (state.dodgeTimerFrames <= 0) {
                state.isDodging = false;
                return false; 
            } else {
                unit.movePref(state.dodgeDirection.setLength(unit.speed() * DODGE_SPEED_MULTIPLIER));
                return true; 
            }
        }

        if (isTakingFire) {
            boolean shouldConsiderDodge = currentTarget == null || !unit.within(currentTarget, unit.type.range * 0.90f);

            if (shouldConsiderDodge) {
                state.isDodging = true;
                state.dodgeTimerFrames = DODGE_DURATION_FRAMES;
                
                Vec2 dodgeVector = Tmp.v1.set(0,0); // Corrected to use Tmp.v1

                if (currentTarget != null && currentTarget.isAdded()) {
                    dodgeVector.set(currentTarget.x() - unit.x, currentTarget.y() - unit.y).nor();
                    dodgeVector.rotate90(Mathf.chance(0.5) ? 1 : -1); 
                } else if (unit.vel().len() > 0.01f) {
                    dodgeVector.set(unit.vel()).nor();
                    dodgeVector.rotate90(Mathf.chance(0.5) ? 1 : -1);
                } else {
                    dodgeVector.setToRandomDirection(Mathf.rand);
                }
                
                state.dodgeDirection.set(dodgeVector); 
                
                unit.movePref(state.dodgeDirection.setLength(unit.speed() * DODGE_SPEED_MULTIPLIER));
                return true; 
            }
        }
        return false; 
    }
}
