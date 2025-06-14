
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jordydev.cashflow.data.local.TransactionEntity
import com.jordydev.cashflow.util.TransactionType
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionItem(
    txn: TransactionEntity,
    onEdit: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    val isFuture = txn.isGenerated && txn.date > LocalDate.now().toString()

    var displayedAmount by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(txn.id) {
        displayedAmount = 0f
        kotlinx.coroutines.delay(50) // small delay for smoothness
        displayedAmount = txn.amount.toFloat()
    }

    val animatedAmount by animateFloatAsState(
        targetValue = displayedAmount,
        animationSpec = tween(durationMillis = 600)
    )
    val dismissState = rememberDismissState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            DismissValue.DismissedToStart -> {
                onDelete(txn)
                dismissState.reset()
            }
            DismissValue.DismissedToEnd -> {
                onEdit(txn)
                dismissState.reset()
            }
            else -> {}
        }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(
            DismissDirection.StartToEnd,
            DismissDirection.EndToStart
        ),
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.Edit
                DismissDirection.EndToStart -> Icons.Default.Delete
            }
            val color = when (direction) {
                DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary
                DismissDirection.EndToStart -> MaterialTheme.colorScheme.error
            }

            Surface(
                color = color,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = if (direction == DismissDirection.StartToEnd)
                        Arrangement.Start else Arrangement.End
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                }
            }
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor =
                    if (isFuture) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = txn.title,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1
                        )
                        Text(
                            text = txn.frequency.displayName(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.widthIn(min = 110.dp) // Consistent width for right column
                    ) {
                        Text(
                            text = if (txn.type == TransactionType.INCOME)
                                "+$${String.format("%.2f", animatedAmount)}"
                            else
                                "-$${String.format("%.2f", animatedAmount)}",
                            color = if (txn.type == TransactionType.INCOME) Color.Green else Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = txn.date,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    )
}


