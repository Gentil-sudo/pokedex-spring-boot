import { artworkUrl, formatNumber, mainType, pixelSpriteUrl, totalStats } from '../utils/pokemon'
import PokemonImage from './PokemonImage'
import TypeBadges from './TypeBadges'

/** Card de um Pokémon na lista. Clicar abre os detalhes. */
function PokemonCard({ pokemon, selected, onSelect }) {
  const sprite = pixelSpriteUrl(pokemon)
  const image = sprite ?? artworkUrl(pokemon)

  return (
    <li>
      <button
        className={`pokemon-card card-${mainType(pokemon)} ${selected ? 'selected' : ''}`}
        onClick={() => onSelect(pokemon.id)}
        title={`Ver ${pokemon.name}`}
      >
        <span className='card-number'>{formatNumber(pokemon.id)}</span>
        <PokemonImage key={image} src={image} alt={pokemon.name} className='card-image' pixelated={Boolean(sprite)} lazy />
        <span className='card-name'>{pokemon.name}</span>
        <TypeBadges types={pokemon.types} small />
        <span className='card-total'>Total {totalStats(pokemon)}</span>
      </button>
    </li>
  )
}

export default PokemonCard
