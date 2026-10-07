package cl.aldemar.musselapp.data.model

/** Centro de cultivo de choritos donde trabaja el usuario (datos ficticios). */
data class CentroCultivo(
    val id: String,
    val nombre: String,
) {
    companion object {
        val disponibles = listOf(
            CentroCultivo("dalcahue", "Centro Canal Dalcahue - Mód. A (Chiloé Central)"),
            CentroCultivo("calbuco", "Centro Calbuco - Isla Huapi (Seno de Reloncaví)"),
            CentroCultivo("quellon", "Centro Quellón Sur - Ensenada Yaldad"),
            CentroCultivo("melinka", "Centro Melinka - Fiordos de las Guaitecas"),
        )
    }
}
