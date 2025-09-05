#!/usr/bin/env python3
"""
性能测试数据生成脚本
用于生成大量测试数据文件，供边界案例测试使用
"""

import csv
import random
from datetime import datetime, timedelta

def generate_todos_csv(filename, count):
    """生成指定数量的Todo测试数据"""
    with open(filename, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        # 写入头部
        writer.writerow(['id', 'title', 'description', 'completed', 'priority', 'dueAt', 'createdAt', 'updatedAt'])
        
        base_time = datetime(2025, 7, 28, 12, 0, 0)
        
        for i in range(1, count + 1):
            todo_id = f"todo-{i:06d}"
            title = f"测试任务{i}"
            description = f"这是第{i}个测试任务的描述" if i % 3 != 0 else ""  # 30%空描述
            completed = "true" if i % 4 == 0 else "false"  # 25%已完成
            priority = random.randint(1, 3)
            
            # 50%有截止日期
            due_at = ""
            if i % 2 == 0:
                due_date = base_time + timedelta(days=random.randint(1, 30))
                due_at = due_date.strftime("%Y-%m-%dT%H:%M:%SZ")
            
            created_at = (base_time + timedelta(minutes=i)).strftime("%Y-%m-%dT%H:%M:%SZ")
            updated_at = (base_time + timedelta(minutes=i + random.randint(1, 60))).strftime("%Y-%m-%dT%H:%M:%SZ")
            
            writer.writerow([todo_id, title, description, completed, priority, due_at, created_at, updated_at])

def generate_habits_csv(filename, count):
    """生成指定数量的Habit测试数据"""
    periods = ['daily', 'weekly', 'monthly']
    colors = ['#FF5722', '#2196F3', '#4CAF50', '#FF9800', '#9C27B0', '#607D8B']
    icons = ['🏃‍♂️', '📚', '🧘‍♀️', '💪', '🎯', '⭐', '📝', '🎨', '🎵', '🌱']
    
    with open(filename, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        # 写入头部
        writer.writerow(['id', 'title', 'description', 'period', 'target', 'color', 'icon', 'createdAt', 'updatedAt'])
        
        base_time = datetime(2025, 7, 28, 12, 0, 0)
        
        for i in range(1, count + 1):
            habit_id = f"habit-{i:06d}"
            title = f"测试习惯{i}"
            description = f"这是第{i}个测试习惯的描述" if i % 4 != 0 else ""  # 25%空描述
            period = random.choice(periods)
            target = random.randint(1, 10)
            color = random.choice(colors)
            icon = random.choice(icons) if i % 5 != 0 else ""  # 20%空图标
            
            created_at = (base_time + timedelta(minutes=i)).strftime("%Y-%m-%dT%H:%M:%SZ")
            updated_at = (base_time + timedelta(minutes=i + random.randint(1, 60))).strftime("%Y-%m-%dT%H:%M:%SZ")
            
            writer.writerow([habit_id, title, description, period, target, color, icon, created_at, updated_at])

def generate_mixed_error_data(filename, total_count, error_rate=0.2):
    """生成混合错误数据，指定错误率"""
    error_count = int(total_count * error_rate)
    normal_count = total_count - error_count
    
    with open(filename, 'w', newline='', encoding='utf-8') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(['id', 'title', 'description', 'completed', 'priority', 'dueAt', 'createdAt', 'updatedAt'])
        
        base_time = datetime(2025, 7, 28, 12, 0, 0)
        record_num = 0
        
        # 生成正常记录和错误记录
        for i in range(total_count):
            record_num += 1
            
            # 每5条记录插入1条错误（20%错误率）
            if i % 5 == 4 and error_count > 0:
                # 生成错误记录
                error_types = [
                    # 字段不足
                    [f"error-{record_num:06d}", "字段不足错误"],
                    # 布尔值错误  
                    [f"error-{record_num:06d}", "布尔值错误", "描述", "invalid_bool", "1", "", "2025-07-28T12:00:00Z", "2025-07-28T12:00:00Z"],
                    # 整数错误
                    [f"error-{record_num:06d}", "整数错误", "描述", "false", "abc", "", "2025-07-28T12:00:00Z", "2025-07-28T12:00:00Z"],
                    # 时间错误
                    [f"error-{record_num:06d}", "时间错误", "描述", "false", "1", "", "invalid-date", "2025-07-28T12:00:00Z"]
                ]
                error_record = random.choice(error_types)
                writer.writerow(error_record)
                error_count -= 1
            else:
                # 生成正常记录
                todo_id = f"normal-{record_num:06d}"
                title = f"正常任务{record_num}"
                description = f"描述{record_num}"
                completed = "true" if record_num % 4 == 0 else "false"
                priority = random.randint(1, 3)
                
                due_at = ""
                if record_num % 3 == 0:
                    due_date = base_time + timedelta(days=random.randint(1, 30))
                    due_at = due_date.strftime("%Y-%m-%dT%H:%M:%SZ")
                
                created_at = (base_time + timedelta(minutes=record_num)).strftime("%Y-%m-%dT%H:%M:%SZ")
                updated_at = (base_time + timedelta(minutes=record_num + random.randint(1, 60))).strftime("%Y-%m-%dT%H:%M:%SZ")
                
                writer.writerow([todo_id, title, description, completed, priority, due_at, created_at, updated_at])

if __name__ == "__main__":
    print("正在生成性能测试数据...")
    
    # 生成不同规模的测试数据
    generate_todos_csv("performance_todos_100.csv", 100)
    generate_todos_csv("performance_todos_1000.csv", 1000)
    generate_todos_csv("performance_todos_10000.csv", 10000)
    
    generate_habits_csv("performance_habits_100.csv", 100)
    generate_habits_csv("performance_habits_1000.csv", 1000)
    generate_habits_csv("performance_habits_10000.csv", 10000)
    
    # 生成混合错误数据
    generate_mixed_error_data("mixed_error_todos_1000.csv", 1000, 0.2)
    generate_mixed_error_data("mixed_error_habits_1000.csv", 1000, 0.2)
    
    print("测试数据生成完成！")
    print("生成的文件：")
    print("- performance_todos_100.csv (100条Todo记录)")
    print("- performance_todos_1000.csv (1000条Todo记录)")  
    print("- performance_todos_10000.csv (10000条Todo记录)")
    print("- performance_habits_100.csv (100条Habit记录)")
    print("- performance_habits_1000.csv (1000条Habit记录)")
    print("- performance_habits_10000.csv (10000条Habit记录)")
    print("- mixed_error_todos_1000.csv (1000条混合错误Todo记录)")
    print("- mixed_error_habits_1000.csv (1000条混合错误Habit记录)")