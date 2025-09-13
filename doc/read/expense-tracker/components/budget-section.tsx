export function BudgetSection() {
  return (
    <div className="px-4 py-4">
      <div className="flex justify-between items-center">
        <h3 className="font-medium text-gray-900">预算</h3>
        <div className="w-5 h-5 text-gray-400 text-lg">⋯</div>
      </div>
      <div className="flex justify-between items-center mt-3 text-sm text-gray-500">
        <span>剩余: --</span>
        <span>总额: 未设置</span>
      </div>
    </div>
  )
}
