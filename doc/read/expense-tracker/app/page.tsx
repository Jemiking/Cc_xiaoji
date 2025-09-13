export default function ExpenseTracker() {
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <div className="bg-blue-500 text-white px-4 py-4">
        <div className="flex justify-between items-center mb-6">
          <div className="text-white text-xl">☰</div>
          <div className="flex items-center gap-1">
            <span className="text-lg font-medium">2023-07</span>
            <span className="text-sm">▼</span>
          </div>
          <div className="flex gap-3">
            <div className="w-8 h-8 border border-white/30 rounded flex items-center justify-center">
              <span className="text-sm">📅</span>
            </div>
            <div className="w-8 h-8 border border-white/30 rounded flex items-center justify-center">
              <span className="text-sm">📊</span>
            </div>
            <div className="w-8 h-8 border border-white/30 rounded flex items-center justify-center">
              <span className="text-sm">↻</span>
            </div>
          </div>
        </div>

        <div className="mb-2">
          <div className="text-sm opacity-90">月支出</div>
          <div className="text-3xl font-light">¥7947.38</div>
        </div>

        <div className="flex justify-between text-sm">
          <span>月收入 ¥4768.00</span>
          <span>本月结余 -¥3179.38</span>
        </div>
      </div>

      {/* Content */}
      <div className="px-4 pb-20">
        {/* Budget Section */}
        <div className="bg-white rounded-lg px-4 py-3 mt-4 flex justify-between items-center">
          <div>
            <span className="text-gray-900 font-medium">预算</span>
            <div className="text-sm text-gray-500 mt-1">剩余: --</div>
          </div>
          <div className="text-right">
            <div className="text-xs text-gray-400">⋯</div>
            <div className="text-sm text-gray-500 mt-1">总额: 未设置</div>
          </div>
        </div>

        {/* Expense Lists */}
        <div className="mt-4 space-y-4">
          {/* 07.31 */}
          <div className="bg-white rounded-lg overflow-hidden">
            <div className="flex justify-between items-center px-4 py-3 border-b border-gray-100">
              <span className="text-gray-900 font-medium">07.31 周一</span>
              <span className="text-gray-900 font-medium">支:¥33.71</span>
            </div>
            <div className="flex items-center px-4 py-3 border-b border-gray-100">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">日用品</div>
                <div className="text-sm text-gray-500">香薰</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥12.89</div>
                <div className="text-xs text-gray-400">榕-微信零钱</div>
              </div>
            </div>
            <div className="flex items-center px-4 py-3 border-b border-gray-100">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">医疗</div>
                <div className="text-sm text-gray-500">酒精棉片</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥6.90</div>
                <div className="text-xs text-gray-400">榕-微信零钱</div>
              </div>
            </div>
            <div className="flex items-center px-4 py-3">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">学习</div>
                <div className="text-sm text-gray-500">数独</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥13.92</div>
                <div className="text-xs text-gray-400">榕-微信零钱</div>
              </div>
            </div>
          </div>

          {/* 07.30 */}
          <div className="bg-white rounded-lg overflow-hidden">
            <div className="flex justify-between items-center px-4 py-3 border-b border-gray-100">
              <span className="text-gray-900 font-medium">07.30 周日</span>
              <span className="text-gray-900 font-medium">支:¥244.30</span>
            </div>
            <div className="flex items-center px-4 py-3 border-b border-gray-100">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">下馆子</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥31.00</div>
                <div className="text-xs text-gray-400">骆-微信零钱</div>
              </div>
            </div>
            <div className="flex items-center px-4 py-3 border-b border-gray-100">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">下馆子</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥117.40</div>
                <div className="text-xs text-gray-400">骆-微信零钱</div>
              </div>
            </div>
            <div className="flex items-center px-4 py-3 border-b border-gray-100">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">娱乐</div>
                <div className="text-sm text-gray-500">彩票</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥50.00</div>
                <div className="text-xs text-gray-400">骆-微信零钱</div>
              </div>
            </div>
            <div className="flex items-center px-4 py-3 border-b border-gray-100">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">衣服</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥36.90</div>
                <div className="text-xs text-gray-400">榕-支付宝</div>
              </div>
            </div>
            <div className="flex items-center px-4 py-3 border-b border-gray-100">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">交通</div>
                <div className="text-sm text-gray-500">停车费</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥2.00</div>
                <div className="text-xs text-gray-400">榕-微信零钱</div>
              </div>
            </div>
            <div className="flex items-center px-4 py-3">
              <div className="w-2 h-2 bg-red-500 rounded-full mr-3"></div>
              <div className="flex-1">
                <div className="text-gray-900 font-medium">饮料</div>
              </div>
              <div className="text-right">
                <div className="text-red-500 font-medium">-¥7.00</div>
                <div className="text-xs text-gray-400">榕-微信零钱</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Shadow */}
      <div className="fixed bottom-0 left-0 right-0 h-20 bg-gradient-to-t from-gray-100 via-gray-100/80 to-transparent pointer-events-none" />

      {/* Floating Add Button */}
      <button className="fixed bottom-8 left-1/2 transform -translate-x-1/2 w-14 h-14 bg-blue-500 rounded-full shadow-lg flex items-center justify-center text-white text-2xl font-light">
        +
      </button>
    </div>
  )
}
