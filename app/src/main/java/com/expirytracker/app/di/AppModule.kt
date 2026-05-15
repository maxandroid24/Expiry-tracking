package com.expirytracker.app.di

import com.expirytracker.app.data.backup.JsonBackupRepository
import com.expirytracker.app.data.ocr.GeminiProductParser
import com.expirytracker.app.data.ocr.MlKitTextExtractor
import com.expirytracker.app.data.ocr.OnlineProductParser
import com.expirytracker.app.data.ocr.TextExtractor
import com.expirytracker.app.data.repository.DefaultExtractionRepository
import com.expirytracker.app.data.repository.LocalImageStorage
import com.expirytracker.app.data.repository.SharedPreferencesSettingsRepository
import com.expirytracker.app.data.repository.SqliteProductRepository
import com.expirytracker.app.domain.repository.BackupRepository
import com.expirytracker.app.domain.repository.ExtractionRepository
import com.expirytracker.app.domain.repository.ImageStorage
import com.expirytracker.app.domain.repository.ProductRepository
import com.expirytracker.app.domain.repository.ReminderScheduler
import com.expirytracker.app.domain.repository.SettingsRepository
import com.expirytracker.app.domain.usecase.ValidateProductUseCase
import com.expirytracker.app.worker.WorkManagerReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindings {
    @Binds @Singleton abstract fun bindProductRepository(implementation: SqliteProductRepository): ProductRepository
    @Binds @Singleton abstract fun bindSettingsRepository(implementation: SharedPreferencesSettingsRepository): SettingsRepository
    @Binds @Singleton abstract fun bindExtractionRepository(implementation: DefaultExtractionRepository): ExtractionRepository
    @Binds @Singleton abstract fun bindTextExtractor(implementation: MlKitTextExtractor): TextExtractor
    @Binds @Singleton abstract fun bindOnlineProductParser(implementation: GeminiProductParser): OnlineProductParser
    @Binds @Singleton abstract fun bindReminderScheduler(implementation: WorkManagerReminderScheduler): ReminderScheduler
    @Binds @Singleton abstract fun bindImageStorage(implementation: LocalImageStorage): ImageStorage
    @Binds @Singleton abstract fun bindBackupRepository(implementation: JsonBackupRepository): BackupRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @Singleton fun provideValidateProductUseCase(): ValidateProductUseCase = ValidateProductUseCase()
}
