package Dante;

import battlecode.common.*;

public class Explore {

    RobotController rc;
    boolean visibleLead;
    int myVisionRange;
    boolean compareLead = false;
    int cumulativeLead = 0;
    MapLocation closestLead = null;
    int distLead = 0;
    MapLocation exploreLoc = null;
    MapLocation closestEnemyArchon = null;
    int distEnemyArchon = 0;

    MapLocation[] checkLocs = new MapLocation[5];
    boolean checker = false;


    static int BYTECODE_EXPLORE_LEAD;

    Explore(RobotController rc){
        this.rc = rc;
        myVisionRange = rc.getType().visionRadiusSquared;
        if (rc.getType() == RobotType.CARRIER || rc.getType() == RobotType.HEADQUARTERS) compareLead = true;
        switch (rc.getType()){
            case CARRIER:
                BYTECODE_EXPLORE_LEAD = 2000;
                break;
            case HEADQUARTERS:
            case LABORATORY:
            case WATCHTOWER:
                BYTECODE_EXPLORE_LEAD = 5000;
                break;
            default:
                BYTECODE_EXPLORE_LEAD = 3000;
                break;
        }
        generateLocs();
    }

    void generateLocs(){
        int w = rc.getMapWidth();
        int h = rc.getMapHeight();
        checkLocs[0] = new MapLocation(w/2,h/2);
        checkLocs[1] = new MapLocation(0,0);
        checkLocs[2] = new MapLocation(w-1,0);
        checkLocs[3] = new MapLocation(0,h-1);
        checkLocs[4] = new MapLocation(w-1,h-1);
    }

    void setChecker(int init){
        exploreLoc = checkLocs[init%checkLocs.length];
        checker = true;
    }

    void reportLead(){
        try {
            visibleLead = false;
            closestLead = null;
            distLead = 0;
            cumulativeLead = 0;
            MapLocation[] mapLocs = rc.senseNearbyLocationsWithLead(myVisionRange);
            if(mapLocs.length > 0) visibleLead = true;
            int i = mapLocs.length;
            MapLocation myLoc = rc.getLocation();
            boolean aggro = Robot.comm.shouldMineAggressively();
            while (i-- > 0){
                if (Clock.getBytecodeNum() > BYTECODE_EXPLORE_LEAD) break;
                int lead = rc.senseLead(mapLocs[i]);
                boolean isEnemy = aggro && Robot.comm.isEnemyTerritory(mapLocs[i]);
                if (compareLead && (isEnemy || lead > Constants.MIN_LEAD_RELEVANT)){
                    cumulativeLead += (lead - Constants.MIN_LEAD_RELEVANT);
                    int d = mapLocs[i].distanceSquaredTo(myLoc);
                    if (closestLead == null || d < distLead){
                        closestLead = mapLocs[i];
                        distLead = d;
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    MapLocation getClosestLead(){
        return closestLead;
    }

    void getEmergencyTarget(int tries, boolean checkDanger) {
        MapLocation myLoc = rc.getLocation();
        int maxX = rc.getMapWidth();
        int maxY = rc.getMapHeight();
        while (tries-- > 0){
            if (exploreLoc != null) return;
            MapLocation newLoc = new MapLocation((int)(Math.random()*maxX), (int)(Math.random()*maxY));
            if (checkDanger && Robot.comm.isEnemyTerritoryRadial(newLoc)) continue;
            if (myLoc.distanceSquaredTo(newLoc) > myVisionRange){
                exploreLoc = newLoc;
            }
        }
    }

    void getCheckerTarget(int tries){
        MapLocation myLoc = rc.getLocation();
        while (tries-- > 0){
            int checkerIndex = (int)(Math.random()* checkLocs.length);
            MapLocation newLoc = checkLocs[checkerIndex];
            if (myLoc.distanceSquaredTo(newLoc) > myVisionRange){
                exploreLoc = newLoc;
            }
        }
    }

    MapLocation getExploreTarget(boolean checkDanger) {
        if (exploreLoc != null && rc.getLocation().distanceSquaredTo(exploreLoc) <= myVisionRange) exploreLoc = null;
        if (exploreLoc == null){
            if (checker) getCheckerTarget(15);
            else getEmergencyTarget(15, checkDanger);
        }
        return exploreLoc;
    }

}
