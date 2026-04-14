# Especificación funcional detallada

## Función principal
BioProtect debe permitir seleccionar apps del móvil para protegerlas.
Al abrir una app protegida, BioProtect debe mostrar una pantalla propia mínima para autenticar al usuario.

## Flujo de protección
- Monitoriza la app en primer plano
- Si detecta una app protegida:
  - muestra pantalla propia simple
  - intenta autenticación con huella
  - si huella no está disponible o no puede usarse, permite usar PIN propio de BioProtect
  - si la autenticación tiene éxito, deja pasar a la app
  - si se cancela o falla, manda al escritorio

## Sin ventana de gracia
No debe existir tiempo de gracia.
Si el usuario sale de una app protegida y luego vuelve, incluso desde recientes, debe pedir autenticación de nuevo.

## Protección de la propia app
BioProtect debe protegerse a sí misma.

## Apps del sistema
Se pueden ocultar para simplificar la lista.

## Permisos / requisitos
Debe guiar al usuario a conceder, como mínimo:
- Accesibilidad
- Acceso al uso
- Mostrar sobre otras aplicaciones
- Batería sin restricciones
- Autoinicio en segundo plano
- Otros permisos útiles de HyperOS si aplica
- Administrador del dispositivo

El interruptor principal debe estar deshabilitado mientras falten permisos esenciales.

## Administrador del dispositivo
Pedido por el usuario para:
- dificultar desinstalación o desactivación
- ayudar a que siga activa/protegida

## Segundo plano
Usar un servicio en primer plano con notificación persistente discreta cuando la protección esté activa.

## Reinicio del dispositivo
Tras reiniciar:
- intentar arrancar automáticamente
- restaurar apps protegidas
- volver a proteger

## UI principal
1. Estado general
2. Permisos requeridos con botones directos
3. Interruptor rojo/verde/gris
4. Buscador
5. Filtros: Todas / Protegidas / No protegidas
6. Lista de apps
7. Botón copiar logs

## Lista de apps
Mostrar:
- icono
- nombre visible

## Persistencia de selección
La selección de apps protegidas debe guardarse al instante.

## PIN propio de BioProtect
- Debe poder cambiarse desde la app
- Longitud no fijada por el usuario; recomendación: 6 dígitos si no se indica lo contrario

## Diseño
- Idioma español
- diseño simple y funcional
- icono: mezcla de huella + escudo

## Nombre visible
- BioProtect

## Logs
Copiar al portapapeles la mayor cantidad de información útil posible:
- versión de la app
- versión Android / HyperOS
- estado de permisos
- estado del servicio de accesibilidad
- estado del foreground service
- app detectada en primer plano
- lista de apps protegidas
- últimos eventos de bloqueo/autenticación/error
