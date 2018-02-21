package com.syngenta.ml.gobang.evt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionConnectEvent;

public class StompConnectEvent implements ApplicationListener<SessionConnectEvent> {
	 
    private final Logger logger = LoggerFactory.getLogger(StompConnectEvent.class);
 
    public void onApplicationEvent(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
 
        String  company = sha.getNativeHeader("company").get(0);
        logger.debug("Connect event [sessionId: " + sha.getSessionId() +"; company: "+ company + " ]");
    }
}