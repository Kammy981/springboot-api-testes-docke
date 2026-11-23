# Testes, execução e observabilidade em uma API Spring Boot

**Integrante** — Kamilly Stephany Saraiva Santos.

**Modalidade** — individual, aulas 10 e 11.

**Repositório GitHub** — [Kammy981/springboot-api-testes-docke](https://github.com/Kammy981/springboot-api-testes-docke).

## Introdução

A API de produtos usa quatro atributos além do identificador e persiste os registros em PostgreSQL. Para que funcione em outros ambientes, ela precisa de um build que possa ser repetido, testes que detectem regressões, configuração fornecida externamente e uma forma de verificar seu estado durante a execução.

## Parte I — Testes automatizados

1. Um teste automatizado executa um cenário em código e decide se o resultado atende às expectativas escritas.
2. No teste manual, uma pessoa executa os passos e avalia a resposta. A automação repete a mesma verificação em cada build e facilita detectar regressões.
3. Arrange prepara dados e dependências. Act executa a operação. Assert verifica seu resultado ou efeito.
4. JUnit descobre e executa métodos de teste, oferece asserções e informa as falhas.
5. Mockito cria dependências simuladas, permite definir seus comportamentos e verifica chamadas.
6. Um mock é um objeto de teste que imita a interface de um colaborador. Sem configuração, Mockito usa respostas padrão, como zero, falso, coleções ou `Optional` vazios para tipos suportados e `null` para muitos outros objetos.
7. O Service é a unidade em análise. Simular o Repository evita que disponibilidade, credenciais e dados do PostgreSQL alterem o resultado. O teste unitário pode rodar sem iniciar a aplicação. A persistência real deve ser conferida em verificações com o banco.
8. `@Mock` cria o Repository simulado. `@InjectMocks` instancia o Service e injeta nele o mock pelo construtor neste projeto.
9. `when(...).thenReturn(...)` programa a resposta à chamada. `verify(...)` confere uma interação. `assertThrows(...)` verifica o tipo da exceção e devolve a instância para conferir detalhes. `assertEquals(...)` compara o valor obtido com o esperado.
10. O teste do Service verifica a lógica e a interação com a persistência. O teste do Controller verifica a validação do JSON, o contrato HTTP e o tratamento da exceção.
11. MockMvc executa requisições simuladas pela infraestrutura Spring MVC sem abrir uma porta. No projeto, `@WebMvcTest` carrega a camada web e `@MockitoBean` substitui o Service.
12. Cobertura mede o código percorrido pelos testes. Uma linha executada pode ter expectativas incompletas, e cenários de fronteira podem continuar ausentes.

### Teste comentado

```java
@Test
void deveCadastrarProdutoValido() {
    // Arrange: o repositório simulado devolve o produto que recebeu.
    when(repository.save(any(Produto.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

    // Act: chama o serviço com os dados de um novo produto.
    Produto produto = service.cadastrar(DADOS);

    // Assert: confere os atributos e a chamada de persistência.
    assertEquals(DADOS.nome(), produto.getNome());
    assertEquals(DADOS.descricao(), produto.getDescricao());
    assertEquals(DADOS.preco(), produto.getPreco());
    assertEquals(DADOS.estoque(), produto.getEstoque());
    verify(repository).save(produto);
}
```

O método `deveCadastrarProdutoValido` em `ProdutoServiceTest` prepara os dados e configura `save` com `thenAnswer` para devolver o próprio produto recebido. A chamada a `service.cadastrar(DADOS)` exercita a criação do objeto. As asserções verificam os quatro atributos e `verify(repository).save(produto)` confirma que o objeto passou pelo Repository. A atualização e os erros de busca e exclusão completam os quatro cenários do Service. O Controller tem cenários para resposta JSON, 404 e corpo inválido.

## Parte II — Aplicação executável e Docker

1. O build transforma fontes e recursos em um artefato executável por etapas reprodutíveis.
2. `mvn clean package` apaga a saída anterior, compila fontes e testes, executa os testes unitários e empacota o resultado. O plugin do Spring Boot reempacota o JAR para execução.
3. JAR é um arquivo no formato ZIP para classes e recursos Java. O JAR executável do Spring Boot inclui dependências e um inicializador. O Maven também guarda classes compiladas e relatórios de testes em `target`.
4. Com um runtime Java compatível e a configuração do banco disponível, `java -jar target/produtos-0.0.1-SNAPSHOT.jar` inicia a aplicação pelo terminal.
5. Segredos versionados podem permanecer no histórico. Em um Dockerfile, podem vazar pela imagem, camadas ou metadados.
6. Variáveis de ambiente permitem usar o mesmo JAR com bancos e credenciais diferentes. O projeto recebe `DB_USER`, `DB_PASSWORD` e `DB_URL`.
7. Profiles ativam configurações ou componentes específicos de um ambiente, por exemplo por `SPRING_PROFILES_ACTIVE`. O projeto usa as mesmas propriedades com valores externos, sem perfil obrigatório.
8. A imagem contém o runtime, arquivos e instruções de inicialização. O container é uma instância criada dessa imagem. Parar preserva a instância e sua camada gravável. Remover descarta a instância e essa camada.
9. No Dockerfile comentado, `FROM` escolhe o Java 21, `WORKDIR` estabelece `/app`, `COPY` coloca o JAR nesse diretório e `ENTRYPOINT` define a execução com Java. `EXPOSE` documenta a porta interna.
10. Compose descreve e inicia serviços com a rede, as variáveis, o volume e as portas declaradas em `compose.yaml`. A API espera a verificação de prontidão do PostgreSQL.
11. Dentro do container da API, `localhost` aponta para a própria API. A URL do Compose usa `db`, nome resolvido na rede interna.
12. O volume `produtos_db` preserva o diretório de dados entre recriações. Se os dados ficarem apenas na camada do container, a remoção descarta os dados.
13. Logs registram eventos como a inicialização e as operações pedidas, com o ID do produto para diagnóstico.
14. Senhas, tokens, strings de conexão com credenciais, dados pessoais desnecessários e corpos de requisições sensíveis devem ficar fora dos logs.
15. O Actuator oferece endpoints operacionais de saúde e monitoramento. Este projeto expõe somente `health` e oculta detalhes dos componentes.
16. `UP` indica que os indicadores consultados, incluindo o acesso ao banco nesta aplicação, estão saudáveis. Testes funcionais continuam necessários para avaliar regras de negócio. Endpoints operacionais podem revelar dados internos ou habilitar ações e precisam de controle de exposição.

### Dockerfile comentado

```dockerfile
# Define o runtime e o diretório de trabalho.
FROM eclipse-temurin:21-jre
WORKDIR /app
# Copia o artefato gerado pelo Maven para a imagem.
COPY target/produtos-0.0.1-SNAPSHOT.jar app.jar
# Documenta a porta e define o processo iniciado pelo container.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

O Compose injeta as credenciais nos dois serviços por variáveis de ambiente. A verificação `pg_isready` sinaliza quando o banco aceita conexões, e o Actuator confirma se a API consegue se conectar. As portas publicadas ficam acessíveis na interface de loopback do host. O volume é montado no diretório de dados usado pela imagem PostgreSQL 17.

## Estudo de caso — API de pedidos

| Recurso | Problema que resolve | Aplicação | Cuidado |
| --- | --- | --- | --- |
| Testes | Regressões passam despercebidas | Simular o repositório no Service e usar MockMvc no Controller | Verificar retornos e erros e testar a persistência separadamente |
| JAR | Dependência da IDE | Gerar com Maven e iniciar com java -jar | Usar runtime Java compatível |
| Variáveis | Credenciais no arquivo de configuração | Injetar URL, usuário e senha em cada ambiente | Evitar versionar segredos e limitar acesso a eles |
| Dockerfile | Divergência no runtime | Copiar o JAR para uma imagem Java 21 | Reconstruir a imagem sempre que o JAR mudar |
| Compose | API e banco exigem configuração manual | Definir serviços, rede e verificação de prontidão | Usar o nome do serviço e conferir portas no host |
| Volume | A recriação descarta dados gravados no container | Montar o diretório de dados do PostgreSQL em volume | Evitar remover volumes por engano e manter backups quando necessário |
| Logs | Falhas e operações difíceis de diagnosticar | Registrar inicialização e identificadores operacionais | Omitir segredos e distinguir solicitação de confirmação |
| Actuator | Falta verificação automática | Consultar /actuator/health | Restringir detalhes e complementar com testes funcionais |

No projeto de produtos, essas medidas foram aplicadas aos mesmos pontos da arquitetura. Para pedidos, os cenários do Service poderiam cobrir cadastro válido e busca de pedido inexistente, enquanto os testes do Controller verificariam o contrato HTTP correspondente.

## Conclusão

O build reproduzível e a configuração externa permitem transportar o mesmo artefato. Os testes verificam o comportamento esperado durante o desenvolvimento e os indicadores de saúde e logs ajudam a acompanhar a execução.

## Referências consultadas em 23/11/2026

- JUnit Team. [JUnit User Guide](https://docs.junit.org/current/user-guide/).
- Mockito Team. [Mockito](https://site.mockito.org/).
- Spring. [MockMvc](https://docs.spring.io/spring-framework/reference/testing/mockmvc.html), [Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) e [Actuator Endpoints](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html).
- Apache Maven Project. [Introduction to the Build Lifecycle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html).
- Docker Inc. [Dockerfile reference](https://docs.docker.com/reference/dockerfile/) e [Networking in Compose](https://docs.docker.com/compose/how-tos/networking/).

## Evidências da execução

Verificações realizadas em 23/11/2026 no Windows com Java 21, Maven, Docker Desktop e PostgreSQL 17. Os valores abaixo resumem as saídas observadas no terminal.

| Verificação | Resultado observado |
| --- | --- |
| `mvn clean package` | `BUILD SUCCESS`. JAR executável em `target/produtos-0.0.1-SNAPSHOT.jar` (55.208.714 bytes). |
| Surefire | `ProdutoServiceTest` executou 4 testes e `ProdutoControllerTest` executou 3. Zero falhas, erros e testes ignorados. |
| `java -jar` fora da IDE | Aplicação iniciou na porta 18081 e conectou ao PostgreSQL publicado na porta 15432. |
| `docker compose up --build -d` e `docker compose ps` | Imagem da API construída, container da API iniciado na porta 18080 e container PostgreSQL 17 marcado como saudável. |
| `docker inspect` | Volume `produtos-verificacao_produtos_db` montado em `/var/lib/postgresql/data`. |
| `GET /actuator/health` | HTTP 200 e `"status":"UP"` no JAR e no Compose. |
| CRUD via HTTP | Nos dois modos, o cadastro retornou 201, a listagem e a busca retornaram 200, a atualização persistiu a alteração, a exclusão retornou 204 e a busca após a exclusão retornou 404. |
| Logs | O container registrou `Started ProdutosApplication` e `Cadastro de produto solicitado para ID 2`. |

As saídas detalhadas dos testes podem ser reproduzidas em `target/surefire-reports/` com `mvn clean package`. As portas de execução acima foram escolhidas para isolar a verificação. O [README](README.md) usa as portas padrão.
