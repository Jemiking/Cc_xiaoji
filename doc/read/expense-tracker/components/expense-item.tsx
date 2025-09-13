interface ExpenseItemProps {
  category: string
  description: string
  amount: string
  paymentMethod: string
}

const categoryIcons: Record<string, string> = {
  日用品: "🧴",
  医疗: "💊",
  学习: "📚",
  下馆子: "🍽️",
  娱乐: "🎮",
  衣服: "👕",
  交通: "🚗",
  饮料: "🥤",
}

export function ExpenseItem({ category, description, amount, paymentMethod }: ExpenseItemProps) {
  return (
    <div
      className="flex items-center justify-between px-4 py-3 !bg-white"
      style={{
        backgroundColor: "#ffffff !important",
        border: "none !important",
        outline: "none !important",
        boxShadow: "none !important",
        cursor: "default !important",
      }}
    >
      <div className="flex items-center gap-3">
        <div className="w-2 h-2 bg-red-400 rounded-full"></div>
        <div>
          <div className="font-medium !text-gray-900" style={{ color: "#111827 !important" }}>
            {category}
          </div>
          {description && (
            <div className="text-sm !text-gray-600" style={{ color: "#4b5563 !important" }}>
              {description}
            </div>
          )}
        </div>
      </div>
      <div className="text-right">
        <div className="font-medium !text-red-600" style={{ color: "#dc2626 !important" }}>
          {amount}
        </div>
        <div className="text-xs !text-gray-500" style={{ color: "#6b7280 !important" }}>
          {paymentMethod}
        </div>
      </div>
    </div>
  )
}
