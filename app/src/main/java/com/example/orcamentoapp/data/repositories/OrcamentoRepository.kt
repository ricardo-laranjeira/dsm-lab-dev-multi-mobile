package com.example.orcamentoapp.data.repositories

import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.api.ClienteApiService
import com.example.orcamentoapp.data.api.MaterialApiService
import com.example.orcamentoapp.data.api.OrcamentoApiService
import com.example.orcamentoapp.data.api.OrcamentoMaterialApiService
import com.example.orcamentoapp.data.api.OrcamentoServicoApiService
import com.example.orcamentoapp.data.api.ServicoApiService
import com.example.orcamentoapp.data.models.OrcamentoMaterialRequest
import com.example.orcamentoapp.data.models.OrcamentoMaterialUpdateRequest
import com.example.orcamentoapp.data.models.OrcamentoRequest
import com.example.orcamentoapp.data.models.OrcamentoServicoRequest
import com.example.orcamentoapp.data.models.OrcamentoServicoUpdateRequest
import com.example.orcamentoapp.data.models.OrcamentoUpdateRequest
import com.example.orcamentoapp.screens.orcamentos.ClienteOrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.ItemOrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.OrcamentoUi
import com.example.orcamentoapp.screens.orcamentos.ProdutoOrcamentoUi

/** Reúne as operações HTTP usadas pela tela de orçamentos. */
internal open class OrcamentoRepository(
    private val clienteApi: ClienteApiService = ApiClient.clienteApi,
    private val materialApi: MaterialApiService = ApiClient.materialApi,
    private val servicoApi: ServicoApiService = ApiClient.servicoApi,
    private val orcamentoApi: OrcamentoApiService = ApiClient.orcamentoApi,
    private val orcamentoMaterialApi: OrcamentoMaterialApiService = ApiClient.orcamentoMaterialApi,
    private val orcamentoServicoApi: OrcamentoServicoApiService = ApiClient.orcamentoServicoApi
) {

    internal data class DadosOrcamentos(
        val orcamentos: List<OrcamentoUi>,
        val clientes: List<ClienteOrcamentoUi>,
        val materiais: List<ProdutoOrcamentoUi>,
        val servicos: List<ProdutoOrcamentoUi>
    )

    open suspend fun carregar(): DadosOrcamentos {
        val respostaClientes = clienteApi.listarClientes()
        val respostaMateriais = materialApi.listarMateriais()
        val respostaServicos = servicoApi.listarServicos()
        val respostaOrcamentos = orcamentoApi.listarOrcamentos()

        check(respostaClientes.isSuccessful && respostaMateriais.isSuccessful &&
            respostaServicos.isSuccessful && respostaOrcamentos.isSuccessful) {
            "Não foi possível carregar os dados. Verifique sua sessão e a API."
        }

        val clientes = respostaClientes.body().orEmpty().map {
            ClienteOrcamentoUi(it.idclientes, it.nome)
        }
        val materiais = respostaMateriais.body().orEmpty().map {
            ProdutoOrcamentoUi(it.idmateriais, it.descricao, it.valor.toDoubleOrNull() ?: 0.0)
        }
        val servicos = respostaServicos.body().orEmpty().map {
            ProdutoOrcamentoUi(it.idservicos, it.descricao, it.valor.toDoubleOrNull() ?: 0.0)
        }

        val clientesPorId = clientes.associateBy { it.id }
        val materiaisPorId = materiais.associateBy { it.id }
        val servicosPorId = servicos.associateBy { it.id }
        val orcamentos = respostaOrcamentos.body().orEmpty().mapNotNull { orcamento ->
            val rm = orcamentoMaterialApi
                .listarMateriaisOrcamento(orcamento.idorcamentos)
            val rs = orcamentoServicoApi
                .listarServicosOrcamento(orcamento.idorcamentos)

            check(rm.isSuccessful && rs.isSuccessful) {
                "Não foi possível carregar os itens do orçamento #${orcamento.idorcamentos}."
            }

            val itens = buildList {
                rm.body().orEmpty().forEach { item ->
                    materiaisPorId[item.materiaisId]?.let { produto ->
                        add(ItemOrcamentoUi(
                            id = item.materiaisId,
                            tipo = "Material",
                            referenciaId = item.materiaisId,
                            descricao = produto.nome,
                            quantidade = item.quantidade,
                            valorUnitario = produto.valor
                        ))
                    }
                }
                rs.body().orEmpty().forEach { item ->
                    servicosPorId[item.servicosId]?.let { produto ->
                        add(ItemOrcamentoUi(
                            id = item.servicosId,
                            tipo = "Serviço",
                            referenciaId = item.servicosId,
                            descricao = produto.nome,
                            quantidade = item.quantidade,
                            valorUnitario = produto.valor
                        ))
                    }
                }
            }
            val cliente = clientesPorId[orcamento.clientesId]
            OrcamentoUi(
                id = orcamento.idorcamentos,
                clienteId = orcamento.clientesId,
                clienteNome = cliente?.nome ?: "Cliente #${orcamento.clientesId}",
                validade = orcamento.validade,
                status = if (orcamento.status) "Aprovado" else "Não aprovado",
                itens = itens
            )
        }

        return DadosOrcamentos(orcamentos, clientes, materiais, servicos)
    }

    open suspend fun salvar(atual: OrcamentoUi?, novo: OrcamentoUi) {
        val resposta = if (atual == null) {
            orcamentoApi.criarOrcamento(
                OrcamentoRequest(novo.validade, novo.status == "Aprovado", novo.clienteId)
            )
        } else {
            orcamentoApi.atualizarOrcamento(
                atual.id,
                OrcamentoUpdateRequest(novo.validade, novo.status == "Aprovado", novo.clienteId)
            )
        }
        check(resposta.isSuccessful) {
            "Falha ao salvar o orçamento (HTTP ${resposta.code()})."
        }
        val salvo = checkNotNull(resposta.body()) { "A API não retornou o orçamento salvo." }
        sincronizarMateriais(salvo.idorcamentos, novo.itens)
        sincronizarServicos(salvo.idorcamentos, novo.itens)
    }

    open suspend fun excluir(idOrcamento: Int) {
        val resposta = orcamentoApi.excluirOrcamento(idOrcamento)
        check(resposta.isSuccessful) {
            "Falha ao excluir o orçamento (HTTP ${resposta.code()})."
        }
    }

    private suspend fun sincronizarMateriais(
        idOrcamento: Int,
        itensNovos: List<ItemOrcamentoUi>
    ) {
        val resposta = orcamentoMaterialApi.listarMateriaisOrcamento(idOrcamento)
        check(resposta.isSuccessful) { "Orçamento salvo, mas não foi possível consultar os materiais." }
        val antigos = resposta.body().orEmpty().associateBy { it.materiaisId }
        val novos = itensNovos.filter { it.tipo == "Material" }.associateBy { it.referenciaId }

        antigos.forEach { (id, antigo) ->
            val novo = novos[id]
            when {
                novo == null -> check(
                    orcamentoMaterialApi.excluirMaterialOrcamento(idOrcamento, id).isSuccessful
                ) { "Falha ao remover o material #$id." }
                novo.quantidade != antigo.quantidade -> check(
                    orcamentoMaterialApi.atualizarMaterialOrcamento(
                        idOrcamento, id, OrcamentoMaterialUpdateRequest(novo.quantidade)
                    ).isSuccessful
                ) { "Falha ao atualizar o material #$id." }
            }
        }
        novos.forEach { (id, item) ->
            if (id !in antigos) check(
                orcamentoMaterialApi.adicionarMaterialOrcamento(
                    idOrcamento, OrcamentoMaterialRequest(id, item.quantidade)
                ).isSuccessful
            ) { "Falha ao adicionar o material #$id." }
        }
    }

    private suspend fun sincronizarServicos(
        idOrcamento: Int,
        itensNovos: List<ItemOrcamentoUi>
    ) {
        val resposta = orcamentoServicoApi.listarServicosOrcamento(idOrcamento)
        check(resposta.isSuccessful) { "Orçamento salvo, mas não foi possível consultar os serviços." }
        val antigos = resposta.body().orEmpty().associateBy { it.servicosId }
        val novos = itensNovos.filter { it.tipo == "Serviço" }.associateBy { it.referenciaId }

        antigos.forEach { (id, antigo) ->
            val novo = novos[id]
            when {
                novo == null -> check(
                    orcamentoServicoApi.excluirServicoOrcamento(idOrcamento, id).isSuccessful
                ) { "Falha ao remover o serviço #$id." }
                novo.quantidade != antigo.quantidade -> check(
                    orcamentoServicoApi.atualizarServicoOrcamento(
                        idOrcamento, id, OrcamentoServicoUpdateRequest(novo.quantidade)
                    ).isSuccessful
                ) { "Falha ao atualizar o serviço #$id." }
            }
        }
        novos.forEach { (id, item) ->
            if (id !in antigos) check(
                orcamentoServicoApi.adicionarServicoOrcamento(
                    idOrcamento, OrcamentoServicoRequest(id, item.quantidade)
                ).isSuccessful
            ) { "Falha ao adicionar o serviço #$id." }
        }
    }
}
