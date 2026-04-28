CREATE TABLE hospedes (
    id UUID PRIMARY KEY,
    nome_completo VARCHAR(160) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    email VARCHAR(160) NOT NULL,
    telefone VARCHAR(30) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_at TIMESTAMP NOT NULL,
    atualizado_at TIMESTAMP NOT NULL
);

CREATE TABLE reservas (
    id UUID PRIMARY KEY,
    hospede_id UUID NOT NULL,
    quarto_id UUID NOT NULL,
    quarto_numero VARCHAR(40),
    check_in_data DATE NOT NULL,
    check_out_data DATE NOT NULL,
    reserva_status VARCHAR(40) NOT NULL,
    pagamento_modo VARCHAR(40) NOT NULL,
    pagamento_status VARCHAR(40) NOT NULL,
    criado_at TIMESTAMP NOT NULL,
    atualizado_at TIMESTAMP NOT NULL,
    check_in_realizado_at TIMESTAMP,
    check_out_realizado_at TIMESTAMP,
    CONSTRAINT fk_reserva_hospede FOREIGN KEY (hospede_id) REFERENCES hospedes(id)
);

CREATE TABLE reserva_comentarios (
    id UUID PRIMARY KEY,
    reserva_id UUID NOT NULL,
    comentario VARCHAR(1000) NOT NULL,
    criado_at TIMESTAMP NOT NULL,
    atualizado_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_comentario_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(id)
);

CREATE TABLE reserva_status_historico (
    id UUID PRIMARY KEY,
    reserva_id UUID NOT NULL,
    status_anterior VARCHAR(40),
    status_novo VARCHAR(40) NOT NULL,
    motivo VARCHAR(255),
    atualizado_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_historico_reserva FOREIGN KEY (reserva_id) REFERENCES reservas(id)
);

CREATE TABLE evento_publicacao_log (
    id UUID PRIMARY KEY,
    tipo_evento VARCHAR(120) NOT NULL,
    agregado_id UUID NOT NULL,
    payload_resumo VARCHAR(4000) NOT NULL,
    publicado_em TIMESTAMP NOT NULL,
    status_publicacao VARCHAR(20) NOT NULL,
    mensagem_erro VARCHAR(1000),
    criado_at TIMESTAMP NOT NULL,
    atualizado_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_hospedes_cpf ON hospedes (cpf);
CREATE INDEX idx_reservas_hospede ON reservas (hospede_id);
CREATE INDEX idx_reservas_quarto ON reservas (quarto_id);
CREATE INDEX idx_reservas_status ON reservas (reserva_status);
CREATE INDEX idx_comentarios_reserva ON reserva_comentarios (reserva_id);
CREATE INDEX idx_historico_reserva ON reserva_status_historico (reserva_id);
CREATE INDEX idx_evento_publicacao_agregado ON evento_publicacao_log (agregado_id);
