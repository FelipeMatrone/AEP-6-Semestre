package br.com.cesumar.aep.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

// O que este teste prova: o $jsonSchema de banco/init.js, aplicado a um Mongo
// real, recusa tarefa sem dono. É o que os outros *IT não cobrem: eles sobem
// um Mongo limpo, sem validador.
// O controle é uma tarefa do próprio seed: a mesma, com usuarioId, entra; sem
// ele, não. Assim a recusa só pode vir do campo ausente.
@Testcontainers
class ValidadorMongoIT {

	// 121 = DocumentValidationFailure
	private static final int FALHA_DE_VALIDACAO = 121;

	@Container
	static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0")
			// o failsafe roda com o diretório do módulo backend como raiz
			.withCopyFileToContainer(MountableFile.forHostPath(Path.of("..", "banco", "init.js")), "/init.js");

	private static MongoClient cliente;

	private static MongoCollection<Document> tarefas;

	@BeforeAll
	static void aplicarInitJs() throws Exception {
		ExecResult resultado = MONGO.execInContainer("mongosh", "--quiet", "aep", "/init.js");
		assertThat(resultado.getExitCode()).as(resultado.getStderr()).isZero();

		cliente = MongoClients.create(MONGO.getReplicaSetUrl("aep"));
		tarefas = cliente.getDatabase("aep").getCollection("tarefas");
	}

	@AfterAll
	static void fecharCliente() {
		cliente.close();
	}

	@Test
	void tarefaComUsuarioIdPassaNoValidador() {
		Document tarefa = tarefaDoSeedSemId();

		tarefas.insertOne(tarefa);

		assertThat(tarefa.get("_id")).isNotNull();
	}

	@Test
	void tarefaSemUsuarioIdERecusadaPeloValidador() {
		Document tarefa = tarefaDoSeedSemId();
		tarefa.remove("usuarioId");

		assertThatThrownBy(() -> tarefas.insertOne(tarefa))
				.isInstanceOf(MongoWriteException.class)
				.extracting(erro -> ((MongoWriteException) erro).getCode())
				.isEqualTo(FALHA_DE_VALIDACAO);
	}

	private Document tarefaDoSeedSemId() {
		Document tarefa = tarefas.find().first();
		assertThat(tarefa).as("o init.js deveria semear tarefas").isNotNull();
		tarefa.remove("_id");
		return tarefa;
	}

}
