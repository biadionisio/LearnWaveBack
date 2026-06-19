package com.example.learnwave.repository;

import com.example.learnwave.model.entity.ProfessorAluno;
import com.example.learnwave.model.entity.ProfessorAlunoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessorAlunoRepository extends JpaRepository<ProfessorAluno, ProfessorAlunoId> {

    List<ProfessorAluno> findByIdProfessorId(Integer professorId);
    List<ProfessorAluno> findByIdAlunoId(Integer alunoId);
}
