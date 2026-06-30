package br.com.contastermometro.lancamentos.enums

enum class EscopoEdicao {
    ESTE_MES,          // Cria exceção e mantém a regra original intacta para o futuro
    ESTE_E_PROXIMOS,   // Encerra a série atual, cria uma nova e ajusta o lançamento atual
    TODA_A_SERIE       // (Opcional no MVP) Altera todos, inclusive passados (perigoso!)
}