# BOUT-EAGLE

## Mapa: causa raiz do erro de moblist

**O que estava acontecendo**
- O servidor recebia um ID de mapa correto do cliente, mas em `Room.setMap(...)` aplicava um **offset indevido** quando `roommode != 2` (`map - 1`).  
- Como o `Standard.moblist(map)` usa `switch(map)` com `case 0 = map 0` (índice direto, sem offset), esse `-1` fazia o servidor **consultar a case errada** para o mapa atual.  
- Resultado: o cliente entrava no mapa correto, mas o servidor validava kills usando a moblist de outro mapa, gerando erros como `expected=386 actual=310`.  

**Maior erro**
- **O offset `map - 1` em `setMap`**. Ele deslocava o índice e fazia o servidor escolher o `case` incorreto no `moblist`, causando a divergência entre o mapa real e a lista de mobs usada na validação.  

## Chat em room (roommode=2): por que comandos só funcionavam no lobby

**O que estava acontecendo**
- O parser de chat no servidor assumia que o texto do comando (`@...`) sempre começava em um **offset fixo** com base no tamanho do nome do jogador.  
- Isso funciona no lobby, mas **dentro do room/jogo o formato do chat muda**, então o offset fixo acabava cortando o texto errado e o `@` não era detectado.  

**Correção aplicada**
- O parsing agora pega o texto **após o primeiro `]`** (ex.: `"[Nick] mensagem"`), aplica `trim()` e verifica se começa com `@`.  
- Assim, o comando é detectado corretamente **no lobby e dentro do room/jogo**, permitindo que os comandos do lobby funcionem também durante a partida.  
