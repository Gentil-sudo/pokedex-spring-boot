import { useState } from 'react'
import { STAT_KEYS, STAT_NAMES, totalStats } from '../utils/pokemon'
import PokemonCard from './PokemonCard'
import './PokemonList.css'

const SORT_OPTIONS = [
  { value: 'id', label: 'Número' },
  { value: 'name', label: 'Nome' },
  { value: 'total', label: 'Total' },
  ...STAT_KEYS.map((key) => ({ value: key, label: STAT_NAMES[key] })),
]

function sortPokemons(list, sortBy) {
  if (sortBy === 'id') return list
  if (sortBy === 'name') return list.toSorted((a, b) => a.name.localeCompare(b.name))
  const value = sortBy === 'total' ? totalStats : (p) => p[sortBy]
  return list.toSorted((a, b) => value(b) - value(a) || a.id - b.id)
}

/** Grade com os resultados da busca/filtro (vindos da API) e ordenação. */
function PokemonList({ pokemons, searching, error, currentId, onSelect, onClearFilters, hasFilters }) {
  const [sortBy, setSortBy] = useState('id')
  const sorted = sortPokemons(pokemons, sortBy)

  return (
    <div className='list-view'>
      <div className='list-toolbar'>
        <p className='list-count'>
          {searching ? 'Buscando...' : `${pokemons.length} Pokémon encontrado${pokemons.length === 1 ? '' : 's'}`}
        </p>
        <label className='list-sort'>
          Ordenar por
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
            {SORT_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>{option.label}</option>
            ))}
          </select>
        </label>
      </div>

      {error ? (
        <p className='pokedex-error'>{error.message}</p>
      ) : sorted.length === 0 && !searching ? (
        <div className='list-empty'>
          <p>Nenhum Pokémon encontrado.</p>
          {hasFilters && <button onClick={onClearFilters}>Limpar filtros</button>}
        </div>
      ) : (
        <ul className={`pokemon-grid ${searching ? 'is-searching' : ''}`}>
          {sorted.map((pokemon) => (
            <PokemonCard
              key={pokemon.id}
              pokemon={pokemon}
              selected={pokemon.id === currentId}
              onSelect={onSelect}
            />
          ))}
        </ul>
      )}
    </div>
  )
}

export default PokemonList
