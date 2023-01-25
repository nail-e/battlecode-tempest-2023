package examplefuncsplayer2;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Pathing {
    //Bug Nav which is Bug 0

    static Direction currentDirection = null;

    static void moveTowards(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation().equals(target)) {
            return;
        }
        if (!rc.isActionReady()) {
            return;
        }
        Direction d = rc.getLocation().directionTo(target);
        if (rc.canMove(d)) {
            rc.move(d);
            currentDirection = null; //there's no obstacle we're going around
        } else {
            //Going around some obstacle: can't move towards d because there's an obstacle there
            // try to keep obstacle on our right hand

            if (currentDirection == null) {
                currentDirection = d;
            }
            // try to move in a way that keeps the obstacle on the right hand side
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(currentDirection)) {
                    rc.move(currentDirection);
                    currentDirection = currentDirection.rotateRight();
                } else {
                    currentDirection = currentDirection.rotateLeft();
                }
            }
        }
    }
}
