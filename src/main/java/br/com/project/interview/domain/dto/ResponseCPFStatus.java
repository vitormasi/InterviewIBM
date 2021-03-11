package br.com.project.interview.domain.dto;

import br.com.project.interview.domain.enumerated.StatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseCPFStatus {

    private StatusEnum status;

}
