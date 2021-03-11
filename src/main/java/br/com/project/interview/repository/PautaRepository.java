package br.com.project.interview.repository;

import br.com.project.interview.domain.Pauta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PautaRepository extends JpaRepository<Pauta, String> {
}
