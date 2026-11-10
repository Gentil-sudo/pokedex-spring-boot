import { useEffect, useState } from 'react'
import { getPokemon, getPokemonTrainers } from '../services/api'

/**
 * Consulta os detalhes de um Pokémon (GET /api/pokemons/{id}) e os treinadores que o
 * possuem (GET /api/pokemons/{id}/trainers). `version` força nova consulta após edições.
 */
export function usePokemonDetails(id, version) {
  const key = `${id}|${version}`
  const [result, setResult] = useState({ key: null, pokemon: null, trainers: [], error: null })

  useEffect(() => {
    if (id == null) return undefined
    const controller = new AbortController()
    Promise.all([getPokemon(id, controller.signal), getPokemonTrainers(id, controller.signal)])
      .then(([pokemon, trainers]) => setResult({ key, pokemon, trainers, error: null }))
      .catch((error) => {
        if (error.name !== 'AbortError') setResult({ key, pokemon: null, trainers: [], error })
      })
    return () => controller.abort()
  }, [id, key])

  const ready = result.key === key
  return {
    pokemon: ready ? result.pokemon : null,
    trainers: ready ? result.trainers : null,
    error: ready ? result.error : null,
    loading: !ready,
  }
}
