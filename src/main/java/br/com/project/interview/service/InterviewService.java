package br.com.project.interview.service;

import br.com.project.interview.domain.Pauta;
import br.com.project.interview.domain.Votacao;
import br.com.project.interview.domain.enumerated.VotoEnum;
import javassist.NotFoundException;

public interface InterviewService {

    public Pauta createPauta(Pauta pauta);

    public Votacao abrirSessaoVotacao(String pautaId, Long fechamento) throws NotFoundException;

    public void registraVoto(String cpf, String votacaoId, VotoEnum voto) throws Exception;

    public Votacao resultadoVotacao(String votacaoId) throws NotFoundException;
}
