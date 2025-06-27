#!/bin/bash
# 自动迁移模块到版本目录的脚本

echo "🚀 开始迁移到版本目录..."

# 定义版本映射
declare -A version_mappings=(
    # AndroidX
    ["androidx.core:core-ktx:1.12.0"]="libs.androidx.core.ktx"
    ["androidx.lifecycle:lifecycle-runtime-ktx:2.7.0"]="libs.androidx.lifecycle.runtime.ktx"
    ["androidx.lifecycle:lifecycle-runtime-compose:2.7.0"]="libs.androidx.lifecycle.runtime.compose"
    ["androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0"]="libs.androidx.lifecycle.viewmodel.compose"
    ["androidx.activity:activity-compose:1.8.2"]="libs.androidx.activity.compose"
    ["androidx.navigation:navigation-compose:2.7.6"]="libs.androidx.navigation.compose"
    ["androidx.datastore:datastore-preferences:1.0.0"]="libs.androidx.datastore"
    ["androidx.work:work-runtime-ktx:2.9.0"]="libs.androidx.work"
    
    # Compose
    ["androidx.compose.ui:ui"]="libs.compose.ui"
    ["androidx.compose.ui:ui-graphics"]="libs.compose.ui.graphics"
    ["androidx.compose.ui:ui-tooling-preview"]="libs.compose.ui.tooling.preview"
    ["androidx.compose.material3:material3"]="libs.compose.material3"
    ["androidx.compose.material:material-icons-extended"]="libs.compose.material.icons"
    
    # Hilt
    ["com.google.dagger:hilt-android:2.48.1"]="libs.hilt.android"
    ["com.google.dagger:hilt-compiler:2.48.1"]="libs.hilt.compiler"
    ["androidx.hilt:hilt-navigation-compose:1.1.0"]="libs.hilt.navigation.compose"
    ["androidx.hilt:hilt-work:1.1.0"]="libs.hilt.work"
    ["androidx.hilt:hilt-compiler:1.1.0"]="libs.hilt.androidx.compiler"
    
    # Room
    ["androidx.room:room-runtime:2.6.1"]="libs.room.runtime"
    ["androidx.room:room-ktx:2.6.1"]="libs.room.ktx"
    ["androidx.room:room-compiler:2.6.1"]="libs.room.compiler"
    
    # Network
    ["com.squareup.retrofit2:retrofit:2.9.0"]="libs.retrofit"
    ["com.squareup.retrofit2:converter-gson:2.9.0"]="libs.retrofit.gson"
    ["com.squareup.okhttp3:logging-interceptor:4.12.0"]="libs.okhttp.logging"
    ["com.google.code.gson:gson:2.10.1"]="libs.gson"
    
    # Kotlin
    ["org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3"]="libs.kotlin.coroutines"
    ["org.jetbrains.kotlinx:kotlinx-datetime:0.5.0"]="libs.kotlin.datetime"
    ["org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0"]="libs.kotlin.serialization.json"
    
    # Testing
    ["junit:junit:4.13.2"]="libs.junit"
    ["io.mockk:mockk:1.13.8"]="libs.mockk"
    ["com.google.truth:truth:1.1.5"]="libs.truth"
    ["org.robolectric:robolectric:4.11.1"]="libs.robolectric"
    ["org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"]="libs.coroutines.test"
)

# 查找所有build.gradle.kts文件
find . -name "build.gradle.kts" -path "*/feature/*" -o -path "*/shared/*" -o -path "*/core/*" | while read -r file; do
    echo "📝 处理文件: $file"
    
    # 创建备份
    cp "$file" "${file}.backup"
    
    # 执行替换
    for old_dep in "${!version_mappings[@]}"; do
        new_dep="${version_mappings[$old_dep]}"
        
        # 替换implementation依赖
        sed -i "s|implementation(\"$old_dep\")|implementation($new_dep)|g" "$file"
        
        # 替换api依赖
        sed -i "s|api(\"$old_dep\")|api($new_dep)|g" "$file"
        
        # 替换ksp依赖
        sed -i "s|ksp(\"$old_dep\")|ksp($new_dep)|g" "$file"
        
        # 替换testImplementation依赖
        sed -i "s|testImplementation(\"$old_dep\")|testImplementation($new_dep)|g" "$file"
        
        # 替换androidTestImplementation依赖
        sed -i "s|androidTestImplementation(\"$old_dep\")|androidTestImplementation($new_dep)|g" "$file"
    done
    
    # 替换compileSdk等版本
    sed -i 's|compileSdk = 34|compileSdk = libs.versions.compileSdk.get().toInt()|g' "$file"
    sed -i 's|minSdk = 26|minSdk = libs.versions.minSdk.get().toInt()|g' "$file"
    sed -i 's|targetSdk = 34|targetSdk = libs.versions.targetSdk.get().toInt()|g' "$file"
    sed -i 's|buildToolsVersion = "33.0.2"|buildToolsVersion = libs.versions.buildTools.get()|g' "$file"
    sed -i 's|kotlinCompilerExtensionVersion = "1.5.7"|kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()|g' "$file"
    
    echo "✅ 完成: $file"
done

echo "
🎉 迁移完成！

下一步：
1. 检查修改是否正确: git diff
2. 编译项目验证: ./gradlew build
3. 如果有问题，可以从.backup文件恢复
4. 确认无误后删除备份文件: find . -name '*.backup' -delete
"