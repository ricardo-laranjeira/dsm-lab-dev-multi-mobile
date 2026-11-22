package com.example.orcamentoapp.data

import com.example.orcamentoapp.data.api.AgendamentoApiService
import com.example.orcamentoapp.data.api.AuthInterceptor
import com.example.orcamentoapp.data.api.CategoriaApiService
import com.example.orcamentoapp.data.api.ClienteApiService
import com.example.orcamentoapp.data.api.EnderecoApiService
import com.example.orcamentoapp.data.api.MaterialApiService
import com.example.orcamentoapp.data.api.OrcamentoApiService
import com.example.orcamentoapp.data.api.OrcamentoMaterialApiService
import com.example.orcamentoapp.data.api.OrcamentoServicoApiService
import com.example.orcamentoapp.data.api.ServicoApiService
import com.example.orcamentoapp.data.api.UsuarioApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    var tokenStorage: TokenStorage? = null

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenStorage?.obterToken() })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val usuarioApi: UsuarioApiService by lazy {
        retrofit.create(UsuarioApiService::class.java)
    }

    val clienteApi: ClienteApiService by lazy {
        retrofit.create(ClienteApiService::class.java)
    }

    val enderecoApi: EnderecoApiService by lazy {
        retrofit.create(EnderecoApiService::class.java)
    }

    val categoriaApi: CategoriaApiService by lazy {
        retrofit.create(CategoriaApiService::class.java)
    }

    val materialApi: MaterialApiService by lazy {
        retrofit.create(MaterialApiService::class.java)
    }

    val servicoApi: ServicoApiService by lazy {
        retrofit.create(ServicoApiService::class.java)
    }

    val orcamentoApi: OrcamentoApiService by lazy {
        retrofit.create(OrcamentoApiService::class.java)
    }

    val agendamentoApi: AgendamentoApiService by lazy {
        retrofit.create(AgendamentoApiService::class.java)
    }

    val orcamentoMaterialApi: OrcamentoMaterialApiService by lazy {
        retrofit.create(OrcamentoMaterialApiService::class.java)
    }

    val orcamentoServicoApi: OrcamentoServicoApiService by lazy {
        retrofit.create(OrcamentoServicoApiService::class.java)
    }
}
