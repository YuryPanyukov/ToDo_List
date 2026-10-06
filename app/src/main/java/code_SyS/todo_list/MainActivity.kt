package code_SyS.todo_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import code_SyS.todo_list.data.TodoApp
import code_SyS.todo_list.ui.calendar.CalendarScreen
import code_SyS.todo_list.ui.calendar.CalendarViewModel
import code_SyS.todo_list.ui.theme.TodoAppTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CalendarViewModel by viewModels {
        CalendarViewModelFactory((application as TodoApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoAppTheme {
                CalendarScreen(viewModel = viewModel)
            }
        }
    }
}

class CalendarViewModelFactory(
    private val repository: code_SyS.todo_list.data.TaskRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
