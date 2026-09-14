package com.example.learnwave.controller;

import com.example.learnwave.enums.TipoUsuario;
import com.example.learnwave.dto.LoginResponse;
import com.example.learnwave.dto.LoginRequest;
import com.example.learnwave.model.entity.Usuario;
import com.example.learnwave.security.ChatTokenService;
import com.example.learnwave.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Base64;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ChatTokenService chatTokenService;

    // CADASTRAR usuário
    @PostMapping
    public ResponseEntity<?> cadastrarUsuario(@RequestBody Usuario usuario) {
        try {
            System.out.println("=== CONTROLLER CADASTRO ===");
            System.out.println("Nome: " + usuario.getNome());
            System.out.println("Email: " + usuario.getEmail());
            System.out.println("Tipo recebido: " + usuario.getTipo());
            System.out.println("Status atual: " + usuario.getStatusVerificacao());
            
            validarDadosObrigatorios(usuario);
            Usuario usuarioCriado = usuarioService.criarUsuario(usuario);
            
            System.out.println("Usuario criado com status: " + usuarioCriado.getStatusVerificacao());
            System.out.println("=== FIM CONTROLLER ===");
            return ResponseEntity.ok(usuarioCriado);
        } catch (Exception e) {
            System.err.println("ERRO NO CADASTRO: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }



    // PESQUISAR usuários
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Usuario> buscarPorEmail(@PathVariable String email) {
        Usuario usuario = usuarioService.buscarPorEmail(email);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<Usuario>> buscarPorTipo(@PathVariable TipoUsuario tipo) {
        return ResponseEntity.ok(usuarioService.buscarPorTipo(tipo));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Usuario>> buscarPorStatus(@PathVariable String status) {
        return ResponseEntity.ok(usuarioService.buscarPorStatus(status));
    }

    // ALTERAR usuário
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable Integer id, @RequestBody Usuario usuario) {
        System.out.println("Atualizando usuario ID: " + id);
        usuario.setId(id);
        validarDadosObrigatorios(usuario);
        Usuario usuarioAtualizado = usuarioService.atualizar(usuario);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @PatchMapping("/{id}/nome")
    public ResponseEntity<Void> atualizarNome(@PathVariable Integer id, @RequestParam String nome) {
        if (usuarioService.buscarPorId(id) == null) return ResponseEntity.notFound().build();
        usuarioService.atualizarNome(id, nome);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/senha")
    public ResponseEntity<?> alterarSenha(@PathVariable Integer id, @RequestParam String senhaAtual, @RequestParam String novaSenha) {
        try {
            usuarioService.alterarSenha(id, senhaAtual, novaSenha);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> alterarStatus(@PathVariable Integer id, @RequestParam String status) {
        System.out.println("Controller: Alterando status do usuário ID: " + id + " para: " + status);
        if (!"ativo".equals(status) && !"inativo".equals(status)) {
            throw new RuntimeException("Status deve ser 'ativo' ou 'inativo'");
        }
        usuarioService.alterarStatus(id, status);
        System.out.println("Controller: Status alterado com sucesso");
        return ResponseEntity.ok().build();
    }

    // PERFIL - Atualizar bio e foto de perfil
    @PatchMapping("/{id}/perfil")
    public ResponseEntity<?> atualizarPerfil(@PathVariable Integer id, @RequestBody java.util.Map<String, String> body) {
        try {
            Usuario usuario = usuarioService.buscarPorId(id);
            if (usuario == null) {
                return ResponseEntity.notFound().build();
            }

            String bio = body.get("bio");
            String fotoPerfil = body.get("fotoPerfil");

            if (bio != null) {
                if (bio.length() > 500) {
                    return ResponseEntity.badRequest().body("Bio deve ter no máximo 500 caracteres");
                }
                usuario.setBio(bio);
            }

            if (fotoPerfil != null) {
                // Validar tamanho (~700KB em base64 para ~500KB de imagem)
                if (fotoPerfil.length() > 700000) {
                    return ResponseEntity.badRequest().body("Foto de perfil muito grande. Máximo ~500KB");
                }
                usuario.setFotoPerfil(fotoPerfil);
            }

            Usuario atualizado = usuarioService.atualizarPerfil(id, bio, fotoPerfil);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao atualizar perfil: " + e.getMessage());
        }
    }

    // PERFIL - Recuperar bio e foto de perfil
    @GetMapping("/{id}/perfil")
    public ResponseEntity<?> buscarPerfil(@PathVariable Integer id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null) {
            return ResponseEntity.notFound().build();
        }
        java.util.Map<String, Object> perfil = new java.util.HashMap<>();
        perfil.put("id", usuario.getId());
        perfil.put("nome", usuario.getNome());
        perfil.put("email", usuario.getEmail());
        perfil.put("bio", usuario.getBio());
        perfil.put("fotoPerfil", usuario.getFotoPerfil());
        perfil.put("tipo", usuario.getTipo());
        return ResponseEntity.ok(perfil);
    }

    // LOGAR usuário
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest login) {
        String email = login.email();
        String senha = login.senha();
        
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email e obrigatorio");
        }
        if (senha == null || senha.trim().isEmpty()) {
            throw new RuntimeException("Senha e obrigatoria");
        }

        Usuario usuario = usuarioService.autenticar(email, senha);
        System.out.println("Usuario encontrado: " + (usuario != null ? usuario.getEmail() + ", Status: " + usuario.getStatus() + ", Verificacao: " + usuario.getStatusVerificacao() : "null"));
        
        if (usuario == null) {
            System.out.println("Usuario nao encontrado ou senha incorreta");
            return ResponseEntity.badRequest().build();
        }

        // Verificar status de verificação do professor ANTES de checar status geral
        if (TipoUsuario.PROFESSOR.equals(usuario.getTipo())) {
            if (usuario.getStatusVerificacao() == null || 
                com.example.learnwave.enums.StatusVerificacao.PENDENTE.equals(usuario.getStatusVerificacao())) {
                throw new RuntimeException("Cadastro aguardando aprovação do administrador");
            }
            if (com.example.learnwave.enums.StatusVerificacao.REJEITADO.equals(usuario.getStatusVerificacao())) {
                throw new RuntimeException("Cadastro reprovado pelo administrador");
            }
        }

        if ("inativo".equals(usuario.getStatus())) {
            System.out.println("Usuario inativo");
            throw new RuntimeException("Usuário inativo");
        }

        System.out.println("Login bem-sucedido para: " + usuario.getEmail());
        return ResponseEntity.ok(LoginResponse.from(usuario, chatTokenService.create(usuario.getId())));
    }

    // APAGAR usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable Integer id) {
        if (!usuarioService.deletar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    // Operações específicas para professores
    @GetMapping("/professores/pendentes")
    public ResponseEntity<List<Usuario>> listarProfessoresPendentes() {
        return ResponseEntity.ok(usuarioService.listarProfessoresPendentes());
    }

    @GetMapping("/professores/aprovados")
    public ResponseEntity<List<Usuario>> listarProfessoresAprovados() {
        return ResponseEntity.ok(usuarioService.buscarPorTipoEVerificacao(TipoUsuario.PROFESSOR, com.example.learnwave.enums.StatusVerificacao.APROVADO));
    }

    @PatchMapping("/{id}/aprovar")
    public ResponseEntity<Void> aprovarProfessor(@PathVariable Integer id) {
        System.out.println("Controller: Recebida solicitacao para aprovar professor ID: " + id);
        if (!usuarioService.aprovarProfessor(id)) {
            System.out.println("Controller: Falha ao aprovar professor ID: " + id);
            return ResponseEntity.notFound().build();
        }
        System.out.println("Controller: Professor aprovado com sucesso ID: " + id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<Void> rejeitarProfessor(@PathVariable Integer id) {
        try {
            System.out.println("Controller: Rejeitando professor ID: " + id);
            if (!usuarioService.rejeitarProfessor(id)) {
                return ResponseEntity.notFound().build();
            }
            System.out.println("Controller: Professor rejeitado com sucesso ID: " + id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println("Controller: Erro ao rejeitar professor: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Rotas específicas baseadas no script SQL
    @GetMapping("/area/{area}")
    public ResponseEntity<List<Usuario>> buscarPorAreaEnsino(@PathVariable String area) {
        return ResponseEntity.ok(usuarioService.buscarPorAreaEnsino(area));
    }

    @GetMapping("/escola/{escola}")
    public ResponseEntity<List<Usuario>> buscarPorEscola(@PathVariable String escola) {
        return ResponseEntity.ok(usuarioService.buscarPorEscola(escola));
    }

    @GetMapping("/verificacao/{status}")
    public ResponseEntity<List<Usuario>> buscarPorStatusVerificacao(@PathVariable String status) {
        return ResponseEntity.ok(usuarioService.buscarPorStatusVerificacao(status));
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<String> buscarDocumentoUsuario(@PathVariable Integer id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        if (usuario == null || usuario.getDocumentoUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario.getDocumentoUrl());
    }



    // Validações
    private void validarDadosObrigatorios(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().trim().isEmpty()) {
            throw new RuntimeException("Nome e obrigatorio");
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email e obrigatorio");
        }
        if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
            throw new RuntimeException("Senha e obrigatoria");
        }
        if (usuario.getTipo() == null) {
            throw new RuntimeException("Tipo de usuario e obrigatorio");
        }
    }
}
