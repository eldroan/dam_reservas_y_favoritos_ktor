# DAM Reservas y Favoritos

Este es un proyecto de ejemplo para ser utilizado en las prácticas de DAM.


## Generar una imagen de docker local
Las imagenes del proyecto se generan utilizando el archivo [Dockerfile](./Dockerfile). Asegurese de encontrarse en ese directorio y ejecutar el siguiente comando:
```
docker build -t dam-reservas-favoritos-ktor .
```
## Ejecutar una imagen de docker local
```
docker run -p 8080:8080 dam-reservas-favoritos-ktor
```
