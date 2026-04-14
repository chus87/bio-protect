# BioProtect

**BioProtect** es una app Android diseñada para añadir una capa real de privacidad a las aplicaciones que más importa proteger.

Fotos, correo, mensajería, archivos, nube, documentos personales.  
Con BioProtect, puedes elegir qué apps quieres bloquear y exigir **autenticación biométrica** cada vez que alguien intente abrirlas.

Su objetivo es simple: que aunque otra persona tenga el móvil en la mano, **no pueda entrar en tus apps sensibles sin tu huella**.

## Qué hace

BioProtect permite:

- proteger apps concretas con **huella / biometría**
- pedir autenticación al abrir una app protegida
- pedir autenticación también al entrar en la propia app **BioProtect**
- marcar rápidamente un grupo de **apps recomendadas**
- organizar la lista mostrando primero las apps protegidas
- mantener una configuración clara y rápida de usar

## Para qué sirve

BioProtect está pensada para situaciones muy reales:

- cuando dejas el móvil un momento a otra persona
- cuando quieres proteger tus fotos, mensajes o correo
- cuando compartes dispositivo en casa o en el trabajo
- cuando buscas una capa extra de privacidad sin complicarte con ajustes complejos

No intenta reemplazar la seguridad del sistema.  
Añade una barrera práctica y directa sobre las apps que tú decidas.

## Apps que suele tener sentido proteger

BioProtect es especialmente útil para bloquear acceso a apps como:

- **Galería**
- **Google Fotos**
- **Gmail**
- **WhatsApp**
- **Telegram**
- **Google Drive**
- **Mensajes**
- **Archivos / Files**

La app incluye un botón para marcar o desmarcar rápidamente este grupo de apps recomendadas.

## Enfoque de la app

BioProtect está construida con una idea muy clara:

- experiencia sencilla
- enfoque directo
- autenticación **solo biométrica**
- sin PIN alternativo
- pensada para abrir, configurar y usar sin vueltas

## Dispositivo de referencia

La app se ha trabajado y validado especialmente sobre:

- **POCO F6 Pro**
- **HyperOS 2.0.210.0.VNKEUXM**
- **Android 15**

Aun así, el comportamiento puede variar según fabricante, versión de Android y capa del sistema.

## Requisitos de funcionamiento

Para que BioProtect funcione correctamente, Android puede requerir que el usuario revise ciertos permisos o ajustes del sistema, según el dispositivo:

1. **Administrador del dispositivo**
2. **Ajustes restringidos**
3. **Accesibilidad**
4. **Autoinicio en segundo plano**

La propia app guía estos pasos desde su interfaz.

## Tecnología

- **Kotlin**
- **Android SDK**
- **Material Components**
- **GitHub Actions** para compilación de APK debug

## Compilación local

### Requisitos

- Android Studio reciente
- JDK 17
- Android SDK 35
- Build Tools 35.0.0

### Build debug

```bash
./gradlew :app:assembleDebug
```

APK generada en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions

El repositorio incluye un workflow para compilar la APK debug automáticamente.

Archivo:

```text
.github/workflows/android-debug.yml
```

## Licencia

Este proyecto se distribuye bajo licencia **MIT**.
