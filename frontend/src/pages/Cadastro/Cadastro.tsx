import { Link } from 'react-router-dom'

/** Placeholder: existe para a rota do "Cadastre-se" ser real. */
export default function Cadastro() {
  return (
    <main style={{ display: 'grid', placeItems: 'center', minHeight: '100dvh' }}>
      <div style={{ textAlign: 'center' }}>
        <h1>Cadastro</h1>
        <p>Esta tela ainda será implementada.</p>
        <Link to="/login">Voltar para o login</Link>
      </div>
    </main>
  )
}
