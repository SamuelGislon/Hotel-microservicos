ALTER TABLE reservas
    ADD COLUMN quarto_servico_id BIGINT,
    ADD COLUMN valor_diaria NUMERIC(12, 2),
    ADD COLUMN metodo_pagamento VARCHAR(40);
