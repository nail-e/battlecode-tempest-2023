package Dante;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.*;

import java.util.Map;

import static Dante.Communication.AddLauncherDestination;
import static Dante.Communication.GetAllLauncherDestinations;
import static Dante.RobotPlayer.*;

public class Headquarters {

    static MapLocation wellLoc;

    static void runHeadquarters(RobotController rc) throws GameActionException {

        rc.setIndicatorString(rc.readSharedArray(1) + " " + rc.readSharedArray(2));
        for(RobotInfo robot: rc.senseNearbyRobots(20, rc.getTeam().opponent()))
            if(robot.type.equals(RobotType.LAUNCHER)) AddLauncherDestination(rc, robot.getLocation());
        if(rc.readSharedArray(turnOrderIndex) > turnIndex) turnIndex = 0;
        else turnIndex = rc.readSharedArray(turnOrderIndex);
        rc.writeSharedArray(turnOrderIndex, turnIndex + 1);
        if((rc.getResourceAmount(ResourceType.ADAMANTIUM) > 100) || rng.nextInt(rc.getRobotCount() - turnIndex) == 0) {
            RobotType toBuild;
            RobotInfo[] enemies = rc.senseNearbyRobots(20, rc.getTeam().opponent());
            if (enemies.length > 0) toBuild = RobotType.LAUNCHER;
            else if (rc.readSharedArray(1) * 5 + 5 < rc.readSharedArray(2)) return;
            else if (GetAllLauncherDestinations(rc).length == 0) toBuild = RobotType.CARRIER;
            else toBuild = RobotType.CARRIER;
            for (Direction dir : Direction.values()) {
                dir = directions[ rng.nextInt(directions.length) ];
                MapLocation newLoc = rc.getLocation().add(dir);
                if (rc.canBuildRobot(toBuild, newLoc)) {
                    rc.buildRobot(toBuild, newLoc);
                    switch (toBuild) {
                        case CARRIER: {
                            carrierCount++;
                            rc.writeSharedArray(2, rc.readSharedArray(2) + 1);
                            break;
                        }
                        case LAUNCHER: launcherCount++; break;
                        case DESTABILIZER: destabilizerCount++; break;
                    }
                    break;
                }
            }
        }
    }
}
