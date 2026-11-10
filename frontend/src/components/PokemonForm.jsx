import { useState } from 'react'
import { FaSave } from 'react-icons/fa'
import { STAT_KEYS, STAT_NAMES, artworkUrl, mainType, typeLabel } from '../utils/pokemon'
import Modal from './Modal'
import PokemonImage from './PokemonImage'
import './PokemonForm.css'

const MIN_STAT = 1
const MAX_STAT = 255

function initialValues(pokemon) {
  if (!pokemon) {
    return {
      id: '', name: '', type1: '', type2: '', imageUrl: '',
      ...Object.fromEntries(STAT_KEYS.map((key) => [key, 50])),
    }
  }
  return {
    id: pokemon.id,
    name: pokemon.name,
    type1: pokemon.types[0] ?? '',
    type2: pokemon.types[1] ?? '',
    imageUrl: pokemon.imageUrl ?? '',
    ...Object.fromEntries(STAT_KEYS.map((key) => [key, pokemon[key]])),
  }
}

/** Converte o estado do formulário no corpo esperado pela API (PokemonRequest). */
function toPayload(values, isEdit) {
  return {
    id: !isEdit && values.id !== '' ? Number(values.id) : undefined,
    name: values.name.trim(),
    ...Object.fromEntries(STAT_KEYS.map((key) => [key, Number(values[key])])),
    types: [values.type1, values.type2].filter(Boolean),
    imageUrl: values.imageUrl.trim() || null,
  }
}

/**
 * Modal de cadastro (POST) e edição (PUT). Os erros de validação devolvidos pela API
 * (campo "errors" do Problem Details) aparecem embaixo de cada campo.
 */
function PokemonForm({ pokemon, types, onSubmit, onClose }) {
  const isEdit = Boolean(pokemon)
  const [values, setValues] = useState(() => initialValues(pokemon))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const [fieldErrors, setFieldErrors] = useState({})

  const typeOptions = types.filter((t) => t.name !== 'Typeless')
  const total = STAT_KEYS.reduce((sum, key) => sum + (Number(values[key]) || 0), 0)
  const preview = {
    id: isEdit ? pokemon.id : Number(values.id) || 0,
    imageUrl: values.imageUrl.trim() || null,
    types: [values.type1].filter(Boolean),
  }
  const previewSrc = artworkUrl(preview)

  function update(field, value) {
    setValues((v) => ({ ...v, [field]: value }))
    setFieldErrors((errors) => ({ ...errors, [field]: undefined }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (values.type2 && values.type2 === values.type1) {
      setFieldErrors({ type2: 'O segundo tipo deve ser diferente do primeiro' })
      return
    }
    setSaving(true)
    setError(null)
    try {
      await onSubmit(toPayload(values, isEdit), pokemon)
    } catch (err) {
      setError(err.message)
      setFieldErrors(err.fieldErrors ?? {})
      setSaving(false)
    }
  }

  const fieldError = (name) => fieldErrors[name] && <span className='field-error'>{fieldErrors[name]}</span>

  return (
    <Modal title={isEdit ? `Editar ${pokemon.name}` : 'Cadastrar Pokémon'} onClose={onClose} size='large'>
      <form className='pokemon-form' onSubmit={handleSubmit}>
        <div className='modal-body form-grid'>
          <div className={`form-preview type-${mainType(preview)}`}>
            <PokemonImage key={previewSrc} src={previewSrc} alt='Pré-visualização' className='form-preview-image' />
            <span className='form-preview-total'>Total {total}</span>
          </div>

          <div className='form-fields'>
            <div className='form-row'>
              <label className='field field-id'>
                <span>Número</span>
                <input
                  type='number'
                  min='1'
                  value={values.id}
                  onChange={(e) => update('id', e.target.value)}
                  placeholder='Automático'
                  disabled={isEdit}
                  title={isEdit ? 'O número não pode ser alterado' : 'Deixe vazio para usar o próximo número livre'}
                />
                {fieldError('id')}
              </label>
              <label className='field field-grow'>
                <span>Nome *</span>
                <input
                  type='text'
                  value={values.name}
                  onChange={(e) => update('name', e.target.value)}
                  maxLength={30}
                  required
                  autoFocus
                />
                {fieldError('name')}
              </label>
            </div>

            <div className='form-row'>
              <label className='field field-grow'>
                <span>Tipo principal *</span>
                <select value={values.type1} onChange={(e) => update('type1', e.target.value)} required>
                  <option value=''>Selecione...</option>
                  {typeOptions.map((t) => (
                    <option key={t.id} value={t.name}>{typeLabel(t.name)}</option>
                  ))}
                </select>
                {fieldError('types')}
              </label>
              <label className='field field-grow'>
                <span>Tipo secundário</span>
                <select value={values.type2} onChange={(e) => update('type2', e.target.value)}>
                  <option value=''>Nenhum</option>
                  {typeOptions
                    .filter((t) => t.name !== values.type1)
                    .map((t) => (
                      <option key={t.id} value={t.name}>{typeLabel(t.name)}</option>
                    ))}
                </select>
                {fieldError('type2')}
              </label>
            </div>

            <fieldset className='stats-fieldset'>
              <legend>Atributos base ({MIN_STAT} a {MAX_STAT})</legend>
              {STAT_KEYS.map((key) => (
                <label key={key} className='stat-input'>
                  <span>{STAT_NAMES[key]}</span>
                  <input
                    type='range'
                    min={MIN_STAT}
                    max={MAX_STAT}
                    value={Number(values[key]) || MIN_STAT}
                    onChange={(e) => update(key, e.target.value)}
                    aria-label={`${STAT_NAMES[key]} (controle deslizante)`}
                  />
                  <input
                    type='number'
                    min={MIN_STAT}
                    max={MAX_STAT}
                    value={values[key]}
                    onChange={(e) => update(key, e.target.value)}
                    required
                  />
                  {fieldError(key)}
                </label>
              ))}
            </fieldset>

            <label className='field'>
              <span>URL da imagem (opcional)</span>
              <input
                type='url'
                value={values.imageUrl}
                onChange={(e) => update('imageUrl', e.target.value)}
                placeholder='https://... (vazio = arte oficial pelo número)'
              />
              {fieldError('imageUrl')}
            </label>

            {error && <p className='form-error'>{error}</p>}
          </div>
        </div>

        <footer className='modal-footer'>
          <button type='button' className='button-secondary' onClick={onClose} disabled={saving}>
            Cancelar
          </button>
          <button type='submit' disabled={saving}>
            <FaSave /> {saving ? 'Salvando...' : isEdit ? 'Salvar alterações' : 'Cadastrar'}
          </button>
        </footer>
      </form>
    </Modal>
  )
}

export default PokemonForm
