package br.com.project.interview.repository;

import br.com.project.interview.domain.Votacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotacaoRepository extends JpaRepository<Votacao, String> {
}
