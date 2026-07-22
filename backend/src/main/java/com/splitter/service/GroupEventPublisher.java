package com.splitter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

/**
 * Deliberately sends only a small event type + timestamp — never the actual
 * expense/balance data. The frontend treats this as a "go refetch" signal over
 * the normal authenticated REST API, rather than trusting data pushed over the
 * socket directly. Keeps authorization checks in exactly one place (the REST
 * controllers/services), instead of duplicating them for the socket layer too.
 */
@Component
@RequiredArgsConstructor
public class GroupEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(Long groupId, String eventType) {
        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId,
                (Object) Map.of("type", eventType, "timestamp", Instant.now().toString())
        );
    }

    /** Separate from the group-wide topic so a new comment doesn't force every
     *  viewer to refetch the whole group — only whoever has that expense's
     *  comment thread open is subscribed to this one. */
    public void publishExpenseEvent(Long expenseId, String eventType) {
        messagingTemplate.convertAndSend(
                "/topic/expenses/" + expenseId,
                (Object) Map.of("type", eventType, "timestamp", Instant.now().toString())
        );
    }
}

