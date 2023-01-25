package Jimmy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Pathing {

    static MapLocation targetLocation = null;
    static RobotController rc;
    static Utils utils;
    static Direction currentDir = null;
    static MapLocation obstacleStartLocation = null;
    static MapLocation breakPoint = null;
    static double obstacleStartDistanceToGoal = Double.MAX_VALUE;

    static void moveTowards(MapLocation target) throws GameActionException {
        rc.setIndicatorString("moving towards" + target + " o "+obstacleStartLocation + "dir " + currentDir);
        if (!rc.isMovementReady())
            return;
        // if we reached the target clear everything and reset
        if (Robot.location.equals(target)) {
            currentDir = null;
            obstacleStartLocation = null;
            breakPoint = null;
            obstacleStartDistanceToGoal = Double.MAX_VALUE;
            return;
        }

        // if the target has changed clear everything and reset
        if (!target.equals(targetLocation)) {
            targetLocation = target;
            currentDir = null;
            obstacleStartLocation = null;
            breakPoint = null;
            obstacleStartDistanceToGoal = Double.MAX_VALUE;
        }

        Direction dir = Robot.location.directionTo(target);
        if (currentDir == null) {
            currentDir = dir;
        }

        if (obstacleStartLocation == null) {
            MapLocation potentialNextLocation = Robot.location.add(dir);

            if (rc.onTheMap(potentialNextLocation)) {
                MapInfo mapInfo = rc.senseMapInfo(potentialNextLocation);
                if (mapInfo.isPassable() && mapInfo.getCurrentDirection() == Direction.CENTER) {
                    boolean blockingRobot = rc.canSenseRobotAtLocation(potentialNextLocation);
                    if (!blockingRobot) {

                        MovableRobot.handleMove(dir);

                        currentDir = null;
                        return;
                    } else {
                        moveAroundObstacle();
                        return;
                    }
                }
            }

        }

        if (obstacleStartLocation == null) {
            obstacleStartLocation = Robot.location;
            obstacleStartDistanceToGoal = Utils.getDistanceBetweenTwoPoints(Robot.location, targetLocation);
        }

        moveAroundObstacle();

        // you made a circle
        // if (Robot.location.equals(obstacleStartLocation)) {
        //     leftRight = !leftRight;
        //     obstacleStartLocation = null;
        //     obstacleStartDistanceToGoal = Double.MAX_VALUE;
        // }

        double newDistance = Utils.getDistanceBetweenTwoPoints(Robot.location, targetLocation);
        if (newDistance < obstacleStartDistanceToGoal) {
            obstacleStartLocation = null;
            obstacleStartDistanceToGoal = Double.MAX_VALUE;
        }
    }

    static boolean leftRight = false;

    static void moveAroundObstacle() throws GameActionException {
        int robotCount = 0;

        // currentDir = currentDir.rotateRight();
        for (int i = 0; i < 7; i++) {
            MapLocation potentialNextLocation = Robot.location.add(currentDir);
            if (!rc.onTheMap(potentialNextLocation)) {
                currentDir = currentDir.rotateRight();
                continue;
            }

            boolean blockingRobot = rc.canSenseRobotAtLocation(potentialNextLocation);
            if (blockingRobot) {
                currentDir = currentDir.rotateRight();
                robotCount++;

                continue;
            }

            MapInfo mapInfo = rc.senseMapInfo(potentialNextLocation);

            if (mapInfo.isPassable() && mapInfo.getCurrentDirection() == Direction.CENTER) {
                MovableRobot.handleMove(currentDir);

                currentDir = currentDir.rotateLeft();
                break;
            }
            currentDir = currentDir.rotateRight();
        }
        for (int j = 0; j < robotCount; j++) {
            currentDir = currentDir.rotateLeft();

        }
    }

    static boolean isValidLocation(MapLocation location, Direction dir) throws GameActionException {
        // if(visited.contains(location)) return false;

        if (!rc.canMove(dir))
            return false;

        RobotInfo blockingRobot = rc.senseRobotAtLocation(location);
        if (blockingRobot != null)
            return false;

        MapInfo potentialMapInfo = rc.senseMapInfo(location);
        if (!potentialMapInfo.isPassable())
            return false;

        if (Utils.isInRangeOfEnemy(location)) {
            return false;
        }
        ;

        return true;
    }

    static boolean isGoodLocation(MapLocation location, Direction dir) throws GameActionException {
        MapInfo potentialMapInfo = rc.senseMapInfo(location);

        Direction _currentDirection = potentialMapInfo.getCurrentDirection();
        if (!_currentDirection.equals(Direction.CENTER) && !_currentDirection.equals(dir))
            return false;

        return true;
    }

    // static MapLocation[] getGoalLine()
}
