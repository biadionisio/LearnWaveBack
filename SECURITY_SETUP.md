# Configuração de segurança do chat

Antes de publicar a API, crie estas variáveis de ambiente no Render. Não coloque os valores no Git nem no arquivo `application.properties`.

| Variável | Uso |
| --- | --- |
| `CHAT_AUTH_SECRET` | Segredo com pelo menos 32 caracteres para assinar tokens de login. |
| `CHAT_ENCRYPTION_KEY` | Chave AES-256 em Base64 para criptografar mensagens no banco. |
| `CHAT_MODERATION_BLOCKED_WORDS` | Opcional. Lista de termos bloqueados, separada por vírgula. Substitui a lista padrão. |

Exemplos para gerar valores localmente (não reutilize estes exemplos):

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

Use uma saída do comando para `CHAT_ENCRYPTION_KEY`. Para `CHAT_AUTH_SECRET`, use uma frase longa, aleatória e exclusiva do ambiente de produção.

Após definir ou trocar uma chave, reinicie o serviço. Se `CHAT_ENCRYPTION_KEY` for trocada, mensagens já criptografadas com a chave anterior não poderão mais ser abertas; guarde a chave de produção em um gerenciador de segredos.

Mensagens antigas em texto simples continuam legíveis para permitir a transição. Somente novas mensagens serão gravadas criptografadas. Migre ou apague as antigas após fazer backup, se necessário.

## Moderação automática

Antes de uma mensagem ser salva, a API bloqueia os termos configurados. A lista padrão inclui palavrões e insultos comuns em português. Para trocar a lista no Render, defina, por exemplo:

```text
CHAT_MODERATION_BLOCKED_WORDS=termo um,termo dois,frase proibida
```

A verificação ignora maiúsculas/minúsculas e acentos. Ela bloqueia a mensagem inteira e não grava a tentativa no banco.
