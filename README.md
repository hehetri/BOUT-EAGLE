# Portas necessárias

Abra as portas abaixo no firewall/roteador para o servidor funcionar.

## TCP
- **11000** — LoginServer (LoginServer/src/LoginServer/Main.java)
- **11102** — BotsServer ChannelServer (BotsServer/src/botsserver/Main.java)
- **11104** — BotsServer RelayTCP (BotsServer/src/botsserver/Main.java)

## UDP
- **11010** — LoginServer ChannelServer (LoginServer/src/LoginServer/ChannelServer.java)
- **11011** — BotsServer RoomServer (BotsServer/src/botsserver/Main.java)
- **11013** — BotsServer RelayServer (BotsServer/src/botsserver/Main.java)
