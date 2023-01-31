package Dante;

import battlecode.common.GameActionException;
import battlecode.common.*;

import static Dante.Communication.*;
import static Dante.Destabilizer.AttackLowestHealth;

public class Launcher {

    static void runLauncher(RobotController rc) throws GameActionException {

        boolean headquarters = false;
        for (RobotInfo robot : rc.senseNearbyRobots(20, rc.getTeam()))
            if(robot.getType().equals(RobotType.HEADQUARTERS)) {
                headquarters = true;
                break;
            }
        AttackLowestHealth(rc);
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        if (rc.getHealth() < 10 && !headquarters) MoveToHeadquarters(rc);
        else if (enemies.length > 0) MoveBestIsland(rc);
        else if (GetAllLauncherDestinations(rc).length > 0) MoveToTarget(rc);
        else explore(rc);
    }
}
