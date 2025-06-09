package com.ccxiaoji.core.common.constants

/**
 * 应用程序常量定义
 */
object Constants {
    
    /**
     * 数据库相关常量
     */
    object Database {
        const val DATABASE_NAME = "cc_xiaoji.db"
        const val DATABASE_VERSION = 1
    }
    
    /**
     * SharedPreferences/DataStore相关常量
     */
    object Preferences {
        const val PREFERENCES_NAME = "cc_xiaoji_preferences"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_LANGUAGE = "language"
        const val KEY_FIRST_LAUNCH = "first_launch"
        const val KEY_USER_ID = "user_id"
    }
    
    /**
     * 网络相关常量
     */
    object Network {
        const val BASE_URL = "https://api.ccxiaoji.com/"
        const val CONNECT_TIMEOUT = 30L // 秒
        const val READ_TIMEOUT = 30L // 秒
        const val WRITE_TIMEOUT = 30L // 秒
    }
    
    /**
     * 日期格式常量
     */
    object DateFormat {
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val TIME_PATTERN = "HH:mm:ss"
        const val DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
        const val MONTH_PATTERN = "yyyy-MM"
        const val YEAR_PATTERN = "yyyy"
    }
    
    /**
     * 通知相关常量
     */
    object Notification {
        const val CHANNEL_ID_DEFAULT = "cc_xiaoji_default"
        const val CHANNEL_ID_REMINDER = "cc_xiaoji_reminder"
        const val CHANNEL_ID_SYNC = "cc_xiaoji_sync"
    }
    
    /**
     * WorkManager相关常量
     */
    object Worker {
        const val TAG_SYNC = "sync_worker"
        const val TAG_RECURRING_TRANSACTION = "recurring_transaction_worker"
        const val TAG_CREDIT_CARD_BILL = "credit_card_bill_worker"
        const val TAG_BACKUP = "backup_worker"
    }
}