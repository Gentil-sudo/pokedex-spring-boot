// Camada de acesso à API REST (Spring Boot).
// A URL base pode ser trocada pela variável VITE_API_URL (arquivo frontend/.env).
export const API_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api').replace(/\/$/, '')

/** Erro lançado quando a API responde com status de erro (ou está fora do ar). */
export class ApiError extends Error {
  constructor(message, { status = 0, fieldErrors = {} } = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function request(path, { method = 'GET', body, signal } = {}) {
  let response
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      signal,
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    })
  } catch (error) {
    if (error.name === 'AbortError') throw error
    throw new ApiError(`Não foi possível conectar à API em ${API_URL}. O backend está rodando?`)
  }

  if (response.status === 204) return null

  const data = await response.json().catch(() => null)
  if (!response.ok) {
    // O backend responde no formato Problem Details: { title, detail, errors }
    throw new ApiError(data?.detail ?? `Erro ${response.status} ao acessar a API`, {
      status: response.status,
      fieldErrors: data?.errors ?? {},
    })
  }
  return data
}

function query(params) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      search.set(key, String(value).trim())
    }
  })
  const text = search.toString()
  return text ? `?${text}` : ''
}

// ===== Pokémon =====

/** GET /api/pokemons?search=&type= */
export const listPokemons = ({ search, type } = {}, signal) =>
  request(`/pokemons${query({ search, type })}`, { signal })

/** GET /api/pokemons/{id} */
export const getPokemon = (id, signal) => request(`/pokemons/${id}`, { signal })

/** GET /api/pokemons/{id}/trainers */
export const getPokemonTrainers = (id, signal) => request(`/pokemons/${id}/trainers`, { signal })

/** POST /api/pokemons */
export const createPokemon = (pokemon) => request('/pokemons', { method: 'POST', body: pokemon })

/** PUT /api/pokemons/{id} */
export const updatePokemon = (id, pokemon) => request(`/pokemons/${id}`, { method: 'PUT', body: pokemon })

/** DELETE /api/pokemons/{id} */
export const deletePokemon = (id) => request(`/pokemons/${id}`, { method: 'DELETE' })

// ===== Tipos e treinadores =====

/** GET /api/types */
export const listTypes = (signal) => request('/types', { signal })

/** GET /api/trainers */
export const listTrainers = (signal) => request('/trainers', { signal })
