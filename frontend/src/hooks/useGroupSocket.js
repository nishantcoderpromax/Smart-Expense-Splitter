import { useTopicSocket } from "./useTopicSocket";

/** Thin wrapper over useTopicSocket for the group-wide broadcast channel. */
export function useGroupSocket(groupId, onEvent) {
  useTopicSocket(groupId ? `/topic/groups/${groupId}` : null, onEvent);
}
