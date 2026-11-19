# AntiAFKMX

Plugin de detección de inactividad para servidores Nukkit. Detecta jugadores AFK, envía avisos y puede expulsarlos después del tiempo configurado.

## Características

- Detección de actividad en todos los mundos cargados.
- Muestreo periódico de posición y mundo, además de eventos de movimiento, chat, comandos e interacción.
- Reconoce cambios de mundo y teletransportes como actividad.
- Avisos antes de expulsar a jugadores inactivos.
- Mensajes predeterminados en español y personalizables.
- Exenciones mediante lista de jugadores o permiso.
- Lista configurable de mundos excluidos.

## Permisos

- `antiafk.bypass`: excluye al jugador de la detección AFK y de las expulsiones.

## Configuración

```yaml
afk-time: 5
kick-time: 10
warning-time: 2
broadcast-afk: true

# Mundos excluidos. Vacío = todos los mundos.
disabled-worlds: []

exempt-players:
  - "admin"
  - "moderator"

messages:
  prefix: "&7[&cAntiAFK&7] "
  afk: "{prefix}&e{player} &7ahora está AFK."
  no-longer-afk: "{prefix}&e{player} &7ya no está AFK."
  warning: "{prefix}&c¡Atención! &7Serás expulsado por inactividad en &e{time} &7minutos."
  kick-reason: "Has sido expulsado por permanecer inactivo demasiado tiempo."
```

La detección se aplica a todos los mundos por defecto. Para excluir alguno, agrega su nombre exacto a `disabled-worlds`.

## Instalación

1. Descarga el JAR desde **Actions / Artifacts** o desde Releases.
2. Colócalo en la carpeta `plugins`.
3. Reinicia el servidor.
4. Ajusta `plugins/AntiAFK/config.yml` si necesitas cambiar tiempos o mensajes.

## Compilación

El proyecto utiliza Gradle y Nukkit MOT. Compila con `./gradlew clean build`.

## Créditos y licencia

Este repositorio es un fork de AntiAFKMX y conserva la licencia GPL-3.0 del proyecto original.
