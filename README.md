# Biogin X - Sistema de Autenticación por Reconocimiento Facial

## Equipo de Trabajo

- **Scrum Master**: Juan Manuel Cabrera
- **Líder de Desarrollo**: Mariano Chun
- **Desarrolladores**: Mariano Chun, Francisco Barrientos, Matías Daniel Nissero, Fernando Iván Antúnez
- **Tester**: Agustina Camila Hirschfeld
- **Analistas Funcionales**: Adrián Gustavo Verón

## Misión

Proveer productos fiables, seguros y de calidad, asegurando que el usuario se sienta cómodo y confiado al utilizar el sistema de autenticación de Biogin X.

## Alcance

El proyecto incluye:

- Registro de usuarios utilizando reconocimiento facial.
- Proceso de autenticación mediante comparación de imágenes faciales.
- Interfaz de usuario intuitiva.
- Seguridad y privacidad de los datos biométricos.
- Tolerancia a variaciones en condiciones de iluminación, ángulos de visión, y expresiones faciales.
- Registro de accesos y capacidad de actualización.

## Introducción
Biogin X es una aplicación móvil simple y segura diseñada para autenticar usuarios rápidamente utilizando reconocimiento facial. Este sistema está implementado para mejorar la seguridad y eficiencia en el acceso a instalaciones como universidades.

## Pantalla Principal

### 1. Ingresar con reconocimiento facial (Seguridad)
Esta opción está destinada al personal de seguridad encargado de autenticar a estudiantes, docentes y otros usuarios que ingresen a la universidad. La autenticación se realiza mediante reconocimiento facial y requiere que la persona esté previamente registrada en la base de datos.

### 2. Ingresar con reconocimiento facial (RRHH)
Este módulo está destinado al personal de recursos humanos, quienes tienen la capacidad de registrar nuevos usuarios en la base de datos. Para el registro, se ingresan los datos correspondientes del usuario, su rol (estudiante, docente, etc.) y se capturan tres fotos. Posteriormente, se genera una codificación de su rostro en la API de detección de rostros.

### 3. Ingresar con DNI
Esta opción se utiliza cuando no hay conexión a Internet. Primero, se escanea el DNI de la persona de seguridad, luego se puede dar de alta a los usuarios previamente registrados utilizando una base de datos local con los DNIs autorizados.

### 4. Ingresar con reconocimiento facial (Jerárquico)
Este módulo permite a los usuarios con rol jerárquico configurar el correo electrónico para recibir reportes y establecer los días en que se realizará el entrenamiento del algoritmo con fotos de nuevas personas.

### 5. Ingresar con reconocimiento facial (Admin)
Esta vista permite cambiar la base de datos principal en caso de que no esté disponible. Las opciones principales son Firebase y Back4apps.

## Funcionamiento de la API

La API de reconocimiento facial está desarrollada en Python utilizando la biblioteca 'face_recognition' de Adam Geitgey. Permite entrenar el modelo y realizar el reconocimiento facial mediante la cámara.

- **Entrenamiento del Modelo**: Recibe una solicitud de entrenamiento con tres imágenes de la nueva persona. Utiliza el método HOG (Histogram of Oriented Gradients) para procesar las imágenes y generar un vector de características del rostro.
- **Reconocimiento Facial**: En el momento de la autenticación, la API recibe una solicitud POST de reconocimiento y devuelve el DNI asociado al rostro o un mensaje de "desconocido" si la persona no está registrada.

La API está alojada en el sitio web de PythonAnywhere.

## Base de Datos

Biogin X utiliza Firebase para la gestión de su base de datos en la nube, lo que permite una mejor organización y la capacidad de ejecutar funciones del lado del servidor.

## Objetivos del Proyecto

- **Eficiencia**: Optimizar el uso de batería, memoria y procesador.
- **Usabilidad**: Proveer una interfaz fácil de usar para usuarios con diferentes niveles de experiencia.
- **Seguridad**: Cumplir con las regulaciones de privacidad de datos biométricos.
- **Rendimiento**: Asegurar tiempos de respuesta aceptables en el proceso de autenticación facial.
