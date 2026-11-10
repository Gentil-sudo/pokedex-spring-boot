import { useState } from 'react'
import { FaTrash } from 'react-icons/fa'
import Modal from './Modal'

/** Confirmação de ações destrutivas (ex.: excluir um Pokémon). */
function ConfirmDialog({ title, message, confirmLabel = 'Excluir', onConfirm, onClose }) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  async function handleConfirm() {
    setBusy(true)
    setError(null)
    try {
      await onConfirm()
    } catch (err) {
      setError(err.message)
      setBusy(false)
    }
  }

  return (
    <Modal title={title} onClose={busy ? () => {} : onClose} size='small'>
      <div className='modal-body'>
        <p>{message}</p>
        {error && <p className='form-error'>{error}</p>}
      </div>
      <footer className='modal-footer'>
        <button type='button' className='button-secondary' onClick={onClose} disabled={busy}>
          Cancelar
        </button>
        <button type='button' className='button-danger' onClick={handleConfirm} disabled={busy}>
          <FaTrash /> {busy ? 'Excluindo...' : confirmLabel}
        </button>
      </footer>
    </Modal>
  )
}

export default ConfirmDialog
