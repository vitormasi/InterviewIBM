package br.com.project.interview.domain;

import br.com.project.interview.domain.dto.TotalVotos;
import br.com.project.interview.domain.enumerated.VotoEnum;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Votacao implements Serializable {

    private static final long serialVersionUID = 7200016762277981903L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private Pauta pauta;
    private Instant encerramento;
    private VotoEnum resultado;

    @Transient
    private List<TotalVotos> votos;

}
