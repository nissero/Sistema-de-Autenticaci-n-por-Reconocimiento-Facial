# Reconocimiento Facial

Este repositorio contiene scripts en Python para el reconocimiento facial utilizando la biblioteca `face_recognition` de Adam Geitgey. Los scripts permiten entrenar un modelo, validar un modelo ya entrenado y realizar reconocimiento facial en tiempo real utilizando la cámara web.

## Dependencias

Asegúrate de tener las siguientes dependencias instaladas antes de ejecutar los scripts:

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
### Reconocimiento en Video en Tiempo Real
Para ejecutar el reconocimiento facial en tiempo real desde la cámara web, simplemente ejecuta el script detector-real-time.py:
```bash
python detector-real-time.py
```

El script abrirá una ventana de video en tiempo real con el reconocimiento facial superpuesto en las caras detectadas. Para salir presione 'q'.
