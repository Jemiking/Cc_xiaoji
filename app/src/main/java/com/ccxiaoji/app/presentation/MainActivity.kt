package com.ccxiaoji.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ccxiaoji.ui.theme.CcXiaoJiTheme
import com.ccxiaoji.app.presentation.ui.components.BottomNavBar
import com.ccxiaoji.app.presentation.ui.navigation.NavGraph
import com.ccxiaoji.app.presentation.ui.navigation.Screen
import com.ccxiaoji.app.notification.NotificationScheduler
import com.ccxiaoji.app.data.sync.SyncManager
import com.ccxiaoji.feature.ledger.api.LedgerApi
import com.ccxiaoji.feature.plan.api.PlanApi
import com.ccxiaoji.feature.ledger.worker.creditcard.CreditCardReminderManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    @Inject
    lateinit var notificationScheduler: NotificationScheduler
    
    @Inject
    lateinit var syncManager: SyncManager
    
    @Inject
    lateinit var creditCardReminderManager: CreditCardReminderManager
    
    @Inject
    lateinit var ledgerApi: LedgerApi
    
    @Inject
    lateinit var planApi: PlanApi
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate started")
        
        try {
            // enableEdgeToEdge()  // 移除以解决顶部空隙问题
            Log.d(TAG, "Edge to edge disabled to fix top gap")
            
            // 启动每日检查任务
            Log.d(TAG, "Scheduling daily check")
            notificationScheduler.scheduleDailyCheck()
            Log.d(TAG, "Daily check scheduled")
            
            // 启动定期同步
            Log.d(TAG, "Starting periodic sync")
            syncManager.startPeriodicSync()
            Log.d(TAG, "Periodic sync started")
            
            // 启动信用卡还款提醒
            Log.d(TAG, "Starting credit card reminders")
            creditCardReminderManager.startPeriodicReminders()
            Log.d(TAG, "Credit card reminders started")
            
            Log.d(TAG, "Setting content")
            setContent {
            CcXiaoJiTheme {
                val navController = rememberNavController()
                
                // 获取当前路由
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // 定义需要显示底部导航栏的路由
                val routesWithBottomBar = setOf(
                    Screen.Home.route,
                    Screen.Profile.route
                )
                
                // 判断是否显示底部导航栏
                val showBottomBar = currentRoute in routesWithBottomBar
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        ledgerApi = ledgerApi,
                        planApi = planApi,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            }
            
            Log.d(TAG, "MainActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in MainActivity onCreate", e)
            throw e
        }
    }
}