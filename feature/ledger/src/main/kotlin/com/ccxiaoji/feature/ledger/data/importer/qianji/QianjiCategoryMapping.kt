package com.ccxiaoji.feature.ledger.data.importer.qianji

/**
 * 钱迹分类到CC小记二级分类的映射配置
 */
object QianjiCategoryMapping {
    
    /**
     * 钱迹分类映射到CC小记的二级分类结构
     * key: 钱迹分类名
     * value: Pair(父分类名, 子分类名)，如果子分类名为null，则创建为父分类
     */
    val expenseCategoryMapping = mapOf(
        // 餐饮类
        "下馆子" to Pair("餐饮", "外出就餐"),
        "早餐" to Pair("餐饮", "早餐"),
        "午餐" to Pair("餐饮", "午餐"),
        "晚餐" to Pair("餐饮", "晚餐"),
        "买菜" to Pair("餐饮", "买菜"),
        "外卖" to Pair("餐饮", "外卖"),
        "聚餐" to Pair("餐饮", "聚餐"),
        
        // 交通类
        "交通" to Pair("交通", "公共交通"),
        "公交" to Pair("交通", "公交"),
        "地铁" to Pair("交通", "地铁"),
        "打车" to Pair("交通", "打车"),
        "加油" to Pair("交通", "加油"),
        "停车费" to Pair("交通", "停车费"),
        "高速费" to Pair("交通", "过路费"),
        "火车" to Pair("交通", "火车"),
        "飞机" to Pair("交通", "机票"),
        
        // 购物类
        "日用品" to Pair("购物", "日用品"),
        "衣服" to Pair("购物", "服饰"),
        "鞋包" to Pair("购物", "鞋包"),
        "化妆品" to Pair("购物", "化妆品"),
        "美妆" to Pair("购物", "化妆品"),
        "数码" to Pair("购物", "数码产品"),
        "家电" to Pair("购物", "家电"),
        "家具" to Pair("购物", "家具"),
        
        // 娱乐类
        "娱乐" to Pair("娱乐", "休闲娱乐"),
        "电影" to Pair("娱乐", "电影"),
        "游戏" to Pair("娱乐", "游戏"),
        "旅游" to Pair("娱乐", "旅游"),
        "运动" to Pair("娱乐", "运动健身"),
        "健身" to Pair("娱乐", "运动健身"),
        
        // 居住类
        "房租" to Pair("居住", "房租"),
        "水费" to Pair("居住", "水费"),
        "电费" to Pair("居住", "电费"),
        "燃气费" to Pair("居住", "燃气费"),
        "物业费" to Pair("居住", "物业费"),
        "取暖费" to Pair("居住", "取暖费"),
        
        // 通讯类
        "话费网费" to Pair("通讯", "话费"),
        "话费" to Pair("通讯", "话费"),
        "网费" to Pair("通讯", "宽带"),
        "流量" to Pair("通讯", "流量"),
        
        // 医疗类
        "医疗" to Pair("医疗", "看病"),
        "药品" to Pair("医疗", "药品"),
        "体检" to Pair("医疗", "体检"),
        "保健" to Pair("医疗", "保健品"),
        
        // 教育类
        "学习" to Pair("教育", "培训"),
        "书籍" to Pair("教育", "书籍"),
        "培训" to Pair("教育", "培训"),
        "考试" to Pair("教育", "考试"),
        
        // 人情类
        "请客送礼" to Pair("人情", "送礼"),
        "红包" to Pair("人情", "红包"),
        "礼物" to Pair("人情", "送礼"),
        "请客" to Pair("人情", "请客"),
        
        // 金融类
        "股票基金" to Pair("金融", "投资理财"),
        "保险" to Pair("金融", "保险"),
        "贷款" to Pair("金融", "贷款"),
        "利息" to Pair("金融", "利息"),
        
        // 其他
        "水果" to Pair("餐饮", "水果"),
        "零食" to Pair("餐饮", "零食"),
        "饮料" to Pair("餐饮", "饮料"),
        "酒水" to Pair("餐饮", "酒水"),
        "宠物" to Pair("其他", "宠物"),
        "其它" to Pair("其他", "其他"),
        "其他" to Pair("其他", "其他")
    )
    
    val incomeCategoryMapping = mapOf(
        // 职业收入
        "工资" to Pair("职业收入", "工资"),
        "奖金" to Pair("职业收入", "奖金"),
        "补贴" to Pair("职业收入", "补贴"),
        "提成" to Pair("职业收入", "提成"),
        "加班费" to Pair("职业收入", "加班费"),
        
        // 其他收入
        "外快" to Pair("其他收入", "兼职"),
        "兼职" to Pair("其他收入", "兼职"),
        "红包" to Pair("其他收入", "红包"),
        "礼金" to Pair("其他收入", "礼金"),
        "报销" to Pair("其他收入", "报销"),
        
        // 理财收入
        "利息" to Pair("理财收入", "利息"),
        "股票" to Pair("理财收入", "股票"),
        "基金" to Pair("理财收入", "基金"),
        "分红" to Pair("理财收入", "分红"),
        
        // 营业收入
        "营业收入" to Pair("营业收入", "销售收入"),
        "销售" to Pair("营业收入", "销售收入"),
        
        // 其他
        "其它" to Pair("其他收入", "其他"),
        "其他" to Pair("其他收入", "其他")
    )
    
    /**
     * 获取映射的父子分类
     * @param qianjiCategory 钱迹分类名
     * @param qianjiSubCategory 钱迹二级分类名（可选）
     * @param type 交易类型（支出/收入）
     * @return Pair(父分类名, 子分类名?)
     */
    fun getMappedCategory(
        qianjiCategory: String,
        qianjiSubCategory: String?,
        type: String
    ): Pair<String, String?> {
        val mapping = if (type == "收入") incomeCategoryMapping else expenseCategoryMapping
        
        // 先尝试用钱迹的二级分类查找
        if (!qianjiSubCategory.isNullOrEmpty()) {
            mapping[qianjiSubCategory]?.let { return it }
        }
        
        // 再用一级分类查找
        return mapping[qianjiCategory] ?: {
            // 如果没有找到映射，根据有无二级分类决定返回格式
            if (!qianjiSubCategory.isNullOrEmpty()) {
                // 有二级分类：钱迹一级作为父分类，二级作为子分类
                Pair(qianjiCategory, qianjiSubCategory)
            } else {
                // 无二级分类：创建一个默认子分类
                Pair(qianjiCategory, "一般")
            }
        }()
    }
    
    /**
     * 获取分类图标建议
     */
    fun suggestCategoryIcon(parentName: String, childName: String? = null): String {
        // 优先根据子分类返回图标
        return when (childName) {
            "早餐" -> "☕"
            "午餐" -> "🍜"
            "晚餐" -> "🍽️"
            "外卖" -> "🥡"
            "买菜" -> "🥬"
            "聚餐" -> "🍻"
            "公交" -> "🚌"
            "地铁" -> "🚇"
            "打车" -> "🚕"
            "加油" -> "⛽"
            "停车费" -> "🅿️"
            "火车" -> "🚄"
            "机票" -> "✈️"
            "服饰" -> "👔"
            "鞋包" -> "👜"
            "化妆品" -> "💄"
            "数码产品" -> "📱"
            "家电" -> "🔌"
            "电影" -> "🎬"
            "游戏" -> "🎮"
            "旅游" -> "✈️"
            "运动健身" -> "💪"
            "房租" -> "🏠"
            "水费" -> "💧"
            "电费" -> "⚡"
            "话费" -> "📞"
            "宽带" -> "🌐"
            "看病" -> "🏥"
            "药品" -> "💊"
            "体检" -> "🩺"
            "培训" -> "📚"
            "书籍" -> "📖"
            "红包" -> "🧧"
            "送礼" -> "🎁"
            "工资" -> "💰"
            "奖金" -> "🏆"
            "兼职" -> "💼"
            else -> when (parentName) {
                "餐饮" -> "🍔"
                "交通" -> "🚗"
                "购物" -> "🛒"
                "娱乐" -> "🎮"
                "居住" -> "🏠"
                "通讯" -> "📱"
                "医疗" -> "🏥"
                "教育" -> "📚"
                "人情" -> "🎁"
                "金融" -> "💳"
                "职业收入" -> "💰"
                "其他收入" -> "💵"
                "理财收入" -> "📈"
                "营业收入" -> "💼"
                else -> "📝"
            }
        }
    }
    
    /**
     * 获取分类颜色建议
     */
    fun suggestCategoryColor(parentName: String): String {
        return when (parentName) {
            "餐饮" -> "#FF9800"
            "交通" -> "#2196F3"
            "购物" -> "#E91E63"
            "娱乐" -> "#9C27B0"
            "居住" -> "#4CAF50"
            "通讯" -> "#00BCD4"
            "医疗" -> "#FF5722"
            "教育" -> "#3F51B5"
            "人情" -> "#F44336"
            "金融" -> "#795548"
            "职业收入" -> "#4CAF50"
            "其他收入" -> "#8BC34A"
            "理财收入" -> "#FFC107"
            "营业收入" -> "#FF9800"
            else -> "#6200EE"
        }
    }
}