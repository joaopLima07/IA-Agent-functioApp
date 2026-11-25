package br.com.aisaas.conversation.tools;

import br.com.aisaas.conversation.tools.dto.PriceResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExchangeAgentTool {
    private final String API_BASE;
    private final RestClient restClient;

    public ExchangeAgentTool() {
        this.API_BASE = "https://economia.awesomeapi.com.br/json/last";
        this.restClient = RestClient.create();
    }

    @Tool(description = "Consulta a cotação atual entre duas moedas: valor de compra, venda, máxima, mínima e variação. Use quando o usuário perguntar o preço/cotação de uma moeda (ex.: dólar, euro) em relação a outra. O par de moedas é consultado no formato ORIGEM-DESTINO, por exemplo USD-BRL (dólar para real), EUR-BRL, BTC-USD.")
    public PriceResponse searchPrice(
            @ToolParam(description = "Código ISO da moeda de origem, em maiúsculas, com 3 letras, sem hífen. Na consulta USD-BRL, este é o USD") String currencyFrom,
            @ToolParam(description = "Código ISO da moeda de destino, em maiúsculas, com 3 letras, sem hífen. Na consulta USD-BRL, este é o BRL") String currencyFor){
        return restClient.get()
                .uri(API_BASE + "/" + currencyFrom + "-" + currencyFor)
                .retrieve()
                .body(PriceResponse.class);
    }


}
