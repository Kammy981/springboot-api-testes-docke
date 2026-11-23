# API de produtos

API REST com Java 21, Spring Boot e PostgreSQL. Cada produto tem identificador, nome, descrição, preço e estoque.

## Build

Com Java 21 e Maven, execute `mvn clean package`. No Windows, também é possível usar `.\mvnw.cmd clean package`. O build roda os testes e gera `target/produtos-0.0.1-SNAPSHOT.jar`.

## Execução com Docker Compose

No PowerShell, configure um usuário e uma senha local. No PowerShell 7, use `Read-Host 'Senha do PostgreSQL' -MaskInput` para mascarar a digitação.

```powershell
$env:DB_USER = 'produtos'
$credencial = Get-Credential -UserName $env:DB_USER -Message 'Senha local do PostgreSQL'
$env:DB_PASSWORD = $credencial.GetNetworkCredential().Password
docker compose up --build -d
docker compose ps
Invoke-RestMethod http://localhost:8080/actuator/health
docker compose logs api
```

A API usa a porta 8080 no host e o PostgreSQL usa 5432. Configure `API_PORT` ou `DB_PORT` se essas portas estiverem ocupadas. O Compose conecta a API ao banco pelo nome `db` e guarda os dados em um volume que permanece após `docker compose down`. Reutilize a senha da primeira inicialização ao usar o mesmo volume.

## Execução do JAR

Para usar apenas o PostgreSQL do Compose, rode `docker compose up -d --wait db`. Se a API do Compose estiver em execução, use `docker compose stop api` para liberar a porta. Reutilize as mesmas credenciais e execute:

```powershell
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

O [relatório](RELATORIO.md) reúne a pesquisa e a aplicação dos conceitos.
