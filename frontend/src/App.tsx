import { Route, Routes } from 'react-router'
import { Layout } from './components/layout/Layout/Layout'
import { SesionProvider } from './components/layout/SesionProvider/SesionProvider'
import { useSesion } from './hooks/useSesion'
import { Actividad } from './pages/Actividad'
import { BloqueoFormPage } from './pages/BloqueoFormPage'
import { Bloqueos } from './pages/Bloqueos'
import { Disponibilidad } from './pages/Disponibilidad'
import { Ingreso } from './pages/Ingreso'
import { MisReservas } from './pages/MisReservas'
import { UsoEspacios } from './pages/UsoEspacios'

// Sin haber ingresado con el mail solo se ve la pantalla de ingreso
function Rutas() {
  const { usuario, cargando } = useSesion()

  if (cargando) return null
  if (!usuario) return <Ingreso />

  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Disponibilidad />} />
        <Route path="mis-reservas" element={<MisReservas />} />
        <Route path="actividad" element={<Actividad />} />
        <Route path="admin/bloqueos" element={<Bloqueos />} />
        <Route path="admin/uso" element={<UsoEspacios />} />
        <Route path="admin/bloqueos/nuevo" element={<BloqueoFormPage />} />
        <Route path="admin/bloqueos/:id/editar" element={<BloqueoFormPage />} />
      </Route>
    </Routes>
  )
}

export default function App() {
  return (
    <SesionProvider>
      <Rutas />
    </SesionProvider>
  )
}
