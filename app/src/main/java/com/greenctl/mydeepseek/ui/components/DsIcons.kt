package com.greenctl.mydeepseek.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

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
 * 手绘线性图标，风格对齐 DeepSeek 客户端的细线图标。
 */
@Composable
fun DsIcon(
    type: DsIconType,
    tint: Color,
    size: Dp = 22.dp,
    strokeWidth: Dp = 1.7.dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(size)) {
        drawDsIcon(type, tint, strokeWidth.toPx())
    }
}

private fun DrawScope.drawDsIcon(type: DsIconType, tint: Color, stroke: Float) {
    val u = this.size.minDimension / 24f
    val w = stroke

    fun line(x1: Float, y1: Float, x2: Float, y2: Float) {
        drawLine(tint, Offset(x1 * u, y1 * u), Offset(x2 * u, y2 * u), strokeWidth = w, cap = StrokeCap.Round)
    }

    fun circle(cx: Float, cy: Float, r: Float) {
        drawCircle(tint, r * u, Offset(cx * u, cy * u), style = Stroke(w))
    }

    fun strokePath(block: Path.() -> Unit) {
        val path = Path().apply(block)
        drawPath(path, tint, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    fun fillPath(block: Path.() -> Unit) {
        val path = Path().apply(block)
        drawPath(path, tint)
    }

    fun roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float) {
        drawRoundRect(
            color = tint,
            topLeft = Offset(left * u, top * u),
            size = Size((right - left) * u, (bottom - top) * u),
            cornerRadius = CornerRadius(radius * u),
            style = Stroke(w),
        )
    }

    fun thumbUp() {
        roundRect(9.5f, 11f, 19.5f, 20f, 2.6f)
        strokePath {
            moveTo(9.5f * u, 11f * u)
            lineTo(9.5f * u, 7.4f * u)
            quadraticBezierTo(9.5f * u, 5.4f * u, 11.1f * u, 4.9f * u)
            quadraticBezierTo(12.7f * u, 4.4f * u, 12.9f * u, 6.6f * u)
            lineTo(12.9f * u, 11f * u)
        }
    }

    when (type) {
        DsIconType.Menu -> {
            line(4f, 7f, 20f, 7f)
            line(4f, 12f, 20f, 12f)
            line(4f, 17f, 20f, 17f)
        }

        DsIconType.Plus -> {
            line(12f, 5f, 12f, 19f)
            line(5f, 12f, 19f, 12f)
        }

        DsIconType.Back -> {
            line(19f, 12f, 5.5f, 12f)
            line(11f, 5.8f, 5f, 12f)
            line(11f, 18.2f, 5f, 12f)
        }

        DsIconType.Search -> {
            circle(10.8f, 10.8f, 6.4f)
            line(15.5f, 15.5f, 20f, 20f)
        }

        DsIconType.Send -> {
            line(12f, 19f, 12f, 5.5f)
            line(6f, 11.5f, 12f, 5.5f)
            line(18f, 11.5f, 12f, 5.5f)
        }

        DsIconType.Stop -> {
            drawRoundRect(
                color = tint,
                topLeft = Offset(8f * u, 8f * u),
                size = Size(8f * u, 8f * u),
                cornerRadius = CornerRadius(2f * u),
            )
        }

        DsIconType.Copy -> {
            roundRect(8.5f, 8.5f, 20f, 20f, 2.6f)
            strokePath {
                moveTo(16f * u, 8.5f * u)
                lineTo(16f * u, 6f * u)
                quadraticBezierTo(16f * u, 4f * u, 14f * u, 4f * u)
                lineTo(6f * u, 4f * u)
                quadraticBezierTo(4f * u, 4f * u, 4f * u, 6f * u)
                lineTo(4f * u, 14f * u)
                quadraticBezierTo(4f * u, 16f * u, 6f * u, 16f * u)
                lineTo(8.5f * u, 16f * u)
            }
        }

        DsIconType.Regenerate -> {
            drawArc(
                color = tint,
                startAngle = 0f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(4f * u, 4f * u),
                size = Size(16f * u, 16f * u),
                style = Stroke(w, cap = StrokeCap.Round),
            )
            line(8.6f, 7.4f, 12f, 4f)
            line(15.4f, 7.4f, 12f, 4f)
        }

        DsIconType.Like -> thumbUp()

        DsIconType.Dislike -> scale(scaleX = 1f, scaleY = -1f, pivot = center) { thumbUp() }

        DsIconType.Bulb -> {
            circle(12f, 9.6f, 4.8f)
            line(9.9f, 16.6f, 14.1f, 16.6f)
            line(10.4f, 19.4f, 13.6f, 19.4f)
            line(10f, 16.2f, 10.5f, 14.4f)
            line(14f, 16.2f, 13.5f, 14.4f)
        }

        DsIconType.Globe -> {
            circle(12f, 12f, 8f)
            drawOval(
                color = tint,
                topLeft = Offset(8f * u, 4f * u),
                size = Size(8f * u, 16f * u),
                style = Stroke(w),
            )
            line(4f, 12f, 20f, 12f)
        }

        DsIconType.Attach -> strokePath {
            moveTo(16.8f * u, 9f * u)
            lineTo(16.8f * u, 15.5f * u)
            quadraticBezierTo(16.8f * u, 19.2f * u, 13.2f * u, 19.2f * u)
            quadraticBezierTo(9.6f * u, 19.2f * u, 9.6f * u, 15.5f * u)
            lineTo(9.6f * u, 7.3f * u)
            quadraticBezierTo(9.6f * u, 4.8f * u, 12.1f * u, 4.8f * u)
            quadraticBezierTo(14.6f * u, 4.8f * u, 14.6f * u, 7.3f * u)
            lineTo(14.6f * u, 14.4f * u)
            quadraticBezierTo(14.6f * u, 16.4f * u, 12.6f * u, 16.4f * u)
            quadraticBezierTo(11.2f * u, 16.4f * u, 11.2f * u, 14.4f * u)
            lineTo(11.2f * u, 8.4f * u)
        }

        DsIconType.Mic -> {
            roundRect(9f, 3f, 15f, 13f, 3f)
            strokePath {
                moveTo(6.5f * u, 11.5f * u)
                quadraticBezierTo(6.5f * u, 17.2f * u, 12f * u, 17.2f * u)
                quadraticBezierTo(17.5f * u, 17.2f * u, 17.5f * u, 11.5f * u)
            }
            line(12f, 17.4f, 12f, 20.5f)
        }

        DsIconType.Pin -> {
            strokePath {
                moveTo(9.2f * u, 13.6f * u)
                lineTo(9.2f * u, 6.2f * u)
                lineTo(14.8f * u, 6.2f * u)
                lineTo(14.8f * u, 13.6f * u)
            }
            line(7f, 13.6f, 17f, 13.6f)
            line(12f, 13.6f, 12f, 20.5f)
        }

        DsIconType.Trash -> {
            line(5.5f, 7f, 18.5f, 7f)
            strokePath {
                moveTo(8f * u, 7f * u)
                lineTo(9f * u, 20f * u)
                lineTo(15f * u, 20f * u)
                lineTo(16f * u, 7f * u)
            }
            strokePath {
                moveTo(9.5f * u, 7f * u)
                lineTo(9.5f * u, 5f * u)
                lineTo(14.5f * u, 5f * u)
                lineTo(14.5f * u, 7f * u)
            }
        }

        DsIconType.Edit -> strokePath {
            moveTo(4.8f * u, 19.2f * u)
            lineTo(7.6f * u, 13.2f * u)
            lineTo(16.4f * u, 4.4f * u)
            quadraticBezierTo(17.6f * u, 3.2f * u, 18.8f * u, 4.4f * u)
            quadraticBezierTo(20f * u, 5.6f * u, 18.8f * u, 6.8f * u)
            lineTo(10f * u, 15.6f * u)
            close()
        }

        DsIconType.More -> {
            drawCircle(tint, 1.7f * u, Offset(5f * u, 12f * u))
            drawCircle(tint, 1.7f * u, Offset(12f * u, 12f * u))
            drawCircle(tint, 1.7f * u, Offset(19f * u, 12f * u))
        }

        DsIconType.Close -> {
            line(6.5f, 6.5f, 17.5f, 17.5f)
            line(17.5f, 6.5f, 6.5f, 17.5f)
        }

        DsIconType.ChevronDown -> strokePath {
            moveTo(6.5f * u, 9.5f * u)
            lineTo(12f * u, 15f * u)
            lineTo(17.5f * u, 9.5f * u)
        }

        DsIconType.ChevronRight -> strokePath {
            moveTo(9.5f * u, 6.5f * u)
            lineTo(15f * u, 12f * u)
            lineTo(9.5f * u, 17.5f * u)
        }

        DsIconType.Check -> strokePath {
            moveTo(5f * u, 12.5f * u)
            lineTo(9.8f * u, 17.3f * u)
            lineTo(19f * u, 6f * u)
        }

        DsIconType.Gear -> {
            circle(12f, 12f, 3.1f)
            circle(12f, 12f, 7.6f)
            for (i in 0 until 8) {
                val angle = Math.toRadians((i * 45).toDouble())
                val sx = 12f + 7.6f * cos(angle).toFloat()
                val sy = 12f + 7.6f * sin(angle).toFloat()
                val ex = 12f + 10.2f * cos(angle).toFloat()
                val ey = 12f + 10.2f * sin(angle).toFloat()
                line(sx, sy, ex, ey)
            }
        }

        DsIconType.Speaker -> {
            strokePath {
                moveTo(4.5f * u, 9.5f * u)
                lineTo(8f * u, 9.5f * u)
                lineTo(12f * u, 5.5f * u)
                lineTo(12f * u, 18.5f * u)
                lineTo(8f * u, 14.5f * u)
                lineTo(4.5f * u, 14.5f * u)
                close()
            }
            strokePath {
                moveTo(15f * u, 9f * u)
                quadraticBezierTo(17.2f * u, 12f * u, 15f * u, 15f * u)
            }
            strokePath {
                moveTo(17.6f * u, 6.6f * u)
                quadraticBezierTo(21.2f * u, 12f * u, 17.6f * u, 17.4f * u)
            }
        }
    }
}
