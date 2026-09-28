package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities =
    [
      BusinessProfileEntity::class,
      MetalRateEntity::class,
      CustomerEntity::class,
      TransactionEntity::class,
      InvoiceEntity::class,
      PendingSyncQueueEntity::class,
      PendingDriveUploadEntity::class,
      GoogleAccountConfigEntity::class,
      OpeningStockEntity::class,
      InventoryMovementEntity::class,
      InventoryReconciliationEntity::class,
      VendorEntity::class,
      PurchaseEntity::class,
      SaleEntity::class,
      ScrapProcessingEntity::class,
      SecuritySettingsEntity::class,
      AuditLogEntity::class,
      ReminderEntity::class,
    ],
  version = 7,
  exportSchema = false,
)
abstract class JewelleryDatabase : RoomDatabase() {
  abstract fun jewelleryDao(): JewelleryDao

  companion object {
    @Volatile private var INSTANCE: JewelleryDatabase? = null

    fun getInstance(context: Context): JewelleryDatabase {
      return INSTANCE
        ?: synchronized(this) {
          val instance =
            Room.databaseBuilder(
                context.applicationContext,
                JewelleryDatabase::class.java,
                "jewellery_business_manager.db",
              )
              .fallbackToDestructiveMigration(dropAllTables = true)
              .build()
          INSTANCE = instance
          instance
        }
    }
  }
}
