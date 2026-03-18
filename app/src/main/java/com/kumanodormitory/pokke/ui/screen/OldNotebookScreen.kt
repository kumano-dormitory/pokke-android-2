package com.kumanodormitory.pokke.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumanodormitory.pokke.data.local.entity.ParcelEntity
import com.kumanodormitory.pokke.ui.util.SoundManager
import com.kumanodormitory.pokke.ui.util.formatDateTime
import com.kumanodormitory.pokke.ui.util.formatParcelType
import com.kumanodormitory.pokke.ui.viewmodel.OldNotebookViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 旧アプリ準拠カラー
private val OldNoteHeaderBg = Color(0xFFEEEEEE) // oldnote_theme
private val RowEvenBg = Color.White
private val RowOddBg = Color(0xFFEEEEEE) // verylightgray
private val RowSeparator = Color(0xFFA9A9A9) // darkgray

// 旧アプリ準拠: 列weight比率
// 受取日時(10), 持ち主(7), 受取事務当(7), 種類(4), 引渡日時(6), 引渡者(7), 荷物確認日(10)
private const val W_REGISTER_TIME = 10f
private const val W_OWNER = 7f
private const val W_REGISTER_STAFF = 7f
private const val W_TYPE = 4f
private const val W_RELEASE_TIME = 6f
private const val W_RELEASE_STAFF = 7f
private const val W_LAST_CONFIRMED = 10f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OldNotebookScreen(
    viewModel: OldNotebookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    val selectedBlock by viewModel.selectedBlock.collectAsState()

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showBlockDropdown by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN) }

    val blockOptions = listOf(
        null to "全体",
        "A" to "A棟",
        "B" to "B棟",
        "C" to "C棟",
        "臨キャパ" to "臨キャパ"
    )

    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        // カスタムヘッダー（TopAppBarの高さ制限を回避）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(OldNoteHeaderBg)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1段目: 戻るボタン + タイトル + ブロック選択
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "戻る", tint = Color.Black)
                }
                Text(
                    text = "荷物履歴一覧",
                    fontSize = 22.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "ブロック:", fontSize = 16.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Box {
                    OutlinedButton(onClick = { showBlockDropdown = true }) {
                        Text(
                            text = blockOptions.find { it.first == selectedBlock }?.second ?: "全体",
                            fontSize = 16.sp
                        )
                    }
                    DropdownMenu(
                        expanded = showBlockDropdown,
                        onDismissRequest = { showBlockDropdown = false }
                    ) {
                        blockOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.setBlock(value)
                                    showBlockDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2段目: 日付選択ボタン + プリセット + 検索
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp)
            ) {
                OutlinedButton(onClick = { showStartDatePicker = true }) {
                    Text(
                        text = dateFormat.format(Date(startDate)),
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = " ～ ",
                    fontSize = 14.sp,
                    color = Color.Black
                )
                OutlinedButton(onClick = { showEndDatePicker = true }) {
                    Text(
                        text = dateFormat.format(Date(endDate)),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = {
                        SoundManager.playSearch(context)
                        val today = todayRange(0)
                        viewModel.setStartDate(today.first)
                        viewModel.setEndDate(today.second)
                        viewModel.loadParcels()
                    }
                ) {
                    Text("今日", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = {
                        SoundManager.playSearch(context)
                        val range = todayRange(3)
                        viewModel.setStartDate(range.first)
                        viewModel.setEndDate(range.second)
                        viewModel.loadParcels()
                    }
                ) {
                    Text("3日間", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = {
                        SoundManager.playSearch(context)
                        val range = todayRange(7)
                        viewModel.setStartDate(range.first)
                        viewModel.setEndDate(range.second)
                        viewModel.loadParcels()
                    }
                ) {
                    Text("1週間", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(onClick = {
                    SoundManager.playSearch(context)
                    viewModel.loadParcels()
                }) {
                    Text("検索", fontSize = 14.sp)
                }
            }
        }

        // テーブル本体
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 34.dp, end = 34.dp)
        ) {
            // テーブルヘッダー（旧アプリ準拠: 黒文字、20sp、weight比率）
            OldNoteTableHeader()

            // テーブル本体
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.parcels.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "該当する荷物はありません",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(uiState.parcels) { index, parcel ->
                        OldNoteTableRow(
                            parcel = parcel,
                            bgColor = if (index % 2 == 0) RowEvenBg else RowOddBg
                        )
                    }
                }
            }
        }
    }

    // 開始日DatePicker（独立選択）
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setStartDate(it) }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 終了日DatePicker（独立選択）
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setEndDate(it) }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("キャンセル") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * プリセット日付範囲を計算する。
 * @param daysBack 0=今日のみ, 3=3日前～今日, 7=1週間前～今日
 * @return Pair(startMillis, endMillis) startは0:00, endは23:59:59.999
 */
private fun todayRange(daysBack: Int): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    // 今日の23:59:59.999
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val endMillis = cal.timeInMillis

    // daysBack日前の0:00
    cal.add(Calendar.DAY_OF_YEAR, -daysBack)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startMillis = cal.timeInMillis

    return startMillis to endMillis
}

@Composable
private fun OldNoteTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
    ) {
        OldNoteHeaderCell(text = "受取日時", weight = W_REGISTER_TIME)
        OldNoteHeaderCell(text = "持ち主", weight = W_OWNER)
        OldNoteHeaderCell(text = "受取事務当", weight = W_REGISTER_STAFF)
        OldNoteHeaderCell(text = "種類", weight = W_TYPE)
        OldNoteHeaderCell(text = "引渡日時", weight = W_RELEASE_TIME)
        OldNoteHeaderCell(text = "引渡者", weight = W_RELEASE_STAFF)
        OldNoteHeaderCell(text = "荷物確認日", weight = W_LAST_CONFIRMED)
    }
}

@Composable
private fun RowScope.OldNoteHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        textAlign = TextAlign.Center,
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun OldNoteTableRow(parcel: ParcelEntity, bgColor: Color) {
    // 旧アプリ準拠: darkgray(#A9A9A9)背景の行 + 白/灰色のセル背景
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RowSeparator) // 行間のセパレータ色
    ) {
        OldNoteDataCell(
            text = formatDateTime(parcel.createdAt),
            weight = W_REGISTER_TIME,
            bgColor = bgColor
        )
        OldNoteDataCell(
            text = "${parcel.ownerRoomName} ${parcel.ownerName}",
            weight = W_OWNER,
            bgColor = bgColor
        )
        OldNoteDataCell(
            text = "(${parcel.registeredByName})",
            weight = W_REGISTER_STAFF,
            bgColor = bgColor
        )
        OldNoteDataCell(
            text = formatParcelType(parcel.parcelType),
            weight = W_TYPE,
            bgColor = bgColor
        )
        OldNoteDataCell(
            text = parcel.deliveredAt?.let { formatDateTime(it) } ?: "",
            weight = W_RELEASE_TIME,
            bgColor = bgColor
        )
        OldNoteDataCell(
            text = parcel.deliveredByName?.let { "($it)" } ?: "",
            weight = W_RELEASE_STAFF,
            bgColor = bgColor
        )
        OldNoteDataCell(
            text = parcel.lastConfirmedAt?.let { formatDateTime(it) } ?: "",
            weight = W_LAST_CONFIRMED,
            bgColor = bgColor
        )
    }
}

@Composable
private fun RowScope.OldNoteDataCell(
    text: String,
    weight: Float,
    bgColor: Color
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .background(bgColor)
            .padding(start = 1.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
