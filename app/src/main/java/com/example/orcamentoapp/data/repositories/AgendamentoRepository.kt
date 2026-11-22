package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.AgendamentoApiService
import com.example.orcamentoapp.data.api.ClienteApiService
import com.example.orcamentoapp.data.api.OrcamentoApiService
import com.example.orcamentoapp.data.models.AgendamentoRequest
import com.example.orcamentoapp.data.models.AgendamentoResponse
import com.example.orcamentoapp.data.models.AgendamentoUpdateRequest
import com.example.orcamentoapp.data.models.ClienteResponse
import com.example.orcamentoapp.data.models.OrcamentoResponse

internal open class AgendamentoRepository(
    private val agendamentoApi: AgendamentoApiService = ApiClient.agendamentoApi,
    private val orcamentoApi: OrcamentoApiService = ApiClient.orcamentoApi,
    private val clienteApi: ClienteApiService = ApiClient.clienteApi
) {

    data class DadosAgendamentos(
        val agendamentos: List<AgendamentoResponse>,
        val orcamentos: List<OrcamentoResponse>,
        val clientes: List<ClienteResponse>
    )

    open suspend fun carregarDados(): DadosAgendamentos {
        val ra = agendamentoApi.listarAgendamentos()
        val ro = orcamentoApi.listarOrcamentos()
        val rc = clienteApi.listarClientes()

        check(ra.isSuccessful && ro.isSuccessful && rc.isSuccessful) {
            "Não foi possível carregar agendamentos (HTTP A:${ra.code()} O:${ro.code()} C:${rc.code()})."
        }

        return DadosAgendamentos(ra.body().orEmpty(), ro.body().orEmpty(), rc.body().orEmpty())
    }

    open suspend fun salvar(
        id: Int?,
        data: String,
        hora: String,
        orcamentoId: Int
    ): AgendamentoResponse {
        val resposta = if (id == null) {
            agendamentoApi.criarAgendamento(
                AgendamentoRequest(data, hora, orcamentoId)
            )
        } else {
            agendamentoApi.atualizarAgendamento(
                id,
                AgendamentoUpdateRequest(data, hora)
            )
        }
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível salvar o agendamento (HTTP ${resposta.code()}).")
        }
        return checkNotNull(resposta.body()) { "A API não retornou o agendamento salvo." }
    }

    open suspend fun excluir(id: Int) {
        val resposta = agendamentoApi.excluirAgendamento(id)
        check(resposta.isSuccessful) {
            resposta.errorBody()?.string()?.takeIf { it.isNotBlank() }
                ?: ("Não foi possível excluir o agendamento (HTTP ${resposta.code()}).")
        }
    }
}
