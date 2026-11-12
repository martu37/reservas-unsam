import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'
import { useState } from 'react'

type ConfirmDialogProps = {
  open: boolean
  title: string
  message: string
  confirmLabel: string
  confirmColor?: 'primary' | 'error'
  onConfirm: () => void | Promise<void>
  onCancel: () => void
}

export function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel,
  confirmColor = 'error',
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  // Mientras el diálogo se cierra el mensaje ya viene vacío: se muestra el último
  const [lastMessage, setLastMessage] = useState(message)
  if (message && message !== lastMessage) setLastMessage(message)

  // Mientras se envía el pedido los botones quedan desactivados: un doble clic
  // no manda dos veces lo mismo
  const [enviando, setEnviando] = useState(false)

  const confirmar = async () => {
    setEnviando(true)
    try {
      await onConfirm()
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialog open={open} onClose={enviando ? undefined : onCancel}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message || lastMessage}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={enviando}>
          Cancelar
        </Button>
        <Button
          color={confirmColor}
          variant="contained"
          onClick={confirmar}
          disabled={enviando}
        >
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
