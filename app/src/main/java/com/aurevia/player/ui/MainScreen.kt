package com.aurevia.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.aurevia.player.audio.TenBandEqualizer
import com.aurevia.player.data.MusicSource
import com.aurevia.player.data.Track

val PureBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF121214)
val CardDark = Color(0xFF1C1C21)
val AccentCyan = Color(0xFF00E5FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AureviaApp(
    tracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    equalizer: TenBandEqualizer,
    onSearch: (String, MusicSource) -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayPause: () -> Unit,
    onStartWave: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf(MusicSource.ALL) }
    var showSourceSheet by remember { mutableStateOf(false) }
    var showEqSheet by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf("Ночной драйв") }

    val moods = listOf("Ночной драйв", "Агрессивный", "Фонк", "Меланхолия", "Чистый бит")

    Scaffold(
        containerColor = PureBlack,
        bottomBar = {
            if (currentTrack != null) {
                MiniPlayerBar(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause,
                    onOpenEq = { showEqSheet = true }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aurevia",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = { showEqSheet = true }) {
                    Icon(Icons.Rounded.Tune, contentDescription = "EQ", tint = AccentCyan)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            SearchBarWithSource(
                query = searchQuery,
                selectedSource = selectedSource,
                onQueryChange = {
                    searchQuery = it
                    onSearch(it, selectedSource)
                },
                onOpenSourcePicker = { showSourceSheet = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            MyWaveCard(
                moods = moods,
                selectedMood = selectedMood,
                onMoodSelect = { selectedMood = it },
                onLaunchWave = { onStartWave(selectedMood) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (searchQuery.isEmpty()) "Поток рекомендаций" else "Лучшие результаты",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(items = tracks, key = { it.id }) { track ->
                    TrackRowItem(track = track, onClick = { onTrackClick(track) })
                }
            }
        }

        if (showSourceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSourceSheet = false },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                SourcePickerSheetContent(
                    selectedSource = selectedSource,
                    onSelect = { source ->
                        selectedSource = source
                        showSourceSheet = false
                        onSearch(searchQuery, source)
                    }
                )
            }
        }

        if (showEqSheet) {
            ModalBottomSheet(
                onDismissRequest = { showEqSheet = false },
                containerColor = SurfaceDark,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                EqualizerSheetContent(equalizer = equalizer)
            }
        }
    }
}

@Composable
fun SearchBarWithSource(
    query: String,
    selectedSource: MusicSource,
    onQueryChange: (String) -> Unit,
    onOpenSourcePicker: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Трек, артист, текст...", color = Color.Gray, fontSize = 14.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2A32))
                .clickable { onOpenSourcePicker() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedSource.name,
                color = AccentCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Площадка",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun MyWaveCard(
    moods: List<String>,
    selectedMood: String,
    onMoodSelect: (String) -> Unit,
    onLaunchWave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF151922), Color(0xFF0D1F2D))
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Моя волна • AI Audio", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Подбор по АЧХ, BPM и истории скипов", color = Color.Gray, fontSize = 12.sp)
            }
            IconButton(
                onClick = onLaunchWave,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentCyan)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = "Старт", tint = PureBlack)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(moods) { mood ->
                val active = mood == selectedMood
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (active) AccentCyan.copy(alpha = 0.2f) else Color(0xFF1E222B))
                        .clickable { onMoodSelect(mood) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mood,
                        color = if (active) AccentCyan else Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun TrackRowItem(track: Track, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = track.artist,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = track.qualityBadge,
                    color = if (track.source.supportsLossless) AccentCyan else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E262C))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SourcePickerSheetContent(
    selectedSource: MusicSource,
    onSelect: (MusicSource) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text("Площадка поиска", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        MusicSource.entries.forEach { source ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelect(source) }
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(source.title, color = Color.White, fontSize = 16.sp)
                if (source == selectedSource) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MiniPlayerBar(
    track: Track,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onOpenEq: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${track.artist} • ${track.qualityBadge}", color = AccentCyan, fontSize = 11.sp, maxLines = 1)
        }
        IconButton(onClick = onOpenEq) {
            Icon(Icons.Rounded.GraphicEq, contentDescription = "Эквалайзер", tint = Color.White)
        }
        IconButton(onClick = onPlayPause) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = "Play/Pause",
                tint = AccentCyan
            )
        }
    }
}

@Composable
fun EqualizerSheetContent(equalizer: TenBandEqualizer) {
    var bitPerfect by remember { mutableStateOf(equalizer.isBitPerfectMode) }
    val gains = remember { mutableStateListOf(*Array(10) { 0f }) }

    Column(modifier = Modifier.padding(20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("10-полосный EQ", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Аппаратный DynamicsProcessing", color = Color.Gray, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bit-Perfect", color = if (bitPerfect) AccentCyan else Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = bitPerfect,
                    onCheckedChange = {
                        bitPerfect = it
                        equalizer.setBitPerfect(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            equalizer.frequencies.forEachIndexed { index, freq ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Slider(
                        value = gains[index],
                        onValueChange = {
                            gains[index] = it
                            equalizer.setBandGain(index, it)
                        },
                        valueRange = -12f..12f,
                        enabled = !bitPerfect,
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = 270f
                                transformOrigin = TransformOrigin(0f, 0f)
                            }
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(
                                    Constraints(
                                        minWidth = constraints.minHeight,
                                        maxWidth = constraints.maxHeight,
                                        minHeight = constraints.minWidth,
                                        maxHeight = constraints.maxHeight
                                    )
                                )
                                layout(placeable.height, placeable.width) {
                                    placeable.place(-placeable.width, 0)
                                }
                            }
                            .width(130.dp)
                            .height(30.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val label = if (freq >= 1000) "${(freq / 1000).toInt()}k" else "${freq.toInt()}"
                    Text(label, color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
