import { useState } from 'react'
import { MdCatchingPokemon } from 'react-icons/md'

/**
 * Imagem de Pokémon com fallback: se não houver URL ou a imagem falhar ao carregar,
 * mostra uma Pokébola. Use `key={src}` no pai para reiniciar o estado quando a URL mudar.
 */
function PokemonImage({ src, alt, className = '', pixelated = false, lazy = false }) {
  const [failed, setFailed] = useState(false)

  if (!src || failed) {
    return (
      <span className={`image-fallback ${className}`} role='img' aria-label={alt}>
        <MdCatchingPokemon />
      </span>
    )
  }

  return (
    <img
      src={src}
      alt={alt}
      className={`${className} ${pixelated ? 'pixelated' : ''}`}
      loading={lazy ? 'lazy' : 'eager'}
      onError={() => setFailed(true)}
    />
  )
}

export default PokemonImage
