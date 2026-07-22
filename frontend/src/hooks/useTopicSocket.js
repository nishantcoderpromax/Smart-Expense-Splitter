import { useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useAuthStore } from "../store/authStore";
import { API_BASE_URL } from "../api/apiConfig";

/**
 * Subscribes to any STOMP topic for the lifetime of the component. The server
 * only ever sends a small { type, timestamp } event — never actual data — so
 * onEvent should refetch over the normal REST API rather than trust anything
 * delivered over the socket directly.
 */
export function useTopicSocket(topic, onEvent) {
  useEffect(() => {
    if (!topic) return;

    const token = useAuthStore.getState().accessToken;
    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws?token=${encodeURIComponent(token)}`),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe(topic, (message) => {
          try {
            onEvent(JSON.parse(message.body));
          } catch {
            // ignore malformed frames rather than crashing the UI
          }
        });
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [topic]);
}
