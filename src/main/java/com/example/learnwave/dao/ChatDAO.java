package com.example.learnwave.dao;

import com.example.learnwave.model.entity.Mensagem;
import com.example.learnwave.model.entity.Usuario;

import java.util.List;

public interface ChatDAO {

    List<Usuario> buscarContatos(Integer userId);
    List<Mensagem> buscarMensagens(Integer remetenteId, Integer destinatarioId);
    Mensagem salvarMensagem(Mensagem mensagem);
    void salvarVinculo(Integer professorId, Integer alunoId);
    void removerVinculo(Integer professorId, Integer alunoId);
    List<Usuario> buscarAlunosDoProfessor(Integer professorId);
}
