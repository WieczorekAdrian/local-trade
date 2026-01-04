package io.github.adrian.wieczorek.local_trade.controller;

import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatMessageDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.ChatSummaryDto;
import io.github.adrian.wieczorek.local_trade.service.chat.dto.UnreadCountDto;
import io.github.adrian.wieczorek.local_trade.service.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class WebChatRestController {

    private final ChatMessageService chatMessageService;

    @GetMapping("/history/{recipientName}")
    @PreAuthorize("isAuthenticated()")
    public List<ChatMessageDto> getHistory(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String recipientName) {
       return chatMessageService.getChatHistory(userDetails,recipientName);
    }

    @PatchMapping("/read-all/{senderEmail}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String senderEmail) {
        chatMessageService.markMessagesAsRead(senderEmail, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/inbox")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatSummaryDto>> getInbox(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail =  userDetails.getUsername();
        return ResponseEntity.ok(chatMessageService.getInbox(userEmail));
    }

    @GetMapping("/unread-total")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnreadCountDto> getTotalUnread(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        return ResponseEntity.ok(chatMessageService.getTotalUnreadCount(userEmail));
    }
}
