package br.com.project.interview.repository;

import br.com.project.interview.domain.Voto;
import br.com.project.interview.domain.enumerated.VotoEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotoRepository extends JpaRepository<Voto, String> {

    public Optional<Voto> findByUserIdAndVotacaoId(String userId, String votacaoId);

    public Long countByVotacaoIdAndVoto(String votacaoId, VotoEnum voto);
}
