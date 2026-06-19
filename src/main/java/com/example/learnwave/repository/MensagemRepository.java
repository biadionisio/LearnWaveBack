package com.example.learnwave.repository;

import com.example.learnwave.model.entity.Mensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensagemRepository extends JpaRepository<Mensagem, Integer> {

    @Query("SELECT m FROM Mensagem m WHERE (m.remetenteId = :a AND m.destinatarioId = :b) OR (m.remetenteId = :b AND m.destinatarioId = :a) ORDER BY m.dataEnvio ASC")
    List<Mensagem> findConversa(@Param("a") Integer a, @Param("b") Integer b);
}
