package br.com.aisaas.conversation.tools;

import br.com.aisaas.conversation.tools.dto.BrasilApiCepResponse;
import br.com.aisaas.conversation.tools.dto.BrasilApiCnpjResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RegisterAgentTool {
    private final String apiBase;
    private final RestClient restClient;

    public RegisterAgentTool() {
        this.apiBase = "https://brasilapi.com.br/api";
        this.restClient = RestClient.create();
    }

    String sanitizeString(String nonSanitize){
        return nonSanitize.replaceAll("\\D", "");
    }

    @Tool(description = "Consulta os dados cadastrais de uma empresa brasileira pelo CNPJ: razão social, nome fantasia, situação cadastral, atividade principal, porte, endereço, telefone e e-mail. Use quando o usuário informar um CNPJ ou precisar validar/preencher dados de uma empresa.")
    public BrasilApiCnpjResponse searchCNPJ(
            @ToolParam(description = "CNPJ da empresa com 14 dígitos, somente números, sem pontos, barra ou traço") String cnpj){

        return restClient.get()
                .uri(apiBase + "/cnpj/v1/{cnpj}", sanitizeString(cnpj))
                .retrieve()
                .body(BrasilApiCnpjResponse.class);
    }

    @Tool(description = "Consulta o endereço de um CEP brasileiro: rua, bairro, cidade e estado. Use quando o usuário informar um CEP ou precisar completar um endereço.")
    public BrasilApiCepResponse searchCEP(
            @ToolParam(description = "CEP com 8 dígitos, somente números, sem hífen") String cep){


        return restClient.get()
                .uri(apiBase + "/cep/v2/{cep}", sanitizeString(cep))
                .retrieve()
                .body(BrasilApiCepResponse.class);
    }
}
