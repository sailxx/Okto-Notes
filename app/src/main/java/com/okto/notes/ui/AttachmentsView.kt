package com.okto.notes.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.okto.notes.OktoViewModel
import com.okto.notes.data.Attachment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private fun humanSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "%.1f MB".format(bytes / 1048576.0)
}

private fun open(context: Context, file: File, a: Attachment, noApp: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, a.mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, noApp, Toast.LENGTH_SHORT).show()
    }
}

/** Декодирует уменьшенную копию картинки, чтобы не держать в памяти оригинал. */
@Composable
private fun rememberThumb(file: File, target: Int): ImageBitmap? {
    val bmp by produceState<ImageBitmap?>(null, file) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.path, bounds)
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= target && bounds.outHeight / (sample * 2) >= target) sample *= 2
                BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = sample })?.asImageBitmap()
            }.getOrNull()
        }
    }
    return bmp
}

/** Вложения записи: фото — миниатюры в ряд, остальные файлы — строки. Нажатие открывает во внешнем приложении. */
@Composable
fun AttachmentsView(items: List<Attachment>, vm: OktoViewModel, fg: Color, muted: Color) {
    if (items.isEmpty()) return
    val t = T
    val c = t.c
    val context = LocalContext.current
    val noApp = S.noAppToOpen
    val radius = if (t.okto) 10.dp else 18.dp
    val shape = RoundedCornerShape(radius)
    val photos = items.filter { it.isImage }
    val files = items.filterNot { it.isImage }

    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (photos.isNotEmpty()) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                photos.forEach { a ->
                    val f = vm.attachments.file(a)
                    val thumb = rememberThumb(f, 480)
                    Box(
                        Modifier
                            .size(132.dp)
                            .clip(shape)
                            .background(c.surfaceHi)
                            .clickable { open(context, f, a, noApp) },
                    ) {
                        thumb?.let { Image(it, a.name, Modifier.size(132.dp), contentScale = ContentScale.Crop) }
                        RemoveDot(Modifier.align(Alignment.TopEnd).padding(6.dp)) { vm.removeAttachment(a) }
                    }
                }
            }
        }
        files.forEach { a ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(c.surfaceHi)
                    .clickable { open(context, vm.attachments.file(a), a, noApp) }
                    .padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(a.name, style = t.cardTitle.copy(fontSize = 15.sp), color = c.onBg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(humanSize(a.size), style = t.caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium), color = c.muted)
                }
                Spacer(Modifier.width(8.dp))
                RemoveDot(Modifier) { vm.removeAttachment(a) }
            }
        }
    }
}

@Composable
private fun RemoveDot(modifier: Modifier, onClick: () -> Unit) {
    val c = C
    Box(
        modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(c.bg.copy(alpha = 0.85f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(Icons.Filled.Close, null, tint = c.onBg, modifier = Modifier.size(16.dp)) }
}
