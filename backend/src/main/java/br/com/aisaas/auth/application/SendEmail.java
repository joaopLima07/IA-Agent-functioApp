package br.com.aisaas.auth.application;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
public class SendEmail {

    private static final Logger log = LoggerFactory.getLogger(SendEmail.class);

    private final JavaMailSender javaMailSender;
    private final String from;

    public SendEmail(JavaMailSender javaMailSender,  @Value("${spring.mail.username}") String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Async
    public void sendEmail(String email, String token, String name, int min){

        var htmlTemplate = String.format("""
                <!DOCTYPE html>
                       <html lang="pt-BR">
                       <head>
                         <meta charset="UTF-8">
                         <meta name="viewport" content="width=device-width, initial-scale=1.0">
                         <title>Código de redefinição de senha</title>
                       </head>
                       <body style="margin:0; padding:0; background-color:#f4f5f7; font-family:Arial, Helvetica, sans-serif;">
                         <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f5f7; padding:32px 16px;">
                           <tr>
                             <td align="center">
                               <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:520px; background-color:#ffffff; border-radius:8px; overflow:hidden;">
                
                                 <!-- Cabeçalho -->
                                 <tr>
                                   <td style="background-color:#2c5bd1; padding:24px 32px;">
                                     <span style="color:#ffffff; font-size:20px; font-weight:bold;">AI-Saas</span>
                                   </td>
                                 </tr>
                
                                 <!-- Conteúdo -->
                                 <tr>
                                   <td style="padding:32px;">
                                     <h1 style="margin:0 0 16px; font-size:22px; color:#1a2230;">Redefinir sua senha</h1>
                
                                     <p style="margin:0 0 16px; font-size:15px; line-height:1.6; color:#3d4756;">
                                       Olá, %s!
                                     </p>
                                     <p style="margin:0 0 24px; font-size:15px; line-height:1.6; color:#3d4756;">
                                       Recebemos um pedido para redefinir a senha da sua conta.
                                       Use o código abaixo na página de recuperação de senha:
                                     </p>
                
                                     <!-- Código -->
                                     <p style="margin:0 0 24px; padding:16px; background-color:#f4f5f7; border-radius:6px; font-size:32px; font-weight:bold; letter-spacing:8px; color:#1a2230; text-align:center;">
                                       %s
                                     </p>
                
                                     <p style="margin:0 0 8px; font-size:14px; line-height:1.6; color:#3d4756;">
                                       Este código expira em <strong>%d minutos</strong> e só pode ser usado uma vez.
                                     </p>
                                     <p style="margin:0; font-size:14px; line-height:1.6; color:#3d4756;">
                                       Se você não pediu a redefinição, ignore este e-mail. Sua senha continua a mesma.
                                       Nunca compartilhe este código com ninguém.
                                     </p>
                                   </td>
                                 </tr>
                
                                 <!-- Rodapé -->
                                 <tr>
                                   <td style="background-color:#f4f5f7; padding:16px 32px; font-size:12px; color:#7a8494; text-align:center;">
                                     Este é um e-mail automático, por favor não responda.
                                   </td>
                                 </tr>
                
                               </table>
                             </td>
                           </tr>
                         </table>
                       </body>
                       </html>""", HtmlUtils.htmlEscape(name), token, min);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Seu código de redefinição de senha");
            helper.setText(htmlTemplate, true);

            javaMailSender.send(message);
        } catch (Exception e){
            log.error("It was not possible to send the email.", e);
        }
    }
}
