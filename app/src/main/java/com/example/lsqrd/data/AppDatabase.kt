package com.example.lsqrd.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [Vault::class, Credential::class, CredentialField::class, VaultDefaultCredentialField::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vaultDao(): VaultDao
    abstract fun credentialDao(): CredentialDao
    abstract fun credentialFieldDao(): CredentialFieldDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private const val DB_NAME = "lsqrd_db"

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXIST vault_default_credential_fields (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        vaultId INTEGER NOT NULL,
                        label TEXT NOT NULL,
                        value TEXT NOT NULL DEFAULT '',
                        isSecret INTEGER NOT NULL DEFAULT 0,
                        position INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(vaultId) REFERENCES vaults(id) ON DELETE CASCADE
                    )
                """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_vault_default_credential_fields_vaultId " + "ON vault_default_credential_fields(vaultId)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext

                if (!DatabaseKeyManager.isMigrated(appContext)) {
                    appContext.deleteDatabase(DB_NAME)
                    DatabaseKeyManager.markMigrated(appContext)
                }

                val passPhrase = DatabaseKeyManager.getPassPhrase(appContext)
                val factory = SupportFactory(passPhrase)
                passPhrase.fill(0)

                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, DB_NAME
                ).openHelperFactory(factory).addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration(false).build()

                INSTANCE = instance
                instance
            }
        }
    }
}