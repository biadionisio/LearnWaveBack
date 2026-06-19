package com.example.learnwave.model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "professor_aluno")
public class ProfessorAluno {

    @EmbeddedId
    private ProfessorAlunoId id;

    public ProfessorAluno() {}

    public ProfessorAluno(Integer professorId, Integer alunoId) {
        this.id = new ProfessorAlunoId(professorId, alunoId);
    }

    public ProfessorAlunoId getId() { return id; }
    public void setId(ProfessorAlunoId id) { this.id = id; }

    public Integer getProfessorId() { return id != null ? id.getProfessorId() : null; }
    public Integer getAlunoId() { return id != null ? id.getAlunoId() : null; }
}
