import { useState } from 'react'
import { FaSearch } from 'react-icons/fa'
import TrainerCard from './TrainerCard'
import './Trainers.css'

function TrainerList({ trainers, selectedTrainerId, currentPokemonId, onSelectPokemon }) {
  const [search, setSearch] = useState('')
  const [sortBy, setSortBy] = useState('name')

  const query = search.trim().toLowerCase()
  const visibleTrainers = trainers
    .filter((trainer) =>
      trainer.name.toLowerCase().includes(query) ||
      trainer.pokemon.some((p) => p.name.toLowerCase().includes(query)),
    )
    .toSorted((a, b) =>
      sortBy === 'badges'
        ? b.badges - a.badges || a.name.localeCompare(b.name)
        : a.name.localeCompare(b.name),
    )

  return (
    <div className='trainer-view'>
      <div className='trainer-toolbar'>
        <label className='trainer-search'>
          <FaSearch />
          <input
            type='text'
            placeholder='Buscar treinador ou Pokémon'
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </label>
        <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} aria-label='Ordenação'>
          <option value='name'>Ordenar por nome</option>
          <option value='badges'>Ordenar por insígnias</option>
        </select>
      </div>

      {visibleTrainers.length === 0 ? (
        <p className='empty trainer-empty'>Nenhum treinador encontrado.</p>
      ) : (
        <div className='trainer-grid'>
          {visibleTrainers.map((trainer) => (
            <TrainerCard
              key={trainer.id}
              trainer={trainer}
              highlighted={trainer.id === selectedTrainerId}
              currentPokemonId={currentPokemonId}
              onSelectPokemon={onSelectPokemon}
            />
          ))}
        </div>
      )}
    </div>
  )
}

export default TrainerList
