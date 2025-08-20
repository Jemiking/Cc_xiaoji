package com.ccxiaoji.feature.ledger.domain.model

/**
 * 地理位置数据模型
 * 用于记录交易发生的地理位置信息
 */
data class LocationData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String? = null,
    val precision: Float? = null, // GPS精度（米）
    val provider: String? = null // 位置提供者（"gps", "network", "passive"）
) {
    /**
     * 检查是否有有效的坐标信息
     */
    fun hasValidCoordinates(): Boolean {
        return latitude != null && longitude != null && 
               latitude >= -90.0 && latitude <= 90.0 &&
               longitude >= -180.0 && longitude <= 180.0
    }
    
    /**
     * 格式化显示地址
     */
    fun getDisplayAddress(): String? {
        return when {
            !address.isNullOrBlank() -> address
            hasValidCoordinates() -> "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
            else -> null
        }
    }
    
    /**
     * 检查位置精度是否足够好（< 100米）
     */
    fun hasGoodPrecision(): Boolean {
        return precision != null && precision < 100f
    }
}