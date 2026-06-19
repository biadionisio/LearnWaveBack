package com.example.learnwave.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProfessorAlunoId implements Serializable {

    @Column(name = "professor_id")
    private Integer professorId;

    @Column(name = "aluno_id")
    private Integer alunoId;

    public ProfessorAlunoId() {}

    public ProfessorAlunoId(Integer professorId, Integer alunoId) {
        this.professorId = professorId;
        this.alunoId = alunoId;
    }

    public Integer getProfessorId() { return professorId; }
    public void setProfessorId(Integer professorId) { this.professorId = professorId; }

    public Integer getAlunoId() { return alunoId; }
    public void setAlunoId(Integer alunoId) { this.alunoId = alunoId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfessorAlunoId that)) return false;
        return Objects.equals(professorId, that.professorId) && Objects.equals(alunoId, that.alunoId);
    }

    @Override
    public int hashCode() { return Objects.hash(professorId, alunoId); }
}
