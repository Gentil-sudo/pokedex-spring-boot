import { useCallback, useEffect, useState } from 'react'
import { listPokemons, listTrainers, listTypes } from '../services/api'

const byId = (a, b) => a.id - b.id

/**
 * Carrega da API os Pokémon, os tipos e os treinadores.
 * `reload()` busca tudo de novo; `applySaved`/`applyRemoved` atualizam a lista local
 * imediatamente após um cadastro, edição ou exclusão (antes do reload terminar).
 */
export function usePokedexData() {
  const [version, setVersion] = useState(0)
  const [state, setState] = useState({
    loadedVersion: -1,
    pokemons: [],
    types: [],
    trainers: [],
    error: null,
  })

  useEffect(() => {
    const controller = new AbortController()
    Promise.all([
      listPokemons({}, controller.signal),
      listTypes(controller.signal),
      listTrainers(controller.signal),
    ])
      .then(([pokemons, types, trainers]) =>
        setState({ loadedVersion: version, pokemons, types, trainers, error: null }))
      .catch((error) => {
        if (error.name !== 'AbortError') {
          setState((s) => ({ ...s, loadedVersion: version, error }))
        }
      })
    return () => controller.abort()
  }, [version])

  const reload = useCallback(() => setVersion((v) => v + 1), [])

  const applySaved = useCallback((saved) => {
    setState((s) => ({
      ...s,
      pokemons: [...s.pokemons.filter((p) => p.id !== saved.id), saved].sort(byId),
    }))
  }, [])

  const applyRemoved = useCallback((id) => {
    setState((s) => ({ ...s, pokemons: s.pokemons.filter((p) => p.id !== id) }))
  }, [])

  return {
    pokemons: state.pokemons,
    types: state.types,
    trainers: state.trainers,
    error: state.error,
    loading: state.loadedVersion !== version,
    loaded: state.loadedVersion >= 0 && !(state.error && state.pokemons.length === 0),
    version,
    reload,
    applySaved,
    applyRemoved,
  }
}
