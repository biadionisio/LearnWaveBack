package com.example.learnwave.dao.impl;

import com.example.learnwave.dao.ChatDAO;
import com.example.learnwave.model.entity.Mensagem;
import com.example.learnwave.model.entity.ProfessorAluno;
import com.example.learnwave.model.entity.ProfessorAlunoId;
import com.example.learnwave.model.entity.Usuario;
import com.example.learnwave.repository.MensagemRepository;
import com.example.learnwave.repository.ProfessorAlunoRepository;
import com.example.learnwave.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatDAOImpl implements ChatDAO {

    @Autowired
    private MensagemRepository mensagemRepository;

    @Autowired
    private ProfessorAlunoRepository professorAlunoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Usuario> buscarContatos(Integer userId) {
        Usuario usuario = usuarioRepository.findById(userId).orElse(null);
        if (usuario == null) return List.of();

        String tipo = usuario.getTipo() != null ? usuario.getTipo().name() : "";

        if ("PROFESSOR".equals(tipo)) {
            List<Integer> alunoIds = professorAlunoRepository.findByIdProfessorId(userId)
                    .stream().map(pa -> pa.getId().getAlunoId()).toList();
            return usuarioRepository.findAllById(alunoIds);
        } else {
            List<Integer> professorIds = professorAlunoRepository.findByIdAlunoId(userId)
                    .stream().map(pa -> pa.getId().getProfessorId()).toList();
            return usuarioRepository.findAllById(professorIds);
        }
    }

    @Override
    public List<Mensagem> buscarMensagens(Integer remetenteId, Integer destinatarioId) {
        return mensagemRepository.findConversa(remetenteId, destinatarioId);
    }

    @Override
    public Mensagem salvarMensagem(Mensagem mensagem) {
        return mensagemRepository.save(mensagem);
    }

    @Override
    public void salvarVinculo(Integer professorId, Integer alunoId) {
        ProfessorAlunoId id = new ProfessorAlunoId(professorId, alunoId);
        if (!professorAlunoRepository.existsById(id)) {
            professorAlunoRepository.save(new ProfessorAluno(professorId, alunoId));
        }
    }

    @Override
    public void removerVinculo(Integer professorId, Integer alunoId) {
        professorAlunoRepository.deleteById(new ProfessorAlunoId(professorId, alunoId));
    }

    @Override
    public List<Usuario> buscarAlunosDoProfessor(Integer professorId) {
        List<Integer> alunoIds = professorAlunoRepository.findByIdProfessorId(professorId)
                .stream().map(pa -> pa.getId().getAlunoId()).toList();
        return usuarioRepository.findAllById(alunoIds);
    }
}
