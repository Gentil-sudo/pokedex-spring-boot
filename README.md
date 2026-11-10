# 🐾 Pokédex Full Stack — React + Spring Boot + MongoDB

Atividade 04 de **Desenvolvimento e Integração de Aplicações Web**: a Pokédex da
[Atividade 03](https://github.com/rafaelgantunes/PokedexEmReact) deixou de ler o
`pokedex-data.json` e passou a consumir uma **API REST em Spring Boot**, que guarda os dados no
**MongoDB**. Os dados vêm do `Script_Pokedex.sql`, convertido para o modelo de documentos.

```text
Script_Pokedex.sql ──► MongoDB (Atlas) ──► Spring Boot REST API ──► React + Vite
```

**Dupla (pair programming):** Enzo Gentil Xavier Barroso e Rafael Gomes Antunes de Oliveira

![Pokédex](docs/pokedex.png)

---

## ✨ Funcionalidades

| Requisito | Como foi atendido |
| --- | --- |
| 🔎 Pesquisa por nome ou ID | Busca **na API** (`?search=`) enquanto o usuário digita (com *debounce*). Enter abre o resultado único |
| 🧬 Filtro por tipo | Select com os tipos de `GET /api/types`; filtragem **na API** (`?type=`). Pode ser combinado com a busca |
| 📄 Detalhes | Tela da Pokédex consulta `GET /api/pokemons/{id}` e `GET /api/pokemons/{id}/trainers` |
| 📊 Atributos | Barras coloridas pelo tipo + total |
| ➕ Cadastrar | Botão **Novo** → formulário em modal → `POST /api/pokemons` |
| ✏️ Editar | Botão **Editar** nos detalhes → `PUT /api/pokemons/{id}` |
| 🗑️ Excluir | Botão **Excluir** com confirmação → `DELETE /api/pokemons/{id}` |
| 🧑‍🚀 Treinadores *(desafio)* | Coleção `trainers` no MongoDB, CRUD na API e tela com o time de cada um |
| 🔗 Relação treinador ↔ Pokémon *(desafio)* | `pokemonIds` no treinador; a API devolve o time "resolvido" (nome, tipos) |
| 🧬 Consulta por tipo / 🔎 busca por nome na API *(desafios)* | `GET /api/pokemons?type=Fire` e `GET /api/pokemons?search=char` |

Também mantidos da Atividade 03: navegação anterior/próximo (e setas ← → do teclado), sortear,
versão shiny, tema claro/escuro e layout responsivo. Novidades: lista em grade com ordenação
por qualquer atributo, mensagens de erro da API exibidas no formulário, avisos de sucesso
e tela amigável quando o backend está desligado.

<table>
  <tr>
    <td><img src="docs/lista-filtro.png" alt="Lista filtrada por tipo"></td>
    <td><img src="docs/formulario.png" alt="Formulário de edição"></td>
  </tr>
  <tr>
    <td><img src="docs/treinadores.png" alt="Treinadores"></td>
    <td align="center"><img src="docs/mobile-escuro.png" alt="Celular com tema escuro" width="260"></td>
  </tr>
</table>

---

## 🧰 Tecnologias

| Camada | Tecnologias |
| --- | --- |
| Frontend | React 19, Vite 8, React Icons, React Compiler, ESLint |
| Backend | Java 25, Spring Boot 4.1 (Spring Web MVC, Spring Data MongoDB, Bean Validation), JUnit 5, Mockito |
| Banco | MongoDB (Atlas ou local) |
| Imagens | Sprites e artes oficiais do repositório [PokeAPI/sprites](https://github.com/PokeAPI/sprites) (carregadas pela URL, a partir do número do Pokémon) |

---

## 📋 Pré-requisitos

- **JDK 25** (`java -version` deve mostrar 25). O Maven não precisa ser instalado: o projeto usa o *Maven Wrapper* (`mvnw`).
- **Node.js 20.19+** (`node -v`).
- Um banco **MongoDB**: uma conta gratuita no [MongoDB Atlas](https://www.mongodb.com/cloud/atlas/register) (recomendado) ou o MongoDB instalado localmente.

---

## 🔐 1. Configurar o MongoDB Atlas

1. Crie uma conta e um cluster gratuito (**M0**) em <https://cloud.mongodb.com>.
2. Em **Database Access**, crie um usuário com senha (permissão *Read and write to any database*).
3. Em **Network Access**, clique em **Add IP Address** e adicione o seu IP (ou `0.0.0.0/0` para liberar qualquer IP, apenas para testes).
4. Em **Database → Connect → Drivers**, copie a *connection string*. Ela tem este formato:
   `mongodb+srv://USUARIO:SENHA@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority`
   > Se a senha tiver caracteres especiais (`@`, `:`, `/`, `#`...), use a forma codificada (ex.: `@` → `%40`).
5. Na pasta `backend`, copie o arquivo de exemplo e preencha com a sua string:

   ```bash
   cd backend
   cp .env.example .env        # no Windows (PowerShell): Copy-Item .env.example .env
   ```

   ```properties
   # backend/.env
   MONGODB_URI=mongodb+srv://USUARIO:SENHA@cluster0.xxxxx.mongodb.net/?retryWrites=true&w=majority
   MONGODB_DATABASE=pokedex
   ```

> ⚠️ **As credenciais não vão para o GitHub.** O `application.properties` só contém
> `${MONGODB_URI}`; o valor real fica no `backend/.env`, que está no `.gitignore`.
> Também é possível usar variáveis de ambiente do sistema, que têm prioridade sobre o `.env`:
>
> ```powershell
> $env:MONGODB_URI = "mongodb+srv://..."     # PowerShell
> ```
> ```bash
> export MONGODB_URI="mongodb+srv://..."     # Linux/macOS
> ```

Sem `MONGODB_URI`, o backend tenta um MongoDB local em `mongodb://localhost:27017`.

---

## ☕ 2. Executar o backend (Spring Boot)

```bash
cd backend
./mvnw spring-boot:run          # Linux/macOS/Git Bash
.\mvnw.cmd spring-boot:run      # Windows (PowerShell/CMD)
```

A API sobe em **<http://localhost:8080/api>**. Na **primeira execução**, o MongoDB é populado
automaticamente a partir do `Script_Pokedex.sql`:

```text
Conectando ao MongoDB: mongodb+srv://***:***@cluster0.xxxxx.mongodb.net/... (banco 'pokedex')
Coleção 'types' populada com 18 documentos
Coleção 'pokemons' populada com 151 documentos
Coleção 'trainers' populada com 12 documentos
```

Nas próximas execuções os dados existentes são mantidos (incluindo o que foi cadastrado,
editado ou excluído pela aplicação). Para **voltar aos dados originais do script**, inicie com
`SEED_RESET=true` (no `.env` ou como variável de ambiente) uma vez.

No Atlas, os dados podem ser conferidos em **Database → Browse Collections → pokedex**.

### Problemas de conexão

Se o MongoDB não puder ser acessado, a API continua no ar e responde **503**. Em até 10 segundos,
o terminal mostra um quadro **"NÃO FOI POSSÍVEL ACESSAR O MONGODB"** com o erro. Causas comuns:

| Erro no terminal | Solução |
| --- | --- |
| `Timed out while waiting for a server...` | Libere seu IP em **Network Access** (redes de faculdade mudam de IP; `0.0.0.0/0` resolve para testes) |
| `Authentication failed` / `bad auth` | Confira usuário e senha em **Database Access** (é o usuário do banco, não o login do site) |
| Erro de URI inválida | Codifique caracteres especiais da senha (`@` → `%40`, `#` → `%23`, `/` → `%2F`) |
| Conecta em `localhost` em vez do Atlas | O arquivo precisa se chamar exatamente `.env` (não `.env.txt`) e ficar dentro de `backend/` |

Testes automatizados (não precisam de banco):

```bash
./mvnw test
```

---

## ⚛️ 3. Executar o frontend (React + Vite)

Em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Abra **<http://localhost:5173>**.

Se a API estiver em outro endereço, crie `frontend/.env` com `VITE_API_URL=http://host:porta/api`
(veja `frontend/.env.example`). Outros scripts: `npm run build` (versão de produção) e `npm run lint`.

---

## 🌐 API REST

Base: `http://localhost:8080/api`. A pasta `backend` tem o arquivo [`requests.http`](backend/requests.http)
com **todas as requisições prontas** (VS Code com a extensão *REST Client* ou IntelliJ). Também
podem ser usados Postman, Insomnia ou Thunder Client.

### Pokémon

| Método | Endpoint | Descrição | Respostas |
| --- | --- | --- | --- |
| `GET` | `/api/pokemons` | Lista todos (ordenados pelo número) | 200 |
| `GET` | `/api/pokemons?search=char` | Busca por parte do nome **ou** número exato (`?search=25`) | 200 |
| `GET` | `/api/pokemons?type=Fire` | Filtra por tipo (pode combinar com `search`) | 200 |
| `GET` | `/api/pokemons/{id}` | Busca pelo ID | 200, 404 |
| `GET` | `/api/pokemons/{id}/trainers` | Treinadores que possuem o Pokémon | 200, 404 |
| `POST` | `/api/pokemons` | Cadastra (ID opcional: sem ele, usa o próximo livre) | 201 + `Location`, 400, 409 |
| `PUT` | `/api/pokemons/{id}` | Atualiza | 200, 400, 404, 409 |
| `DELETE` | `/api/pokemons/{id}` | Remove (e o retira dos times dos treinadores) | 204, 404 |

### Tipos e treinadores

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `GET` | `/api/types` | Lista os 18 tipos do script |
| `GET` | `/api/trainers` | Lista os treinadores com o time resolvido (`?pokemonId=25` filtra) |
| `GET` | `/api/trainers/{id}` | Busca um treinador |
| `POST` | `/api/trainers` | Cadastra (máx. 6 Pokémon, todos precisam existir) |
| `PUT` | `/api/trainers/{id}` | Atualiza |
| `DELETE` | `/api/trainers/{id}` | Remove |

### Exemplo: cadastrar um Pokémon

```http
POST /api/pokemons
Content-Type: application/json

{
  "name": "Pichu",
  "hp": 20, "attack": 40, "defense": 15,
  "specialAttack": 35, "specialDefense": 35, "speed": 60,
  "types": ["Electric"]
}
```

```http
HTTP/1.1 201 Created
Location: http://localhost:8080/api/pokemons/152

{ "id": 152, "name": "Pichu", "hp": 20, "attack": 40, "defense": 15, "specialAttack": 35,
  "specialDefense": 35, "speed": 60, "types": ["Electric"], "imageUrl": null, "total": 205 }
```

**Regras de validação:** nome obrigatório (até 30 caracteres) e único; atributos de 1 a 255;
1 ou 2 tipos existentes na coleção `types` (aceita maiúsculas/minúsculas, ex.: `"electric"`),
sem repetição; `imageUrl` opcional (`http://` ou `https://`).

### Formato dos erros

Os erros seguem o padrão **Problem Details (RFC 9457)**. Erros de validação trazem a mensagem
de cada campo em `errors`, que o frontend mostra embaixo do campo correspondente:

```json
{
  "status": 400,
  "title": "Dados inválidos",
  "detail": "Um ou mais campos estão inválidos.",
  "errors": { "name": "O nome é obrigatório", "hp": "O HP deve ser no mínimo 1" }
}
```

| Status | Quando |
| --- | --- |
| 400 | Campos inválidos, JSON malformado, tipo inexistente, ID não numérico |
| 404 | Pokémon/treinador não encontrado |
| 409 | ID ou nome já cadastrado |
| 503 | MongoDB inacessível (confira a `MONGODB_URI`) |

---

## 🗄️ Modelagem: do SQL para o MongoDB

O script relacional tem 5 tabelas. No MongoDB elas viraram **3 coleções**:

| SQL | MongoDB | Decisão |
| --- | --- | --- |
| `Pokemon` + `Rel_Pokemon_Type` + `Types` | `pokemons` | Tipos **embutidos** como array de nomes: são poucos, quase nunca mudam e sempre são lidos junto com o Pokémon, então não há necessidade de *join* |
| `Types` | `types` | Mantida como coleção própria para listar os tipos no filtro e **validar** cadastros |
| `Trainers` + `Rel_Trainer_Pokemon` | `trainers` | A relação N:N vira um array de **referências** (`pokemonIds`). Referência, e não cópia, porque o Pokémon pode ser editado ou excluído pela API |

```jsonc
// pokemons — o número da Pokédex é o _id
{ "_id": 1, "name": "Bulbasaur", "hp": 45, "attack": 49, "defense": 49,
  "specialAttack": 65, "specialDefense": 65, "speed": 45, "types": ["Grass", "Poison"] }

// types
{ "_id": 5, "name": "Electric" }

// trainers — a lista mantém a ordem e permite repetições (o Koga tem 3 Venonat no script)
{ "_id": 6, "name": "Koga", "role": "Líder de Ginásio", "badges": 5, "pokemonIds": [48, 48, 48, 49] }
```

**Como o script é importado:** o `pom.xml` copia o `Script_Pokedex.sql` da raiz do repositório
para o *classpath*, garantindo uma única fonte dos dados. Na inicialização, o `DataSeeder` usa o
`SqlScriptParser`, que lê os `INSERT INTO ... VALUES` e trata strings como `'Farfetch''d'`. Em seguida, o
`PokedexSqlMapper` monta os documentos e grava cada coleção que estiver vazia. Não é necessário
MySQL nem importação manual.

**Observações sobre os dados:**
- No script, o **Mew (#151) não tem tipo** (não há linha em `Rel_Pokemon_Type`). O documento
  foi mantido fiel ao script (`types: []`), e a interface o exibe como "Sem tipo". Ele pode ser
  corrigido pela tela de edição.
- O campo `role` ("Líder de Ginásio", "Rival"...) não existe no script. Ele foi acrescentado
  na importação para enriquecer a tela de treinadores.
- Exclusões mantêm a integridade: ao excluir um Pokémon, a API o remove (`$pull`) dos
  times de todos os treinadores.

---

## 📁 Estrutura do projeto

```text
pokedex/
├── Script_Pokedex.sql                 # Fonte dos dados (copiado para o classpath no build)
├── docs/                              # Imagens do README
├── backend/                           # API REST — Spring Boot
│   ├── pom.xml
│   ├── .env.example                   # Modelo das variáveis (MONGODB_URI...)
│   ├── requests.http                  # Requisições prontas para testar a API
│   └── src/
│       ├── main/java/br/com/pokedex/
│       │   ├── controller/            # PokemonController, TrainerController, PokemonTypeController
│       │   ├── service/               # Regras de negócio e validações
│       │   ├── repository/            # Spring Data MongoDB (+ busca com filtros opcionais)
│       │   ├── model/                 # Documentos: Pokemon, PokemonType, Trainer
│       │   ├── dto/                   # Corpos de requisição/resposta (records + Bean Validation)
│       │   ├── exception/             # Exceções e GlobalExceptionHandler (Problem Details)
│       │   ├── seed/                  # SqlScriptParser, PokedexSqlMapper, DataSeeder
│       │   └── config/                # CORS e timeout de conexão do MongoDB
│       ├── main/resources/application.properties
│       └── test/                      # Testes do parser, do service e do controller
└── frontend/                          # Pokédex — React + Vite (evolução da Atividade 03)
    ├── package.json
    ├── .env.example                   # VITE_API_URL
    └── src/
        ├── services/api.js            # Todas as chamadas à API (fetch)
        ├── hooks/                     # usePokedexData, usePokemonSearch, usePokemonDetails
        ├── components/                # PokemonDetails, PokemonList, PokemonCard, PokemonForm,
        │                              # Modal, ConfirmDialog, TrainerList, TrainerCard, ...
        ├── utils/pokemon.js           # Nomes traduzidos, URLs das imagens, cálculos
        ├── App.jsx                    # Tela principal e estado da aplicação
        └── App.css
```

### O que mudou no frontend em relação à Atividade 03

| Antes | Agora |
| --- | --- |
| `import pokedex from './data/pokedex-data.json'` | `services/api.js` + hooks que buscam os dados com `fetch` |
| `trainers.json` escrito à mão | Treinadores do `Script_Pokedex.sql`, vindos de `GET /api/trainers` |
| Busca só ao apertar Enter, por nome exato | Busca na API enquanto digita, por parte do nome ou número |
| Sem filtro por tipo | Filtro por tipo (API) + navegação anterior/próximo dentro do filtro |
| Somente leitura | Cadastro, edição e exclusão persistidos no MongoDB |
| Mew (sem tipo) quebrava a tela | Pokémon sem tipo exibido como "Sem tipo" |
