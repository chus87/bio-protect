# Decisiones cerradas por el usuario

- La app será en español
- El nombre visible final será BioProtect
- Diseño simple y funcional
- El icono será mezcla de huella + escudo
- La protección debe pedir autenticación siempre
- No habrá ventana de gracia
- Si una app protegida vuelve desde recientes, debe pedir autenticación otra vez
- Tras reinicio, la app debe intentar arrancar automáticamente y seguir protegiendo
- Habrá buscador
- Habrá filtros: Todas / Protegidas / No protegidas
- No hace falta “Seleccionar todas”
- Orden de apps: protegidas arriba y luego alfabético
- BioProtect debe poder bloquearse a sí misma
- Se pueden ocultar apps del sistema
- El usuario quiere permisos de administrador para dificultar desinstalación y desactivación
- Guardado local
- Si no puede ser con huella, usar PIN propio de BioProtect
- Logs: copiar al portapapeles con todo lo útil
- Notificación persistente: sí
- APK de salida deseada: debug

## Flujo de fallo confirmado
Cuando falle o se cancele la autenticación:
1. retirar la pantalla propia
2. mandar al escritorio
3. la app protegida no debe quedar utilizable

## Interruptor principal
- Deshabilitado hasta tener permisos necesarios
- Verde: protegiendo
- Rojo: no protegiendo
- Gris: faltan permisos

## Instalación
Lo más automática posible dentro de Android/HyperOS.

## Aceptación explícita del límite técnico
El usuario acepta que Android/HyperOS no ofrece una API pública perfecta para bloquear cualquier app al 100% como función de sistema.
