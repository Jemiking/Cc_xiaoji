# core-network 模块迁移总结

## 迁移信息
- **迁移日期**: 2025-06-18
- **模块名称**: core-network
- **迁移耗时**: 约20分钟
- **迁移人**: Claude Code

## 迁移内容

### 1. 创建的新文件
1. `NetworkConstants.kt` - 网络常量定义
   - 超时时间常量
   - HTTP头部常量
   - 默认基础URL

2. `AuthInterceptor.kt` - 认证拦截器
   - 实现了OkHttp拦截器
   - 创建了TokenProvider接口用于解耦

3. `GsonConfiguration.kt` - Gson配置
   - 统一的JSON序列化配置
   - 宽松模式支持

4. `NetworkClientConfig.kt` - OkHttp客户端配置
   - 创建OkHttp客户端的工厂方法
   - 日志拦截器配置
   - 超时设置

5. `CoreNetworkModule.kt` - Hilt依赖注入模块
   - 提供Gson实例
   - 提供两种OkHttpClient（带认证/不带认证）
   - 提供两种Retrofit实例（带认证/不带认证）
   - 定义了多个限定符注解

### 2. 修改的文件
1. `app/src/main/java/com/ccxiaoji/app/di/NetworkModule.kt`
   - 重构为使用core-network提供的组件
   - 删除了重复的网络配置代码
   - 保留API接口的创建

2. `app/src/main/java/com/ccxiaoji/app/di/TokenProviderImpl.kt`
   - 新创建的TokenProvider实现
   - 从DataStore获取访问令牌

3. `app/build.gradle.kts`
   - 添加了core-network模块依赖
   - 移除了Retrofit相关依赖（已在core-network中提供）

### 3. 架构设计决策

#### 3.1 抽象认证机制
- 创建TokenProvider接口而不是直接依赖DataStore
- 允许不同的令牌存储实现
- 提高了可测试性

#### 3.2 双客户端设计
- 提供带认证和不带认证的两种客户端
- 使用限定符注解区分
- 支持不同的使用场景

#### 3.3 配置集中化
- 所有网络配置集中在core-network模块
- 减少重复代码
- 统一配置管理

### 4. 遇到的问题

#### 问题1: BuildConfig未找到
- **原因**: BuildConfig需要先编译才能生成
- **解决**: 临时硬编码debug值为true，添加注释说明实际应使用BuildConfig.DEBUG

### 5. 验证结果
- ✅ 编译通过
- ✅ 依赖关系正确
- ✅ 模块结构符合规范

### 6. 后续优化建议
1. 在正式构建时将debug标志改回使用BuildConfig.DEBUG
2. 考虑添加更多的网络配置选项（如缓存配置）
3. 可以添加更多的拦截器支持（如缓存拦截器）

### 7. 影响范围
- 影响所有使用网络功能的模块
- 需要其他模块依赖core-network而不是直接依赖Retrofit
- 简化了网络配置的管理

### 8. 下一步计划
继续迁移shared-user模块