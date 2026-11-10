import { typeLabel } from '../utils/pokemon'

/** Etiquetas coloridas dos tipos. Pokémon sem tipo aparecem como "Sem tipo". */
function TypeBadges({ types, small = false }) {
  const list = types?.length ? types : ['Typeless']
  return (
    <div className={`types ${small ? 'types-small' : ''}`}>
      {list.map((type) => (
        <span key={type} className={`type type-${type.toLowerCase()}`}>
          {typeLabel(type)}
        </span>
      ))}
    </div>
  )
}

export default TypeBadges
