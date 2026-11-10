import { FaRedo } from 'react-icons/fa'
import { MdCatchingPokemon } from 'react-icons/md'
import { API_URL } from '../services/api'

/** Tela de carregamento ou de erro (API fora do ar) exibida dentro da Pokédex. */
function StatusScreen({ error, onRetry }) {
  if (!error) {
    return (
      <div className='status-screen'>
        <MdCatchingPokemon className='status-pokeball spinning' />
        <p>Carregando a Pokédex...</p>
      </div>
    )
  }

  return (
    <div className='status-screen'>
      <MdCatchingPokemon className='status-pokeball offline' />
      <h3>Não foi possível carregar os dados</h3>
      <p>{error.message}</p>
      <p className='status-hint'>
        Inicie o backend Spring Boot (<code>backend</code>) e confira se ele responde em{' '}
        <code>{API_URL}/pokemons</code>.
      </p>
      <button onClick={onRetry}>
        <FaRedo /> Tentar novamente
      </button>
    </div>
  )
}

export default StatusScreen
