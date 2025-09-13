export function ExpenseHeader() {
  return (
    <div
      className="!bg-blue-500 text-white px-4 py-6"
      style={{
        backgroundColor: "#4F7EFF !important",
        background: "linear-gradient(to right, #4F7EFF, #6B8FFF) !important",
      }}
    >
      {/* Navigation bar */}
      <div className="flex justify-between items-center mb-6">
        <div className="w-6 h-6 text-white text-xl">☰</div>
        <div className="flex items-center gap-2">
          <span className="text-lg font-medium text-white">2023-07</span>
          <div className="w-4 h-4 text-white text-sm">▼</div>
        </div>
        <div className="flex gap-3">
          <div className="w-6 h-6 text-white text-xl">📅</div>
          <div className="w-6 h-6 text-white text-xl">📊</div>
          <div className="w-6 h-6 text-white text-xl">🔄</div>
        </div>
      </div>

      {/* Monthly summary */}
      <div className="space-y-2">
        <div className="text-sm text-white opacity-80">月支出</div>
        <div className="text-3xl font-bold text-white">¥7947.38</div>
        <div className="flex justify-between text-sm text-white">
          <span>月收入 ¥4768.00</span>
          <span>本月结余 -¥3179.38</span>
        </div>
      </div>
    </div>
  )
}
