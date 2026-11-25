package br.com.aisaas.conversation.tools.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BrasilApiCnpjResponse(
        String cnpj,
        @JsonProperty("razao_social") String razaoSocial,
        @JsonProperty("nome_fantasia") String nomeFantasia,
        @JsonProperty("descricao_situacao_cadastral") String situacaoCadastral,
        @JsonProperty("data_situacao_cadastral") String dataSituacaoCadastral,
        @JsonProperty("descricao_motivo_situacao_cadastral") String motivoSituacao,
        @JsonProperty("cnae_fiscal_descricao") String atividadePrincipal,
        @JsonProperty("natureza_juridica") String naturezaJuridica,
        String porte,
        @JsonProperty("descricao_identificador_matriz_filial") String tipoUnidade,
        @JsonProperty("data_inicio_atividade") String dataInicioAtividade,
        @JsonProperty("opcao_pelo_simples") Boolean optanteSimples,
        @JsonProperty("opcao_pelo_mei") Boolean optanteMei,
        @JsonProperty("descricao_tipo_de_logradouro") String tipoLogradouro,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String municipio,
        String uf,
        String cep,
        @JsonProperty("ddd_telefone_1") String telefone,
        String email
) {
}