package br.com.aisaas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// sobe o contexto com H2 em memória e valores falsos, sem depender das variáveis de ambiente de produção
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:contextLoads;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=test-secret-with-at-least-32-bytes-for-hs256",
        "spring.mail.username=test@example.com",
        "spring.mail.password=test",
        "spring.ai.google.genai.api-key=test"
})
class AiSaasApplicationTests {

    @Test
    void contextLoads() {
    }

}
