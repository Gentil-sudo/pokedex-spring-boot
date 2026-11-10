import { useEffect, useRef } from 'react'
import { IoClose } from 'react-icons/io5'
import './Modal.css'

/**
 * Janela modal baseada no elemento nativo <dialog>, que já cuida do foco,
 * da tecla Esc e do bloqueio do conteúdo ao fundo.
 */
function Modal({ title, onClose, children, size = 'medium', className = '' }) {
  const dialogRef = useRef(null)

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog.open) dialog.showModal()
    return () => dialog.close()
  }, [])

  function handleCancel(e) {
    e.preventDefault() // Esc: deixa o React decidir quando fechar
    onClose()
  }

  function handleClick(e) {
    if (e.target === dialogRef.current) onClose() // clique fora do conteúdo
  }

  return (
    <dialog
      ref={dialogRef}
      className={`modal modal-${size} ${className}`}
      onCancel={handleCancel}
      onClick={handleClick}
      aria-labelledby='modal-title'
    >
      <div className='modal-content'>
        <header className='modal-header'>
          <h3 id='modal-title'>{title}</h3>
          <button type='button' className='modal-close' onClick={onClose} aria-label='Fechar'>
            <IoClose />
          </button>
        </header>
        {children}
      </div>
    </dialog>
  )
}

export default Modal
