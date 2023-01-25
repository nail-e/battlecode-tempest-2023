package Jimmy;

import java.util.ArrayList;

import battlecode.common.*;

class Message {
    int turnAdded;
    int idx;
    int value;

    Message(int turnAdded, int idx, int value) {
        this.turnAdded = turnAdded;
        this.idx = idx;
        this.value = value;
    }
}

class MyIslandInfo {
    MapLocation location;
    int health;
    Team team;
    int id;
    MyIslandInfo(MapLocation location, int health, Team team, int id) {
        this.location = location;
        this.health = health;
        this.team = team;
        this.id = id;
    }
}

class MyWellInfo {
    MapLocation location;
    ResourceType resourceType;
    MyWellInfo(MapLocation location, ResourceType resourceType) {
        this.location = location;
        this.resourceType = resourceType;
    }
}

class MyRobotInfo{
    MapLocation location;
    RobotType robotType;
    MyRobotInfo(MapLocation location, RobotType robotType){
        this.location = location;
        this.robotType = robotType;
    }
}


public class Communication {

    private static final int AMPLIFIER_INDEX = 0;
    private static final int HEADQUARTERS_START_INDEX = 1;
    private static final int HEADQUARTERS_END_INDEX = HEADQUARTERS_START_INDEX
            + GameConstants.MAX_STARTING_HEADQUARTERS;


    private static final int ISLANDS_START_INDEX = HEADQUARTERS_END_INDEX;
    private static final int ISLANDS_END_INDEX = ISLANDS_START_INDEX + GameConstants.MAX_NUMBER_ISLANDS - 15;

    private static final int WELLS_START_INDEX = ISLANDS_END_INDEX;
    private static final int WELLS_END_INDEX = WELLS_START_INDEX + 10;

    private static final int ENEMIES_START_INDEX = WELLS_END_INDEX;
    private static final int ENEMIES_END_INDEX = ENEMIES_START_INDEX + 10;

    private static final int COMMUNICATION_ARRAY_SIZE = GameConstants.SHARED_ARRAY_LENGTH;
    private static final int OUTDATED_TURNS_AMOUNT = 3;

    // bit stuff
    private static final int TOTAL_BITS = 16;
    private static final int MAPLOC_BITS = 12;
    private static final int TEAM_BITS = 1;
    private static final int HEALTH_BITS = 3;
    private static final int HEALTH_SIZE = (int) Math.ceil(Anchor.ACCELERATING.totalHealth / 8.0);
    private static final int WELL_TYPE_BITS = 1;
    private static final int WELL_SPACE_BITS = 3;

    static MapLocation amplifierLocation = null;
    static MapLocation[] headquarterLocs = new MapLocation[GameConstants.MAX_STARTING_HEADQUARTERS];
    static MyIslandInfo[] islandInfos = new MyIslandInfo[ISLANDS_END_INDEX - ISLANDS_START_INDEX];
    static MyWellInfo[] wellInfos = new MyWellInfo[WELLS_END_INDEX - WELLS_START_INDEX];
    static MyRobotInfo[] enemyRobots = new MyRobotInfo[ENEMIES_END_INDEX - ENEMIES_START_INDEX];

    static ArrayList<Message> islandMessageQueue = new ArrayList<Message>();
    static ArrayList<Message> headQuartersMessageQueue = new ArrayList<Message>();
    static ArrayList<Message> enemyHeadquartersMessageQueue = new ArrayList<Message>();

    static ArrayList<Message> wellMessageQueue = new ArrayList<Message>();
    static ArrayList<Message> enemyMessageQueue = new ArrayList<Message>();

    static boolean canWriteSharedArray = false;
    static RobotController rc;

    /**
     * amplifier
     */
    static MapLocation getAmplifierLocation() throws GameActionException{
        int intLocation = rc.readSharedArray(AMPLIFIER_INDEX);
        MapLocation mapLocation = intToLocation(intLocation);
        amplifierLocation = mapLocation;
        return mapLocation;
    }

    static void updateAmplifierLocation(MapLocation location) throws GameActionException{
        int intLocation = locationToInt(location);
        rc.writeSharedArray(AMPLIFIER_INDEX, intLocation);
    }

    static void clearAmplifierLocation() throws GameActionException{
        rc.writeSharedArray(AMPLIFIER_INDEX, 0);
    }

    /**
     * enemies
     *
     * @throws GameActionException
     */
    static void clearObsoleteEnemies() throws GameActionException {
        for (int i = ENEMIES_START_INDEX; i < ENEMIES_END_INDEX; i++) {
            MapLocation enemyLoc = intToLocation(rc.readSharedArray(i));
            if (enemyLoc == null)
                continue;
            if (!rc.canSenseLocation(enemyLoc))
                continue;
            RobotInfo nearbyRobot = Utils.senseRobotAtLocation(enemyLoc);
            if (nearbyRobot == null || Robot.myTeam == nearbyRobot.team) {
                writeEnemySharedArray(i, 0);
                enemyRobots[i-ENEMIES_START_INDEX] = null;
            }
        }
    }

    static void senseEnemies() throws GameActionException {
        RobotInfo[] enemyRobots = Utils.getEnemyRobots();
        for (int i = 0; i < enemyRobots.length; i++) {
            RobotInfo enemyRobot = enemyRobots[i];
            updateEnemyInfo(enemyRobot);
        }
    }
    public static boolean robotAlreadyExistsInCommArray(RobotInfo robot){
        for(int i = 0; i<enemyRobots.length;i++){
            MyRobotInfo knownRobot = enemyRobots[i];
            if(knownRobot == null) continue;
            if(knownRobot.location.equals(robot.location)) return true;
        }
        return false;
    }

    static void updateEnemyInfo(RobotInfo robot) throws GameActionException{
        if(robotAlreadyExistsInCommArray(robot)) return;
        for(int i = ENEMIES_START_INDEX;i<ENEMIES_END_INDEX;i++){
            int enemyRobotInt = rc.readSharedArray(i);
            if(enemyRobotInt == 0){
                int newEnemyRobotInt = bitPackEnemyInfo(robot);
                writeEnemySharedArray(i, newEnemyRobotInt);
                break;
            }
        }
    }

    static MyRobotInfo[] getEnemies() throws GameActionException {
        for (int i = ENEMIES_START_INDEX; i < ENEMIES_END_INDEX; i++) {
            MyRobotInfo myRobotInfo = unpackBitEnemyInfo(rc.readSharedArray(i));
            enemyRobots[i - ENEMIES_START_INDEX] = myRobotInfo;
        }

        for(int j = 0; j<enemyMessageQueue.size();j++){
            Message msg = enemyMessageQueue.get(j);
            MyRobotInfo myRobotInfo = unpackBitEnemyInfo(msg.value);
            enemyRobots[msg.idx - ENEMIES_START_INDEX] = myRobotInfo;
        }


        return enemyRobots;
    }

    static int bitPackEnemyInfo(RobotInfo robotInfo) throws GameActionException{
        int intRobotLocation = locationToInt(robotInfo.location);
        intRobotLocation = intRobotLocation << (TOTAL_BITS - MAPLOC_BITS);
        try{
            intRobotLocation += robotInfo.type.ordinal();
            return intRobotLocation;
        }catch(Exception e){
            return intRobotLocation;
        }
    }

    static MyRobotInfo unpackBitEnemyInfo(int intRobotInfo){
        if(intRobotInfo == 0) return null;
        int robotLocInt = intRobotInfo >> 4;
        MapLocation robotLocation = intToLocation(robotLocInt);
        int robotTypeMask = 0b111;
        int typeInt = intRobotInfo & robotTypeMask;
        RobotType robotType = RobotType.values()[typeInt];
        return new MyRobotInfo(robotLocation, robotType);
    }


    /**
     *
     * HEADQUARTERS
     */

    static void addHeadquarter() throws GameActionException {
        for (int i = HEADQUARTERS_START_INDEX; i < HEADQUARTERS_END_INDEX; i++) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, locationToInt(Robot.location));
                break;
            }
        }
    }

    static MapLocation[] getHeadquarterInfo() throws GameActionException {
        for (int i = HEADQUARTERS_START_INDEX; i < HEADQUARTERS_END_INDEX; i++) {
            int intValue = rc.readSharedArray(i);
            if (intValue == 0)
                break;
            Communication.headquarterLocs[i - HEADQUARTERS_START_INDEX] = intToLocation(intValue);
        }

        return Communication.headquarterLocs;
    }

    /*
     * WELLS
     */


    static WellInfo[] senseNearbyWells() throws GameActionException {
        WellInfo[] wells = Robot.nearbyWells;
        for (int i = 0; i < wells.length; i++) {
            WellInfo well = wells[i];
            updateWellInfo(well);
        }

        return wells;
    }

    static MyWellInfo[] getWells() throws GameActionException {

        for (int i = WELLS_START_INDEX; i < WELLS_END_INDEX; i++) {
            int wellInt = rc.readSharedArray(i);
            wellInfos[i - WELLS_START_INDEX] = unpackBitWellInfo(wellInt);
        }

        return wellInfos;
    }

    static void updateWellInfo(WellInfo well) throws GameActionException {
        for (int i = WELLS_START_INDEX; i < WELLS_END_INDEX; i++) {
            int wellInt = rc.readSharedArray(i);
            if (wellInt == 0) {
                int newWellInt = bitPackWellInfo(well.getMapLocation(), well.getResourceType());

                writeSharedWellArray(i, newWellInt);
                break;
            }
            MyWellInfo myWellInfo = unpackBitWellInfo(wellInt);

            if (myWellInfo.location.equals(well.getMapLocation()))
                break;

        }
    }

    static MyWellInfo unpackBitWellInfo(int wellInt) {
        try {
            if (wellInt == 0)
                return null;
            int wellLocIdx = wellInt;
            MapLocation location = intToLocation(wellLocIdx >> 4);
            int typeMask = 0b1111;
            int typeInt = wellInt & typeMask;
            ResourceType type = ResourceType.values()[typeInt];
            return new MyWellInfo(location, type);
        } catch (Exception e) {
        }
        ;
        return null;
    }

    static int bitPackWellInfo(MapLocation location, ResourceType type) throws GameActionException {
        int intWellLocation = locationToInt(location);
        intWellLocation = intWellLocation << (TOTAL_BITS - MAPLOC_BITS);
        try {
            intWellLocation += type.ordinal();
            return intWellLocation;
        } catch (Exception e) {
            return intWellLocation;
        }

    }

    /**
     * ISLANDS
     */
    static void senseNearbyIslands() throws GameActionException {
        int[] ids = Robot.nearbyIslands;
        if (ids == null) return;
        for (int i = 0; i < ids.length; i++) {
            updateIslandInfo(ids[i]);
        }
    }

    static void updateIslandInfo(int id) throws GameActionException {
        MapLocation[] islandLocations = rc.senseNearbyIslandLocations(Robot.location, -1, id);
        if (islandLocations.length > 0) {
            int idx_to_write = (id - 1) + ISLANDS_START_INDEX;

            int value = bitPackIslandInfo(id, islandLocations[0]);
            writeIslandSharedArray(idx_to_write, value);
        }
    }

    static int bitPackIslandInfo(int islandId, MapLocation closestLoc) throws GameActionException {
        int intIsland = locationToInt(closestLoc);
        intIsland = intIsland << (TOTAL_BITS - MAPLOC_BITS);
        try {
            Team team = rc.senseTeamOccupyingIsland(islandId);
            intIsland += team.ordinal() << HEALTH_BITS;
            int health = rc.senseAnchorPlantedHealth(islandId);
            intIsland += (int) Math.ceil((float) health / HEALTH_SIZE);
            return intIsland;
        } catch (GameActionException e) {
            return intIsland;
        }
    }

    static MyIslandInfo readIslandInfo(int islandId) {
        try {
            int islandInt = rc.readSharedArray(ISLANDS_START_INDEX + islandId);
            if (islandInt == 0)
                return null;
            int islandLocIdx = islandInt >> (HEALTH_BITS + TEAM_BITS);
            MapLocation location = intToLocation(islandLocIdx);
            int healthMask = 0b111;
            int health = islandInt & healthMask;
            int team = (islandInt >> HEALTH_BITS) % 0b1;
            Team _team = Team.NEUTRAL;
            if (health > 0) {
                _team = Team.values()[team];
            }
            return new MyIslandInfo(location, health, _team, islandId + 1);
        } catch (GameActionException e) {
        }
        ;
        return null;
    }

    static MyIslandInfo[] getIslandInfos() {

        for (int i = ISLANDS_START_INDEX; i < ISLANDS_END_INDEX; i++) {
            islandInfos[i - ISLANDS_START_INDEX] = readIslandInfo(i - ISLANDS_START_INDEX);
        }

        for (int j = 0; j<islandMessageQueue.size();j++){
            Message islandMessage = islandMessageQueue.get(j);
            MyIslandInfo islandInfo = readIslandInfo(islandMessage.value);
            if(islandInfo != null){
                System.out.println("over-writing index " + (islandMessage.idx - ISLANDS_START_INDEX) + " with "+islandInfo.id);
            }
            islandInfos[islandMessage.idx - ISLANDS_START_INDEX] = readIslandInfo(islandMessage.value);
        }

        return islandInfos;
    }

    static MapLocation readIslandLocation(int islandId) throws GameActionException {
        try {
            islandId = islandId + ISLANDS_START_INDEX;
            int islandInt = rc.readSharedArray(islandId);
            int islandLocIdx = islandInt >> (HEALTH_BITS + TEAM_BITS);
            return intToLocation(islandLocIdx);
        } catch (GameActionException e) {
            return null;
        }
    }

    static int readMaxIslandHealth(int islandId) {
        try {
            islandId = islandId + ISLANDS_START_INDEX;
            int islandInt = rc.readSharedArray(islandId);
            int healthMask = 0b111;
            int health = islandInt & healthMask;
            return health;
        } catch (GameActionException e) {
            return -1;
        }
    }

    /*
     * Helpers
     */
    private static int locationToInt(MapLocation m) {
        if (m == null)
            return 0;

        return 1 + m.x + m.y * rc.getMapWidth();
    }

    private static MapLocation intToLocation(int m) {
        if (m == 0)
            return null;
        m--;
        return new MapLocation(m % rc.getMapWidth(), m / rc.getMapWidth());
    }

    private static void writeIslandSharedArray(int index, int value) throws GameActionException {
        if (rc.canWriteSharedArray(0, 0)) {
            try {
                rc.writeSharedArray(index, value);
            } catch (GameActionException e) {
                System.out.println("ugh");
                islandMessageQueue.add(new Message(RobotPlayer.turnCount, index, value));
                if(islandMessageQueue.size() > 5) islandMessageQueue.remove(0);
            }
        } else {
            islandMessageQueue.add(new Message(RobotPlayer.turnCount, index, value));
            if(islandMessageQueue.size() > 5) islandMessageQueue.remove(0);
        }
    }

    private static void writeEnemySharedArray(int index, int value) throws GameActionException {
        if (rc.canWriteSharedArray(0, 0)) {
            try {
                rc.writeSharedArray(index, value);
            } catch (GameActionException e) {
                System.out.println("ugh");
                enemyMessageQueue.add(new Message(RobotPlayer.turnCount, index, value));
            }
        } else {
            enemyMessageQueue.add(new Message(RobotPlayer.turnCount, index, value));
        }
    }


    private static void writeSharedWellArray(int index, int value) throws GameActionException {
        if (rc.canWriteSharedArray(0, 0)) {
            try {
                rc.writeSharedArray(index, value);
            } catch (GameActionException e) {
                System.out.println("ugh");
                wellMessageQueue.add(new Message(RobotPlayer.turnCount, index, value));
            }
        } else {
            wellMessageQueue.add(new Message(RobotPlayer.turnCount, index, value));
        }
    }

    static void tryWriteMessages() throws GameActionException {
        islandMessageQueue.removeIf(msg -> msg.turnAdded + OUTDATED_TURNS_AMOUNT < RobotPlayer.turnCount);
        enemyMessageQueue.removeIf(msg -> msg.turnAdded + OUTDATED_TURNS_AMOUNT < RobotPlayer.turnCount);

        if (rc.canWriteSharedArray(0, 0)) {
            while (islandMessageQueue.size() > 0) {

                Message msg = islandMessageQueue.remove(0);
                try {
                    rc.writeSharedArray(msg.idx, msg.value);
                } catch (GameActionException e) {
                    System.out.println("ugh");
                    islandMessageQueue.add(new Message(msg.turnAdded, msg.idx, msg.value));
                    break;
                }

            }

            while (enemyMessageQueue.size() > 0) {

                Message msg = enemyMessageQueue.remove(0);
                try {
                    rc.writeSharedArray(msg.idx, msg.value);
                } catch (GameActionException e) {
                    System.out.println("ugh");
                    enemyMessageQueue.add(new Message(msg.turnAdded, msg.idx, msg.value));
                    break;
                }

            }

            while (wellMessageQueue.size() > 0) {

                Message msg = wellMessageQueue.remove(0);
                try {
                    rc.writeSharedArray(msg.idx, msg.value);
                } catch (GameActionException e) {
                    System.out.println("ugh");
                    wellMessageQueue.add(new Message(msg.turnAdded, msg.idx, msg.value));
                    break;
                }

            }
        }
    }


}
