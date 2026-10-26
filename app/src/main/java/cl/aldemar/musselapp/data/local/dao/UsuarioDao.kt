package cl.aldemar.musselapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.aldemar.musselapp.data.local.entidad.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(usuarios: List<UsuarioEntity>)

    /** [rut] normalizado: solo dígitos y K. */
    @Query("SELECT * FROM usuarios WHERE rut = :rut")
    suspend fun buscarPorRut(rut: String): UsuarioEntity?
}
