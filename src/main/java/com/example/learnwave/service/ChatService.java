package com.example.learnwave.service;

import com.example.learnwave.dao.ChatDAO;
import com.example.learnwave.dto.ChatMessageResponse;
import com.example.learnwave.model.entity.Mensagem;
import com.example.learnwave.model.entity.Usuario;
import com.example.learnwave.repository.ProfessorAlunoRepository;
import com.example.learnwave.repository.UsuarioRepository;
import com.example.learnwave.security.MessageEncryptionService;
import com.example.learnwave.enums.TipoUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatDAO chatDAO;

    @Autowired
    private ProfessorAlunoRepository professorAlunoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MessageEncryptionService messageEncryption;

    @Autowired
    private ChatModerationService moderation;

    public List<Usuario> buscarContatos(Integer userId) {
        return chatDAO.buscarContatos(userId);
    }

    public List<ChatMessageResponse> buscarMensagens(Integer usuarioAutenticadoId, Integer outroUsuarioId) {
        validarVinculo(usuarioAutenticadoId, outroUsuarioId);
        return chatDAO.buscarMensagens(usuarioAutenticadoId, outroUsuarioId).stream()
                .map(this::paraResposta)
                .toList();
    }

    public ChatMessageResponse salvarMensagem(Integer remetenteId, Integer destinatarioId, String texto) {
        moderation.validate(texto);
        validarVinculo(remetenteId, destinatarioId);
        Mensagem mensagem = new Mensagem();
        mensagem.setRemetenteId(remetenteId);
        mensagem.setDestinatarioId(destinatarioId);
        mensagem.setTexto(messageEncryption.encrypt(texto));
        mensagem.setDataEnvio(LocalDateTime.now());
        Mensagem salva = chatDAO.salvarMensagem(mensagem);
        return new ChatMessageResponse(salva.getId(), salva.getRemetenteId(), salva.getDestinatarioId(), texto, salva.getDataEnvio());
    }

    public List<Usuario> buscarAlunosDoProfessor(Integer professorId) {
        return chatDAO.buscarAlunosDoProfessor(professorId);
    }

    public void salvarVinculo(Integer professorId, Integer alunoId) {
        if (professorId == null || alunoId == null || professorId.equals(alunoId)
                || usuarioRepository.findById(professorId).map(Usuario::getTipo).orElse(null) != TipoUsuario.PROFESSOR
                || usuarioRepository.findById(alunoId).map(Usuario::getTipo).orElse(null) != TipoUsuario.ALUNO) {
            throw new SecurityException("O vínculo deve ser entre um professor e um aluno existentes.");
        }
        chatDAO.salvarVinculo(professorId, alunoId);
    }

    public void removerVinculo(Integer professorId, Integer alunoId) {
        chatDAO.removerVinculo(professorId, alunoId);
    }

    private void validarVinculo(Integer primeiroUsuarioId, Integer segundoUsuarioId) {
        boolean existe = professorAlunoRepository.existsByIdProfessorIdAndIdAlunoId(primeiroUsuarioId, segundoUsuarioId)
                || professorAlunoRepository.existsByIdProfessorIdAndIdAlunoId(segundoUsuarioId, primeiroUsuarioId);
        if (!existe) throw new SecurityException("Você não tem permissão para acessar esta conversa.");
    }

    private ChatMessageResponse paraResposta(Mensagem mensagem) {
        return new ChatMessageResponse(
                mensagem.getId(), mensagem.getRemetenteId(), mensagem.getDestinatarioId(),
                messageEncryption.decrypt(mensagem.getTexto()), mensagem.getDataEnvio());
    }
}
