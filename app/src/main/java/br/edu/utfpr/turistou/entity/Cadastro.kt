package br.edu.utfpr.turistou.entity

data class Cadastro (
    val id: Int = 0,
    val nome: String,
    val descricao: String,
    val latitude: String,
    val longitude: String,
    val endereco: String = "",
    val imagem: ByteArray? = null
)