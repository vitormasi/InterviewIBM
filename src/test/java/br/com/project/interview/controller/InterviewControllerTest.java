package br.com.project.interview.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

import br.com.project.interview.InterviewApplication;
import br.com.project.interview.domain.Pauta;
import br.com.project.interview.domain.Votacao;
import br.com.project.interview.domain.dto.TotalVotos;
import br.com.project.interview.domain.enumerated.VotoEnum;
import br.com.project.interview.request.RequestCPF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.Arrays;

@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = InterviewApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class InterviewControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private RequestCPF requestCPF;

//    criar pauta
    @Test
    public void testCriarPauta() {

        Pauta pauta = new Pauta("1", "Titulo", "Descrição");

        final ResponseEntity<Pauta> response = this.criaPautaDefault();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(pauta);

    }

    private ResponseEntity<Pauta> criaPautaDefault() {
        Pauta pauta = new Pauta("1", "Titulo", "Descrição");

        return restTemplate.exchange("/interview/pauta",
                HttpMethod.POST,
                new HttpEntity<>(pauta, new HttpHeaders()),
                Pauta.class);
    }

//    abrir sessao /abrir-sessao/{pauta_id}
    @Test
    public void testAbrirSessao() {

        ResponseEntity<Votacao> response = this.criaVotacaoDefault("300");

        Votacao votacao = new Votacao("1",
                new Pauta("1", "Titulo", "Descrição"),
                Instant.now().plusSeconds(300),
                null,
                null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("encerramento").isEqualTo(votacao);
        assertThat(response.getBody().getEncerramento().with(ChronoField.MILLI_OF_SECOND, 0))
                .isEqualTo(votacao.getEncerramento().with(ChronoField.MILLI_OF_SECOND, 0));


    }

    private ResponseEntity<Votacao> criaVotacaoDefault(String fechamento) {
        this.criaPautaDefault();

        return restTemplate.exchange("/interview/abrir-sessao/1?fechamento=" + fechamento,
                HttpMethod.POST,
                new HttpEntity<>(new HttpHeaders()),
                Votacao.class);
    }

//    abrir sessao sem passar data
    @Test
    public void testAbrirSessaoSemEnvioFechamento() {

        this.criaPautaDefault();

        final ResponseEntity<Votacao> response = restTemplate.exchange("/interview/abrir-sessao/1",
                HttpMethod.POST,
                new HttpEntity<>(new HttpHeaders()),
                Votacao.class);

        Votacao votacao = new Votacao("1",
                new Pauta("1", "Titulo", "Descrição"),
                Instant.now().plusSeconds(60),
                null,
                null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("encerramento").isEqualTo(votacao);
        assertThat(response.getBody().getEncerramento().with(ChronoField.MILLI_OF_SECOND, 0))
                .isEqualTo(votacao.getEncerramento().with(ChronoField.MILLI_OF_SECOND, 0));


    }

//    abrir sessao com pauta invalida
@Test
public void testAbrirSessaoPautaInvalida() {

    final ResponseEntity<String> response = restTemplate.exchange("/interview/abrir-sessao/999?fechamento=300",
            HttpMethod.POST,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isEqualTo("Pauta não encontrada para o id: 999");

}

//    registrar voto (lembrar mock) /voto/{cpf}/votacao/{votacao_id}
@Test
public void testRegistraVoto() {

        this.criaVotacaoDefault("300");

    Mockito.when(requestCPF.cpfIsAble(anyString())).thenReturn(true);

    final ResponseEntity<String> response = restTemplate.exchange("/interview/voto/85138392050/votacao/1?voto=SIM",
            HttpMethod.PUT,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

}

    //    registrar voto com cpf invalido
    @Test
    public void testRegistraVotoCpfInvalido() {

        this.criaVotacaoDefault("300");

        Mockito.when(requestCPF.cpfIsAble(anyString())).thenReturn(true);

        final ResponseEntity<String> response = restTemplate.exchange("/interview/voto/99999999999/votacao/1?voto=SIM",
                HttpMethod.PUT,
                new HttpEntity<>(new HttpHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("O CPF informado não é válido");

    }


//    registrar voto com mock rejeitando
@Test
public void testRegistraVotoCpfServicoCpfUnable() {

    this.criaVotacaoDefault("300");

    Mockito.when(requestCPF.cpfIsAble(anyString())).thenReturn(false);

    final ResponseEntity<String> response = restTemplate.exchange("/interview/voto/85138392050/votacao/1?voto=SIM",
            HttpMethod.PUT,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("O CPF informado não possui permissão para votar");

}
//    registrar voto para votacao encerrada
@Test
public void testRegistraVotoVotacaoEncerrada() {

    this.criaVotacaoDefault("0");

    Mockito.when(requestCPF.cpfIsAble(anyString())).thenReturn(true);

    final ResponseEntity<String> response = restTemplate.exchange("/interview/voto/85138392050/votacao/1?voto=SIM",
            HttpMethod.PUT,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("A sessão de votação informada está encerrada");

}
//    registrar voto com usuario que já registrou voto
@Test
public void testRegistraVotoUsuarioJaVotou() {

    this.criaVotacaoDefault("300");

    Mockito.when(requestCPF.cpfIsAble(anyString())).thenReturn(true);

    ResponseEntity<String> response = restTemplate.exchange("/interview/voto/85138392050/votacao/1?voto=SIM",
            HttpMethod.PUT,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    response = restTemplate.exchange("/interview/voto/85138392050/votacao/1?voto=SIM",
            HttpMethod.PUT,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("O usuário informado já registrou voto para esta sessão de votação");

}
//    registrar voto sem voto
@Test
public void testRegistraVotoSemEnviarVoto() {

    this.criaVotacaoDefault("300");

    Mockito.when(requestCPF.cpfIsAble(anyString())).thenReturn(true);

    ResponseEntity response = restTemplate.exchange("/interview/voto/85138392050/votacao/1",
            HttpMethod.PUT,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

}
//
//    resultado /resultado/{votacao_id}
@Test
public void testResultadoParcial() {

    ResponseEntity<Votacao> votacaoResponseEntity = this.criaVotacaoDefault("300");
    Votacao votacao = votacaoResponseEntity.getBody();
    votacao.setVotos(Arrays.asList(new TotalVotos(VotoEnum.SIM, (long) 0), new TotalVotos(VotoEnum.NAO, (long) 0)));

    ResponseEntity<Votacao> response = restTemplate.exchange("/interview/resultado/1",
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            Votacao.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("encerramento").isEqualTo(votacao);

}
//    resultado com id invalido

    @Test
    public void testResultadoVotacaoInvalida() {

        ResponseEntity<Votacao> votacaoResponseEntity = this.criaVotacaoDefault("300");
        Votacao votacao = votacaoResponseEntity.getBody();
        votacao.setVotos(Arrays.asList(new TotalVotos(VotoEnum.SIM, (long) 0), new TotalVotos(VotoEnum.NAO, (long) 0)));

        ResponseEntity<String> response = restTemplate.exchange("/interview/resultado/999",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("A sessão de votação informada não existe");

    }

}
