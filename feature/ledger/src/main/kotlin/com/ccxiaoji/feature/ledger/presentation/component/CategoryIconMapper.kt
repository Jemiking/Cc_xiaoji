package com.ccxiaoji.feature.ledger.presentation.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 分类图标映射器
 * 将分类名称映射到Material Design图标，实现双图标系统
 */
object CategoryIconMapper {
    
    /**
     * 支出分类图标映射
     * 基于中文分类名称映射到对应的Material图标
     */
    private val expenseIconMap = mapOf(
        // 餐饮类
        "餐饮" to Icons.Default.Restaurant,
        "早餐" to Icons.Default.FreeBreakfast,
        "午餐" to Icons.Default.LunchDining,
        "晚餐" to Icons.Default.DinnerDining,
        "夜宵" to Icons.Default.NightlightRound,
        "饮品" to Icons.Default.LocalCafe,
        "酒水" to Icons.Default.LocalBar,
        "零食" to Icons.Default.Cookie,
        
        // 购物类
        "购物" to Icons.Default.ShoppingBag,
        "服装" to Icons.Default.Checkroom,
        "鞋靴" to Icons.Default.ShoppingBag,
        "数码" to Icons.Default.PhoneAndroid,
        "家电" to Icons.Default.Tv,
        "化妆品" to Icons.Default.Face,
        "日用品" to Icons.Default.ShoppingCart,
        "书籍" to Icons.Default.MenuBook,
        
        // 交通类
        "交通" to Icons.Default.DirectionsCar,
        "打车" to Icons.Default.LocalTaxi,
        "公交" to Icons.Default.DirectionsBus,
        "地铁" to Icons.Default.Subway,
        "火车" to Icons.Default.Train,
        "飞机" to Icons.Default.Flight,
        "加油" to Icons.Default.LocalGasStation,
        "停车" to Icons.Default.LocalParking,
        "维修" to Icons.Default.Build,
        
        // 娱乐类
        "娱乐" to Icons.Default.Movie,
        "电影" to Icons.Default.Theaters,
        "游戏" to Icons.Default.SportsEsports,
        "KTV" to Icons.Default.Mic,
        "运动" to Icons.Default.FitnessCenter,
        "旅游" to Icons.Default.TravelExplore,
        "音乐" to Icons.Default.MusicNote,
        "摄影" to Icons.Default.CameraAlt,
        
        // 医疗类
        "医疗" to Icons.Default.LocalHospital,
        "药品" to Icons.Default.Medication,
        "体检" to Icons.Default.HealthAndSafety,
        "牙科" to Icons.Default.Healing,
        "眼科" to Icons.Default.Visibility,
        "保健" to Icons.Default.Spa,
        
        // 教育类
        "教育" to Icons.Default.School,
        "培训" to Icons.Default.Class,
        "考试" to Icons.Default.Quiz,
        "文具" to Icons.Default.Edit,
        
        // 住房类
        "住房" to Icons.Default.Home,
        "房租" to Icons.Default.House,
        "物业" to Icons.Default.Apartment,
        "装修" to Icons.Default.Handyman,
        "家具" to Icons.Default.Chair,
        
        // 水电通讯类
        "水电" to Icons.Default.ElectricalServices,
        "电费" to Icons.Default.ElectricalServices,
        "水费" to Icons.Default.WaterDrop,
        "燃气" to Icons.Default.LocalFireDepartment,
        "通讯" to Icons.Default.Phone,
        "话费" to Icons.Default.PhoneInTalk,
        "网费" to Icons.Default.Wifi,
        
        // 其他支出
        "其它支出" to Icons.Default.MoreHoriz,
        "礼品" to Icons.Default.CardGiftcard,
        "捐赠" to Icons.Default.VolunteerActivism,
        "罚款" to Icons.Default.Warning,
        "维修" to Icons.Default.BuildCircle,
        "宠物" to Icons.Default.Pets,
        "美容" to Icons.Default.Face,
        "理发" to Icons.Default.ContentCut
    )
    
    /**
     * 收入分类图标映射
     */
    private val incomeIconMap = mapOf(
        // 工作收入
        "工资" to Icons.Default.Work,
        "薪水" to Icons.Default.AttachMoney,
        "加班费" to Icons.Default.AccessTime,
        "提成" to Icons.Default.Percent,
        
        // 奖励收入
        "奖金" to Icons.Default.EmojiEvents,
        "年终奖" to Icons.Default.Stars,
        "绩效奖" to Icons.Default.Grade,
        
        // 投资收入
        "投资" to Icons.Default.TrendingUp,
        "股票" to Icons.Default.ShowChart,
        "基金" to Icons.Default.PieChart,
        "理财" to Icons.Default.AccountBalance,
        "分红" to Icons.Default.MonetizationOn,
        
        // 兼职收入
        "兼职" to Icons.Default.Schedule,
        "外包" to Icons.Default.Assignment,
        "咨询" to Icons.Default.Support,
        
        // 其他收入
        "礼金" to Icons.Default.Redeem,
        "红包" to Icons.Default.CardGiftcard,
        "退款" to Icons.Default.Replay,
        "报销" to Icons.Default.Receipt,
        "其它收入" to Icons.Default.MoreHoriz,
        "转账" to Icons.Default.SwapHoriz,
        "借款" to Icons.Default.CreditCard,
        "利息" to Icons.Default.Savings
    )
    
    /**
     * 根据分类名称和类型获取对应的Material图标
     * @param categoryName 分类名称
     * @param isIncome 是否为收入分类
     * @return 对应的Material图标，如果没有找到则返回null
     */
    fun getMaterialIcon(categoryName: String, isIncome: Boolean): ImageVector? {
        val map = if (isIncome) incomeIconMap else expenseIconMap
        return map[categoryName] ?: map[categoryName.trim()]
    }
    
    /**
     * 根据分类名称模糊匹配Material图标
     * 当完全匹配失败时，尝试关键词匹配
     */
    fun getMaterialIconFuzzy(categoryName: String, isIncome: Boolean): ImageVector? {
        // 先尝试完全匹配
        getMaterialIcon(categoryName, isIncome)?.let { return it }
        
        // 如果完全匹配失败，尝试关键词匹配
        val map = if (isIncome) incomeIconMap else expenseIconMap
        val normalizedName = categoryName.trim()
        
        // 查找包含关键词的分类
        for ((key, icon) in map) {
            if (normalizedName.contains(key) || key.contains(normalizedName)) {
                return icon
            }
        }
        
        return null
    }
    
    /**
     * 获取默认图标
     * 当没有找到匹配的图标时使用
     */
    fun getDefaultIcon(isIncome: Boolean): ImageVector {
        return if (isIncome) {
            Icons.Default.AttachMoney
        } else {
            Icons.Default.ShoppingCart
        }
    }
    
    /**
     * 检查是否有对应的Material图标
     */
    fun hasMaterialIcon(categoryName: String, isIncome: Boolean): Boolean {
        return getMaterialIcon(categoryName, isIncome) != null
    }
    
    /**
     * 根据emoji图标直接获取对应的Material图标
     * 这是新的核心映射函数，解决图标选择后显示错误的问题
     */
    fun getMaterialIconByEmoji(emojiIcon: String, isIncome: Boolean): ImageVector? {
        // 将emoji映射到语义分类名称（区分收入和支出）
        val semanticName = getSemanticNameForEmoji(emojiIcon, isIncome)
        // 然后使用语义名称获取Material图标
        return getMaterialIconFuzzy(semanticName, isIncome)
    }
    
    /**
     * 将emoji图标映射到语义分类名称
     * 确保每个emoji都能映射到正确的Material图标
     * @param emoji emoji图标
     * @param isIncome 是否为收入分类
     */
    private fun getSemanticNameForEmoji(emoji: String, isIncome: Boolean): String {
        return if (isIncome) {
            // 收入分类的emoji映射
            when (emoji) {
                "💵" -> "其它收入"    // 钞票图标
                "💰" -> "工资"       // 钱袋图标
                "📝" -> "其它收入"    // 备忘录图标（通用收入）
                "📊" -> "股票"       // 图表图标  
                "📈" -> "投资"       // 上升图表图标
                "🎁" -> "红包"       // 礼物图标
                "💳" -> "报销"       // 信用卡图标
                "🏦" -> "利息"       // 银行图标
                "💼" -> "兼职"       // 公文包图标
                "🏆" -> "奖金"       // 奖杯图标
                "⭐" -> "年终奖"     // 星星图标
                "💎" -> "分红"       // 钻石图标
                "🔄" -> "转账"       // 循环图标
                "🔙" -> "退款"       // 返回图标
                "📄" -> "其它收入"   // 文档图标
                // 默认收入
                else -> "其它收入"
            }
        } else {
            // 支出分类的emoji映射
            when (emoji) {
                // 餐饮类
                "🍔" -> "餐饮"     // Restaurant图标
                "☕" -> "饮品"     // LocalCafe图标
                "🍕" -> "夜宵"     // NightlightRound图标  
                "🥗" -> "午餐"     // LunchDining图标
                "🍜" -> "早餐"     // FreeBreakfast图标
                "🍱" -> "晚餐"     // DinnerDining图标
                "🥡" -> "酒水"     // LocalBar图标
                "🍰" -> "零食"     // Cookie图标
                
                // 交通类
                "🚗" -> "交通"     // DirectionsCar图标
                "🚌" -> "公交"     // DirectionsBus图标
                "🚇" -> "地铁"     // Subway图标
                "✈️" -> "飞机"     // Flight图标
                "🚲" -> "停车"     // LocalParking图标
                "⛽" -> "加油"     // LocalGasStation图标
                "🚕" -> "打车"     // LocalTaxi图标
                "🏍️" -> "火车"     // Train图标
                
                // 生活类
                "🏠" -> "住房"     // Home图标
                "💡" -> "水电"     // ElectricalServices图标
                "💧" -> "水费"     // WaterDrop图标
                "🔥" -> "燃气"     // LocalFireDepartment图标
                "📱" -> "通讯"     // Phone图标
                "💻" -> "数码"     // PhoneAndroid图标
                "🛒" -> "购物"     // ShoppingBag图标
                "🎮" -> "游戏"     // SportsEsports图标
                
                // 服饰类
                "👕" -> "服装"     // Checkroom图标
                "👗" -> "家电"     // Tv图标
                "👠" -> "鞋靴"     // ShoppingBag图标
                "👜" -> "日用品"   // ShoppingCart图标
                "💄" -> "化妆品"   // Face图标
                "💍" -> "维修"     // BuildCircle图标
                "⌚" -> "家具"     // Chair图标
                "🕶️" -> "眼科"     // Visibility图标
                
                // 学习娱乐类
                "📚" -> "书籍"     // MenuBook图标
                "✏️" -> "文具"     // Edit图标
                "🎨" -> "娱乐"     // Movie图标
                "🎭" -> "摄影"     // CameraAlt图标
                "🎬" -> "电影"     // Theaters图标
                "🎵" -> "音乐"     // MusicNote图标
                "🏃" -> "运动"     // FitnessCenter图标
                "⚽" -> "装修"     // Handyman图标
                
                // 医疗类
                "💊" -> "药品"     // Medication图标
                "🏥" -> "医疗"     // LocalHospital图标
                "💉" -> "体检"     // HealthAndSafety图标
                "🩺" -> "保健"     // Spa图标
                "🦷" -> "牙科"     // Healing图标
                "🏨" -> "物业"     // Apartment图标
                "✂️" -> "理发"     // ContentCut图标
                
                // 礼品其他类
                "🎁" -> "礼品"     // CardGiftcard图标
                "🎂" -> "KTV"      // Mic图标
                "🎉" -> "旅游"     // TravelExplore图标
                "❤️" -> "捐赠"     // VolunteerActivism图标
                "💰" -> "其它支出" // MoreHoriz图标
                "💳" -> "宠物"     // Pets图标
                "📈" -> "教育"     // School图标
                "💼" -> "培训"     // Class图标
                "📝" -> "其它支出" // 备忘录在支出中映射到其它支出
                
                // 特殊处理的复合emoji
                "\uD83D\uDC68\u200D\u2695\uFE0F" -> "医疗" // 👨‍⚕️ 男医生
                
                // 默认支出
                else -> "其它支出"
            }
        }
    }
    
    /**
     * 获取所有支持的分类名称
     */
    fun getSupportedCategoryNames(isIncome: Boolean): Set<String> {
        return if (isIncome) incomeIconMap.keys else expenseIconMap.keys
    }
}