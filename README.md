# API de produtos

Esta API cadastra produtos e permite consultar, atualizar e excluir os registros. Cada produto tem nome, descrição, preço e estoque. O PostgreSQL guarda os dados e gera o identificador.

## Build

Com Java 21 e Maven, execute `mvn clean package`. No Windows, também é possível usar `.\mvnw.cmd clean package`. O build roda os testes e gera `target/produtos-0.0.1-SNAPSHOT.jar`.

## Execução com Docker Compose

Na raiz do projeto, abra o PowerShell e rode os comandos abaixo. Na primeira execução, escolha uma senha para o banco local. Nas próximas, use a mesma senha enquanto o volume existir.

```powershell
$env:DB_USER = 'produtos'
$credencial = Get-Credential -UserName $env:DB_USER -Message 'Senha local do PostgreSQL'
$env:DB_PASSWORD = $credencial.GetNetworkCredential().Password
docker compose up --build -d
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
docker compose logs api
```

A API usa a porta 8080 e o PostgreSQL usa 5432. Se precisar trocar alguma delas, defina `API_PORT` ou `DB_PORT` antes de subir os containers e ajuste os comandos que consultam essa porta. Para encerrar os serviços e manter os dados, rode `docker compose down`.

## Execução do JAR

Defina `DB_USER` e `DB_PASSWORD` no PowerShell como na seção anterior. Se a API já estiver rodando pelo Compose, pare esse serviço para liberar a porta. Em seguida, suba o banco e execute o JAR.

```powershell
docker compose stop api
docker compose up -d --wait db
$env:DB_URL = 'jdbc:postgresql://localhost:5432/produtos'
java -jar target/produtos-0.0.1-SNAPSHOT.jar
```

Ajuste a URL se você mudou a porta do banco. Em outro terminal, consulte `http://localhost:8080/actuator/health`. A aplicação cria ou atualiza a tabela no banco local por meio do Hibernate.

## Endpoints

| Método | Caminho | Resposta de sucesso |
| --- | --- | --- |
| POST | /produtos | 201 e cabeçalho Location |
| GET | /produtos | 200 e lista |
| GET | /produtos/{id} | 200 e produto |
| PUT | /produtos/{id} | 200 e produto atualizado |
| DELETE | /produtos/{id} | 204 |
| GET | /actuator/health | 200 quando saudável |

Busca, atualização e exclusão de um ID inexistente retornam 404. Dados inválidos retornam 400.

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/produtos -ContentType 'application/json' -Body '{"nome":"Caderno","descricao":"Capa dura","preco":19.90,"estoque":10}'
```
