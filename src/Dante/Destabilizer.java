package Dante;

import battlecode.common.*;

import java.util.Arrays;

import static Dante.Communication.*;
import static Dante.RobotPlayer.attackPriority;
import static Dante.RobotPlayer.attackingTypes;

public class Destabilizer {

    static void runDestabilizer(RobotController rc) throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        MapLocation location = rc.getLocation();
        if (rc.canDestabilize(location) && enemies.length > 3) rc.destabilize(location);
        else AttackLowestHealth(rc);
        if (enemies.length > 0) {
            RobotInfo closestEnemy = null;
            int closestDistance = 10000;
            for (RobotInfo enemy : enemies) {
                if (location.distanceSquaredTo(enemy.location) < closestDistance
                        && Arrays.asList(attackingTypes).contains(enemy.type)) {
                    closestEnemy = enemy;
                    closestDistance = location.distanceSquaredTo(enemy.location);
                }
            }
            if (closestEnemy != null) {
                if (closestDistance > rc.getType().actionRadiusSquared) {
                    navigateToLocation(rc, enemies[ 0 ].location);
                } else {
                    navigateToLocation(rc, location.add(location.directionTo(closestEnemy.location).opposite()).add(
                            location.directionTo(closestEnemy.location).opposite()));
                }
            }
        } else if (GetAllLauncherDestinations(rc).length > 0) MoveToTarget(rc);
        else explore(rc);
    }

    static void AttackLowestHealth(RobotController rc) throws GameActionException{
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        boolean exit = false;
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        if(enemies.length < 1) return;
        for (RobotType type : attackPriority) {
            int bestHealth = 10000;
            RobotInfo bestEnemy = null;
            for (RobotInfo enemy : enemies) {
                if (enemy.type != type) continue;
                if(enemy.getHealth() < bestHealth){
                    bestHealth = enemy.getHealth();
                    bestEnemy = enemy;
                }
                if(bestEnemy != null && rc.canAttack(bestEnemy.getLocation())) {
                    rc.attack(bestEnemy.getLocation());
                    AddLauncherDestination(rc, enemies[0].location);
                    exit = true;
                    break;
                }
            }
            if (exit) break;
        }
    }

}
