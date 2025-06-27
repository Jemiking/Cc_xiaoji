package com.ccxiaoji.feature.plan.domain.usecase.template

import com.ccxiaoji.feature.plan.domain.model.*
import com.ccxiaoji.feature.plan.domain.repository.TemplateRepository
import javax.inject.Inject

/**
 * 初始化系统预置模板用例
 */
class InitializeSystemTemplatesUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * 初始化系统模板
     * 如果已存在系统模板则跳过
     */
    suspend operator fun invoke() {
        // 检查是否已有系统模板
        if (templateRepository.hasSystemTemplates()) {
            return
        }
        
        // 创建预置模板
        val templates = createSystemTemplates()
        templateRepository.insertTemplates(templates)
    }
    
    /**
     * 创建系统预置模板
     */
    private fun createSystemTemplates(): List<Template> {
        return listOf(
            // 工作类模板
            createProjectTemplate(),
            createProductLaunchTemplate(),
            createTeamBuildingTemplate(),
            
            // 学习类模板
            createSkillLearningTemplate(),
            createExamPreparationTemplate(),
            createReadingPlanTemplate(),
            
            // 健身类模板
            createFitnessBeginnerTemplate(),
            createMarathonTrainingTemplate(),
            createWeightLossTemplate(),
            
            // 生活类模板
            createTravelPlanTemplate(),
            createHomeRenovationTemplate(),
            createFinancialPlanTemplate()
        )
    }
    
    /**
     * 项目开发模板
     */
    private fun createProjectTemplate() = Template(
        title = "项目开发计划",
        description = "适用于软件开发项目的标准流程模板",
        category = TemplateCategory.PROJECT,
        tags = listOf("项目", "开发", "软件", "敏捷"),
        color = "#4285F4",
        duration = 90,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "项目开发",
                description = "完整的项目开发流程",
                tags = listOf("项目", "开发")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "需求分析",
                    description = "收集和分析项目需求",
                    dayOffset = 0,
                    duration = 14,
                    milestones = listOf(
                        MilestoneTemplate("需求文档完成", "完成详细的需求文档", 10),
                        MilestoneTemplate("需求评审通过", "需求评审会议通过", 14)
                    )
                ),
                SubPlanTemplate(
                    title = "系统设计",
                    description = "系统架构和详细设计",
                    dayOffset = 14,
                    duration = 14,
                    milestones = listOf(
                        MilestoneTemplate("架构设计完成", "完成系统架构设计", 7),
                        MilestoneTemplate("详细设计完成", "完成详细设计文档", 14)
                    )
                ),
                SubPlanTemplate(
                    title = "开发实现",
                    description = "功能开发和单元测试",
                    dayOffset = 28,
                    duration = 42,
                    subPlans = listOf(
                        SubPlanTemplate("前端开发", "前端界面和交互开发", 0, 42),
                        SubPlanTemplate("后端开发", "后端服务和API开发", 0, 42),
                        SubPlanTemplate("数据库开发", "数据库设计和优化", 0, 21)
                    ),
                    milestones = listOf(
                        MilestoneTemplate("Alpha版本", "完成Alpha版本", 28),
                        MilestoneTemplate("Beta版本", "完成Beta版本", 42)
                    )
                ),
                SubPlanTemplate(
                    title = "测试验收",
                    description = "系统测试和用户验收",
                    dayOffset = 70,
                    duration = 14,
                    milestones = listOf(
                        MilestoneTemplate("测试完成", "所有测试用例通过", 10),
                        MilestoneTemplate("验收通过", "用户验收测试通过", 14)
                    )
                ),
                SubPlanTemplate(
                    title = "部署上线",
                    description = "系统部署和上线",
                    dayOffset = 84,
                    duration = 6,
                    milestones = listOf(
                        MilestoneTemplate("部署完成", "生产环境部署完成", 3),
                        MilestoneTemplate("正式上线", "系统正式上线运行", 6)
                    )
                )
            ),
            milestones = listOf(
                MilestoneTemplate("项目启动", "项目正式启动", 0),
                MilestoneTemplate("项目交付", "项目正式交付", 90)
            )
        )
    )
    
    /**
     * 产品发布模板
     */
    private fun createProductLaunchTemplate() = Template(
        title = "产品发布计划",
        description = "新产品发布的完整流程模板",
        category = TemplateCategory.WORK,
        tags = listOf("产品", "发布", "营销"),
        color = "#EA4335",
        duration = 60,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "新产品发布",
                description = "从准备到发布的完整流程",
                tags = listOf("产品", "发布")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "市场调研",
                    description = "目标市场和竞品分析",
                    dayOffset = 0,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "产品准备",
                    description = "产品最终优化和包装",
                    dayOffset = 14,
                    duration = 21
                ),
                SubPlanTemplate(
                    title = "营销策划",
                    description = "制定营销策略和材料",
                    dayOffset = 21,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "发布执行",
                    description = "产品正式发布",
                    dayOffset = 35,
                    duration = 25
                )
            )
        )
    )
    
    /**
     * 团队建设模板
     */
    private fun createTeamBuildingTemplate() = Template(
        title = "团队建设计划",
        description = "提升团队凝聚力和效率",
        category = TemplateCategory.WORK,
        tags = listOf("团队", "管理", "培训"),
        color = "#34A853",
        duration = 30,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "团队建设",
                description = "系统化的团队建设方案",
                tags = listOf("团队", "建设")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "团队评估",
                    description = "评估团队现状和问题",
                    dayOffset = 0,
                    duration = 7
                ),
                SubPlanTemplate(
                    title = "技能培训",
                    description = "专业技能提升培训",
                    dayOffset = 7,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "团建活动",
                    description = "组织团队建设活动",
                    dayOffset = 14,
                    duration = 7
                ),
                SubPlanTemplate(
                    title = "效果评估",
                    description = "评估建设效果",
                    dayOffset = 21,
                    duration = 9
                )
            )
        )
    )
    
    /**
     * 技能学习模板
     */
    private fun createSkillLearningTemplate() = Template(
        title = "技能学习计划",
        description = "系统学习新技能的计划模板",
        category = TemplateCategory.STUDY,
        tags = listOf("学习", "技能", "成长"),
        color = "#9C27B0",
        duration = 60,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "新技能学习",
                description = "从零开始掌握一项新技能",
                tags = listOf("学习", "技能")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "基础知识",
                    description = "学习基础理论知识",
                    dayOffset = 0,
                    duration = 14,
                    milestones = listOf(
                        MilestoneTemplate("完成基础课程", "完成所有基础课程学习", 14)
                    )
                ),
                SubPlanTemplate(
                    title = "实践练习",
                    description = "动手实践和练习",
                    dayOffset = 14,
                    duration = 30,
                    milestones = listOf(
                        MilestoneTemplate("完成练习项目", "完成3个练习项目", 30)
                    )
                ),
                SubPlanTemplate(
                    title = "进阶提升",
                    description = "深入学习和提升",
                    dayOffset = 44,
                    duration = 16,
                    milestones = listOf(
                        MilestoneTemplate("掌握进阶技能", "完成进阶内容学习", 16)
                    )
                )
            )
        )
    )
    
    /**
     * 考试准备模板
     */
    private fun createExamPreparationTemplate() = Template(
        title = "考试备考计划",
        description = "系统化的考试准备计划",
        category = TemplateCategory.STUDY,
        tags = listOf("考试", "学习", "备考"),
        color = "#FF5722",
        duration = 90,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "考试备考",
                description = "全面系统的备考计划",
                tags = listOf("考试", "备考")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "知识梳理",
                    description = "梳理考试大纲和知识点",
                    dayOffset = 0,
                    duration = 21
                ),
                SubPlanTemplate(
                    title = "重点突破",
                    description = "攻克重点难点",
                    dayOffset = 21,
                    duration = 30
                ),
                SubPlanTemplate(
                    title = "习题训练",
                    description = "大量习题练习",
                    dayOffset = 51,
                    duration = 21
                ),
                SubPlanTemplate(
                    title = "模拟冲刺",
                    description = "模拟考试和查漏补缺",
                    dayOffset = 72,
                    duration = 18
                )
            )
        )
    )
    
    /**
     * 阅读计划模板
     */
    private fun createReadingPlanTemplate() = Template(
        title = "阅读计划",
        description = "培养阅读习惯的计划",
        category = TemplateCategory.STUDY,
        tags = listOf("阅读", "书籍", "习惯"),
        color = "#795548",
        duration = 30,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "月度阅读计划",
                description = "每月阅读2本书",
                tags = listOf("阅读")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "第一本书",
                    description = "阅读并做笔记",
                    dayOffset = 0,
                    duration = 14,
                    milestones = listOf(
                        MilestoneTemplate("完成阅读", "读完整本书", 12),
                        MilestoneTemplate("完成笔记", "整理读书笔记", 14)
                    )
                ),
                SubPlanTemplate(
                    title = "第二本书",
                    description = "阅读并做笔记",
                    dayOffset = 14,
                    duration = 14,
                    milestones = listOf(
                        MilestoneTemplate("完成阅读", "读完整本书", 12),
                        MilestoneTemplate("完成笔记", "整理读书笔记", 14)
                    )
                ),
                SubPlanTemplate(
                    title = "总结分享",
                    description = "总结心得并分享",
                    dayOffset = 28,
                    duration = 2
                )
            )
        )
    )
    
    /**
     * 健身入门模板
     */
    private fun createFitnessBeginnerTemplate() = Template(
        title = "健身入门计划",
        description = "零基础健身入门计划",
        category = TemplateCategory.FITNESS,
        tags = listOf("健身", "运动", "入门"),
        color = "#4CAF50",
        duration = 30,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "健身入门",
                description = "30天养成健身习惯",
                tags = listOf("健身", "入门")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "适应期",
                    description = "身体适应和基础训练",
                    dayOffset = 0,
                    duration = 7,
                    milestones = listOf(
                        MilestoneTemplate("完成体测", "完成基础体能测试", 1),
                        MilestoneTemplate("建立习惯", "连续运动7天", 7)
                    )
                ),
                SubPlanTemplate(
                    title = "基础期",
                    description = "基础力量和耐力训练",
                    dayOffset = 7,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "提升期",
                    description = "增加强度和时长",
                    dayOffset = 21,
                    duration = 9,
                    milestones = listOf(
                        MilestoneTemplate("达成目标", "完成月度健身目标", 9)
                    )
                )
            )
        )
    )
    
    /**
     * 马拉松训练模板
     */
    private fun createMarathonTrainingTemplate() = Template(
        title = "马拉松训练计划",
        description = "半程马拉松训练计划",
        category = TemplateCategory.FITNESS,
        tags = listOf("跑步", "马拉松", "耐力"),
        color = "#FF9800",
        duration = 120,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "半马训练",
                description = "16周半程马拉松训练",
                tags = listOf("马拉松", "跑步")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "基础建立",
                    description = "建立跑步基础",
                    dayOffset = 0,
                    duration = 28
                ),
                SubPlanTemplate(
                    title = "耐力提升",
                    description = "提升长距离耐力",
                    dayOffset = 28,
                    duration = 42
                ),
                SubPlanTemplate(
                    title = "速度训练",
                    description = "提升配速",
                    dayOffset = 70,
                    duration = 28
                ),
                SubPlanTemplate(
                    title = "赛前调整",
                    description = "赛前减量调整",
                    dayOffset = 98,
                    duration = 22
                )
            )
        )
    )
    
    /**
     * 减重计划模板
     */
    private fun createWeightLossTemplate() = Template(
        title = "健康减重计划",
        description = "科学健康的减重计划",
        category = TemplateCategory.HEALTH,
        tags = listOf("减重", "健康", "饮食"),
        color = "#E91E63",
        duration = 90,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "90天减重计划",
                description = "通过运动和饮食控制健康减重",
                tags = listOf("减重", "健康")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "习惯养成期",
                    description = "建立健康的生活习惯",
                    dayOffset = 0,
                    duration = 21
                ),
                SubPlanTemplate(
                    title = "稳定减重期",
                    description = "保持稳定的减重节奏",
                    dayOffset = 21,
                    duration = 42
                ),
                SubPlanTemplate(
                    title = "巩固维持期",
                    description = "巩固成果防止反弹",
                    dayOffset = 63,
                    duration = 27
                )
            )
        )
    )
    
    /**
     * 旅行计划模板
     */
    private fun createTravelPlanTemplate() = Template(
        title = "旅行计划",
        description = "完整的旅行规划模板",
        category = TemplateCategory.LIFE,
        tags = listOf("旅行", "度假", "规划"),
        color = "#00BCD4",
        duration = 30,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "度假旅行",
                description = "轻松愉快的度假之旅",
                tags = listOf("旅行")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "行前准备",
                    description = "预订和准备工作",
                    dayOffset = 0,
                    duration = 20,
                    milestones = listOf(
                        MilestoneTemplate("完成预订", "机票酒店预订完成", 7),
                        MilestoneTemplate("行李准备", "行李打包完成", 19)
                    )
                ),
                SubPlanTemplate(
                    title = "旅行期间",
                    description = "享受旅行",
                    dayOffset = 20,
                    duration = 7
                ),
                SubPlanTemplate(
                    title = "返程整理",
                    description = "返程和整理",
                    dayOffset = 27,
                    duration = 3
                )
            )
        )
    )
    
    /**
     * 装修计划模板
     */
    private fun createHomeRenovationTemplate() = Template(
        title = "家装计划",
        description = "家庭装修改造计划",
        category = TemplateCategory.LIFE,
        tags = listOf("装修", "家居", "改造"),
        color = "#607D8B",
        duration = 90,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "家庭装修",
                description = "全屋装修改造计划",
                tags = listOf("装修", "家居")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "设计规划",
                    description = "设计方案和预算",
                    dayOffset = 0,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "拆改工程",
                    description = "拆除和结构改造",
                    dayOffset = 14,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "水电工程",
                    description = "水电线路改造",
                    dayOffset = 28,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "基础工程",
                    description = "泥工和木工",
                    dayOffset = 42,
                    duration = 21
                ),
                SubPlanTemplate(
                    title = "面层工程",
                    description = "油漆和贴砖",
                    dayOffset = 63,
                    duration = 14
                ),
                SubPlanTemplate(
                    title = "安装收尾",
                    description = "设备安装和清洁",
                    dayOffset = 77,
                    duration = 13
                )
            )
        )
    )
    
    /**
     * 理财计划模板
     */
    private fun createFinancialPlanTemplate() = Template(
        title = "理财计划",
        description = "个人理财规划模板",
        category = TemplateCategory.LIFE,
        tags = listOf("理财", "储蓄", "投资"),
        color = "#FFC107",
        duration = 365,
        isSystem = true,
        structure = TemplateStructure(
            planTemplate = PlanTemplate(
                title = "年度理财计划",
                description = "实现财务目标的年度计划",
                tags = listOf("理财", "储蓄")
            ),
            subPlans = listOf(
                SubPlanTemplate(
                    title = "第一季度",
                    description = "建立理财习惯",
                    dayOffset = 0,
                    duration = 90,
                    milestones = listOf(
                        MilestoneTemplate("建立预算", "制定月度预算", 7),
                        MilestoneTemplate("开始储蓄", "完成首月储蓄目标", 30)
                    )
                ),
                SubPlanTemplate(
                    title = "第二季度",
                    description = "优化支出结构",
                    dayOffset = 90,
                    duration = 91
                ),
                SubPlanTemplate(
                    title = "第三季度",
                    description = "投资理财实践",
                    dayOffset = 181,
                    duration = 92
                ),
                SubPlanTemplate(
                    title = "第四季度",
                    description = "总结和规划",
                    dayOffset = 273,
                    duration = 92,
                    milestones = listOf(
                        MilestoneTemplate("年度总结", "完成年度财务总结", 92)
                    )
                )
            )
        )
    )
}