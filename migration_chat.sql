USE LearnWave;
GO

CREATE TABLE mensagens (
    id INT IDENTITY(1,1) PRIMARY KEY,
    remetente_id INT NOT NULL,
    destinatario_id INT NOT NULL,
    texto NVARCHAR(MAX) NOT NULL,
    data_envio DATETIME2,
    FOREIGN KEY (remetente_id) REFERENCES usuarios(id),
    FOREIGN KEY (destinatario_id) REFERENCES usuarios(id)
);
GO

CREATE TABLE professor_aluno (
    professor_id INT NOT NULL,
    aluno_id INT NOT NULL,
    PRIMARY KEY (professor_id, aluno_id),
    FOREIGN KEY (professor_id) REFERENCES usuarios(id),
    FOREIGN KEY (aluno_id) REFERENCES usuarios(id)
);
GO
