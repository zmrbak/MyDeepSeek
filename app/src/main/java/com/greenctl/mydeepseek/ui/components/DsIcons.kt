package com.greenctl.mydeepseek.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DsIconType {
    Menu,
    Plus,
    Back,
    Search,
    Send,
    Stop,
    Copy,
    Regenerate,
    Like,
    Dislike,
    Bulb,
    Globe,
    Attach,
    Mic,
    Pin,
    Trash,
    Edit,
    More,
    Close,
    ChevronDown,
    ChevronRight,
    Check,
    Gear,
    Speaker,
}

/**
 * 图标统一使用 Material Outlined 线性图标，与 DeepSeek 客户端的细线风格一致。
 */
@Composable
fun DsIcon(
    type: DsIconType,
    tint: Color,
    size: Dp = 22.dp,
    strokeWidth: Dp = 1.7.dp,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = type.vector,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/**
 * 官方客户端的两横线菜单图标：上长下短，左对齐。
 */
@Composable
fun DsMenuIcon(
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
) {
    Canvas(modifier.size(size)) {
        val stroke = 1.8.dp.toPx()
        val y1 = this.size.height * 0.32f
        val y2 = this.size.height * 0.72f
        drawLine(tint, Offset(0f, y1), Offset(this.size.width, y1), stroke, StrokeCap.Round)
        drawLine(tint, Offset(0f, y2), Offset(this.size.width * 0.56f, y2), stroke, StrokeCap.Round)
    }
}

private val DsIconType.vector: ImageVector
    get() = when (this) {
        DsIconType.Menu -> Icons.Outlined.Menu
        DsIconType.Plus -> Icons.Outlined.Add
        DsIconType.Back -> Icons.Outlined.ArrowBack
        DsIconType.Search -> Icons.Outlined.Search
        DsIconType.Send -> Icons.Outlined.ArrowUpward
        DsIconType.Stop -> Icons.Outlined.Stop
        DsIconType.Copy -> Icons.Outlined.ContentCopy
        DsIconType.Regenerate -> Icons.Outlined.Refresh
        DsIconType.Like -> Icons.Outlined.ThumbUp
        DsIconType.Dislike -> Icons.Outlined.ThumbDown
        DsIconType.Bulb -> Icons.Outlined.Psychology
        DsIconType.Globe -> Icons.Outlined.Language
        DsIconType.Attach -> Icons.Outlined.AttachFile
        DsIconType.Mic -> Icons.Outlined.GraphicEq
        DsIconType.Pin -> Icons.Outlined.PushPin
        DsIconType.Trash -> Icons.Outlined.Delete
        DsIconType.Edit -> Icons.Outlined.EditNote
        DsIconType.More -> Icons.Outlined.MoreHoriz
        DsIconType.Close -> Icons.Outlined.Close
        DsIconType.ChevronDown -> Icons.Outlined.KeyboardArrowDown
        DsIconType.ChevronRight -> Icons.Outlined.KeyboardArrowRight
        DsIconType.Check -> Icons.Outlined.Check
        DsIconType.Gear -> Icons.Outlined.Settings
        DsIconType.Speaker -> Icons.Outlined.VolumeUp
    }
