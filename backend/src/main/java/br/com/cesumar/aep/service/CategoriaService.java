package br.com.cesumar.aep.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import br.com.cesumar.aep.dto.AnotacaoCreateRequest;
import br.com.cesumar.aep.dto.AnotacaoResponse;
import br.com.cesumar.aep.dto.AnotacaoUpdateRequest;
import br.com.cesumar.aep.dto.CategoriaCreateRequest;
import br.com.cesumar.aep.dto.CategoriaResponse;
import br.com.cesumar.aep.dto.CategoriaSummaryResponse;
import br.com.cesumar.aep.dto.CategoriaUpdateRequest;
import br.com.cesumar.aep.exception.AnotacaoNotFoundException;
import br.com.cesumar.aep.exception.CategoriaNotFoundException;
import br.com.cesumar.aep.mapper.CategoriaMapper;
import br.com.cesumar.aep.model.Anotacao;
import br.com.cesumar.aep.model.Categoria;
import br.com.cesumar.aep.model.Tarefa;
import br.com.cesumar.aep.repository.CategoriaRepository;

@Service
public class CategoriaService {

	private final CategoriaRepository repository;

	private final MongoTemplate mongoTemplate;

	private final CategoriaMapper mapper;

	private final Clock clock;

	public CategoriaService(CategoriaRepository repository, MongoTemplate mongoTemplate, CategoriaMapper mapper,
			Clock clock) {
		this.repository = repository;
		this.mongoTemplate = mongoTemplate;
		this.mapper = mapper;
		this.clock = clock;
	}

	public List<CategoriaSummaryResponse> listar(String usuarioId) {
		return repository.findByUsuarioId(usuarioId).stream()
				.map(mapper::toSummaryResponse)
				.toList();
	}

	public CategoriaResponse buscarPorId(String id, String usuarioId) {
		return mapper.toResponse(buscarOuFalhar(id, usuarioId));
	}

	public CategoriaResponse criar(CategoriaCreateRequest request, String usuarioId) {
		Categoria categoria = mapper.toModel(request);

		Instant agora = Instant.now(clock);
		categoria.setUsuarioId(usuarioId);
		categoria.setCriadaEm(agora);
		categoria.setAtualizadaEm(agora);

		return mapper.toResponse(repository.save(categoria));
	}

	public CategoriaResponse atualizar(String id, CategoriaUpdateRequest request, String usuarioId) {
		Categoria categoria = buscarOuFalhar(id, usuarioId);

		mapper.updateModel(categoria, request);
		categoria.setAtualizadaEm(Instant.now(clock));

		return mapper.toResponse(repository.save(categoria));
	}

	public void excluir(String id, String usuarioId) {
		Categoria categoria = buscarOuFalhar(id, usuarioId);

		// desvincula antes de apagar: se falhar no meio, sobra tarefa sem
		// disciplina, nunca tarefa apontando para categoria que não existe.
		mongoTemplate.updateMulti(
				Query.query(Criteria.where("categoriaId").is(id).and("usuarioId").is(usuarioId)),
				new Update().unset("categoriaId"),
				Tarefa.class);
		repository.delete(categoria);
	}

	public AnotacaoResponse criarAnotacao(String categoriaId, AnotacaoCreateRequest request, String usuarioId) {
		Categoria categoria = buscarOuFalhar(categoriaId, usuarioId);

		Instant agora = Instant.now(clock);
		Anotacao anotacao = mapper.toAnotacaoModel(request);
		anotacao.setId(new ObjectId().toHexString());
		anotacao.setCriadaEm(agora);
		anotacao.setAtualizadaEm(agora);

		categoria.getAnotacoes().add(anotacao);
		categoria.setAtualizadaEm(agora);
		repository.save(categoria);

		return mapper.toAnotacaoResponse(anotacao);
	}

	public AnotacaoResponse atualizarAnotacao(String categoriaId, String anotacaoId, AnotacaoUpdateRequest request,
			String usuarioId) {
		Categoria categoria = buscarOuFalhar(categoriaId, usuarioId);
		Anotacao anotacao = buscarAnotacaoOuFalhar(categoria, anotacaoId);

		Instant agora = Instant.now(clock);
		mapper.updateAnotacao(anotacao, request);
		anotacao.setAtualizadaEm(agora);
		categoria.setAtualizadaEm(agora);
		repository.save(categoria);

		return mapper.toAnotacaoResponse(anotacao);
	}

	public void excluirAnotacao(String categoriaId, String anotacaoId, String usuarioId) {
		Categoria categoria = buscarOuFalhar(categoriaId, usuarioId);

		categoria.getAnotacoes().remove(buscarAnotacaoOuFalhar(categoria, anotacaoId));
		categoria.setAtualizadaEm(Instant.now(clock));
		repository.save(categoria);
	}

	// categoria de outro usuário é tratada como inexistente: 404, não 403, para
	// não revelar que o id existe.
	private Categoria buscarOuFalhar(String id, String usuarioId) {
		return repository.findById(id)
				.filter(categoria -> usuarioId.equals(categoria.getUsuarioId()))
				.orElseThrow(() -> new CategoriaNotFoundException(id));
	}

	// a anotação só é procurada dentro da categoria já carregada e validada:
	// buscar por id solto poderia acertar a anotação de outra categoria.
	private Anotacao buscarAnotacaoOuFalhar(Categoria categoria, String anotacaoId) {
		return categoria.getAnotacoes().stream()
				.filter(anotacao -> anotacaoId.equals(anotacao.getId()))
				.findFirst()
				.orElseThrow(() -> new AnotacaoNotFoundException(anotacaoId));
	}

}
