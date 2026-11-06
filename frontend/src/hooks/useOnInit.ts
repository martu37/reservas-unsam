import { useEffect } from 'react'

export const useOnInit = (initialCallback: () => void) => {
  useEffect(() => {
    initialCallback()
  }, [])
}
