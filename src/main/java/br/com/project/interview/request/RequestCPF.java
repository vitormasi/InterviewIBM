package br.com.project.interview.request;

import br.com.project.interview.domain.dto.ResponseCPFStatus;
import br.com.project.interview.domain.enumerated.StatusEnum;
import com.sun.istack.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
public class RequestCPF {

    private static final Logger log = LoggerFactory.getLogger(RequestCPF.class);

    @Value("${microservice.requestCPF.url}")
    private String url;

    public Boolean cpfIsAble(@NotNull final String cpf) {

        try {

            log.info("Request consulta cpf permitido votação - iniciado");

            ResponseEntity<ResponseCPFStatus> response = new RestTemplate().exchange(url.concat(cpf), HttpMethod.GET, null, ResponseCPFStatus.class);

            log.info("Request consulta cpf permitido votação - concluído");

            if (Objects.nonNull(response.getBody()) && response.getBody().getStatus().equals(StatusEnum.ABLE_TO_VOTE))
                return true;

            return false;

        } catch (Exception e) {
            log.error("Erro '{}' no request de consulta para o cpf {}", e.getMessage(), cpf);

            throw new InternalError("Erro no request de consulta cpf: " + e.getMessage());

        }
    }

}
