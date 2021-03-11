package br.com.project.interview.service.impl;

import br.com.project.interview.domain.Pauta;
import br.com.project.interview.domain.Votacao;
import br.com.project.interview.domain.Voto;
import br.com.project.interview.domain.dto.TotalVotos;
import br.com.project.interview.domain.enumerated.VotoEnum;
import br.com.project.interview.repository.PautaRepository;
import br.com.project.interview.repository.VotacaoRepository;
import br.com.project.interview.repository.VotoRepository;
import br.com.project.interview.request.RequestCPF;
import br.com.project.interview.service.InterviewService;
import br.com.project.interview.utils.CPFUtils;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class InterviewServiceImpl implements InterviewService {

    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);

    @Autowired
    PautaRepository pautaRepository;

    @Autowired
    VotacaoRepository votacaoRepository;

    @Autowired
    VotoRepository votoRepository;

    @Autowired
    RequestCPF requestCPF;

    @Override
    public Pauta createPauta(Pauta pauta) {
        log.info("Criando pauta");

        return this.pautaRepository.save(pauta);
    }

    @Override
    public Votacao abrirSessaoVotacao(String pautaId, Long fechamento) throws NotFoundException {

        Optional<Pauta> pauta = this.pautaRepository.findById(pautaId);

        if(!pauta.isPresent())
            throw new NotFoundException("Pauta não encontrada para o id: " + pautaId);

        Votacao votacao = new Votacao();
        votacao.setEncerramento(Instant.now().plusSeconds(fechamento));
        votacao.setPauta(pauta.get());

        log.info("Abrindo sessão de votação para pauta id: '{}' encerrando em '{}'", pautaId, votacao.getEncerramento());

        return this.votacaoRepository.save(votacao);
    }

    @Override
    public void registraVoto(String cpf, String votacaoId, VotoEnum votoEnum) throws Exception {
        log.info("Registrando voto para CPF: {}", cpf);

        this.validaPermissaoVoto(cpf);
        this.validaVotacaoExistente(votacaoId);

        if(!this.verificaVotacaoAtiva(votacaoId))
            throw new Exception("A sessão de votação informada está encerrada");

        if(this.votoRepository.findByUserIdAndVotacaoId(cpf, votacaoId).isPresent())
            throw new Exception("O usuário informado já registrou voto para esta sessão de votação");

        Voto voto = new Voto();
        voto.setUserId(cpf);
        voto.setVotacaoId(votacaoId);
        voto.setVoto(votoEnum);

        this.votoRepository.save(voto);

        log.info("Voto registrado para CPF: {}", cpf);
    }

    private void validaPermissaoVoto(String cpf) throws Exception {
        if (!CPFUtils.isCPF(cpf))
            throw new Exception("O CPF informado não é válido");

        if (!this.requestCPF.cpfIsAble(cpf))
            throw new Exception("O CPF informado não possui permissão para votar");
    }

    private void validaVotacaoExistente(String votacaoId) throws NotFoundException {
        Optional<Votacao> votacao = this.votacaoRepository.findById(votacaoId);

        if(!votacao.isPresent())
            throw new NotFoundException("A sessão de votação informada não existe");
    }

    private Boolean verificaVotacaoAtiva(String votacaoId) {
        Optional<Votacao> votacao = this.votacaoRepository.findById(votacaoId);

        if(!votacao.get().getEncerramento().isAfter(Instant.now()))
            return false;

        return true;
    }

    public Votacao resultadoVotacao(String votacaoId) throws NotFoundException {
        log.info("Consultando resultado para votação id: '{}'", votacaoId);

        this.validaVotacaoExistente(votacaoId);

        Optional<Votacao> votacaoOptional = this.votacaoRepository.findById(votacaoId);
        Votacao votacao = votacaoOptional.get();

        List<TotalVotos> totalVotos = new ArrayList<>();

        Arrays.stream(VotoEnum.values()).forEach(votoEnum ->
                totalVotos.add(
                        new TotalVotos(votoEnum, this.votoRepository.countByVotacaoIdAndVoto(votacaoId, votoEnum)
                        )
                )
        );

        votacao.setVotos(totalVotos);
        if(!this.verificaVotacaoAtiva(votacaoId)) {
            log.info("Registrando resultado da votação '{}' já encerrada", votacaoId);

            TotalVotos resultado = totalVotos
                    .stream()
                    .max(Comparator.comparing(TotalVotos::getQuantidade))
                    .orElseThrow(NoSuchElementException::new);

            votacao.setResultado(resultado.getVoto());
            //realiza o save para atualizar o BD com o resultado
            this.votacaoRepository.save(votacao);
        }

        return votacao;
    }

}
