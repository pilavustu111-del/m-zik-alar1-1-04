@file:OptIn(androidx.media3.common.util.UnstableApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package app.cepcalar

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs

val BG = Color(0xFF121212)
val CHIP = Color(0xFF2A2A2A)
val GREEN = Color(0xFF1ED760)
val MUTED = Color(0xFFB3B3B3)
val INK = Color(0xFF121212)
val PASTELS = listOf(
    Color(0xFFFAE24B), Color(0xFF8BB8F8), Color(0xFFD8A6F5),
    Color(0xFFF59BCB), Color(0xFFA7F0B5), Color(0xFFF6B07A),
)

fun tintFor(s: String, sat: Float = 0.35f, v: Float = 0.30f): Color =
    Color.hsv((abs(s.hashCode()) % 360).toFloat(), sat, v)

fun fmt(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

object ThumbCache {
    val cache = LruCache<String, ImageBitmap>(80)
}

@Composable
fun Art(song: Song?, modifier: Modifier = Modifier, corner: Dp = 6.dp) {
    val ctx = LocalContext.current
    val title = song?.title ?: ""
    Box(
        modifier.clip(RoundedCornerShape(corner)).background(tintFor(title, 0.38f, 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            (title.trim().firstOrNull()?.uppercase() ?: "♪"),
            color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, fontSize = 22.sp
        )
        if (song != null) {
            if (song.artUrl != null) {
                AsyncImage(
                    model = song.artUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else if (!song.remote) {
                val bmp by produceState<ImageBitmap?>(ThumbCache.cache.get(song.mediaId), song.mediaId) {
                    if (value == null) {
                        value = withContext(Dispatchers.IO) {
                            runCatching {
                                ctx.contentResolver.loadThumbnail(song.uri, Size(320, 320), null).asImageBitmap()
                            }.getOrNull()
                        }?.also { ThumbCache.cache.put(song.mediaId, it) }
                    }
                }
                bmp?.let {
                    Image(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun CepApp(vm: MusicVM) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var full by rememberSaveable { mutableStateOf(false) }
    val perms = remember {
        if (Build.VERSION.SDK_INT >= 33)
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.scan()
    }
    LaunchedEffect(Unit) { if (!vm.checkPermission()) launcher.launch(perms) }
    LaunchedEffect(Unit) {
        while (true) {
            vm.tick()
            delay(400)
        }
    }
    LaunchedEffect(vm.openFull) {
        if (vm.openFull) {
            full = true
            vm.openFull = false
        }
    }
    BackHandler(enabled = full) { full = false }

    MaterialTheme(colorScheme = darkColorScheme(primary = GREEN, background = BG, surface = Color(0xFF1F1F1F))) {
        Box(Modifier.fillMaxSize().background(BG)) {
            Column(Modifier.fillMaxSize().imePadding()) {
                Box(Modifier.weight(1f).statusBarsPadding()) {
                    when (tab) {
                        0 -> Home(vm) { launcher.launch(perms) }
                        1 -> SearchTab(vm)
                        2 -> AssistantTab(vm)
                        3 -> LibraryTab(vm)
                        4 -> TopTab(vm)
                        else -> LikedTab(vm)
                    }
                }
                if (vm.current != null) MiniPlayer(vm) { full = true }
                NavBar(tab) { tab = it }
            }
            AnimatedVisibility(
                visible = full,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) { FullPlayer(vm) { full = false } }
        }
    }
}

// ---------- Ana sayfa ----------

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(if (selected) GREEN else CHIP)
            .clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(text, color = if (selected) INK else Color.White, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
fun SectionTitle(t: String) {
    Text(
        t, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)
    )
}

@Composable
fun Hint(t: String, modifier: Modifier = Modifier) {
    Text(t, color = MUTED, fontSize = 14.sp, modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp))
}

@Composable
fun Home(vm: MusicVM, onAsk: () -> Unit) {
    val list = when (vm.filter) {
        1 -> vm.songs.filter { !it.isVideo }
        2 -> vm.songs.filter { it.isVideo }
        else -> vm.songs
    }
    val queue = remember(list, vm.stats, vm.recoTracks) { vm.mixed(list.sortedByDescending { vm.count(it) }, 4) }
    val quick = queue.take(8)
    val videos = remember(vm.songs) { vm.songs.filter { it.isVideo } }

    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip("Tümü", vm.filter == 0) { vm.filter = 0 }
                Chip("Müzik", vm.filter == 1) { vm.filter = 1 }
                Chip("Klipler", vm.filter == 2) { vm.filter = 2 }
            }
        }
        if (!vm.hasPermission) {
            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Telefondaki müzikleri bulabilmem için izin ver.", color = Color.White, fontSize = 16.sp)
                    Button(
                        onClick = onAsk,
                        colors = ButtonDefaults.buttonColors(containerColor = GREEN, contentColor = INK)
                    ) { Text("İzin ver") }
                    Text(
                        "İzin penceresi çıkmazsa Ayarlar > Uygulamalar > Cep Çalar > İzinler bölümünden Müzik ve ses ile Fotoğraf ve video iznini aç.",
                        color = MUTED, fontSize = 13.sp
                    )
                }
            }
        } else if (list.isEmpty()) {
            item { Hint("Bu telefonda henüz müzik bulunamadı. Dosyalar eklenince liste kendiliğinden güncellenir.") }
        }
        items(quick) { s -> SongRow(s, vm) { vm.play(queue, queue.indexOf(s)) } }

        if (vm.similar.isNotEmpty()) {
            item { SectionTitle("Tavsiye Edilen İstasyonlar") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) { items(vm.similar) { a -> StationCard(a) { vm.playStation(a) } } }
            }
        } else if (!vm.online && vm.songs.isNotEmpty()) {
            item { Hint("İnternete bağlanınca en çok dinlediğin sanatçılara göre istasyonlar burada çıkar.", Modifier.padding(top = 16.dp)) }
        }
        if (vm.mine.isNotEmpty()) {
            item { SectionTitle("Senin radyoların") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) { items(vm.mine) { a -> StationCard(a) { vm.playStation(a) } } }
            }
        }
        if (videos.isNotEmpty() && vm.filter != 1) {
            item { SectionTitle("Klipler") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(videos) { v ->
                        Column(Modifier.width(220.dp).clickable { vm.play(videos, videos.indexOf(v)) }) {
                            Art(v, Modifier.fillMaxWidth().aspectRatio(16f / 9f), 8.dp)
                            Text(
                                v.title, color = Color.White, fontWeight = FontWeight.SemiBold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(v.artist.ifEmpty { "Klip" }, color = MUTED, fontSize = 13.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationCard(a: Artist, onClick: () -> Unit) {
    val bg = PASTELS[abs(a.name.hashCode()) % PASTELS.size]
    Column(Modifier.width(160.dp).clickable(onClick = onClick)) {
        Box(Modifier.size(160.dp).clip(RoundedCornerShape(8.dp)).background(bg)) {
            Text(
                "RADYO", color = INK, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
            )
            AsyncImage(
                model = a.pic, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp).size(88.dp)
                    .clip(CircleShape).border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            )
            Text(
                a.name, color = INK, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
            )
        }
        Text(
            "${a.name} ve benzerleri", color = MUTED, fontSize = 14.sp, maxLines = 2,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun SongRow(s: Song, vm: MusicVM, sub: String? = null, rank: Int? = null, onClick: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val now = vm.current?.mediaId == s.mediaId
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rank != null) {
            Text(
                "$rank", color = if (rank == 1) GREEN else MUTED, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp), fontSize = 16.sp
            )
        }
        Art(s, Modifier.size(56.dp))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                s.title, color = if (now) GREEN else Color.White, fontSize = 17.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (s.remote) {
                    Text(
                        "ÖNERİ", color = GREEN, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp).border(1.dp, GREEN, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp)
                    )
                } else if (s.isVideo) {
                    Text(
                        "KLİP", color = MUTED, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp).border(1.dp, MUTED, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp)
                    )
                }
                Text(
                    sub ?: s.artist.ifEmpty { "Sanatçı bilinmiyor" }, color = MUTED, fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (s.remote) {
            val liked = vm.isLiked(s)
            IconButton(onClick = { vm.toggleLike(s) }) {
                Icon(
                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    if (liked) "Beğenmekten vazgeç" else "Beğen",
                    tint = if (liked) GREEN else Color.White
                )
            }
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Daha fazla", tint = Color.White)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Sıradakine ekle") }, onClick = {
                    vm.addNext(s)
                    menu = false
                })
                if (!s.link.isNullOrBlank()) {
                    DropdownMenuItem(text = { Text("Deezer'da aç") }, onClick = {
                        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(s.link))) }
                        menu = false
                    })
                }
            }
        }
    }
}

// ---------- Ara / Kitaplık / En çok ----------

@Composable
fun Header(t: String, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(t, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun SearchTab(vm: MusicVM) {
    var q by rememberSaveable { mutableStateOf("") }
    val res = remember(q, vm.songs) {
        if (q.isBlank()) emptyList()
        else vm.songs.filter { it.title.contains(q, true) || it.artist.contains(q, true) }
    }
    Column(Modifier.fillMaxSize()) {
        Header("Ara")
        TextField(
            value = q, onValueChange = { q = it }, singleLine = true,
            placeholder = { Text("Ne dinlemek istiyorsun?") },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = INK) },
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
                focusedTextColor = INK, unfocusedTextColor = INK, cursorColor = INK,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                focusedPlaceholderColor = Color(0xFF555555), unfocusedPlaceholderColor = Color(0xFF555555),
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (q.isNotBlank() && res.isEmpty()) Hint("Sonuç yok.")
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(res) { s -> SongRow(s, vm) { vm.play(res, res.indexOf(s)) } }
        }
    }
}

@Composable
fun LibraryTab(vm: MusicVM) {
    val shown = remember(vm.songs, vm.recoTracks) { vm.mixed(vm.songs, 5) }
    Column(Modifier.fillMaxSize()) {
        Header("Kitaplığın") {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(GREEN)
                    .clickable { vm.shuffleAll(vm.songs) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Shuffle, "Karışık çal", tint = INK) }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${vm.songs.size} parça · Tüm videoları göster", color = MUTED, fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = vm.allVideos, onCheckedChange = { vm.setShowAllVideos(it) },
                colors = SwitchDefaults.colors(checkedTrackColor = GREEN, checkedThumbColor = INK)
            )
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(shown) { s ->
                val c = vm.count(s)
                SongRow(
                    s, vm,
                    sub = (if (c > 0) "$c kez dinlendi · " else "") + s.artist.ifEmpty { "Sanatçı bilinmiyor" }
                ) { vm.play(shown, shown.indexOf(s)) }
            }
        }
    }
}

@Composable
fun TopTab(vm: MusicVM) {
    val ranked = remember(vm.songs, vm.stats) {
        vm.songs.filter { vm.count(it) > 0 }.sortedByDescending { vm.count(it) }.take(50)
    }
    Column(Modifier.fillMaxSize()) {
        Header("En çok dinlediklerin")
        if (ranked.isEmpty()) Hint("Henüz dinleme kaydı yok. Bir parçayı yaklaşık 30 saniye dinleyince sayılır.")
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            itemsIndexed(ranked) { i, s ->
                SongRow(
                    s, vm, rank = i + 1,
                    sub = "${vm.count(s)} kez · " + s.artist.ifEmpty { "Sanatçı bilinmiyor" }
                ) { vm.play(ranked, i) }
            }
        }
    }
}

// ---------- Alt çubuklar ----------

@Composable
fun MiniPlayer(vm: MusicVM, onOpen: () -> Unit) {
    val s = vm.current ?: return
    val frac = if (vm.duration > 0) (vm.position.toFloat() / vm.duration).coerceIn(0f, 1f) else 0f
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp)).background(tintFor(s.title, 0.40f, 0.30f)).clickable(onClick = onOpen)
    ) {
        Row(Modifier.padding(start = 8.dp, top = 8.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Art(s, Modifier.size(42.dp), 4.dp)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(s.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(s.artist.ifEmpty { "Sanatçı bilinmiyor" }, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { vm.toggle() }) {
                Icon(
                    if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (vm.isPlaying) "Duraklat" else "Oynat", tint = Color.White, modifier = Modifier.size(30.dp)
                )
            }
        }
        Box(
            Modifier.align(Alignment.BottomCenter).padding(horizontal = 8.dp).fillMaxWidth().height(2.dp)
                .background(Color(0x40FFFFFF))
        ) { Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(Color.White)) }
    }
}

@Composable
fun NavBar(selected: Int, onSelect: (Int) -> Unit) {
    val items: List<Pair<String, ImageVector>> = listOf(
        "Ana sayfa" to Icons.Filled.Home,
        "Ara" to Icons.Filled.Search,
        "Asistan" to Icons.Filled.AutoAwesome,
        "Kitaplık" to Icons.Filled.LibraryMusic,
        "En çok" to Icons.Filled.Star,
        "Beğenilen" to Icons.Filled.Favorite,
    )
    Row(
        Modifier.fillMaxWidth().background(BG.copy(alpha = 0.97f)).navigationBarsPadding().padding(vertical = 8.dp)
    ) {
        items.forEachIndexed { i, (label, icon) ->
            Column(
                Modifier.weight(1f).clickable { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, label, tint = if (i == selected) Color.White else MUTED, modifier = Modifier.size(26.dp))
                Text(label, color = if (i == selected) Color.White else MUTED, fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

// ---------- Şimdi çalıyor ----------

@Composable
fun FullPlayer(vm: MusicVM, onClose: () -> Unit) {
    val s = vm.current
    val tint = tintFor(s?.title ?: "", 0.45f, 0.38f)
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(tint, BG)))
            .pointerInput(Unit) {}.statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.KeyboardArrowDown, "Kapat", tint = Color.White, modifier = Modifier.size(34.dp))
            }
            Text(
                "ŞİMDİ ÇALIYOR", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(48.dp))
        }
        if (s != null) {
            if (s.isVideo) {
                AndroidView(
                    factory = { c ->
                        PlayerView(c).apply {
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            player = vm.controller
                        }
                    },
                    update = { it.player = vm.controller },
                    onRelease = { it.player = null },
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp))
                        .background(Color.Black)
                )
            } else {
                Art(s, Modifier.align(Alignment.CenterHorizontally).size(220.dp), 8.dp)
            }
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        s.title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(s.artist.ifEmpty { "Sanatçı bilinmiyor" }, color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                }
                if (s.remote) {
                    val liked = vm.isLiked(s)
                    IconButton(onClick = { vm.toggleLike(s) }) {
                        Icon(
                            if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            if (liked) "Beğenmekten vazgeç" else "Beğen",
                            tint = if (liked) GREEN else Color.White, modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            var drag by remember { mutableStateOf<Float?>(null) }
            val dur = vm.duration.coerceAtLeast(1L)
            val frac = drag ?: (vm.position.toFloat() / dur).coerceIn(0f, 1f)
            Slider(
                value = frac,
                onValueChange = { drag = it },
                onValueChangeFinished = {
                    drag?.let { vm.seekTo((it * dur).toLong()) }
                    drag = null
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White, activeTrackColor = Color.White,
                    inactiveTrackColor = Color(0x55FFFFFF)
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(fmt((frac * dur).toLong()), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(fmt(vm.duration), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.toggleShuffle() }) {
                    Icon(Icons.Filled.Shuffle, "Karıştır", tint = if (vm.shuffle) GREEN else Color.White)
                }
                IconButton(onClick = { vm.prev() }) {
                    Icon(Icons.Filled.SkipPrevious, "Önceki", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Box(
                    Modifier.size(64.dp).clip(CircleShape).background(Color.White).clickable { vm.toggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        if (vm.isPlaying) "Duraklat" else "Oynat", tint = INK, modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { vm.next() }) {
                    Icon(Icons.Filled.SkipNext, "Sonraki", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { vm.cycleRepeat() }) {
                    Icon(
                        if (vm.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        "Tekrarla",
                        tint = if (vm.repeatMode == Player.REPEAT_MODE_OFF) Color.White else GREEN
                    )
                }
            }
            Box(
                Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Color(0x33000000))
            ) {
                val ly = vm.lyrics
                if (ly == null) {
                    Text(
                        when {
                            vm.lyricsLoading -> "Sözler aranıyor…"
                            s.remote -> "Bu 30 saniyelik önizlemede söz yok."
                            !vm.online -> "Sözler için internete bağlan. Bulunan sözler telefona kaydedilir."
                            else -> "Bu parça için söz bulunamadı."
                        },
                        color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LyricsList(ly, vm.position) { vm.seekTo(it) }
                }
            }
        }
    }
}

@Composable
fun LyricsList(ly: Lyrics, pos: Long, onSeek: (Long) -> Unit) {
    val state = remember(ly) { LazyListState() }
    val active = if (ly.synced) ly.lines.indexOfLast { it.timeMs <= pos + 200 } else -1
    LaunchedEffect(active, ly) {
        if (active >= 0) state.animateScrollToItem(active, -140)
    }
    LazyColumn(state = state, contentPadding = PaddingValues(16.dp)) {
        itemsIndexed(ly.lines) { i, l ->
            Text(
                l.text.ifBlank { "♪" },
                fontSize = if (ly.synced) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (!ly.synced || i == active) Color.White else Color.White.copy(alpha = 0.45f),
                modifier = Modifier.padding(vertical = 6.dp).clickable(enabled = ly.synced) { onSeek(l.timeMs) }
            )
        }
    }
}

// ---------- Asistan ----------

@Composable
fun lightFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.White, unfocusedContainerColor = Color.White,
    focusedTextColor = INK, unfocusedTextColor = INK, cursorColor = INK,
    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
    focusedPlaceholderColor = Color(0xFF555555), unfocusedPlaceholderColor = Color(0xFF555555),
)

@Composable
fun Bubble(m: ChatMsg) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (m.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            m.text, color = if (m.fromUser) INK else Color.White, fontSize = 16.sp,
            modifier = Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(16.dp))
                .background(if (m.fromUser) GREEN else CHIP).padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun AssistantTab(vm: MusicVM) {
    var text by rememberSaveable { mutableStateOf("") }
    var editKey by rememberSaveable { mutableStateOf(false) }
    var keyDraft by rememberSaveable { mutableStateOf("") }
    val speech = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val t = r.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (!t.isNullOrBlank()) vm.ask(t)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(vm.chat.size, vm.aiBusy) {
        val n = vm.chat.size + (if (vm.aiBusy) 1 else 0)
        if (n > 0) listState.animateScrollToItem(n - 1)
    }

    Column(Modifier.fillMaxSize()) {
        Header("Asistan") {
            IconButton(onClick = { editKey = !editKey; keyDraft = "" }) {
                Icon(Icons.Filled.Settings, "Ayarlar", tint = Color.White)
            }
        }
        if (vm.apiKey.isBlank() || editKey) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Yapay zekâ asistanı için bir Anthropic API anahtarı gerekir. console.anthropic.com adresinden " +
                        "anahtar oluşturup buraya yapıştır. Anahtar yalnızca bu telefonda saklanır.",
                    color = Color.White, fontSize = 15.sp
                )
                TextField(
                    value = keyDraft, onValueChange = { keyDraft = it }, singleLine = true,
                    placeholder = { Text(if (vm.apiKey.isBlank()) "sk-ant-..." else "Yeni anahtar") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp), colors = lightFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (keyDraft.isNotBlank()) { vm.saveKey(keyDraft); keyDraft = ""; editKey = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GREEN, contentColor = INK)
                    ) { Text("Kaydet") }
                    if (vm.apiKey.isNotBlank()) {
                        Button(
                            onClick = { vm.saveKey(""); keyDraft = ""; editKey = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CHIP, contentColor = Color.White)
                        ) { Text("Anahtarı sil") }
                    }
                }
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (vm.chat.isEmpty()) {
                    item {
                        Hint(
                            "Ruh halini ya da ne dinlemek istediğini söyle. Telefonundaki listene bakıp sana göre çalarım. " +
                                "Mesela: \"Ruh halim kötü, iyileştirecek oyun havaları aç\" ya da \"Hüzünlü şarkılar aç\"."
                        )
                    }
                }
                items(vm.chat) { m -> Bubble(m) }
                if (vm.aiBusy) item { Bubble(ChatMsg(false, "Listene bakıyorum…")) }
            }
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Ruh halim kötü, iyileştir", "Hüzünlü şarkılar aç", "Oyun havası, heyecanlı",
                    "Sakin bir şeyler", "Bana enerji ver"
                ).forEach { q -> Chip(q, false) { vm.ask(q) } }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text, onValueChange = { text = it }, maxLines = 3,
                    placeholder = { Text("Ruh halin ne? Ne çalayım?") },
                    shape = RoundedCornerShape(24.dp), colors = lightFieldColors(),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Ruh halini söyle")
                    }
                    runCatching { speech.launch(i) }
                }) { Icon(Icons.Filled.Mic, "Sesle söyle", tint = GREEN) }
                IconButton(onClick = { vm.ask(text); text = "" }, enabled = !vm.aiBusy) {
                    Icon(Icons.Filled.Send, "Gönder", tint = if (vm.aiBusy) MUTED else GREEN)
                }
            }
        }
    }
}

// ---------- Beğenilenler ----------

@Composable
fun LikedTab(vm: MusicVM) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        Header("Beğendiklerin") {
            if (vm.likes.isNotEmpty()) {
                IconButton(onClick = {
                    val body = buildString {
                        append("Cep Çalar - beğendiğim öneriler\n\n")
                        vm.likes.forEachIndexed { i, s ->
                            append("${i + 1}. ${s.artist} - ${s.title}\n")
                            if (!s.link.isNullOrBlank()) append("   ${s.link}\n")
                        }
                    }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    runCatching { ctx.startActivity(Intent.createChooser(send, "Listeyi paylaş")) }
                }) { Icon(Icons.Filled.Share, "Listeyi paylaş", tint = Color.White) }
            }
        }
        if (vm.likes.isEmpty()) {
            Hint("Listelerin arasındaki ÖNERİ parçalarda kalbe dokun, burada birikir. Sonra paylaş simgesiyle listeyi alıp indirebilirsin.")
        } else {
            Hint("Önizleme 30 saniyedir. Tam hâlini indirmek için listeyi paylaşabilir ya da parçanın menüsünden Deezer'da açabilirsin.")
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            itemsIndexed(vm.likes) { i, s -> SongRow(s, vm) { vm.play(vm.likes, i) } }
        }
    }
}
