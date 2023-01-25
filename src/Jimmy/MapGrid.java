package Jimmy;

import battlecode.common.MapLocation;

enum Tile{
    // map info
    EMPTY,
    OBSTACLE,
    CURRENT,
    CLOUD,
    // robots
    FRIENDLY_HEADQUARTERS,
    FRIENDLY_CARRIER,
    FRIENDLY_LAUNCHER,
    FRIENDLY_AMPLIFIER,
    FRIENDLY_BOOSTER,
    FRIENDLY_DESTABILIZER,
    ENEMY_HEADQUARTERS,
    ENEMY_CARRIER,
    ENEMY_LAUNCHER,
    ENEMY_AMPLIFIER,
    ENEMY_BOOSTER,
    ENEMY_DESTABILIZER,
    // wells
    ADAMANTIUM,
    MANA,
    ELIXER,
    // islands
    NEUTRAL_ISLAND,
    FRIENDLY_ISLAND,
    ENEMY_ISLAND
}

public class MapGrid {
    private static Tile[][] baseGrid;
    static Tile[][] mapGrid;

    static MapLocation getClosestMatchingTile(MapLocation startLocation, Tile targetTile){
        int[] currentPosition = {startLocation.x, startLocation.y};
        int[] directions = {0, 1, 0, -1, 0};
        int directionIndex = 0;
        int steps = 1;
        int stepCount = 0;
        int searchRadius = 0;

        while (true) {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < steps; j++) {
                    if (mapGrid[currentPosition[0]][currentPosition[1]] == targetTile) {
                        return new MapLocation(currentPosition[0], currentPosition[1]);
                    }
                    currentPosition[0] += directions[directionIndex];
                    currentPosition[1] += directions[directionIndex + 1];
                    stepCount++;
                    if (stepCount == searchRadius) {
                        return null;
                    }
                }
                directionIndex = (directionIndex + 1) % 4;
            }
            steps++;
            searchRadius++;
        }
    }
}

