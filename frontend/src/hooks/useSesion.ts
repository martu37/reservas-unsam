import { useContext } from 'react'
import { SesionContext } from '../context/SesionContext'

export const useSesion = () => useContext(SesionContext)
