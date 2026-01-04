package io.github.adrian.wieczorek.local_trade.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessagePayload;
import io.github.adrian.wieczorek.local_trade.service.user.UsersEntity;
import io.github.adrian.wieczorek.local_trade.service.chat.ChatMessageRepository;
import io.github.adrian.wieczorek.local_trade.service.user.UsersRepository;
import io.github.adrian.wieczorek.local_trade.service.chat.service.ChatMessageService;
import io.github.adrian.wieczorek.local_trade.security.JwtService;
import io.github.adrian.wieczorek.local_trade.security.TestJwtUtils;
import io.github.adrian.wieczorek.local_trade.testutils.UserUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "security.jwt.secret-key=41c6701ad7f5abf1db2b053a2f1a39ad41189e00462ec987622b5409dbc0006d")
@Testcontainers
@AutoConfigureMockMvc
public class WebChatIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    JwtService jwtService;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    ChatMessageRepository chatMessageRepository;

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private String url;
    private String senderJwt;
    @Autowired
    private ObjectMapper objectMapper;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @BeforeEach
    public void setup() {
        this.url = "ws://localhost:" + port + "/ws";

        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        this.stompClient.setMessageConverter(converter);

        UsersEntity sender = UserUtils.createUserRoleUser();
        UsersEntity receiver = UserUtils.createUserRoleUser();
        sender.setEmail("Tomek@wp.pl");
        sender.setName("Tomek");
        receiver.setName("Ania");
        receiver.setEmail("Ania@wp.pl");
        receiver.setUserId(UUID.randomUUID());
        usersRepository.saveAndFlush(sender);
        usersRepository.saveAndFlush(receiver);

        this.senderJwt = TestJwtUtils.generateToken(jwtService, sender);
    }

    @Test
    public void sendChatMessage() throws Exception {
        final BlockingQueue<Object> blockingQueue = new ArrayBlockingQueue<>(1);
        ChatMessagePayload payload = new ChatMessagePayload("Hey how are you?");

        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.add("Cookie", "accessToken=" + senderJwt);

        StompHeaders stompHeaders = new StompHeaders();

        StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/user/queue/messages", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessageDto.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        blockingQueue.offer(payload);
                    }
                });
                session.send("/app/chat.sendMessage.private/Ania@wp.pl", payload);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
                blockingQueue.offer(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                exception.printStackTrace();
                blockingQueue.offer(exception);
            }
        };

        CompletableFuture<StompSession> future = stompClient.connectAsync(
                url, handshakeHeaders, stompHeaders, sessionHandler);

        future.get(5, TimeUnit.SECONDS);

        Object result = blockingQueue.poll(5, TimeUnit.SECONDS);

        Assertions.assertNotNull(result, "Nie otrzymano odpowiedzi.");
        if (result instanceof Throwable) {
            Assertions.fail("Błąd STOMP: ", (Throwable) result);
        }

        ChatMessageDto chatMessage = (ChatMessageDto) result;
        Assertions.assertEquals("Hey how are you?", chatMessage.getContent());
        Assertions.assertEquals("Tomek", chatMessage.getSender());

        chatMessageRepository.deleteAll();
    }
}

