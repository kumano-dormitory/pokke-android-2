package com.kumanodormitory.pokke.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kumanodormitory.pokke.data.local.entity.RyoseiEntity
import com.kumanodormitory.pokke.ui.util.debounceClickableItem

// 旧アプリの色定義を再現
private val ListBorderColor = Color(0xFFAAAAAA)
private val SelectedBlockColor = Color(0xFFADD8E6)   // lightblue — 旧アプリのブロック選択色
private val SelectedRoomColor = Color(0xFFADD8E6)     // lightblue
private val ListItemTextColor = Color.Black
private val DisabledTextColor = Color(0xFFA0A0A0)
private val DividerColor = Color(0xFFDDDDDD)

@Composable
fun ThreeColumnSelector(
    blocks: List<String>,
    rooms: List<String>,
    ryoseiList: List<RyoseiEntity>,
    onRyoseiSelected: (RyoseiEntity) -> Unit,
    selectedBlock: String?,
    onBlockSelected: (String) -> Unit,
    selectedRoom: String?,
    onRoomSelected: (String) -> Unit,
    showSearch: Boolean = false,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: () -> Unit = {},
    modifier: Modifier = Modifier,
    isRyoseiEnabled: (RyoseiEntity) -> Boolean = { true },
    ryoseiSuffix: (RyoseiEntity) -> String? = { null },
    onBlockClick: (() -> Unit)? = null,
    onRoomClick: (() -> Unit)? = null,
    onRyoseiClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) {
        // ===== ブロックカラム =====
        ListColumn(
            modifier = Modifier
                .weight(0.15f)
                .fillMaxHeight()
        ) {
            items(blocks) { block ->
                val isSelected = block == selectedBlock
                ListItem(
                    text = block,
                    isSelected = isSelected,
                    selectedColor = SelectedBlockColor,
                    onClick = {
                        onBlockClick?.invoke()
                        onBlockSelected(block)
                    }
                )
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }
        }

        Spacer(modifier = Modifier.weight(0.025f))

        // ===== 部屋カラム =====
        ListColumn(
            modifier = Modifier
                .weight(0.15f)
                .fillMaxHeight()
        ) {
            items(rooms) { room ->
                val isSelected = room == selectedRoom
                ListItem(
                    text = room,
                    isSelected = isSelected,
                    selectedColor = SelectedRoomColor,
                    onClick = {
                        onRoomClick?.invoke()
                        onRoomSelected(room)
                    }
                )
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }
        }

        Spacer(modifier = Modifier.weight(0.025f))

        // ===== 寮生カラム（検索バー付き） =====
        Column(
            modifier = Modifier
                .weight(0.55f)
                .fillMaxHeight()
        ) {
            // 検索バー（旧: EditText 225dp + ImageButton 40dp）
            if (showSearch) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("検索したい名前を入力", fontSize = 14.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() })
                    )
                    IconButton(
                        onClick = onSearchSubmit,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "検索",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // 寮生リスト
            ListColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(ryoseiList) { ryosei ->
                    val enabled = isRyoseiEnabled(ryosei)
                    val suffix = ryoseiSuffix(ryosei)
                    RyoseiListItem(
                        ryosei = ryosei,
                        enabled = enabled,
                        suffix = suffix,
                        onClick = {
                            if (enabled) {
                                onRyoseiClick?.invoke()
                                onRyoseiSelected(ryosei)
                            }
                        }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                }
            }
        }
    }
}

// ===== リストカラム（枠線付き） =====
@Composable
private fun ListColumn(
    modifier: Modifier = Modifier,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    Box(
        modifier = modifier
            .border(1.dp, ListBorderColor)
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            content()
        }
    }
}

// ===== 汎用リストアイテム（ブロック・部屋用） =====
@Composable
private fun ListItem(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) selectedColor else Color.Transparent)
            .debounceClickableItem(400L) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = ListItemTextColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ===== 寮生リストアイテム（旧: "部屋番号 寮生名" 形式） =====
@Composable
private fun RyoseiListItem(
    ryosei: RyoseiEntity,
    enabled: Boolean,
    suffix: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.debounceClickableItem(400L) { onClick() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row {
            Text(
                text = "${ryosei.room}  ${ryosei.name}",
                fontSize = 20.sp,
                color = if (enabled) ListItemTextColor else DisabledTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (suffix != null) {
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = suffix,
                    fontSize = 16.sp,
                    color = Color.Red
                )
            }
        }
    }
}
