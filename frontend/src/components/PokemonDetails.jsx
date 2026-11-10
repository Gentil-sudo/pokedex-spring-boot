import { FaEdit, FaMedal, FaTrash } from 'react-icons/fa'
import { usePokemonDetails } from '../hooks/usePokemonDetails'
import {
  STAT_KEYS, STAT_NAMES, artworkUrl, formatNumber, mainType, pixelSpriteUrl, totalStats,
} from '../utils/pokemon'
import PokemonImage from './PokemonImage'
import TypeBadges from './TypeBadges'

const MAX_STAT = 180

/**
 * Tela de detalhes. Mostra na hora os dados que já vieram na lista e, em seguida,
 * consulta a API (GET /api/pokemons/{id} e /trainers) para exibir a versão atualizada.
 */
function PokemonDetails({ pokemon: listed, version, shiny, onEdit, onDelete, onOpenTrainer }) {
  const details = usePokemonDetails(listed.id, version)
  const pokemon = details.pokemon ?? listed
  const type = mainType(pokemon)
  const artwork = artworkUrl(pokemon, shiny)
  const front = pixelSpriteUrl(pokemon, { shiny })
  const back = pixelSpriteUrl(pokemon, { shiny, back: true })

  return (
    <div className='pokedex-content'>
      <aside className='pokedex-elements-images'>
        <div className={`artwork type-${type}`}>
          <span className='poke-number'>{formatNumber(pokemon.id)}</span>
          <PokemonImage key={artwork} src={artwork} alt={pokemon.name} className='artwork-image' />
        </div>
        {front && (
          <div className='sprites'>
            <PokemonImage key={front} src={front} alt={`${pokemon.name} de frente`} pixelated />
            <PokemonImage key={back} src={back} alt={`${pokemon.name} de costas`} pixelated />
          </div>
        )}
        <h1 className='poke-name'>{pokemon.name}</h1>
        <TypeBadges types={pokemon.types} />
        <div className='detail-actions'>
          <button className='button-secondary' onClick={() => onEdit(pokemon)}>
            <FaEdit /> Editar
          </button>
          <button className='button-danger-outline' onClick={() => onDelete(pokemon)}>
            <FaTrash /> Excluir
          </button>
        </div>
      </aside>

      <section className='pokedex-elements-info'>
        <h4>Atributos base</h4>
        <ul className='stats'>
          {STAT_KEYS.map((key) => (
            <li key={key}>
              <span className='stat-name'>{STAT_NAMES[key]}</span>
              <span className='stat-value'>{pokemon[key]}</span>
              <div className='stat-bar'>
                <div
                  className={`stat-fill type-${type}`}
                  style={{ width: `${Math.min(pokemon[key] / MAX_STAT, 1) * 100}%` }}
                />
              </div>
            </li>
          ))}
        </ul>
        <section className='total'>
          <h3>Total</h3>
          <h2>{totalStats(pokemon)}</h2>
        </section>

        <section className='trainers'>
          <h4>Treinadores</h4>
          {details.trainers === null ? (
            <p className='empty'>Consultando treinadores...</p>
          ) : details.trainers.length === 0 ? (
            <p className='empty'>Nenhum treinador possui este Pokémon.</p>
          ) : (
            <ul>
              {details.trainers.map((trainer) => (
                <li key={trainer.id}>
                  <button onClick={() => onOpenTrainer(trainer.id)} title={`Ver ${trainer.name}`}>
                    {trainer.name}
                    <span className='trainer-chip-badges'>
                      <FaMedal /> {trainer.badges}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>
      </section>
    </div>
  )
}

export default PokemonDetails
