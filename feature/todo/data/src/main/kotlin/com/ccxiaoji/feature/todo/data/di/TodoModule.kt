package com.ccxiaoji.feature.todo.data.di

import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.data.TodoApiImpl
import com.ccxiaoji.feature.todo.data.repository.TaskRepository
import com.ccxiaoji.core.database.dao.TaskDao
import com.ccxiaoji.core.database.dao.ChangeLogDao
import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TodoModule {
    
    @Binds
    abstract fun bindTodoApi(impl: TodoApiImpl): TodoApi
}

@Module
@InstallIn(SingletonComponent::class)
object TodoDataModule {
    
    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        changeLogDao: ChangeLogDao,
        gson: Gson
    ): TaskRepository {
        return TaskRepository(taskDao, changeLogDao, gson)
    }
}

/**
 * Todo模块的桥接接口实现
 * 这些接口需要在app模块中提供具体实现
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TodoBridgeModule {
    
    // TodoNavigator需要在app模块中实现
    // TodoNotificationScheduler需要在app模块中实现
}