import { ExpenseItem } from "./expense-item"

interface ExpenseData {
  category: string
  description: string
  amount: string
  paymentMethod: string
}

interface ExpenseDayProps {
  date: string
  dayOfWeek: string
  total: string
  expenses: ExpenseData[]
}

export function ExpenseDay({ date, dayOfWeek, total, expenses }: ExpenseDayProps) {
  return (
    <div className="mb-4">
      <div className="bg-white rounded-lg shadow-sm mx-4">
        <div className="flex justify-between items-center py-3 px-4 border-b border-gray-100">
          <span className="font-medium text-gray-900">
            {date} {dayOfWeek}
          </span>
          <span className="font-medium text-gray-900">支:{total}</span>
        </div>
        {expenses.map((expense, index) => (
          <ExpenseItem key={index} {...expense} />
        ))}
      </div>
    </div>
  )
}
