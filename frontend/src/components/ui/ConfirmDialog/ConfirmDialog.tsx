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
  onConfirm: () => void
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

  return (
    <Dialog open={open} onClose={onCancel}>
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        <DialogContentText>{message || lastMessage}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel}>Cancelar</Button>
        <Button color={confirmColor} variant="contained" onClick={onConfirm}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
