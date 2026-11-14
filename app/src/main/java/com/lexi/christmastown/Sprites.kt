package com.lexi.christmastown

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlin.math.roundToInt

/** Loads the pixel art from assets/sprites.txt and turns it into crisp little bitmaps. */
object Sprites {
    private val palette = HashMap<Char, Int>()
    private val sprites = HashMap<String, List<String>>()
    private val cache = HashMap<String, ImageBitmap>()

    fun load(context: Context) {
        if (sprites.isNotEmpty()) return
        var current: String? = null
        context.assets.open("sprites.txt").bufferedReader().forEachLine { line ->
            when {
                line.startsWith("#") -> {}
                line.startsWith("=") -> palette[line[1]] = (0xFF000000 or line.substring(3, 9).toLong(16)).toInt()
                line.startsWith("@") -> { current = line.drop(1).trim(); sprites[current!!] = mutableListOf() }
                line.isBlank() -> current = null
                current != null -> (sprites[current!!] as MutableList).add(line)
            }
        }
    }

    fun rows(name: String): List<String> = sprites[name] ?: emptyList()

    /** Lays [top] over [base]: any non-"." pixel in [top] wins. */
    fun overlay(base: List<String>, top: List<String>): List<String> {
        val width = maxOf(base.maxOfOrNull { it.length } ?: 0, top.maxOfOrNull { it.length } ?: 0)
        val grid = (0 until maxOf(base.size, top.size)).map { y -> (base.getOrNull(y) ?: "").padEnd(width, '.').toCharArray() }
        top.forEachIndexed { y, row -> row.forEachIndexed { x, ch -> if (ch != '.') grid[y][x] = ch } }
        return grid.map { String(it) }
    }

    fun bitmap(key: String, rows: List<String>, recolor: Map<Char, Color> = emptyMap()): ImageBitmap = cache.getOrPut(key) {
        val w = rows.maxOfOrNull { it.length } ?: 1
        val h = rows.size.coerceAtLeast(1)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, ch ->
                val c = recolor[ch]?.toArgb() ?: palette[ch]
                if (c != null) bmp.setPixel(x, y, c)
            }
        }
        bmp.asImageBitmap()
    }

    fun named(name: String, recolor: Map<Char, Color> = emptyMap(), tag: String = ""): ImageBitmap =
        bitmap("$name|$tag", rows(name), recolor)

    // MARK: Elves

    private fun shade(c: Color, f: Float) = Color(c.red * f, c.green * f, c.blue * f, 1f)

    fun elfColors(look: ElfLook): Map<Char, Color> {
        val skin = ElfColors.skins[look.skin % ElfColors.skins.size]
        val hair = ElfColors.hairs[look.hairColor % ElfColors.hairs.size]
        val outfit = ElfColors.clothes[look.outfitColor % ElfColors.clothes.size]
        return mapOf(
            'S' to skin, 'Z' to shade(skin, 0.85f),
            'H' to hair, 'J' to shade(hair, 0.75f),
            'O' to outfit, 'Q' to shade(outfit, 0.72f),
            'T' to ElfColors.clothes[look.hatColor % ElfColors.clothes.size],
            'E' to ElfColors.eyes[look.eye % ElfColors.eyes.size],
        )
    }

    fun elf(look: ElfLook): ImageBitmap {
        var rows = rows("elf_base")
        look.outfit.sprite?.let { rows = overlay(rows, rows(it)) }
        look.accessory.sprite?.let { rows = overlay(rows, rows(it)) }
        rows = overlay(rows, rows(look.hairStyle.sprite))
        look.hat.sprite?.let { rows = overlay(rows, rows(it)) }
        return bitmap("elf|$look", rows, elfColors(look))
    }
}

/**
 * Draws a pixel sprite [height] tall (width keeps the sprite's shape), with sharp pixels.
 * [reveal] (0..1) shows only the top part, used when building toys.
 */
@Composable
fun Px(image: ImageBitmap, height: Dp, modifier: Modifier = Modifier, reveal: Float = 1f, alpha: Float = 1f) {
    val width = height * (image.width.toFloat() / image.height)
    Canvas(modifier.size(width, height)) {
        clipRect(bottom = size.height * reveal) {
            drawImage(
                image,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(image.width, image.height),
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                alpha = alpha,
                filterQuality = FilterQuality.None,
            )
        }
    }
}

@Composable
fun Px(name: String, height: Dp, modifier: Modifier = Modifier, reveal: Float = 1f, alpha: Float = 1f) =
    Px(Sprites.named(name), height, modifier, reveal, alpha)

@Composable
fun ElfPx(look: ElfLook, height: Dp, modifier: Modifier = Modifier) = Px(Sprites.elf(look), height, modifier)
