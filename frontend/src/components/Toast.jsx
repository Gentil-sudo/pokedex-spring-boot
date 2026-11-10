import { useEffect } from 'react'
import { FaCheckCircle, FaExclamationCircle } from 'react-icons/fa'

const DURATION_MS = 3500

/** Aviso temporário no canto da tela (sucesso ou erro). */
function Toast({ toast, onDone }) {
  useEffect(() => {
    const timer = setTimeout(onDone, DURATION_MS)
    return () => clearTimeout(timer)
  }, [toast, onDone])

  return (
    <div className={`toast toast-${toast.kind}`} role='status' aria-live='polite'>
      {toast.kind === 'error' ? <FaExclamationCircle /> : <FaCheckCircle />}
      <span>{toast.message}</span>
    </div>
  )
}

export default Toast
