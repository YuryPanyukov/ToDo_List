package code_SyS.todo_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import code_SyS.todo_list.data.TodoApp
import code_SyS.todo_list.ui.AppRoot
import code_SyS.todo_list.ui.calendar.CalendarViewModel
import code_SyS.todo_list.ui.theme.TodoAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory((application as TodoApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Нативный splash (Android 12+ и его backport): показывается до первого кадра Compose,
        // дальше его подхватывает собственный StartScreen.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TodoApp
        setContent {
            TodoAppTheme {
                AppRoot(viewModel = viewModel, prefs = app.prefs)
            }
        }
    }
}

class CalendarViewModelFactory(
    private val repository: code_SyS.todo_list.data.TaskRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
