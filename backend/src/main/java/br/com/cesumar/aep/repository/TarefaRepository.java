package br.com.cesumar.aep.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.cesumar.aep.model.Tarefa;

public interface TarefaRepository extends MongoRepository<Tarefa, String> {
}
