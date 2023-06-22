package com.uu.au.config;

import com.uu.au.controllers.AuthController;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketAuthenticationConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketAuthenticationConfig.class);

    @Autowired
    AuthController authController;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {

            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authorization = accessor.getNativeHeader("token");
                    String accessToken = authorization.get(0);

                    try {
                        var auUser = authController.installUserFromToken(accessToken);

                        var u = new UsernamePasswordAuthenticationToken(auUser, null, auUser.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(u);
                        accessor.setUser(u);

                        logger.info("WebSocket handshake successful");

                        return message;

                    } catch (ExpiredJwtException e) {
                        /// FIXME: suppress for now
                        logger.info("ExpiredJwtException: " + e.getMessage());
                        return MessageBuilder.withPayload("Error: your JWT Token has expired").build();

                    } catch (io.jsonwebtoken.security.SignatureException e) {
                        /// FIXME: suppress for now
                        logger.info("SignatureException: " + e.getMessage());
                        return MessageBuilder.withPayload("Error: your JWT Token was not correctly signed").build();

                    } catch (JwtException e) {
                        /// FIXME: suppress for now
                        logger.info("JwtException: " + e.getMessage());
                        return MessageBuilder.withPayload("Error: processing JWT token: " + e.getMessage()).build();

                    }
                }

                return message;
            }
        });
    }
}