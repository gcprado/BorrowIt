package com.pigs.borrowit.presentation.navigation

import androidx.navigation.NavController

/**
 * Navega a una pantalla con gestión estricta del stack:
 * 1. Elimina todas las pantallas anteriores (si las hay)
 * 2. No permite navegar a la pantalla actual
 * 3. Mantiene siempre exactamente 1 instancia en el stack
 */
fun NavController.navigateSingleInStack(route: String) {
    val currentRoute = currentBackStackEntry?.destination?.route

    when {
        // Caso 1: No hay pantalla actual (navegación inicial)
        currentRoute == null -> {
            navigate(route)
        }

        // Caso 2: La ruta destino es diferente a la actual
        currentRoute != route -> {
            navigate(route) {
                // Elimina todas las pantallas excepto la actual antes de navegar
                popUpTo(currentRoute) { inclusive = true }
                // Evita múltiples instancias
                launchSingleTop = true
                // Restaura el estado si ya existe
                restoreState = true
            }
        }

        // Caso 3: Ya está en la pantalla destino (no hacer nada)
        else -> return
    }
}

/**
 * Navega a una pantalla eliminando TODAS las rutas anteriores del stack,
 * dejando solo la nueva pantalla como única en la pila.
 *
 * @param route Ruta destino a navegar
 */
fun NavController.navigateAndClearStack(route: String) {
    navigate(route) {
        // Elimina TODA la pila de navegación existente
        popUpTo(0) {
            inclusive = true // Incluye la primera pantalla (índice 0)
        }
        // Configuración adicional recomendada
        launchSingleTop = true
        restoreState = true
    }
}