import { useCallback, useEffect, useState } from 'react'
import {
  FaChevronLeft, FaChevronRight, FaDice, FaList, FaMoon, FaPlus, FaSearch, FaSun, FaTimes, FaUsers,
} from 'react-icons/fa'
import { IoSettingsSharp } from 'react-icons/io5'
import { MdCatchingPokemon } from 'react-icons/md'
import ConfirmDialog from './components/ConfirmDialog'
import PokemonDetails from './components/PokemonDetails'
import PokemonForm from './components/PokemonForm'
import PokemonList from './components/PokemonList'
import StatusScreen from './components/StatusScreen'
import Toast from './components/Toast'
import TrainerList from './components/TrainerList'
import { usePokedexData } from './hooks/usePokedexData'
import { usePokemonSearch } from './hooks/usePokemonSearch'
import { createPokemon, deletePokemon, updatePokemon } from './services/api'
import { typeLabel } from './utils/pokemon'
import './App.css'

const VIEWS = {
  pokedex: { label: 'Pokédex', icon: <MdCatchingPokemon /> },
  list: { label: 'Lista', icon: <FaList /> },
  trainers: { label: 'Treinadores', icon: <FaUsers /> },
}

function readDarkMode() {
  try {
    return localStorage.getItem('pokedex-dark') === 'true'
  } catch {
    return false
  }
}

function App() {
  const data = usePokedexData()
  const { pokemons, types, trainers, version } = data

  const [selectedId, setSelectedId] = useState(1)
  const [darkMode, setDarkMode] = useState(readDarkMode)
  const [shiny, setShiny] = useState(false)
  const [showConfig, setShowConfig] = useState(false)
  const [search, setSearch] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [view, setView] = useState('pokedex')
  const [selectedTrainerId, setSelectedTrainerId] = useState(null)
  const [form, setForm] = useState(null) // null | { pokemon?: Pokemon }
  const [pendingDelete, setPendingDelete] = useState(null)
  const [toast, setToast] = useState(null)

  // Busca e filtro são feitos pela API; sem filtros, usamos a lista completa.
  const searchResult = usePokemonSearch(search, typeFilter, version)
  const visible = searchResult.active ? searchResult.items : pokemons
  const navigation = visible.length > 0 ? visible : pokemons
  const current = pokemons.find((p) => p.id === selectedId) ?? navigation[0] ?? null
  const position = navigation.findIndex((p) => p.id === current?.id)
  const modalOpen = Boolean(form || pendingDelete)

  useEffect(() => {
    try {
      localStorage.setItem('pokedex-dark', String(darkMode))
    } catch {
      // armazenamento indisponível (ex.: navegação privada) — apenas ignora
    }
  }, [darkMode])

  const showToast = useCallback((message, kind = 'success') => setToast({ message, kind }), [])
  const hideToast = useCallback(() => setToast(null), [])

  function goTo(id) {
    setSelectedId(id)
    setView('pokedex')
  }

  function step(delta) {
    if (navigation.length === 0) return
    const index = position === -1 ? 0 : (position + delta + navigation.length) % navigation.length
    goTo(navigation[index].id)
  }

  function drawPokemon() {
    const options = navigation.filter((p) => p.id !== current?.id)
    if (options.length === 0) return
    goTo(options[Math.floor(Math.random() * options.length)].id)
  }

  useEffect(() => {
    function handleKey(e) {
      if (['INPUT', 'SELECT', 'TEXTAREA'].includes(e.target.tagName)) return
      if (view !== 'pokedex' || modalOpen) return
      if (e.key === 'ArrowLeft') step(-1)
      if (e.key === 'ArrowRight') step(1)
    }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  })

  function changeView(next) {
    setSelectedTrainerId(null)
    setView(next)
  }

  function openTrainer(trainerId) {
    setSelectedTrainerId(trainerId)
    setView('trainers')
  }

  function handleSearchChange(value) {
    setSearch(value)
    if (value.trim() && view !== 'list') setView('list')
  }

  function handleTypeChange(value) {
    setTypeFilter(value)
    if (value) setView('list')
  }

  function handleSearchSubmit(e) {
    e.preventDefault()
    // Enter com um único resultado abre direto os detalhes
    if (!searchResult.searching && visible.length === 1) goTo(visible[0].id)
  }

  function clearFilters() {
    setSearch('')
    setTypeFilter('')
  }

  async function handleSave(payload, editing) {
    const saved = editing ? await updatePokemon(editing.id, payload) : await createPokemon(payload)
    data.applySaved(saved)
    data.reload()
    setForm(null)
    goTo(saved.id)
    showToast(`${saved.name} ${editing ? 'atualizado' : 'cadastrado'} com sucesso!`)
  }

  async function handleDelete(target) {
    const index = navigation.findIndex((p) => p.id === target.id)
    const next = navigation[index + 1] ?? navigation[index - 1]
    await deletePokemon(target.id)
    data.applyRemoved(target.id)
    data.reload()
    setPendingDelete(null)
    if (next) setSelectedId(next.id)
    showToast(`${target.name} foi excluído da Pokédex.`)
  }

  function renderScreen() {
    if (!data.loaded) {
      return <StatusScreen error={data.loading ? null : data.error} onRetry={data.reload} />
    }
    if (view === 'trainers') {
      return (
        <TrainerList
          trainers={trainers}
          selectedTrainerId={selectedTrainerId}
          currentPokemonId={current?.id}
          onSelectPokemon={goTo}
        />
      )
    }
    if (view === 'list' || !current) {
      return (
        <PokemonList
          pokemons={visible}
          searching={searchResult.searching}
          error={searchResult.error}
          currentId={current?.id}
          onSelect={goTo}
          hasFilters={searchResult.active}
          onClearFilters={clearFilters}
        />
      )
    }
    return (
      <PokemonDetails
        key={current.id}
        pokemon={current}
        version={version}
        shiny={shiny}
        onEdit={(pokemon) => setForm({ pokemon })}
        onDelete={setPendingDelete}
        onOpenTrainer={openTrainer}
      />
    )
  }

  const counter = searchResult.active
    ? `${position + 1 > 0 ? position + 1 : '–'} de ${visible.length}${typeFilter ? ` · ${typeLabel(typeFilter)}` : ''}`
    : `${position + 1} de ${pokemons.length}`

  return (
    <main className={`main ${darkMode ? 'dark' : ''}`}>
      <div className='pokedex-box'>
        <header className='pokedex-box-header'>
          <div className='pokedex-title'>
            <span className='pokedex-light' />
            <div className='pokedex-leds'>
              <span className='led led-red' />
              <span className='led led-yellow' />
              <span className='led led-green' />
            </div>
            <h2 className='poke-title'>Pokédex</h2>
          </div>
          <div className='pokedex-box-header-elements'>
            <label className='dark-toggle' htmlFor='dark-mode' title='Alternar tema'>
              <input
                id='dark-mode'
                type='checkbox'
                checked={darkMode}
                onChange={(e) => setDarkMode(e.target.checked)}
              />
              <span className='toggle-track'>
                <span className='toggle-thumb'>{darkMode ? <FaMoon /> : <FaSun />}</span>
              </span>
            </label>
            <nav className='view-tabs' aria-label='Telas'>
              {Object.entries(VIEWS).map(([key, { label, icon }]) => (
                <button
                  key={key}
                  className={`draw-button ${view === key ? 'active' : ''}`}
                  onClick={() => changeView(key)}
                  aria-pressed={view === key}
                  title={label}
                >
                  {icon} <span className='button-label'>{label}</span>
                </button>
              ))}
            </nav>
            <button className='draw-button' onClick={drawPokemon} title='Sortear' disabled={!data.loaded}>
              <FaDice /> <span className='button-label'>Sortear</span>
            </button>
            <button
              className='draw-button new-button'
              onClick={() => setForm({})}
              title='Cadastrar Pokémon'
              disabled={!data.loaded}
            >
              <FaPlus /> <span className='button-label'>Novo</span>
            </button>
          </div>
        </header>

        <div className='pokedex-elements'>
          <header className='pokedex-elements-header'>
            <form className='search-form' onSubmit={handleSearchSubmit} role='search'>
              <label className='search-field'>
                <FaSearch />
                <input
                  type='text'
                  placeholder='Nome ou número'
                  value={search}
                  onChange={(e) => handleSearchChange(e.target.value)}
                  aria-label='Pesquisar Pokémon por nome ou número'
                />
                {search && (
                  <button type='button' className='clear-search' onClick={() => setSearch('')} aria-label='Limpar busca'>
                    <FaTimes />
                  </button>
                )}
              </label>
              <select
                className='type-filter'
                value={typeFilter}
                onChange={(e) => handleTypeChange(e.target.value)}
                aria-label='Filtrar por tipo'
              >
                <option value=''>Todos os tipos</option>
                {types.map((t) => (
                  <option key={t.id} value={t.name}>{typeLabel(t.name)}</option>
                ))}
              </select>
            </form>
            {data.loaded && <p>{counter}</p>}
            <div className='config'>
              <button
                className={`icon-button config-button ${showConfig ? 'open' : ''}`}
                onClick={() => setShowConfig((v) => !v)}
                aria-label='Configurações'
                title='Configurações'
              >
                <IoSettingsSharp />
              </button>
              {showConfig && (
                <div className='config-panel'>
                  <label htmlFor='shiny'>
                    <input
                      id='shiny'
                      type='checkbox'
                      checked={shiny}
                      onChange={(e) => setShiny(e.target.checked)}
                    />
                    Versão shiny
                  </label>
                </div>
              )}
            </div>
          </header>

          {data.loaded && data.error && (
            <div className='pokedex-error'>
              <span>Falha ao atualizar os dados: {data.error.message}</span>
              <button onClick={data.reload}>Tentar de novo</button>
            </div>
          )}

          {renderScreen()}
        </div>

        <div className='pokedex-footer'>
          <button onClick={() => step(-1)} disabled={!data.loaded}><FaChevronLeft /> Anterior</button>
          <button onClick={() => step(1)} disabled={!data.loaded}>Próximo <FaChevronRight /></button>
        </div>
      </div>

      {form && (
        <PokemonForm
          pokemon={form.pokemon}
          types={types}
          onSubmit={handleSave}
          onClose={() => setForm(null)}
        />
      )}

      {pendingDelete && (
        <ConfirmDialog
          title='Excluir Pokémon'
          message={`Tem certeza que deseja excluir ${pendingDelete.name} (#${pendingDelete.id})? Ele também será removido dos times dos treinadores.`}
          onConfirm={() => handleDelete(pendingDelete)}
          onClose={() => setPendingDelete(null)}
        />
      )}

      {toast && <Toast toast={toast} onDone={hideToast} />}
    </main>
  )
}

export default App
