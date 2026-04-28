package com.hosped.ms_pagamentos.email;

import com.hosped.ms_pagamentos.model.Pagamento;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarEmailPagamentoPendente(Pagamento pagamento) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(pagamento.getEmailHospede());
            helper.setSubject("Confirme seu pagamento - HOSPED");

            String linkPagamento = "http://localhost:8083/pagamentos/pagar/" + pagamento.getId();

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2c3e50;">Confirme seu pagamento</h2>
                    <p>Olá, <strong>%s</strong>!</p>
                    <p>Recebemos sua reserva. Para confirmá-la, efetue o pagamento:</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Valor</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">R$ %s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Forma de pagamento</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px; border: 1px solid #ddd;"><strong>Válido até</strong></td>
                            <td style="padding: 8px; border: 1px solid #ddd;">%s</td>
                        </tr>
                    </table>
                    <a href="%s" style="display: inline-block; background-color: #27ae60; color: white;
                        padding: 14px 28px; text-decoration: none; border-radius: 5px;
                        font-size: 16px; font-weight: bold;">
                        CONFIRMAR PAGAMENTO
                    </a>
                    <p style="color: #888; margin-top: 20px; font-size: 12px;">
                        Atenciosamente,<br>Equipe HOSPED
                    </p>
                </div>
                """.formatted(
                    pagamento.getNomeHospede(),
                    pagamento.getValor(),
                    pagamento.getMetodoPagamento(),
                    pagamento.getDataExpiracao(),
                    linkPagamento
            );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de pagamento pendente", e);
        }
    }

    public void enviarEmailPagamentoAprovado(Pagamento pagamento) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(pagamento.getEmailHospede());
            helper.setSubject("Pagamento confirmado - HOSPED");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #27ae60;">Pagamento confirmado!</h2>
                    <p>Olá, <strong>%s</strong>!</p>
                    <p>Seu pagamento de <strong>R$ %s</strong> foi confirmado com sucesso.</p>
                    <p>Sua reserva está garantida. Até breve!</p>
                    <p style="color: #888; margin-top: 20px; font-size: 12px;">
                        Atenciosamente,<br>Equipe HOSPED
                    </p>
                </div>
                """.formatted(
                    pagamento.getNomeHospede(),
                    pagamento.getValor()
            );

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de pagamento aprovado", e);
        }
    }

    public void enviarEmailPagamentoExpirado(Pagamento pagamento) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(pagamento.getEmailHospede());
            helper.setSubject("Reserva expirada - HOSPED");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #e74c3c;">Reserva expirada</h2>
                    <p>Olá, <strong>%s</strong>!</p>
                    <p>Infelizmente sua reserva expirou pois o pagamento não foi realizado dentro do prazo.</p>
                    <p>Se desejar, faça uma nova reserva em nosso sistema.</p>
                    <p style="color: #888; margin-top: 20px; font-size: 12px;">
                        Atenciosamente,<br>Equipe HOSPED
                    </p>
                </div>
                """.formatted(pagamento.getNomeHospede());

            helper.setText(html, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar email de pagamento expirado", e);
        }
    }
}