package br.com.cesumar.aep.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.cesumar.aep.model.EventoCalendario;

public interface EventoCalendarioRepository extends MongoRepository<EventoCalendario, String> {

	List<EventoCalendario> findByUsuarioIdOrderByDataAscHorarioAsc(String usuarioId);
}
