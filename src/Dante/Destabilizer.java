package Dante;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Destabilizer extends Attacker {

    MicroSage micro;


    Destabilizer(RobotController rc){
        super(rc);
        micro = new MicroSage(rc);
    }

    void play() throws GameActionException {

        if (!micro.doMicro()) tryMove();
        tryAttack(false);

    }

    void tryMove() throws GameActionException {
        if (!rc.isMovementReady()) return;
        MapLocation target = getTarget();
        bfs.move(target);
    }

    MapLocation getTarget() throws GameActionException {
        if (rc.getRoundNum() < Constants.ATTACK_TURN && comm.isEnemyTerritoryRadial(rc.getLocation())) return comm.getClosestAllyArchon();
        MapLocation ans = getBestTarget();
        if (ans != null) return ans;
        ans = comm.getClosestEnemyArchon();
        if (ans != null) return ans;
        return explore.getExploreTarget(true);
    }

}
