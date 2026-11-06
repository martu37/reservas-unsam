import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { BloqueoForm } from '../components/bloqueos/BloqueoForm/BloqueoForm'
import { FormLayout } from '../components/form/FormLayout/FormLayout'
import { SoloAdmin } from '../components/layout/SoloAdmin/SoloAdmin'
import type { BloqueoData } from '../domain/Bloqueo'
import type { Espacio } from '../domain/Espacio'
import {
  EMPTY_BLOQUEO,
  fromBloqueo,
  type BloqueoFormValues,
} from '../forms/bloqueoForm'
import { useSesion } from '../hooks/useSesion'
import { bloqueoService } from '../services/bloqueoService'
import { espacioService } from '../services/espacioService'
import { getErrorMesagge } from '../utils/errorHandling'

const VOLVER = '/admin/bloqueos'

export function BloqueoFormPage() {
  const navigate = useNavigate()
  const { usuario } = useSesion()
  const id = useParams().id
  const isEdit = id !== undefined
  const [espacios, setEspacios] = useState<Espacio[] | null>(null)
  const [initialValues, setInitialValues] = useState<BloqueoFormValues | null>(
    isEdit ? null : EMPTY_BLOQUEO,
  )
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      espacioService.getAll(),
      isEdit ? bloqueoService.getById(Number(id)) : null,
    ])
      .then(([espacios, bloqueo]) => {
        setEspacios(espacios)
        if (bloqueo) setInitialValues(fromBloqueo(bloqueo))
      })
      .catch((e) => setError(getErrorMesagge(e)))
  }, [id, isEdit])

  const handleSubmit = async (data: BloqueoData) => {
    if (!usuario) return
    try {
      if (isEdit) {
        await bloqueoService.actualizar(Number(id), data)
      } else {
        await bloqueoService.crear(data, usuario.id)
      }
      navigate(VOLVER)
    } catch (e) {
      setError(getErrorMesagge(e))
    }
  }

  return (
    <SoloAdmin>
      <FormLayout
        title={isEdit ? 'Editar bloqueo' : 'Nuevo bloqueo'}
        backTo={VOLVER}
        backLabel="Volver a bloqueos"
        error={error}
      >
        {initialValues && espacios && (
          <BloqueoForm
            key={id ?? 'nuevo'}
            initialValues={initialValues}
            espacios={espacios}
            cancelTo={VOLVER}
            onSubmit={handleSubmit}
          />
        )}
      </FormLayout>
    </SoloAdmin>
  )
}
