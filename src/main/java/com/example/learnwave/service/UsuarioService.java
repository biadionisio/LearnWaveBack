package com.example.learnwave.service;

import com.example.learnwave.dao.UsuarioDAO;
import com.example.learnwave.enums.StatusVerificacao;
import com.example.learnwave.enums.TipoUsuario;
import com.example.learnwave.model.entity.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Usuario criarUsuario(Usuario usuario) {
        try {
            System.out.println("=== CRIANDO USUÁRIO ===");
            System.out.println("Tipo recebido: " + usuario.getTipo());
            
            if (usuarioDAO.existeEmail(usuario.getEmail())) {
                throw new RuntimeException("Email já cadastrado");
            }
            if (usuario.getCpf() != null && usuarioDAO.existeCpf(usuario.getCpf())) {
                throw new RuntimeException("CPF já cadastrado");
            }

            // Definir valores padrão
            if (usuario.getStatus() == null) {
                usuario.setStatus("ativo");
            }
            
            // Forçar status baseado no tipo
            if (TipoUsuario.ALUNO.equals(usuario.getTipo()) || 
                TipoUsuario.ESTUDANTE.equals(usuario.getTipo()) || 
                TipoUsuario.ADMIN.equals(usuario.getTipo()) || 
                TipoUsuario.ADMINISTRADOR.equals(usuario.getTipo())) {
                usuario.setStatusVerificacao(StatusVerificacao.APROVADO);
                System.out.println("Status definido como APROVADO");
            } else {
                usuario.setStatusVerificacao(StatusVerificacao.PENDENTE);
                System.out.println("Status definido como PENDENTE");
            }
            
            usuario.setDataCriacao(LocalDateTime.now());
            usuario.setDataAtualizacao(LocalDateTime.now());

            // Criptografar senha antes de salvar
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

            Usuario usuarioSalvo = usuarioDAO.salvar(usuario);
            System.out.println("Usuário salvo com status: " + usuarioSalvo.getStatusVerificacao());
            return usuarioSalvo;
        } catch (Exception e) {
            System.err.println("ERRO AO CRIAR USUÁRIO: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public Usuario buscarPorId(Integer id) {
        return usuarioDAO.buscarPorId(id);
    }

    public Usuario buscarPorEmail(String email) {
        return usuarioDAO.buscarPorEmail(email);
    }

    public List<Usuario> listarTodos() {
        return usuarioDAO.listarTodos();
    }

    public List<Usuario> buscarPorTipo(TipoUsuario tipo) {
        return usuarioDAO.buscarPorTipo(tipo);
    }

    public List<Usuario> buscarPorTipoEVerificacao(TipoUsuario tipo, com.example.learnwave.enums.StatusVerificacao statusVerificacao) {
        return usuarioDAO.buscarPorStatusVerificacao(statusVerificacao).stream()
                .filter(u -> tipo.equals(u.getTipo()))
                .toList();
    }

    public List<Usuario> buscarPorStatus(String status) {
        return usuarioDAO.buscarPorStatus(status);
    }

    public void atualizarNome(Integer id, String nome) {
        usuarioDAO.atualizarNome(id, nome);
    }

    public void alterarSenha(Integer id, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) throw new RuntimeException("Usuário não encontrado");
        if (!verificarSenha(senhaAtual, usuario.getSenha())) throw new RuntimeException("Senha atual incorreta");
        // Salvar nova senha criptografada
        usuarioDAO.atualizarSenha(id, passwordEncoder.encode(novaSenha));
    }

    public Usuario atualizar(Usuario usuario) {
        Usuario usuarioExistente = usuarioDAO.buscarPorId(usuario.getId());
        if (usuarioExistente == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        // Verificar se email já existe para outro usuário
        Usuario usuarioComEmail = usuarioDAO.buscarPorEmail(usuario.getEmail());
        if (usuarioComEmail != null && !usuarioComEmail.getId().equals(usuario.getId())) {
            throw new RuntimeException("Email já cadastrado para outro usuário");
        }

        usuario.setDataAtualizacao(LocalDateTime.now());
        return usuarioDAO.atualizar(usuario);
    }

    public void alterarStatus(Integer id, String status) {
        System.out.println("Service: Alterando status do usuário ID: " + id + " para: " + status);
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            System.out.println("Service: Usuário não encontrado com ID: " + id);
            throw new RuntimeException("Usuário não encontrado");
        }

        System.out.println("Service: Usuário encontrado: " + usuario.getEmail() + ", status atual: " + usuario.getStatus());
        if ("ativo".equals(status)) {
            usuarioDAO.ativarUsuario(id);
        } else {
            usuarioDAO.desativarUsuario(id);
        }
        System.out.println("Service: Operação concluída");
    }

    public boolean deletar(Integer id) {
        return usuarioDAO.deletar(id);
    }

    public List<Usuario> listarProfessoresPendentes() {
        return usuarioDAO.buscarProfessoresPendentes();
    }

    @Transactional
    public boolean aprovarProfessor(Integer id) {
        System.out.println("Service: Aprovando professor ID: " + id);
        return usuarioDAO.aprovarProfessor(id);
    }

    public boolean rejeitarProfessor(Integer id) {
        System.out.println("Service: Rejeitando professor ID: " + id);
        boolean resultado = usuarioDAO.rejeitarProfessor(id);
        System.out.println("Service: Resultado da rejeição: " + resultado);
        return resultado;
    }

    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario != null && verificarSenha(senha, usuario.getSenha())) {
            return usuario;
        }
        return null;
    }

    /**
     * Verifica se a senha informada corresponde à senha armazenada.
     * Suporta tanto senhas em texto plano (legado) quanto BCrypt (novo).
     */
    private boolean verificarSenha(String senhaPlana, String senhaArmazenada) {
        if (senhaArmazenada == null || senhaPlana == null) return false;
        
        // Se a senha armazenada é um hash BCrypt, usa BCrypt para comparar
        if (isBCrypt(senhaArmazenada)) {
            return passwordEncoder.matches(senhaPlana, senhaArmazenada);
        }
        
        // Senão, é texto plano (legado) — comparação direta
        return senhaPlana.equals(senhaArmazenada);
    }

    /**
     * Verifica se uma string é um hash BCrypt (começa com $2a$, $2b$ ou $2y$).
     */
    private boolean isBCrypt(String senha) {
        return senha != null && (senha.startsWith("$2a$") || senha.startsWith("$2b$") || senha.startsWith("$2y$"));
    }

    public List<Usuario> buscarPorAreaEnsino(String area) {
        return usuarioDAO.buscarPorAreaEnsino(area);
    }

    public List<Usuario> buscarPorEscola(String escola) {
        return usuarioDAO.buscarPorEscola(escola);
    }

    public List<Usuario> buscarPorStatusVerificacao(String status) {
        return usuarioDAO.buscarPorStatusVerificacao(status);
    }

    public Usuario atualizarPerfil(Integer id, String bio, String fotoPerfil) {
        Usuario usuario = usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            throw new RuntimeException("Usuário não encontrado");
        }
        if (bio != null) {
            usuario.setBio(bio);
        }
        if (fotoPerfil != null) {
            usuario.setFotoPerfil(fotoPerfil);
        }
        usuario.setDataAtualizacao(LocalDateTime.now());
        return usuarioDAO.atualizar(usuario);
    }
}
