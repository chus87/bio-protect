# Resumen del encargo

## Nombre
**BioProtect**  
Repositorio sugerido: `bioprotect-fingerprint`  
Paquete Android sugerido: `com.bioprotect.fingerprint`

## Objetivo
App Android para bloquear apps del teléfono y exigir huella al abrirlas.

## Plataforma objetivo
- Dispositivo principal: POCO F6 Pro
- Sistema: Xiaomi HyperOS 2.0.210.0.VNKEUXM
- Android: 15
- Idioma deseado: español

## Requisitos principales
- Listar apps instaladas
- Elegir qué apps proteger
- Guardar la selección al instante
- Interruptor principal:
  - verde = protección activa
  - rojo = protección inactiva
  - gris/deshabilitado = faltan permisos
- Debe funcionar también si la app protegida vuelve desde recientes
- Sin ventana de gracia
- Debe seguir protegiendo en segundo plano
- Debe reactivarse tras reinicio
- Botón de logs: copia logs útiles al portapapeles

## Autenticación
- Prioridad: huella
- Si huella no puede usarse: PIN propio de BioProtect
- Si falla o se cancela: volver al escritorio
- Intentar evitar depender del PIN/patrón del sistema

## UI
- Español
- Simple y funcional
- Lista con protegidas arriba y luego orden alfabético
- Buscador
- Filtros: Todas / Protegidas / No protegidas
- Sin botón “Seleccionar todas”

## Persistencia
- Almacenamiento local

## Realismo técnico
El usuario acepta la limitación de Android/HyperOS:
no existe una API pública perfecta para bloquear cualquier app como función de sistema.
