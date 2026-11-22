# Comprobaciones de LevelMate

Fecha: 22/11/2026. Las comprobaciones automáticas las ejecutó Codex. No se atribuyen pruebas manuales a la persona.

## Resultados automáticos

- **21 pruebas unitarias JVM: correctas**, incluyendo fechas imposibles y bisiestas, validación de ajustes, tolerancia, cambio de hora de Madrid, payload compartido sin rutas de fotos, datos corruptos y las seis pruebas de plantillas. Una de las pruebas de plantillas realiza una petición HTTP local con Retrofit y comprueba el User-Agent y el parser con campos desconocidos.
- **10 comprobaciones de reglas de Firestore: correctas** contra el emulador local, proyecto `demo-levelmate`. Comprueban creación, lectura por participantes, denegación a terceros y anónimos, denegación de escritura y borrado por lectores, propietario inmutable, campos de fotos rechazados y nombre inválido.
- **Compilación APK debug: correcta**. Se incluye un APK de prueba; no es una release de Play ni una entrega C2.
- **Lint debug: correcto, 0 errores y 14 avisos no bloqueantes.** Se conservan las versiones solicitadas, aunque lint sugiera actualizaciones. **Compilación del APK de pruebas instrumentadas: correcta.** Su ejecución en un dispositivo sigue pendiente.
- XML y JSON válidos; catálogo publicado y grabado idénticos. Catálogo de versiones, Gradle wrapper, plugins raíz, settings.gradle.kts y HttpClient.kt iguales a los del repositorio de partida. El apartado original Facts del README se conserva byte por byte.

El entorno empezó sin SDK y sin Gradle descargado. Se instaló el SDK 37.0 y se utilizó la JVM 25 declarada por el repositorio. Para la red de este entorno se utilizó su proxy y el almacén de certificados del sistema, manteniendo la verificación TLS. Las peticiones de Gradle de JUnit/Hamcrest recibieron 403; se preparó una caché Maven temporal con los mismos artefactos oficiales JUnit 4.13.2 y Hamcrest 1.3 recuperados mediante curl. Se aplicó sólo mediante un init script externo al repositorio. No cambia las versiones o repositorios de la app entregada. Lint detectó que faltaba declarar la cámara como hardware opcional; se corrigió con uses-feature required=false y se volvió a ejecutar correctamente. El nombre del lanzador se ajustó a LevelMate manteniendo el paquete original.

## Pruebas instrumentadas de Room

`ProjectDatabaseTest.kt` contiene cinco pruebas: base vacía/proyecto ausente, cascada y fotos conservadas, clave foránea inválida, operación inválida sin proyecto parcial y exclusión de proyectos terminados de los recordatorios.

La ejecución instrumentada requiere un dispositivo o emulador Android; **no se han ejecutado** pruebas en un dispositivo en esta sesión. Tampoco se ha afirmado una prueba visual de cámara, intents o notificaciones.

En un equipo con Android Studio y un dispositivo conectado:

```sh
bash gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
bash gradlew :app:connectedDebugAndroidTest
firebase emulators:exec --only firestore --project demo-levelmate "node scripts/check-firestore-rules.mjs"
```

## Recorrido manual pendiente

| Fila | Acción | Resultado esperado |
|---|---|---|
| A2/A4 | Abrir Projects con datos borrados, crear un proyecto y volver a abrir la app | Mensaje de lista vacía al principio; proyecto persistente después |
| A2/A4 | Nombre vacío, más de 40 caracteres o fecha 31/02/2026; rotar con campos válidos | No permite guardar entradas inválidas; campos y operación de guardado sobreviven a la rotación |
| A4 | Añadir materiales con unidades, pasos, marcar uno y borrar el proyecto | Materiales/pasos se eliminan; fotos pasan a generales y sus archivos permanecen |
| A3/A9 | Guardar 01:30 y 2,0 grados, cerrar/abrir; probar 25:70 o NaN | Ajustes válidos persisten y se aplican al nivel; inválidos impiden guardar |
| A5 | Modo grabado, modo avión, Load templates y Use template | Crea un proyecto con sus materiales/pasos sin necesitar red |
| A5 | Publicar la rama, poner USE_RECORDED_TEMPLATES=false y cargar; repetir sin red | Lee el JSON remoto; sin red muestra aviso y la copia grabada |
| A2/A8 | Tomar dos fotos seguidas y compartir una en una app receptora | Dos archivos distintos; receptor puede abrir content:// con permiso temporal |
| A2/A8 | Eliminar el archivo de una foto en Device Explorer | Aparece Photo file is missing, compartir deshabilitado y sin cierre de la app |
| A8/A9 | Denegar cámara y volver desde ajustes tras permitirla; poner la app en segundo plano | Se actualiza el permiso al volver; el sensor se retira al pausar |
| A7 | Proyecto con fecha de hoy y un paso pendiente; activar y guardar hora próxima | Un trabajo periódico único, permiso de notificaciones y notificación aproximada con apertura de la app |
| A7 | Denegar notificaciones o completar todos los pasos; cambiar hora y guardar varias veces | Sin cierres ni notificación indebida; sin trabajos duplicados |
| A6 | Terminar Firebase Spark, entrar con dos cuentas, compartir el UID del segundo usuario | El segundo ve la copia; sólo el propietario actualiza o retira; importar crea una copia local |
| A6 | Cambiar de usuario y probar modo avión | No se muestran datos de la sesión anterior; caché identificada y errores de red recuperables |
| A6 | Abrir Shared sin configuración Firebase | Aviso de función sin configurar; las funciones locales siguen disponibles |

## Pendientes externos

1. **Firebase no está provisionado.** Falta acceso a la cuenta para crear el proyecto Spark, registrar Android, descargar app/google-services.json, habilitar Email/Password y publicar las reglas de producción. No se ha activado Blaze ni facturación.
2. **GitHub rechazó crear la rama** `codex/levelmate-complete` con `403 Resource not accessible by integration`. El repositorio público se pudo clonar y actualizar, pero esta conexión no puede escribir en él. Se entrega un bundle con los commits y un parche; no se afirma un push o PR inexistente.
3. **La URL real de plantillas necesita publicar la rama.** La copia grabada sí funciona y el cliente Retrofit se ha comprobado con HTTP local. Tras subir la rama, la ruta del README sirve el JSON; si se cambia la rama o se elimina, hay que actualizar la URL.
