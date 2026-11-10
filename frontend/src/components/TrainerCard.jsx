import { useEffect, useRef } from 'react'
import { FaMedal } from 'react-icons/fa'
import { artworkUrl, pixelSpriteUrl } from '../utils/pokemon'
import PokemonImage from './PokemonImage'

const MAX_BADGES = 8

function TrainerCard({ trainer, highlighted, currentPokemonId, onSelectPokemon }) {
  const cardRef = useRef(null)

  useEffect(() => {
    if (highlighted) {
      cardRef.current.scrollIntoView({ behavior: 'smooth', block: 'center' })
    }
  }, [highlighted])

  return (
    <article ref={cardRef} className={`trainer-card ${highlighted ? 'highlighted' : ''}`}>
      <header className='trainer-card-header'>
        <div className='trainer-avatar'>{trainer.name[0]}</div>
        <div>
          <h3>{trainer.name}</h3>
          <span className='trainer-role'>{trainer.role}</span>
        </div>
      </header>

      <div className='trainer-badges' title={`${trainer.badges} insígnias`}>
        {Array.from({ length: MAX_BADGES }, (_, i) => (
          <FaMedal key={i} className={i < trainer.badges ? 'earned' : ''} />
        ))}
        <span>{trainer.badges}</span>
      </div>

      {trainer.pokemon.length === 0 ? (
        <p className='empty'>Nenhum Pokémon no time.</p>
      ) : (
        <ul className='trainer-pokemon'>
          {/* O índice entra na chave porque um time pode repetir o mesmo Pokémon (ex.: Koga) */}
          {trainer.pokemon.map((p, index) => {
            const sprite = pixelSpriteUrl(p) ?? artworkUrl(p)
            return (
              <li key={`${p.id}-${index}`}>
                <button
                  className={p.id === currentPokemonId ? 'current' : ''}
                  onClick={() => onSelectPokemon(p.id)}
                  title={`Ver ${p.name}`}
                >
                  <PokemonImage key={sprite} src={sprite} alt={p.name} pixelated lazy />
                  <span>{p.name}</span>
                </button>
              </li>
            )
          })}
        </ul>
      )}
    </article>
  )
}

export default TrainerCard
