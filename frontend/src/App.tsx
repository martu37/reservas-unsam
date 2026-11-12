import { Route, Routes } from 'react-router'
import { Layout } from './components/layout/Layout/Layout'
import { SesionProvider } from './components/layout/SesionProvider/SesionProvider'
import { Actividad } from './pages/Actividad'
import { BloqueoFormPage } from './pages/BloqueoFormPage'
import { Bloqueos } from './pages/Bloqueos'
import { Disponibilidad } from './pages/Disponibilidad'
import { MisReservas } from './pages/MisReservas'
import { UsoEspacios } from './pages/UsoEspacios'

export default function App() {
  return (
    <SesionProvider>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<Disponibilidad />} />
          <Route path="mis-reservas" element={<MisReservas />} />
          <Route path="actividad" element={<Actividad />} />
          <Route path="admin/bloqueos" element={<Bloqueos />} />
          <Route path="admin/uso" element={<UsoEspacios />} />
          <Route path="admin/bloqueos/nuevo" element={<BloqueoFormPage />} />
          <Route
            path="admin/bloqueos/:id/editar"
            element={<BloqueoFormPage />}
          />
        </Route>
      </Routes>
    </SesionProvider>
  )
}
