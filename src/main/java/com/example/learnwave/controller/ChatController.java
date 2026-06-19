package com.example.learnwave.controller;

import com.example.learnwave.model.entity.Mensagem;
import com.example.learnwave.model.entity.Usuario;
import com.example.learnwave.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/contatos/{userId}")
    public ResponseEntity<List<Usuario>> buscarContatos(@PathVariable Integer userId) {
        return ResponseEntity.ok(chatService.buscarContatos(userId));
    }

    @GetMapping("/mensagens")
    public ResponseEntity<List<Mensagem>> buscarMensagens(
            @RequestParam Integer remetenteId,
            @RequestParam Integer destinatarioId) {
        return ResponseEntity.ok(chatService.buscarMensagens(remetenteId, destinatarioId));
    }

    @PostMapping("/mensagens")
    public ResponseEntity<Mensagem> enviarMensagem(@RequestBody Mensagem mensagem) {
        return ResponseEntity.ok(chatService.salvarMensagem(mensagem));
    }

    @GetMapping("/vinculos/professor/{professorId}/alunos")
    public ResponseEntity<List<Usuario>> buscarAlunosDoProfessor(@PathVariable Integer professorId) {
        return ResponseEntity.ok(chatService.buscarAlunosDoProfessor(professorId));
    }

    @PostMapping("/vinculos")
    public ResponseEntity<Void> criarVinculo(@RequestBody Map<String, Integer> body) {
        chatService.salvarVinculo(body.get("professorId"), body.get("alunoId"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vinculos")
    public ResponseEntity<Void> removerVinculo(
            @RequestParam Integer professorId,
            @RequestParam Integer alunoId) {
        chatService.removerVinculo(professorId, alunoId);
        return ResponseEntity.ok().build();
    }
}
