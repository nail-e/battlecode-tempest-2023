package Dante;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Launcher extends Attacker {

    boolean explorer = false;

    Launcher(RobotController rc){
        super(rc);
        checkExploreBehavior();
    }

    void play() throws GameActionException{
        checkChickenBehavior();
        tryAttack(true);
        tryMove();
        tryAttack(false);
    }

    void checkExploreBehavior(){
        try {
            int soldierIndex = rc.readSharedArray(comm.LAUNCHER_COUNT);
            if (soldierIndex%3 == 2) explorer = true;
            comm.increaseIndex(comm.LAUNCHER_COUNT, 1);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    void tryMove() throws GameActionException {
        if (!rc.isMovementReady()) return;
        MapLocation target = getTarget();
        bfs.move(target);
    }

    MapLocation getTarget() throws GameActionException{
        if (rc.getRoundNum() < Constants.ATTACK_TURN && comm.isEnemyTerritoryRadial(rc.getLocation())) return comm.getClosestAllyArchon();
        MapLocation ans = getBestTarget();
        if (ans != null) return ans;
        ans = comm.getClosestEnemyArchon();
        if (ans != null) return ans;
        return explore.getExploreTarget(true);
    }
}
