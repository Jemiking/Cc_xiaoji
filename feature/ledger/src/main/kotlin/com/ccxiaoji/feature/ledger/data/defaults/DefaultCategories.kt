package com.ccxiaoji.feature.ledger.data.defaults

/**
 * 默认分类数据定义
 * 包含系统预设的支出和收入分类
 */
object DefaultCategories {
    
    /**
     * 默认支出分类
     * 结构：父分类 -> 子分类列表
     */
    val expenseCategories = listOf(
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "餐饮",
                icon = "🍔",
                color = "#FF9800"
            ),
            children = listOf(
                DefaultCategory("早餐", "☕"),
                DefaultCategory("午餐", "🍜"),
                DefaultCategory("晚餐", "🍱"),
                DefaultCategory("饮料酒水", "🥤"),
                DefaultCategory("水果零食", "🍎"),
                DefaultCategory("外卖", "🥡")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "交通",
                icon = "🚗",
                color = "#2196F3"
            ),
            children = listOf(
                DefaultCategory("公交地铁", "🚇"),
                DefaultCategory("打车", "🚕"),
                DefaultCategory("加油", "⛽"),
                DefaultCategory("停车费", "🅿️"),
                DefaultCategory("火车票", "🚄"),
                DefaultCategory("机票", "✈️"),
                DefaultCategory("高速费", "🛣️")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "购物",
                icon = "🛒",
                color = "#9C27B0"
            ),
            children = listOf(
                DefaultCategory("日用品", "🧴"),
                DefaultCategory("服饰", "👕"),
                DefaultCategory("电子产品", "📱"),
                DefaultCategory("化妆品", "💄"),
                DefaultCategory("家具家电", "🛋️"),
                DefaultCategory("网购", "📦")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "娱乐",
                icon = "🎮",
                color = "#E91E63"
            ),
            children = listOf(
                DefaultCategory("电影", "🎬"),
                DefaultCategory("游戏", "🎯"),
                DefaultCategory("运动健身", "🏃"),
                DefaultCategory("旅游", "🏖️"),
                DefaultCategory("聚会", "🎉"),
                DefaultCategory("兴趣爱好", "🎨")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "居住",
                icon = "🏠",
                color = "#4CAF50"
            ),
            children = listOf(
                DefaultCategory("房租", "🏘️"),
                DefaultCategory("水电费", "💡"),
                DefaultCategory("物业费", "🏢"),
                DefaultCategory("网费", "📡"),
                DefaultCategory("燃气费", "🔥"),
                DefaultCategory("维修保养", "🔧")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "医疗",
                icon = "🏥",
                color = "#F44336"
            ),
            children = listOf(
                DefaultCategory("看病", "👨‍⚕️"),
                DefaultCategory("买药", "💊"),
                DefaultCategory("体检", "🩺"),
                DefaultCategory("保健品", "💚"),
                DefaultCategory("医美", "✨")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "教育",
                icon = "📚",
                color = "#3F51B5"
            ),
            children = listOf(
                DefaultCategory("培训课程", "🎓"),
                DefaultCategory("书籍", "📖"),
                DefaultCategory("考试报名", "📝"),
                DefaultCategory("学习用品", "✏️"),
                DefaultCategory("在线课程", "💻")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "人情",
                icon = "🎁",
                color = "#FF5722"
            ),
            children = listOf(
                DefaultCategory("礼物", "🎀"),
                DefaultCategory("红包", "🧧"),
                DefaultCategory("请客", "🍻"),
                DefaultCategory("慈善捐款", "❤️")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "金融",
                icon = "💳",
                color = "#00BCD4"
            ),
            children = listOf(
                DefaultCategory("手续费", "💰"),
                DefaultCategory("利息", "📈"),
                DefaultCategory("保险", "🛡️"),
                DefaultCategory("投资亏损", "📉"),
                DefaultCategory("税费", "🧾")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "其他",
                icon = "📌",
                color = "#607D8B"
            ),
            children = listOf(
                DefaultCategory("丢失", "🔍"),
                DefaultCategory("意外支出", "⚠️"),
                DefaultCategory("其他支出", "💸")
            )
        )
    )
    
    /**
     * 默认收入分类
     */
    val incomeCategories = listOf(
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "工资",
                icon = "💼",
                color = "#4CAF50"
            ),
            children = listOf(
                DefaultCategory("基本工资", "💵"),
                DefaultCategory("加班费", "⏰"),
                DefaultCategory("奖金", "🏆"),
                DefaultCategory("提成", "💹"),
                DefaultCategory("补贴", "🎯")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "兼职",
                icon = "💻",
                color = "#2196F3"
            ),
            children = listOf(
                DefaultCategory("兼职收入", "💼"),
                DefaultCategory("外包项目", "📋"),
                DefaultCategory("咨询费", "💬"),
                DefaultCategory("稿费", "✍️")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "投资",
                icon = "📈",
                color = "#FF9800"
            ),
            children = listOf(
                DefaultCategory("股票收益", "📊"),
                DefaultCategory("基金收益", "💰"),
                DefaultCategory("利息收入", "🏦"),
                DefaultCategory("分红", "💎"),
                DefaultCategory("理财收益", "💳")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "生意",
                icon = "🏪",
                color = "#9C27B0"
            ),
            children = listOf(
                DefaultCategory("营业收入", "💸"),
                DefaultCategory("销售收入", "🛍️"),
                DefaultCategory("服务收入", "🤝"),
                DefaultCategory("租金收入", "🏠")
            )
        ),
        DefaultCategoryGroup(
            parent = DefaultCategory(
                name = "其他",
                icon = "🎁",
                color = "#607D8B"
            ),
            children = listOf(
                DefaultCategory("礼金", "🧧"),
                DefaultCategory("报销", "📝"),
                DefaultCategory("退款", "↩️"),
                DefaultCategory("中奖", "🎰"),
                DefaultCategory("其他收入", "💰")
            )
        )
    )
}

/**
 * 默认分类组
 */
data class DefaultCategoryGroup(
    val parent: DefaultCategory,
    val children: List<DefaultCategory>
)

/**
 * 默认分类
 */
data class DefaultCategory(
    val name: String,
    val icon: String,
    val color: String? = null
)