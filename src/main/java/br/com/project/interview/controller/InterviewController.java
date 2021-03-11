package br.com.project.interview.controller;

import br.com.project.interview.domain.Pauta;
import br.com.project.interview.domain.Votacao;
import br.com.project.interview.domain.enumerated.VotoEnum;
import br.com.project.interview.service.InterviewService;
import com.sun.istack.NotNull;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/interview")
public class InterviewController {

    @Autowired
    InterviewService interviewService;

    @PostMapping("/pauta")
    public ResponseEntity<Pauta> cadastrarPauta(@RequestBody Pauta pauta) {

        return new ResponseEntity<>(this.interviewService.createPauta(pauta), HttpStatus.CREATED);

    }

    @PostMapping("/abrir-sessao/{pauta_id}")
    public ResponseEntity abrirSessao(@PathVariable("pauta_id") String pautaId,
                                      @RequestParam(value = "fechamento", required = false) Long fechamento) throws NotFoundException {

        //caso não tenha sido informado, utiliza 1 minuto por default
        if (Objects.isNull(fechamento))
            fechamento = (long) 60;

        try {
            return new ResponseEntity(this.interviewService.abrirSessaoVotacao(pautaId, fechamento), HttpStatus.CREATED);

         } catch (NotFoundException e) {
             return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
         } catch (Exception e) {
             return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
    }

    @PutMapping("/voto/{cpf}/votacao/{votacao_id}")
    public ResponseEntity registraVoto(@PathVariable("cpf") String cpf,
                                       @PathVariable("votacao_id") String votacaoId,
                                       @NotNull @RequestParam("voto") VotoEnum voto) {

        try {
            this.interviewService.registraVoto(cpf, votacaoId, voto);
            return new ResponseEntity(HttpStatus.OK);

        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (InternalError | Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/resultado/{votacao_id}")
    public ResponseEntity resultadoVotacao(@PathVariable("votacao_id") String votacaoId) {

        try {
            return new ResponseEntity(this.interviewService.resultadoVotacao(votacaoId), HttpStatus.OK);

        } catch (NotFoundException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
