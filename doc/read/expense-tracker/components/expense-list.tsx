import { ExpenseDay } from "./expense-day"

const expenseData = [
  {
    date: "07.31",
    dayOfWeek: "周一",
    total: "¥33.71",
    expenses: [
      {
        category: "日用品",
        description: "香薰",
        amount: "-¥12.89",
        paymentMethod: "榕-微信零钱",
      },
      {
        category: "医疗",
        description: "酒精棉片",
        amount: "-¥6.90",
        paymentMethod: "榕-微信零钱",
      },
      {
        category: "学习",
        description: "数独",
        amount: "-¥13.92",
        paymentMethod: "榕-微信零钱",
      },
    ],
  },
  {
    date: "07.30",
    dayOfWeek: "周日",
    total: "¥244.30",
    expenses: [
      {
        category: "下馆子",
        description: "",
        amount: "-¥31.00",
        paymentMethod: "骆-微信零钱",
      },
      {
        category: "下馆子",
        description: "",
        amount: "-¥117.40",
        paymentMethod: "骆-微信零钱",
      },
      {
        category: "娱乐",
        description: "彩票",
        amount: "-¥50.00",
        paymentMethod: "骆-微信零钱",
      },
      {
        category: "衣服",
        description: "",
        amount: "-¥36.90",
        paymentMethod: "榕-支付宝",
      },
      {
        category: "交通",
        description: "停车费",
        amount: "-¥2.00",
        paymentMethod: "榕-微信零钱",
      },
      {
        category: "饮料",
        description: "",
        amount: "-¥7.00",
        paymentMethod: "榕-微信零钱",
      },
    ],
  },
]

export function ExpenseList() {
  return (
    <div className="space-y-4">
      {expenseData.map((day, index) => (
        <ExpenseDay key={index} {...day} />
      ))}
    </div>
  )
}
