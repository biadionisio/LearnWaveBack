package com.example.learnwave.controller;

import com.example.learnwave.dto.ChatMessageRequest;
import com.example.learnwave.dto.ChatMessageResponse;
import com.example.learnwave.model.entity.Usuario;
import com.example.learnwave.security.ChatAuthenticationFilter;
import com.example.learnwave.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/contatos/{userId}")
    public ResponseEntity<List<Usuario>> buscarContatos(@PathVariable Integer userId, HttpServletRequest request) {
        return ResponseEntity.ok(chatService.buscarContatos(usuarioAutenticado(request, userId)));
    }

    @GetMapping("/mensagens")
    public ResponseEntity<List<ChatMessageResponse>> buscarMensagens(
            @RequestParam Integer destinatarioId, HttpServletRequest request) {
        return ResponseEntity.ok(chatService.buscarMensagens(usuarioAutenticado(request, null), destinatarioId));
    }

    @PostMapping("/mensagens")
    public ResponseEntity<ChatMessageResponse> enviarMensagem(@RequestBody ChatMessageRequest mensagem, HttpServletRequest request) {
        return ResponseEntity.ok(chatService.salvarMensagem(
                usuarioAutenticado(request, null), mensagem.destinatarioId(), mensagem.texto()));
    }

    @GetMapping("/vinculos/professor/{professorId}/alunos")
    public ResponseEntity<List<Usuario>> buscarAlunosDoProfessor(@PathVariable Integer professorId, HttpServletRequest request) {
        return ResponseEntity.ok(chatService.buscarAlunosDoProfessor(usuarioAutenticado(request, professorId)));
    }

    @PostMapping("/vinculos")
    public ResponseEntity<Void> criarVinculo(@RequestBody Map<String, Integer> body, HttpServletRequest request) {
        Integer usuarioId = usuarioAutenticado(request, null);
        if (!usuarioId.equals(body.get("professorId")) && !usuarioId.equals(body.get("alunoId"))) {
            return ResponseEntity.status(403).build();
        }
        chatService.salvarVinculo(body.get("professorId"), body.get("alunoId"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vinculos")
    public ResponseEntity<Void> removerVinculo(
            @RequestParam Integer professorId,
            @RequestParam Integer alunoId, HttpServletRequest request) {
        Integer usuarioId = usuarioAutenticado(request, null);
        if (!usuarioId.equals(professorId) && !usuarioId.equals(alunoId)) return ResponseEntity.status(403).build();
        chatService.removerVinculo(professorId, alunoId);
        return ResponseEntity.ok().build();
    }

    private Integer usuarioAutenticado(HttpServletRequest request, Integer expectedUserId) {
        Integer userId = (Integer) request.getAttribute(ChatAuthenticationFilter.USER_ID_ATTRIBUTE);
        if (userId == null || (expectedUserId != null && !expectedUserId.equals(userId))) {
            throw new SecurityException("Você não tem permissão para esta operação.");
        }
        return userId;
    }
}
