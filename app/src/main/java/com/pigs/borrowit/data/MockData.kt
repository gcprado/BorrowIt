package com.pigs.borrowit.data

import com.pigs.borrowit.data.models.BorrowItem
import com.pigs.borrowit.data.models.HistoryTransaction
import com.pigs.borrowit.data.models.InteractionType
import com.pigs.borrowit.data.models.ItemStatus

object MockData {
    
    val myItems = listOf(
        BorrowItem(
            id = 1,
            name = "Taladro Eléctrico Bosch",
            description = "Taladro profesional en excelente estado, incluye 2 baterías y cargador",
            status = ItemStatus.BORROWED,
            condition = "Excelente",
            availableFrom = "01/04/2026",
            availableUntil = "30/06/2026",
            category = "Herramientas",
            borrowerId = 2,
            borrowerName = "María García"
        ),
        BorrowItem(
            id = 2,
            name = "Libro: Clean Code",
            description = "Libro de programación sobre buenas prácticas de código",
            status = ItemStatus.AVAILABLE,
            condition = "Muy bueno",
            availableFrom = "01/04/2026",
            availableUntil = "31/12/2026",
            category = "Libros"
        ),
        BorrowItem(
            id = 3,
            name = "Cafetera Nespresso",
            description = "Cafetera automática con espumador de leche",
            status = ItemStatus.AVAILABLE,
            condition = "Bueno",
            availableFrom = "15/04/2026",
            availableUntil = "31/08/2026",
            category = "Electrodomésticos"
        ),
        BorrowItem(
            id = 4,
            name = "Bicicleta de montaña",
            description = "Bicicleta Trek con 21 velocidades, perfecta para rutas",
            status = ItemStatus.UNAVAILABLE,
            condition = "Excelente",
            availableFrom = "01/05/2026",
            availableUntil = "30/09/2026",
            category = "Deportes"
        )
    )
    
    val borrowedItems = listOf(
        BorrowItem(
            id = 5,
            name = "Cámara Canon EOS",
            description = "Cámara réflex profesional con lente 50mm",
            status = ItemStatus.IN_USE,
            condition = "Excelente",
            availableFrom = "01/04/2026",
            availableUntil = "15/04/2026",
            category = "Fotografía",
            ownerId = 3,
            ownerName = "Carlos López"
        ),
        BorrowItem(
            id = 6,
            name = "Tienda de campaña",
            description = "Tienda para 4 personas, impermeable",
            status = ItemStatus.IN_USE,
            condition = "Muy bueno",
            availableFrom = "05/04/2026",
            availableUntil = "20/04/2026",
            category = "Camping",
            ownerId = 4,
            ownerName = "Ana Martínez"
        )
    )
    
    val historyTransactions = listOf(
        HistoryTransaction(
            id = 1,
            itemName = "Escalera plegable",
            interactionType = InteractionType.LENT,
            date = "15/03/2026",
            userName = "Pedro Sánchez"
        ),
        HistoryTransaction(
            id = 2,
            itemName = "Proyector Epson",
            interactionType = InteractionType.BORROWED,
            date = "10/03/2026",
            userName = "Laura Fernández"
        ),
        HistoryTransaction(
            id = 3,
            itemName = "Cortacésped",
            interactionType = InteractionType.LENT,
            date = "05/03/2026",
            userName = "Miguel Torres"
        ),
        HistoryTransaction(
            id = 4,
            itemName = "Maleta grande",
            interactionType = InteractionType.BORROWED,
            date = "28/02/2026",
            userName = "Isabel Ruiz"
        ),
        HistoryTransaction(
            id = 5,
            itemName = "Juego de herramientas",
            interactionType = InteractionType.LENT,
            date = "20/02/2026",
            userName = "Roberto Díaz"
        ),
        HistoryTransaction(
            id = 6,
            itemName = "Barbacoa portátil",
            interactionType = InteractionType.BORROWED,
            date = "15/02/2026",
            userName = "Carmen Moreno"
        )
    )
}
