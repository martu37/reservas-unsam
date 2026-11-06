import { useState, type ChangeEvent, type SubmitEvent } from 'react'

export type FormErrors<T> = Partial<Record<keyof T, string>>

export function useForm<T extends Record<string, string>>(
  initialValues: T,
  validate: (values: T) => FormErrors<T>,
) {
  const [values, setValues] = useState(initialValues)
  const [errors, setErrors] = useState<FormErrors<T>>({})

  const field = (name: keyof T) => ({
    name: String(name),
    value: values[name],
    onChange: (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
      setValues((current) => ({ ...current, [name]: e.target.value })),
    error: Boolean(errors[name]),
    helperText: errors[name],
  })

  const handleSubmit =
    (onValid: (values: T) => void) => (e: SubmitEvent<HTMLFormElement>) => {
      e.preventDefault()
      const newErrors = validate(values)
      setErrors(newErrors)
      if (Object.keys(newErrors).length === 0) onValid(values)
    }

  return { values, field, handleSubmit }
}
