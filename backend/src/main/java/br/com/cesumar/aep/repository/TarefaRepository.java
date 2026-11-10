package br.com.cesumar.aep.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.cesumar.aep.model.Tarefa;

public interface TarefaRepository extends MongoRepository<Tarefa, String> {

	List<Tarefa> findByUsuarioId(String usuarioId);

	// IsNull casa campo nulo e campo ausente: excluir categoria usa $unset.
	List<Tarefa> findByUsuarioIdAndCategoriaIdIsNull(String usuarioId);
}
