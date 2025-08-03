package com.ccxiaoji.app.di

import com.ccxiaoji.app.data.backup.export.*
import com.ccxiaoji.app.data.backup.importer.*
import com.ccxiaoji.app.data.backup.manager.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 备份模块依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {
    
    @Binds
    @Singleton
    abstract fun bindBackupManager(
        backupManagerImpl: BackupManagerImpl
    ): BackupManager
    
    @Binds
    @Singleton
    abstract fun bindCsvExporter(
        csvExporterImpl: CsvExporterImpl
    ): CsvExporter
    
    @Binds
    @Singleton
    abstract fun bindJsonExporter(
        jsonExporterImpl: JsonExporterImpl
    ): JsonExporter
    
    @Binds
    @Singleton
    abstract fun bindZipPackager(
        zipPackagerImpl: ZipPackagerImpl
    ): ZipPackager
    
    @Binds
    @Singleton
    abstract fun bindCsvImporter(
        csvImporterImpl: CsvImporterImpl
    ): CsvImporter
    
    @Binds
    @Singleton
    abstract fun bindJsonImporter(
        jsonImporterImpl: JsonImporterImpl
    ): JsonImporter
    
    @Binds
    @Singleton
    abstract fun bindZipExtractor(
        zipExtractorImpl: ZipExtractorImpl
    ): ZipExtractor
}