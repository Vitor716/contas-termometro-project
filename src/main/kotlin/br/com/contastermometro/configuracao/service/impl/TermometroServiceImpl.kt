package br.com.contastermometro.configuracao.service.impl

import br.com.contastermometro.configuracao.dto.TermometroRequest
import br.com.contastermometro.configuracao.dto.TermometroResponse
import br.com.contastermometro.configuracao.dto.toModel
import br.com.contastermometro.configuracao.dto.toResponse
import br.com.contastermometro.configuracao.repository.TermometroRepository
import br.com.contastermometro.configuracao.service.TermometroService
import org.springframework.stereotype.Service

@Service
class TermometroServiceImpl (
    private val repository : TermometroRepository
) : TermometroService {

    override fun criar(req: TermometroRequest) : TermometroResponse {
        val entity = req.toModel()
        val saved = repository.save(entity)
        return saved.toResponse()
    }

    override fun buscar() : TermometroResponse {
        val entity = repository.findAll().firstOrNull()
            ?: throw NoSuchElementException("Nenhuma configuração de termômetro encontrada.")
        return entity.toResponse()
    }

    override fun editar(id: Long, req: TermometroRequest) : TermometroResponse {
        val updatedEntity = repository.findById(id).orElseThrow { IllegalArgumentException("Termômetro com id $id não encontrado.") }.apply {
            reservaMinimaIntocavel = req.reservaMinimaIntocavel
            orcamentoDiarioMinimo = req.orcamentoDiarioMinimo
            comprometimentoMaximoRenda = req.comprometimentoMaximoRenda
            margemSeguranca = req.margemSeguranca
            estrategia = req.estrategia
        }

        val saved = repository.save(updatedEntity)
        return saved.toResponse()
    }
}