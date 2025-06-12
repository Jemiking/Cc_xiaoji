package com.ccxiaoji.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ccxiaoji.app.presentation.theme.CcXiaoJiTheme
import com.ccxiaoji.app.presentation.ui.components.BottomNavBar
import com.ccxiaoji.app.presentation.ui.navigation.NavGraph
import com.ccxiaoji.app.navigation.TodoNavigatorImpl
import com.ccxiaoji.app.navigation.HabitNavigatorImpl
import com.ccxiaoji.app.navigation.LedgerNavigatorImpl
import com.ccxiaoji.shared.notification.api.NotificationApi
import com.ccxiaoji.shared.sync.api.SyncApi
import com.ccxiaoji.app.data.sync.CreditCardReminderManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.util.Log

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "CcXiaoJi"
    }
    
    @Inject
    lateinit var notificationApi: NotificationApi
    
    @Inject
    lateinit var syncApi: SyncApi
    
    @Inject
    lateinit var creditCardReminderManager: CreditCardReminderManager
    
    @Inject
    lateinit var todoNavigator: TodoNavigatorImpl
    
    @Inject
    lateinit var habitNavigator: HabitNavigatorImpl
    
    @Inject
    lateinit var ledgerNavigator: LedgerNavigatorImpl
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate started")
        
        try {
            enableEdgeToEdge()
            Log.d(TAG, "Edge to edge enabled")
            
            // 启动每日检查任务
            Log.d(TAG, "Scheduling daily check")
            lifecycleScope.launch {
                notificationApi.scheduleDailyCheck()
                Log.d(TAG, "Daily check scheduled")
            }
            
            // 启动定期同步
            Log.d(TAG, "Starting periodic sync")
            lifecycleScope.launch {
                syncApi.startPeriodicSync()
                Log.d(TAG, "Periodic sync started")
            }
            
            // 启动信用卡还款提醒
            Log.d(TAG, "Starting credit card reminders")
            creditCardReminderManager.startPeriodicReminders()
            Log.d(TAG, "Credit card reminders started")
            
            Log.d(TAG, "Setting content")
            setContent {
            CcXiaoJiTheme {
                val navController = rememberNavController()
                
                // 设置Navigator的NavController
                todoNavigator.setNavController(navController)
                habitNavigator.setNavController(navController)
                ledgerNavigator.setNavController(navController)
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavBar(navController = navController)
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
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