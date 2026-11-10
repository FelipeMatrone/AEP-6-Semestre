package br.com.cesumar.aep.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import br.com.cesumar.aep.model.Usuario;

public interface UsuarioRepository extends MongoRepository<Usuario, String> {

	// o e-mail chega aqui já em minúsculas: a normalização é do Service, e o
	// índice único de banco/init.js é sobre o valor gravado.
	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

}
