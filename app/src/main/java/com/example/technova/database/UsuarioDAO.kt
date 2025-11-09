package com.example.technova.database

import android.content.ContentValues
import android.content.Context
import com.example.technova.models.Usuario
import com.example.technova.utils.PasswordHelper

class UsuarioDAO(context: Context) {
    // Instancia del DatabaseHelper para acceder a la base de datos
    private val dbHelper = DatabaseHelper(context)

    //Funcion de registro con return true / false
    fun registrarUsuario(usuario: Usuario): Boolean {
        //abrir en modo de escritura
        val db = dbHelper.writableDatabase
        //Encriptar la contraseña
        val contrasenaHashed = PasswordHelper.hashPassword(usuario.contrasena)
        //Contenedor clave - valor
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NOMBRE, usuario.nombre)
            put(DatabaseHelper.COLUMN_CORREO, usuario.correo)
            put(DatabaseHelper.COLUMN_CONTRASENA, contrasenaHashed)
            put(DatabaseHelper.COLUMN_ES_ADMIN, 0)
        }
        //Insertar en la base de datos
        // devuelve -1 si no se inserto o el id si se inserto
        val newRowId = db.insert(DatabaseHelper.TABLE_USUARIOS, null, values)
        return newRowId != -1L
    }
    //Validar login
    fun validarLogin(correo: String, contrasena: String): Boolean {
        //Obtener el usuario por su correo electrónico
        val usuario = obtenerUsuarioPorCorreo(correo)?: return false
        //Validar la contraseña
        val contrasenaHashed = PasswordHelper.hashPassword(contrasena)
        if (contrasenaHashed != usuario.contrasena) {
            return false
        }
        return true
    }

    // Obtener el usuario por su correo electrónico
    fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        val db = dbHelper.readableDatabase
        var usuario: Usuario? = null

        // Generar query
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COLUMN_CORREO} = ?"
        val cursor = db.rawQuery(query, arrayOf(correo))

        // Recorrer el cursor
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
            val nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE))
            val correo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CORREO))
            val contrasena = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CONTRASENA))
            val esAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ES_ADMIN)) == 1

            usuario = Usuario(id, nombre, correo, contrasena, esAdmin)
        }

        cursor.close()
        db.close()
        return usuario
    }
    //Validar el correo si existe
    fun validarCorreo(correo: String): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COLUMN_CORREO} = ?"
        val cursor = db.rawQuery(query, arrayOf(correo))
        val existeCorreo = cursor.count > 0
        cursor.close()
        db.close()
        return existeCorreo
    }

    //Eliminar usuario
    fun eliminarUsuario(correo: String): Boolean {
        val db = dbHelper.writableDatabase
        val resultado = db.delete(DatabaseHelper.TABLE_USUARIOS, "${DatabaseHelper.COLUMN_CORREO} = ?", arrayOf(correo))
        db.close()
        return resultado > 0
    }
    //Actualizar usuario
    fun actualizarUsuario(usuario: Usuario, originalCorreo: String): Boolean {
        val db = dbHelper.writableDatabase

        // Sólo hasheamos y actualizamos la contraseña si nos pasan una no-vacía.
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NOMBRE, usuario.nombre)
            put(DatabaseHelper.COLUMN_CORREO, usuario.correo)
            if (usuario.contrasena.isNotEmpty()) {
                val contrasenaHashed = PasswordHelper.hashPassword(usuario.contrasena)
                put(DatabaseHelper.COLUMN_CONTRASENA, contrasenaHashed)
            }
        }

        val resultado = db.update(
            DatabaseHelper.TABLE_USUARIOS,
            values,
            "${DatabaseHelper.COLUMN_CORREO} = ?",
            arrayOf(originalCorreo)
        )
        db.close()
        return resultado > 0
    }


    // Registrar admin
    fun registrarAdmin(usuario: Usuario): Boolean {
        val db = dbHelper.writableDatabase
        val contrasenaHashed = PasswordHelper.hashPassword(usuario.contrasena)
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_NOMBRE, usuario.nombre)
            put(DatabaseHelper.COLUMN_CORREO, usuario.correo)
            put(DatabaseHelper.COLUMN_CONTRASENA, contrasenaHashed)
            put(DatabaseHelper.COLUMN_ES_ADMIN, 1)
        }
        val newRowId = db.insert(DatabaseHelper.TABLE_USUARIOS, null, values)
        db.close()
        return newRowId != -1L
    }

    // Verifica si ya existe al menos un usuario admin
    fun existeAdmin(): Boolean {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) as cnt FROM ${DatabaseHelper.TABLE_USUARIOS} WHERE ${DatabaseHelper.COLUMN_ES_ADMIN} = 1"
        val cursor = db.rawQuery(query, null)
        var existe = false
        if (cursor.moveToFirst()) {
            val count = cursor.getInt(cursor.getColumnIndexOrThrow("cnt"))
            existe = count > 0
        }
        cursor.close()
        db.close()
        return existe
    }

}