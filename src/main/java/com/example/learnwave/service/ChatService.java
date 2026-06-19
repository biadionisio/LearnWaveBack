package com.example.learnwave.service;

import com.example.learnwave.dao.ChatDAO;
import com.example.learnwave.model.entity.Mensagem;
import com.example.learnwave.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatDAO chatDAO;

    public List<Usuario> buscarContatos(Integer userId) {
        return chatDAO.buscarContatos(userId);
    }

    public List<Mensagem> buscarMensagens(Integer remetenteId, Integer destinatarioId) {
        return chatDAO.buscarMensagens(remetenteId, destinatarioId);
    }

    public Mensagem salvarMensagem(Mensagem mensagem) {
        mensagem.setDataEnvio(LocalDateTime.now());
        return chatDAO.salvarMensagem(mensagem);
    }

    public List<Usuario> buscarAlunosDoProfessor(Integer professorId) {
        return chatDAO.buscarAlunosDoProfessor(professorId);
    }

    public void salvarVinculo(Integer professorId, Integer alunoId) {
        chatDAO.salvarVinculo(professorId, alunoId);
    }

    public void removerVinculo(Integer professorId, Integer alunoId) {
        chatDAO.removerVinculo(professorId, alunoId);
    }
}
