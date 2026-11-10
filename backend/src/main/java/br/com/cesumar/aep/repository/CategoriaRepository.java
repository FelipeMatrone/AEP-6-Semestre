package br.com.cesumar.aep.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.cesumar.aep.model.Categoria;

public interface CategoriaRepository extends MongoRepository<Categoria, String> {

	List<Categoria> findByUsuarioId(String usuarioId);

}
