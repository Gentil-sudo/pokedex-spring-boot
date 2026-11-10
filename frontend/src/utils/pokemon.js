export const SPRITE_URL = 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon'

/** Maior número da Pokédex com imagem no repositório PokeAPI/sprites. */
const LAST_SPRITE_ID = 1025

export const STAT_KEYS = ['hp', 'attack', 'defense', 'specialAttack', 'specialDefense', 'speed']

export const STAT_NAMES = {
  hp: 'HP',
  attack: 'Ataque',
  defense: 'Defesa',
  specialAttack: 'Atq. Esp.',
  specialDefense: 'Def. Esp.',
  speed: 'Velocidade',
}

export const TYPE_NAMES = {
  typeless: 'Sem tipo', normal: 'Normal', fire: 'Fogo', water: 'Água', grass: 'Planta',
  electric: 'Elétrico', ice: 'Gelo', fighting: 'Lutador', poison: 'Venenoso',
  ground: 'Terra', flying: 'Voador', psychic: 'Psíquico', bug: 'Inseto',
  rock: 'Pedra', ghost: 'Fantasma', dragon: 'Dragão', dark: 'Sombrio',
  steel: 'Aço', fairy: 'Fada',
}

/** Nome traduzido do tipo (a API usa os nomes em inglês do Script_Pokedex.sql). */
export const typeLabel = (type) => TYPE_NAMES[type.toLowerCase()] ?? type

/** Classe CSS do tipo principal. Pokémon sem tipo (ex.: Mew no script) usam "typeless". */
export const mainType = (pokemon) => (pokemon.types?.[0] ?? 'Typeless').toLowerCase()

export const totalStats = (pokemon) => STAT_KEYS.reduce((sum, key) => sum + (pokemon[key] ?? 0), 0)

export const formatNumber = (id) => `#${String(id).padStart(3, '0')}`

const hasSprite = (pokemon) => !pokemon.imageUrl && pokemon.id >= 1 && pokemon.id <= LAST_SPRITE_ID

/** Arte oficial (ou a imagem cadastrada pelo usuário). */
export function artworkUrl(pokemon, shiny = false) {
  if (pokemon.imageUrl) return pokemon.imageUrl
  if (!hasSprite(pokemon)) return null
  return `${SPRITE_URL}/other/official-artwork${shiny ? '/shiny' : ''}/${pokemon.id}.png`
}

/** Sprite em pixel art (frente/costas). Só existe para Pokémon oficiais. */
export function pixelSpriteUrl(pokemon, { shiny = false, back = false } = {}) {
  if (!hasSprite(pokemon)) return null
  return `${SPRITE_URL}${back ? '/back' : ''}${shiny ? '/shiny' : ''}/${pokemon.id}.png`
}
