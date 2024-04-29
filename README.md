# Reconocimiento Facial

Este repositorio contiene scripts en Python para el reconocimiento facial utilizando la biblioteca `face_recognition` de Adam Geitgey. Los scripts permiten entrenar un modelo, validar un modelo ya entrenado y realizar reconocimiento facial en tiempo real utilizando la cámara web.

## Dependencias

- `Python` (versión 3.6 o superior)
- `pip` (administrador de paquetes de Python)
- `face_recognition`: Se puede instalar ejecutando `pip install face_recognition`
- `scikit-learn`: Se puede instalar ejecutando `pip install scikit-learn`
- `opencv-python`: Se puede instalar ejecutando `pip install opencv-python`
- `Pillow`: Se puede instalar ejecutando `pip install Pillow`

## Uso

### Validación del Modelo

Para validar el modelo entrenado utilizando imágenes de validación, coloca las imágenes en la carpeta `validation` y ejecuta el script con la opción `--validate`:

```bash
python detector.py --validate
```
### Prueba del Modelo
Para probar el modelo con una imagen desconocida, proporciona la ruta de la imagen utilizando la opción -f:
```bash
python detector.py --test -f ruta/a/imagen_desconocida.jpg
```
### Entrenamiento del Modelo
Para entrenar el modelo se debe agregar una carpeta con el nombre de la persona y entre 1-3 fotos de la persona a reconocer, luego se debe ejecutar el siguiente comando:
```bash
python detector.py --train -m="hog"
```
Este comando utiliza el método HOG (Historiagram of Oriented Gradients) que divide la imagen en celdas pequeñas y calcula los gradientes de intensidad en cada celda. Luego, genera un historiagrama de orientaciones de gradientes y concatena estos historiagramas para formar un vector de características.

También se puede usar el método CNN (Convolutional Neural Network) con el siguiente comando:
```bash
python detector.py --train -m="cnn"
```
Este método consiste en redes neuronales que aprenden automáticamente características de las imágenes durante el entrenamiento. Consisten en capas convolucionales que aplican filtros a la imagen de entrada para extraer características, seguido de capas de pooling para reducir la dimensionalidad.

Sin embargo, al ser más preciso, CNN suele tardar más cuando se entrena el modelo, a comparación del método HOG.

### Reconocimiento en Video en Tiempo Real
Para ejecutar el reconocimiento facial en tiempo real desde la cámara web, simplemente ejecuta el script detector-real-time.py:
```bash
python detector-real-time.py
```

## Reconocimiento Facial con Flask

Este script implementa un servicio web Flask para el reconocimiento facial utilizando la biblioteca face_recognition en Python.

### Endpoints API

- **/recognize** (POST): Realiza el reconocimiento facial en una imagen proporcionada y devuelve los nombres de las caras reconocidas.
- **/train** (POST): Entrena el modelo de reconocimiento facial con nuevas imágenes y nombres proporcionados.

### Uso

1. **/recognize**:
   - **Método**: POST
   - **Parámetros**: La imagen a procesar se envía como parte del cuerpo de la solicitud (formato multipart/form-data).
   - **Respuesta**: Devuelve un objeto JSON con la lista de nombres de las caras reconocidas en la imagen.

2. **/train**:
   - **Método**: POST
   - **Parámetros**: La imagen a entrenar y el nombre asociado se envían como parte del cuerpo de la solicitud (formato multipart/form-data).
   - **Respuesta**: Devuelve un objeto JSON con un mensaje indicando el número de nuevas codificaciones agregadas y el éxito del entrenamiento del modelo.

### Configuración y Ejecución

1. Instala las dependencias necesarias utilizando `pip install -r requirements.txt`.
2. Ejecuta el script utilizando `python detectorAPI.py`.
3. Accede a los endpoints API a través de HTTP para realizar el reconocimiento facial y entrenar el modelo.

### Notas

- Las imágenes de entrenamiento se almacenan en un archivo `encodings.pkl` en el directorio de salida.
- El script utiliza el método HOG (Histogram of Oriented Gradients) para codificar las imágenes y realizar el reconocimiento facial.
- El script se ejecuta en modo de depuración (debug=True) para mostrar mensajes detallados de error en caso de problemas.

### Testeo
Se puede testear correctamente a través de un programa para simular HTTP requests como Postman. Además, se hosteó utilizando PythonAnywhere y se puede acceder con el endpoint `http://Biogin.pythonanywhere.com`

### Requisitos

- Python 3.x
- Flask
- face_recognition
- numpy


