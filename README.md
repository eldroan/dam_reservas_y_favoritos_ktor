# DAM Reservas y Favoritos

Este es un proyecto de ejemplo para ser utilizado en las prácticas de DAM.

## Como usar la api?

La api se encuentra accesible de manera publica bajo la url `https://dam-recordatorio-favoritos-api.duckdns.org`.

Para comprobar que este activa existen dos endpoints `/ping` que pueden utilizar.

- [GET] [Sin Autenticación] [https://dam-recordatorio-favoritos-api.duckdns.org/ping](https://dam-recordatorio-favoritos-api.duckdns.org/ping)
- [GET] [Con Autenticación BASIC] [https://dam-recordatorio-favoritos-api.duckdns.org/auth-ping](https://dam-recordatorio-favoritos-api.duckdns.org/auth-ping)

Para ambos deberían ver que responde el mensaje `Pong`.

### Que es la Autenticación BASIC?
Es la forma más sencilla de autenticacion disponible para una aplicación web. Consiste en mandar un usuario y contraseña codificados en Base64 en cada request. Si no fuera porque nuestras request viajan por SSL seria trivial de recuperar los datos enviados al interceptar cualquier request. Existen mecanismos múcho mas seguros de autenticación pero para los fines de nuestro trabajo es suficiente.

Esta información viaja en todas las request de un cliente en un header de nombre `Authorization`. En nuestra app podemos crear un`BasicAuthInterceptor` que se encargue de interceptar todas las llamadas salientes y sumar este header.

#### Crear un Usuario y Contraseña para la api

Utilizando postman, curl o alguna herramienta similar ha un POST a `https://dam-recordatorio-favoritos-api.duckdns.org/usuario` con el siguiente body:
```json
{
    "usuario": "USUARIO",
    "clave": "MICLAVE"
}
```

Aqui un ejemplo de como sería la pegada utilizando el curl de la terminal:
```bash
curl -L -X POST 'https://dam-recordatorio-favoritos-api.duckdns.org/usuario' -H 'Content-Type: application/json' --data-raw '{
    "usuario": "USUARIO",
    "clave": "MICLAVE"
}'
```

Una vez creado nuestro usuario podemos comprobar su funcionamiento con el siguiente curl:
```bash
curl -L -X GET 'https://dam-recordatorio-favoritos-api.duckdns.org/auth-ping' -H 'Authorization: Basic MiUsuarioYPassEnBase64'
```
> Para la prueba pueden generar el valor del header desde [este link](https://www.blitter.se/utils/basic-authentication-header-generator/)

## Como usar una versión local de la api?

> Esto no es necesario, pueden utilizar la versión desplegada que se menciona anteriormente. 
> Se incluye por si hubiera algun problema o si prefieren trabajarla así. 

En este repositorio se encuentra todo lo necesario para levantar la aplicacion de manera local en caso de que así lo desee.
Lo único a tener en cuenta es que al usar un emulador deberán usar la ip `10.0.2.2` y como android bloquea las pegadas que no vengan por `https` también hay que agregar en nuestro manifest lo siguiente:
```xml
<!-- Aca estamos haciendo una prueba pero NUNCA deberíamos hacer esto en producción -->
<application android:usesCleartextTraffic="true">
        <!-- ... -->
</application>
```

### Generar una imagen de docker local
Las imagenes del proyecto se generan utilizando el archivo [Dockerfile](./Dockerfile). Asegurese de encontrarse en ese directorio y ejecutar el siguiente comando:
```
docker build -t dam-reservas-favoritos-ktor .
```
Asegurarse de estar en el mismo directorio que el archivo docker-compose.yml. Este archivo por default no incluye el nombre del schema de la db ni la contraseña del usuario root. Asegurese de reemplazar los valores `DEFAULT_DB_NAME` y `DEFAULT_DB_PASS`, puede ser manual o usando el script `replace.sh`.

Luego ejecute el siguiente comando:
```bash
docker compose up -d
```

Para frenar el entorno ejecute:
```bash
docker compose down
```

## Como pero sobre los Favoritos y Reservas de esta api

Como mencionamos en el trabajo anterior las entidades Favoritos y Reservas tienen la siguiente estructura

```java
class Reserva {
    UUID alojamientoID;
    UUID usuarioID;
    Date fechaIngreso;
    Date fechaSalida;
}

class Favorito {
    UUID alojamientoID;
    UUID usuarioID;
}
```

Las respuestas de esta api estarán en formato JSON por lo cual tendrian la siguiente estructura
```json5
// Reserva
{
    "alojamientoId": "a581defa-6e19-4f03-b572-7e59d26007fe",
    "usuarioId": "0000defa-0000-4f03-b572-7e59d26007fe",
    "fechaIngreso": "2022-10-28T21:15:20+0000", // Este formato es ISO-8601	y es común de encontrar en muchas APIs
    "fechaSalida": "2022-12-28T21:15:20+0000"
}
```
```json5
// Favorito
{
    "alojamientoId": "a581defa-6e19-4f03-b572-7e59d26007fe",
    "usuarioId": "0000defa-0000-4f03-b572-7e59d26007fe"
}
```

### Operaciones soportadas en Reservas - PATH: `/reserva`

#### [GET] - [Con Autenticación BASIC] - Listar Reservas
Retorna el listado de reservas almacenadas por el cliente autenticado.
> Opcionalmente puede recibir el queryparam `usuarioId` para filtrar resultados de un mismo usuario.

Curl de ejemplo sin queryparam:
```bash
curl -L -X GET 'https://dam-recordatorio-favoritos-api.duckdns.org/reserva' -H 'Authorization: Basic MiUsuarioYPassEnBase64'
```
Curl de ejemplo con queryparam:
```bash
curl -L -X GET 'https://dam-recordatorio-favoritos-api.duckdns.org/reserva?usuarioId=0000defa-0000-4f03-b572-7e59d26007fe' -H 'Authorization: Basic MiUsuarioYPassEnBase64'
```
#### [POST] - [Con Autenticación BASIC] - Crear Reserva
Espera recibir una reserva en el body de la petición y en caso de éxito retorna un status code `200` con el objeto 
creado. En caso de fallar retorna un mensaje de error con un status code `500`.

Curl de ejemplo:
```bash
curl -L -X POST 'https://dam-recordatorio-favoritos-api.duckdns.org/reserva' -H 'Authorization: Basic MiUsuarioYPassEnBase64' -H 'Content-Type: application/json' --data-raw '{
    "alojamientoId": "a581defa-6e19-4f03-b572-7e59d26007fe",
    "usuarioId": "0000defa-0000-4f03-b572-7e59d26007fe",
    "fechaIngreso": "2007-12-31T23:59:01",
    "fechaSalida": "2007-12-31T23:59:01"
}'
```

#### [DELETE] - [Con Autenticación BASIC] - Eliminar Reserva
Espera recibir el queryparam `alojamientoId` con el valor del id del alojamiento que se desea borrar. 
En caso de éxito retorna un status code `200` con el objeto creado. En caso de fallar, o no encontrar el 
queryparam `alojamientoId`, retorna un mensaje de error con un status code `400`.

```bash
curl -L -X DELETE 'https://dam-recordatorio-favoritos-api.duckdns.org/reserva?alojamientoId=a581defa-6e19-4f03-b572-7e59d26007fe' -H 'Authorization: Basic MiUsuarioYPassEnBase64' --data-raw ''
```

### Operaciones soportadas en Favoritos - PATH: `/favorito`

#### [GET] - [Con Autenticación BASIC] - Listar Favoritos
Retorna el listado de favoritos almacenadas por el cliente autenticado.
> Opcionalmente puede recibir el queryparam `usuarioId` para filtrar resultados de un mismo usuario.

Curl de ejemplo sin queryparam:
```bash
curl -L -X GET 'https://dam-recordatorio-favoritos-api.duckdns.org/favorito' -H 'Authorization: Basic MiUsuarioYPassEnBase64'
```
Curl de ejemplo con queryparam:
```bash
curl -L -X GET 'https://dam-recordatorio-favoritos-api.duckdns.org/favorito?usuarioId=0000defa-0000-4f03-b572-7e59d26007fe' -H 'Authorization: Basic MiUsuarioYPassEnBase64'
```

#### [POST] - [Con Autenticación BASIC] - Crear Favorito
Espera recibir una reserva en el body de la petición y en caso de éxito retorna un status code `200` con el objeto
creado. En caso de fallar retorna un mensaje de error con un status code `500`.

```bash
curl -L -X POST 'https://dam-recordatorio-favoritos-api.duckdns.org/favorito' -H 'Authorization: Basic MiUsuarioYPassEnBase64' -H 'Content-Type: application/json' --data-raw '{
    "alojamientoId": "a581defa-6e19-4f03-b572-7e59d26007fe",
    "usuarioId": "0000defa-0000-4f03-b572-7e59d26007fe"
}'
```

#### [DELETE] - [Con Autenticación BASIC] - Eliminar Favorito
Espera recibir el queryparam `alojamientoId` con el valor del id del alojamiento que se desea borrar.
En caso de éxito retorna un status code `200` con el objeto creado. En caso de fallar, o no encontrar el
queryparam `alojamientoId`, retorna un mensaje de error con un status code `400`.

```bash
curl -L -X DELETE 'https://dam-recordatorio-favoritos-api.duckdns.org/favorito?alojamientoId=a581defa-6e19-4f03-b572-7e59d26007fe' -H 'Authorization: Basic MiUsuarioYPassEnBase64' --data-raw ''
```