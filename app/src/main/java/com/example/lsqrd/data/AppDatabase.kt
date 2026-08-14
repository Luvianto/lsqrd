package com.example.lsqrd.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        Vault::class,
        Credential::class,
        CredentialField::class
    ],
    version = 2,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext

                if(!DatabaseKeyManager.isMigrated(appContext)){
                    appContext.deleteDatabase(DB_NAME)
                    DatabaseKeyManager.markMigrated(appContext)
                }

                val passPhrase = DatabaseKeyManager.getPassPhrase(appContext)
                val factory = SupportFactory(passPhrase)
                passPhrase.fill(0)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration(false)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}