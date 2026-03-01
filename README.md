# Video Workflow - API Endpoints

Este documento lista os endpoints REST presentes neste projeto, com uma breve descrição do que cada um faz.

Formato das entradas:
- Método: HTTP method
- Caminho: URL do endpoint (concatenando o `@RequestMapping` da classe quando aplicável)
- Parâmetros: parâmetros esperados (query/path/form-data)
- Descrição: resumo curto do comportamento

## Endpoints

### VideoUploadController
Base path: `/videos`

1) Upload de vídeo
- Método: POST
- Caminho: `/videos`
- Consumo: multipart/form-data
- Parâmetros:
  - `file` (form-data, arquivo) - arquivo de vídeo a ser enviado
  - `userId` (form-data, string) - identificador do usuário dono do vídeo
- Descrição: Recebe um arquivo multipart e registra/encaminha o upload para processamento. Realiza validações de presença, tamanho e sanitização do nome do arquivo. Retorna 200 OK em caso de sucesso ou lança exceção em casos de erro.


### ConsultationController
Base path: `/api/consultation`

1) Listar vídeos do usuário
- Método: GET
- Caminho: `/api/consultation/videos`
- Parâmetros:
  - `user_id` (query, string) - identificador do usuário
- Retorno: lista de objetos `VideoResponse` com campos: `videoId`, `userId`, `uploadPath`, `outputPath`, `status`, `uploadedAt`, `processedAt`.
- Descrição: Retorna os vídeos associados ao usuário informado. Retorna 400 Bad Request se `user_id` for ausente ou vazio.

2) Download de vídeo processado
- Método: GET
- Caminho: `/api/consultation/videos/{videoId}/download`
- Parâmetros:
  - `videoId` (path, string) - identificador do vídeo
  - `user_id` (query, string) - identificador do usuário
- Retorno: stream do arquivo (InputStreamResource) com cabeçalho `Content-Disposition` para download; usa o tipo de mídia retornado pelo serviço.
- Descrição: Faz o download do arquivo processado do vídeo para o usuário determinado. Retorna 400 se `user_id` ausente, 404 se o recurso não for encontrado.


## Observações
- Os endpoints encontrados foram identificados por anotações `@RestController` e mapeamentos (`@RequestMapping`, `@GetMapping`, `@PostMapping`).
- Há um componente `MappingLogger` que loga todos os mapeamentos registrados na inicialização da aplicação.
- Se desejar, posso: (a) adicionar exemplos de requisição cURL/HTTPie para cada endpoint, (b) gerar um arquivo OpenAPI/Swagger initial draft, (c) incluir exemplos de payloads/response bodies.


## Executando localmente
Para executar a aplicação localmente (por exemplo via IDE), é necessário ativar o perfil `local`. Existem duas formas comuns:

1) Adicionar a opção de JVM (VM options) na configuração de execução da sua IDE:

    - VM option a ser adicionada: `-Dspring.profiles.active=local`

    Exemplo (IntelliJ IDEA):
    - Run -> Edit Configurations -> selecione a configuração de execução -> informe em "VM options": `-Dspring.profiles.active=local`.

2) Alternativa com Maven (sem precisar configurar VM options na IDE):

    - Execute: `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`

Observação: o projeto está configurado para Java 17 (propriedade `java.version` no `pom.xml`). Se você estiver tendo erros de compilação na IDE (por exemplo relacionados ao compilador `javac` ou a anotações como Lombok), verifique se o JDK da IDE está apontando para uma JDK compatível (Java 17) — veja abaixo sugestões de solução para erros comuns.

---
Gerado automaticamente a partir do código fonte do projeto.
