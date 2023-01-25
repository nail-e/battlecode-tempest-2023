package Barry;

import battlecode.common.*;
import java.util.*;
import Barry.*;

public class BasicCommunicator implements Communicator {
  /***
  There are 64 indices in the shared array.

  Indices 0-3 are for headquarter states.
  Indices 4-23 are for messages intended for carrier robots.
  Indices 24-43 are for messages intended for launcher robots.
  Indices 44-63 are for messages intended for amplifier robots.

  The first index in each of the robot-specific ranges (e.g. 4, 24, 44 etc.) holds
    a counter to the total number of writes made to this range ever made. If the
    counter overlows (2^16), it returns to 0.
  ***/

  // hqIndex is a cached index into the shared array of the headquarters this robot belongs to.
  private static int hqIndex = -1;

  // numReceivedMessages is a cached number of total messages this robot received from the shared array.
  private static int numReceivedMessages = 0;

  // receivedRobotMessages is a cached list of all messages received in the robot's channel during this round.
  private static List<Message> receivedRobotMessages = null;
  // receivedRobotMessagesRound is the cached round number when receivedMessages was received.
  private static int receivedRobotMessagesRound = -1;

  @Override
  public boolean sendMessage(Message message, RobotController rc) throws GameActionException {
    boolean success;
    switch(message.messageType) {
      case HQ_STATE:            success = sendHQStateMessage(message, rc);  break;
      case MN_WELL_LOC:         success = sendLocationMessage(message, rc); break;
      case AD_WELL_LOC:         success = sendLocationMessage(message, rc); break;
      case EX_WELL_LOC:         success = sendLocationMessage(message, rc); break;
      case ENEMY_LOC:           success = sendLocationMessage(message, rc); break;
      case FRIENDLY_ISLAND_LOC: success = sendLocationMessage(message, rc); break;
      case ENEMY_ISLAND_LOC:    success = sendLocationMessage(message, rc); break;
      case NEUTRAL_ISLAND_LOC:  success = sendLocationMessage(message, rc); break;
      default:          throw new RuntimeException("should not be here");
    }

    if (success && message.messageType != MessageType.HQ_STATE) {
      Log.println("Successfully sent " + message.messageType + " loc " + message.loc);
    }

    return success;
  }

  @Override
  public List<Message> receiveMessages(MessageType messageType, RobotController rc) throws GameActionException {
    switch(messageType) {
      case HQ_STATE:              return receiveHQStateMessages(rc);
      default:                    return receiveRobotMessages(messageType, rc);
    }
  }

  private List<Message> receiveRobotMessages(MessageType messageType, RobotController rc) throws GameActionException {
    // Receive all messages for this turn
    receiveAllRobotMessages(rc);

    // Filter the received messages by the requested message type
    List<Message> messages = new LinkedList<>();
    for (Message message : messages) {
      if (messageType == message.messageType) {
        messages.add(message);
      }
    }

    return messages;
  }

  private void receiveAllRobotMessages(RobotController rc) throws GameActionException {
    // If we've already received all messages for this round, do nothing.
    if (rc.getRoundNum() == receivedRobotMessagesRound) {
      return;
    }

    // Get the total number of writes ever made to the given recipient by getting the
    //  value at the first index of the recipient's range
    int firstIndex = getFirstIndexOfRange(Entity.of(rc.getType()));
    int lengthOfRange = getLengthOfRange(Entity.of(rc.getType()));
    int numWrites = rc.readSharedArray(firstIndex);

    receivedRobotMessages = new LinkedList<>();

    // Receive all messages written since the latest message was received. Cap the number of
    //  received messages to the length of the range, since there will be at most `length of range` new messages to receive. Read the
    //  messages in reverse chronological order, but build the message list in chronological order.
    for (int count = numWrites-1; count >= numReceivedMessages && count >= numWrites-lengthOfRange; count--) {
      int targetIdx = firstIndex + (count % lengthOfRange) + 1;
      int encoding = rc.readSharedArray(targetIdx);

      // Ignore empty messages
      if (encoding == 0) {
        continue;
      }

      // Decode the message based on its message type. Assumes the message type is
      //  always the right-most bits in the encoding.
      Message message;
      switch(Decoding.messageType(encoding & Encoding.MESSAGE_TYPE_ENCODING_MASK)) {
        case AD_WELL_LOC:           message = Decoding.locationMessage(encoding); break;
        case MN_WELL_LOC:           message = Decoding.locationMessage(encoding); break;
        case EX_WELL_LOC:           message = Decoding.locationMessage(encoding); break;
        case ENEMY_LOC:             message = Decoding.locationMessage(encoding); break;
        case FRIENDLY_ISLAND_LOC:   message = Decoding.locationMessage(encoding); break;
        case ENEMY_ISLAND_LOC:      message = Decoding.locationMessage(encoding); break;
        case NEUTRAL_ISLAND_LOC:    message = Decoding.locationMessage(encoding); break;
        default:          throw new RuntimeException("should not be here");
      }

      receivedRobotMessages.add(0, message);
    }

    // It's possible we haven't read as many received messages as was ever written, but
    //  that only happens when more than `length of range` messages were written to the range in the last turn.
    //  Those messages are effectively lost, so we count them here anyway to ensure that
    //  subsequent invocations of this function will be correct.
    numReceivedMessages = numWrites;
    receivedRobotMessagesRound = rc.getRoundNum();
  }

  // sendLocationMessage sends messages that only contain a messageType and location
  private boolean sendLocationMessage(Message message, RobotController rc) throws GameActionException {
    // Get the total number of writes ever made to the given recipient by getting the
    //  value at the first index of the recipient's range
    int firstIndex = getFirstIndexOfRange(message.recipient);
    int lengthOfRange = getLengthOfRange(Entity.of(rc.getType()));
    int numWrites = rc.readSharedArray(firstIndex);

    // Use the total number of writes to point to one of the `length of range` available indices
    int targetIdx = firstIndex + (numWrites % lengthOfRange) + 1;

    // If we can't write to the shared array, return false
    int encoding = Encoding.ofLocationMessage(message);
    if (!rc.canWriteSharedArray(targetIdx, encoding)) {
      return false;
    }
    if(!rc.canWriteSharedArray(firstIndex, numWrites+1)) {
      return false;
    }

    rc.writeSharedArray(targetIdx, encoding);
    rc.writeSharedArray(firstIndex, numWrites+1);

    return true;
  }

  // sendHQStateMessage writes the given headquarter's state and location to the shared array
  private boolean sendHQStateMessage(Message message, RobotController rc) throws GameActionException {
    // If we're not an HQ, do nothing for now, since non-HQ reporting of HQ state is not yet supported.
    if (rc.getType() != RobotType.HEADQUARTERS) {
      return false;
    }

    // If we're an HQ but not yet set our cached index, set it by finding the first empty slot
    // in the shared array. This assumes that the shared array is initialized to all 0's at the start of the game.
    if (hqIndex == -1) {
      for (int i = 0; i < 4; i++) {
        if (rc.readSharedArray(i) == 0) {
          hqIndex = i;
          break;
        }
      }

      // Ensure we have set an hqIndex
      if (hqIndex == -1) {
        throw new RuntimeException("should have set an hqIndex");
      }
    }

    // If we can't write to the shared array, return false
    int encoding = Encoding.ofHQStateMessage(message);
    if (!rc.canWriteSharedArray(hqIndex, encoding)) {
      return false;
    }

    rc.writeSharedArray(hqIndex, encoding);
    return true;
  }

  private int getFirstIndexOfRange(Entity entity) {
    switch(entity) {
      case CARRIERS:            return 4;
      case LAUNCHERS:           return 24;
      case AMPLIFIERS:          return 44;
      default: throw new RuntimeException("should not be here");
    }
  }

  // getLengthOfRange gets the length of the range for each entity, not
  //  including the first index (which contains count information)
  private int getLengthOfRange(Entity entity) {
    switch(entity) {
      case CARRIERS:            return 19;
      case LAUNCHERS:           return 19;
      case AMPLIFIERS:          return 19;
      default: throw new RuntimeException("should not be here");
    }
  }

  private List<Message> receiveHQStateMessages(RobotController rc) throws GameActionException {
    // If we don't yet belong to an HQ, find the closest one
    if (hqIndex == -1) {
      hqIndex = getClosestHQIndex(rc);

      // We should always find an HQ, since they cannot be destroyed
      if (hqIndex == -1) {
        throw new RuntimeException("should not be here");
      }
    }

    int encoding = rc.readSharedArray(hqIndex);
    Message message = Decoding.hqStateMessage(encoding);

    return Arrays.asList(message);
  }

  private int getClosestHQIndex(RobotController rc) throws GameActionException {
    MapLocation myLoc = rc.getLocation();

    int closestHQIndex = -1;
    int shortestDistance = 0;
    for (int i = 0; i < 4; i++) {
      int encoding = rc.readSharedArray(i);

      // If HQ slot is empty, ignore it
      if (encoding == 0) {
        continue;
      }

      Message message = Decoding.hqStateMessage(encoding);

      // If the distance to this HQ is shorter than any one we've seen, set the hqIndex to it
      int distance = myLoc.distanceSquaredTo(message.loc);
      if (closestHQIndex == -1 || distance < shortestDistance) {
        closestHQIndex = i;
        shortestDistance = distance;
      }
    }

    return closestHQIndex;
  }
}
