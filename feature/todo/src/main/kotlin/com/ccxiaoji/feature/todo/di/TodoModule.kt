package com.ccxiaoji.feature.todo.di

import com.ccxiaoji.feature.todo.api.TodoApi
import com.ccxiaoji.feature.todo.data.TodoApiImpl
import com.ccxiaoji.feature.todo.data.repository.TodoRepositoryImpl
import com.ccxiaoji.feature.todo.domain.repository.TodoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 待办事项模块的依赖注入配置
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TodoModule {
    
    @Binds
    @Singleton
    abstract fun bindTodoApi(impl: TodoApiImpl): TodoApi
    
    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository
}