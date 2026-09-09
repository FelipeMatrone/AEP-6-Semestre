import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import Login from './pages/Login/Login'
import Cadastro from './pages/Cadastro/Cadastro'
import Inicio from './pages/Inicio/Inicio'
import Anotacoes from './pages/Anotacoes/Anotacoes'
import Categoria from './pages/Anotacoes/Categoria'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro" element={<Cadastro />} />

      <Route element={<Layout />}>
        <Route path="/inicio" element={<Inicio />} />
        <Route path="/anotacoes" element={<Anotacoes />} />
        <Route path="/anotacoes/:id" element={<Categoria />} />
      </Route>

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App