package br.com.cesumar.aep.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.cesumar.aep.dto.SessaoCreateRequest;
import br.com.cesumar.aep.dto.UsuarioResponse;
import br.com.cesumar.aep.service.UsuarioAutenticado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessao")
public class SessaoController {

	private final AuthenticationManager authenticationManager;

	private final SecurityContextRepository securityContextRepository;

	private final SessionAuthenticationStrategy sessionAuthenticationStrategy;

	public SessaoController(AuthenticationManager authenticationManager,
			SecurityContextRepository securityContextRepository,
			SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		this.authenticationManager = authenticationManager;
		this.securityContextRepository = securityContextRepository;
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}

	// montado do principal, sem ir ao banco de novo: o UsuarioAutenticado já
	// carrega os três campos da resposta.
	@GetMapping
	public UsuarioResponse atual(@AuthenticationPrincipal UsuarioAutenticado usuario) {
		return respostaDe(usuario);
	}

	@PostMapping
	public UsuarioResponse entrar(@Valid @RequestBody SessaoCreateRequest request, HttpServletRequest requisicao,
			HttpServletResponse resposta) {
		Authentication autenticado = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

		// troca o id da sessão e descarta o token CSRF de antes do login. É o
		// que os filtros do Spring fariam; autenticando à mão, cabe ao
		// controller chamar, senão uma sessão plantada antes sobrevive ao login.
		sessionAuthenticationStrategy.onAuthentication(autenticado, requisicao, resposta);

		// autenticar à mão não persiste nada: sem o saveContext a requisição
		// seguinte volta 401, porque a sessão não guarda contexto nenhum.
		SecurityContext contexto = SecurityContextHolder.createEmptyContext();
		contexto.setAuthentication(autenticado);
		SecurityContextHolder.setContext(contexto);
		securityContextRepository.saveContext(contexto, requisicao, resposta);

		return respostaDe((UsuarioAutenticado) autenticado.getPrincipal());
	}

	// duas linhas no lugar do logout() do Spring: a rota é DELETE /api/sessao,
	// fixada pelo contrato, e reescrever o matcher do filtro de logout seria
	// mais código do que invalidar a sessão aqui.
	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void sair(HttpServletRequest requisicao) {
		HttpSession sessao = requisicao.getSession(false);
		if (sessao != null) {
			sessao.invalidate();
		}

		SecurityContextHolder.clearContext();
	}

	private UsuarioResponse respostaDe(UsuarioAutenticado usuario) {
		return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email());
	}

}
