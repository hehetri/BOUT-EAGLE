# BOUT-EAGLE

## Mapa: causa raiz do erro de moblist

**O que estava acontecendo**
- O servidor recebia um ID de mapa correto do cliente, mas em `Room.setMap(...)` aplicava um **offset indevido** quando `roommode != 2` (`map - 1`).  
- Como o `Standard.moblist(map)` usa `switch(map)` com `case 0 = map 0` (índice direto, sem offset), esse `-1` fazia o servidor **consultar a case errada** para o mapa atual.  
- Resultado: o cliente entrava no mapa correto, mas o servidor validava kills usando a moblist de outro mapa, gerando erros como `expected=386 actual=310`.  

**Maior erro**
- **O offset `map - 1` em `setMap`**. Ele deslocava o índice e fazia o servidor escolher o `case` incorreto no `moblist`, causando a divergência entre o mapa real e a lista de mobs usada na validação.  
