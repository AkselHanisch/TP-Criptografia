# Secreto Compartido En Imágenes Con Esteganografía:

Implementación del algoritmo de secreto compartido en imagenes descripto en el documento “An Efficient Secret Image Sharing Scheme” cuyos
autores son Luang-Shyr Wu y Tsung-Ming Lo de la Universidad de Tecnología China de Taiwan.

# Compilación:

```bash
mvn clean package
```

# Ejecución:

```bash
java -jar visualSSS.jar [params] 
```

# Utilización:

El programa recibe los siguientes parámetros de forma obligatoria:

 - -d o -r: Indica el modo de operación, -d indica que se va a distribuir una imagen secreta en otras, mientras que -r indica que se va a recuperar una imagen secreta a partir de otras.
 - -secret imagen: La imagen corresponde a un archivo .bmp. Si se eligió -d, el archivo es la imagen a ocultar. Si se eligió -r, el archivo es la salida
 - -k número: Indica la cantidad minima de sombras para recuperar el secreto en un esquema (k, n). Puede tomar valores entre 2 a 10 inclusive.

De forma opcional, recibe:

 - -n número: Indica la cantidad de sombras en las que se distribuira el secreto en un esquema (k, n). Solo puede usarse en modo -d. Por defecto, se toma como n la cantidad total de imágenes del directorio
 - -dir directorio: Indica el directorio en donde se encuentran las imagenes en donde se distribuira el secreto (en -d) o las imagenes que contienen el secreto oculto (en -r). Por defecto, se toma el directorio actual


En todos los casos, la cantidad total de pixeles de la imagen secreto debe ser divisible por k

En caso de que k sea 8, las imagenes en donde se distribuye el secreto deben tener exactamente las mismas dimensiones que la imagen a ocultar
En caso de que k sea distinto a 8, las imagenes en donde se distribuira deben tener una cantidad de pixeles mayor o igual a 32 + 8*S/k, en donde S es la cantidad de pixeles de la imagen secreto


