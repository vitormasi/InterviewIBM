# InterviewIBM

Foi desenvolvido um projeto nomeado interview para avaliar os conhecimentos de Back End com Spring Boot

O projeto foi desenvolvio com Spring Boot utilizando JPA, H2, Lombok e JUnit.
Foi utilizado o banco de dados H2 para facilitar na execução sem utilizar serviço externo.
O banco de dados fica persistido na pasta do projeto /data/db, exceto o utilizado para os testes unitários que utiliza apenas memória e é perdido posteriormente.
Há na raiz do projeto/postman uma collection do postman com requests default para teste.

O projeto possui na pasta src/main/caminho do projeto:
- controller
  possui as chamadas REST
- domain
  possui os modelos de classes
- repository
  possui as lógicas para BD
- request
  possui a conexão rest externa para o serviço de CPF
- service
  possui todas as lógicas de execução
- utils
  possui um util para validar CPF válido
  
O projeto possui na pasta src/test/caminho do projeto:
- controller
  possui diversos testes para as chamadas REST da API
  
  
As chamadas REST para a API:
# - POST localhost:8080/interview/pauta
  Descrição solicitada: Cadastrar uma nova pauta
  O request deve enviar um Body com o objeto Pauta a ser cadastrado:
  { "titulo": "titulo da pauta", "descricao": "descricao da pauta" }
  
  Possíveis resultados:
  - 201 created: objeto Pauta criado
  
# - POST localhost:8080/interview/abrir-sessao/{pauta_id}
  Descrição solicitada: Abrir uma sessão de votação em uma pauta (a sessão de votação deve ficar aberta por um tempo determinado na chamada de abertura ou 1 minuto por default)
  O request deve ser feito passando o id da pauta e parâmetro opcional a variavel fechamento passando o tempo de duração da votação em segundos a partir do horário atual. Caso não seja informado fechamento, será considerado 60 segundos.
  Exemplo: localhost:8080/interview/abrir-sessao/{pauta_id}?fechamento=300
  
  Possíveis resultados:
  - 201 created: objeto Votacao criado
  - 404 not found: Pauta não encontrada para o id: {pauta_id}
  
# - POST localhost:8080/interview/voto/{cpf}/votacao/{votacao_id}
  Descrição solicitada: Receber votos dos associados em pautas (os votos são apenas 'Sim'/'Não'. Cada associado é identificado por um id único e pode votar apenas uma vez por pauta)
  O request deve ser feito passando o cpf do associado (considerado como id), o id da votação e o voto [SIM, NAO] como parâmetro obrigatório
  Exemplo: localhost:8080/interview/voto/73199495044/votacao/1?voto=SIM
  
  Possíveis resultados:
  - 200 ok
  - 404 not found: 
    - A sessão de votação informada não existe
  - 500 bad request:
    - O CPF informado não é válido
    - O CPF informado não possui permissão para votar
    - A sessão de votação informada está encerrada
    - O usuário informado já registrou voto para esta sessão de votação
  
# - POST localhost:8080/interview/resultado/{votacao_id}
  Descrição solicitada: Contabilizar os votos e dar o resultado da votação na pauta
  O request deve ser feito passando o id da votação.
  
  Possíveis resultados:
  - 200 ok: objeto Votacao com resultados (lista com número de votos SIM/NAO, caso esteja encerrado terá variável resultado preenchida)
  - 404 not fount: A sessão de votação informada não existe
  
