package br.com.project.interview.domain.dto;

import br.com.project.interview.domain.enumerated.VotoEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TotalVotos {

    private VotoEnum voto;
    private Long quantidade;

}
