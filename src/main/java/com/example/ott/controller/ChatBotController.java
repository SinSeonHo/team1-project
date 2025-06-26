package com.example.ott.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.ott.dto.ChatBotResponseDTO;
import com.example.ott.service.ChatbotService;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@Log4j2
public class ChatBotController {

    private final ChatbotService chatbotService;

    ChatBotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }
    
   @PostMapping("/ask")
   public ResponseEntity<ChatBotResponseDTO> chatBot(@RequestBody String userInput){

    ChatBotResponseDTO responseDTO = chatbotService.chatBotResponseDTO(userInput);

        return ResponseEntity.ok(responseDTO);
    }
     
    @GetMapping("/ask")
    public ResponseEntity<ChatBotResponseDTO> getChatBot(@RequestParam String userInput) {
        ChatBotResponseDTO responseDTO = chatbotService.chatBotResponseDTO(userInput);
        return ResponseEntity.ok(responseDTO);
    }
    

   
   
    
}
