import { useEffect, useState } from 'react'
import { listPokemons } from '../services/api'

const DEBOUNCE_MS = 250

/**
 * Busca Pokémon na API conforme o usuário digita (GET /api/pokemons?search=&type=).
 * Espera 250 ms sem digitação antes de chamar a API e cancela requisições antigas.
 * Enquanto a nova resposta não chega, mantém os resultados anteriores na tela.
 */
export function usePokemonSearch(search, type, version) {
  const term = search.trim()
  const active = Boolean(term || type)
  const key = `${term}|${type}|${version}`
  const [result, setResult] = useState({ key: null, items: [], error: null })

  useEffect(() => {
    if (!active) return undefined
    const controller = new AbortController()
    const timer = setTimeout(() => {
      listPokemons({ search: term, type }, controller.signal)
        .then((items) => setResult({ key, items, error: null }))
        .catch((error) => {
          if (error.name !== 'AbortError') setResult({ key, items: [], error })
        })
    }, DEBOUNCE_MS)
    return () => {
      clearTimeout(timer)
      controller.abort()
    }
  }, [active, key, term, type])

  return {
    active,
    items: result.items,
    searching: active && result.key !== key,
    error: result.key === key ? result.error : null,
  }
}
