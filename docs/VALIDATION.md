# Validación de la versión local

Variante solicitada desde el contexto del cliente de 22/11/2026. Las comprobaciones automáticas las realiza Codex. La persona no ha declarado pruebas manuales.

## Estado

- **31 pruebas unitarias: correctas**, incluidas las 13 pruebas nuevas de exportación e importación.
- **APK debug: compilado correctamente.**
- **Lint: 0 errores y 18 avisos no bloqueantes.**
- **APK de pruebas instrumentadas: compilado.** No se ejecutó en dispositivo.
- Se comprobó que el APK no contiene los SDK Authentication ni Firestore y que no quedan importaciones ni llamadas a sus API en el código de la app.
- Catálogo de versiones, plugins raíz, wrapper y AppNavGraph.kt conservados respecto a las referencias originales/entrega anterior.

Los 21 tests y las 10 comprobaciones Firestore de la entrega anterior son históricos y no son el resultado de esta variante. Las pruebas Firestore se han retirado porque ya no se utiliza ese backend.

Se ejecutan pruebas unitarias, compilación APK debug, lint y compilación de tests instrumentados con las versiones existentes. Este entorno usa SDK 37.0/JVM 25 y su configuración de proxy/certificados. Conserva la caché temporal externa de los artefactos oficiales JUnit 4.13.2/Hamcrest 1.3 necesaria para las restricciones de red. No se cambian los repositorios, versiones o plugins declarados del proyecto.

## Comprobaciones nuevas de archivos

`ProjectFileTest` comprueba recorrido exportar/importar, contenido sin fotos/rutas/identificadores/cuentas, valores opcionales y listas vacías, campos desconocidos, cabeceras y versiones, fechas imposibles, materiales inválidos, orden de pasos, posiciones repetidas o negativas, límite de elementos y bytes, texto mal formado, UTF-8 inválido, anidamiento excesivo y nombres de archivo sin separadores de ruta.

El archivo sólo contiene datos del proyecto; importarlo crea identificadores nuevos y nunca importa rutas de fotos. Se lee hasta 256 KB y se valida antes de ofrecer la confirmación. Room sigue usando su transacción para crear el proyecto con todos sus materiales y pasos.

## Recorrido manual pendiente en Android Studio

| Comprobación | Resultado esperado |
|---|---|
| Abrir la carpeta levelMate, sincronizar y Run | App Android disponible sin cuenta ni configuración de backend |
| Modo avión: crear proyecto, materiales/pasos y cerrar/abrir | Todo persiste localmente |
| Share / import projects sin proyectos | Exportar deshabilitado; importar disponible |
| Save project file y cancelar el selector | Room no cambia y no se informa de un guardado inexistente |
| Guardar y compartir un proyecto válido | Archivo JSON con fecha, materiales y pasos; sin fotos ni rutas privadas |
| Elegir un archivo y revisar; cancelar la vista previa | Ningún proyecto nuevo se guarda |
| Importar como proyecto nuevo y repetir | Copias independientes; no sobrescribe proyectos anteriores |
| Elegir archivo corrupto, vacío, versión no compatible o más de 256 KB | Aviso de error; no se crea un proyecto parcial |
| Borrar proyecto durante una exportación o denegar acceso al archivo | Error recuperable, sin cierre de la app |
| Compartir una foto del detalle | Intent de imagen con permiso temporal; fotos del proyecto fuera del JSON |
| Ajustes y recordatorios con notificaciones denegadas | Se conserva el comportamiento validado de la entrega anterior |
| Cámara, nivel y escaneo en teléfono/emulador | Se conserva el comportamiento existente; sin prueba visual declarada en esta sesión |

Las pruebas instrumentadas de Room necesitan un dispositivo; compilar su APK no equivale a ejecutarlas. No se atribuye una prueba en teléfono o emulador Android a esta sesión.

## Límites y publicación

- Esta es la alternativa local elegida por la persona. No cumple literalmente la A6 original que pedía login/Firestore. No hay sincronización automática ni control de cuentas; se comparten copias mediante archivos.
- La publicación de la rama y del JSON real de plantillas sigue pendiente del acceso de escritura GitHub. La copia de plantillas en assets funciona sin red.
- No se solicita ni configura Firebase, Supabase, claves de backend ni un plan de pago.
