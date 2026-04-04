package com.spacemint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.LaunchedEffect
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.layout.ColumnScope


val MintGreen = Color(0xFF1D9E75)
val MintDark  = Color(0xFF0F6E56)

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        scheduleNotifications()
        setContent {
            val startDestination = if (
                intent?.getStringExtra("navigate_to") == "review"
            ) "review" else "splash"
            AppNavigation(startDestination = startDestination)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getStringExtra("navigate_to") == "review") {
            setContent {
                AppNavigation(startDestination = "review")
            }
        }

    }


    private fun scheduleNotifications() {
        NotificationHelper.createChannel(this)
        NotificationScheduler.scheduleDailyAlarms(this)

        // ask Samsung users to disable battery optimization
        if (android.os.Build.MANUFACTURER.lowercase() == "samsung") {
            val pm = getSystemService(android.content.Context.POWER_SERVICE)
                    as android.os.PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    ).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Battery opt error: ${e.message}")
                }
            }
        }
    }

} // ← class ends here
@Composable
fun GuideScreen(onFinished: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var currentPage by remember { mutableStateOf(0) }
    var canAdvance by remember { mutableStateOf(false) }
    val totalPages = 4

    // after 2 seconds — unlock tap to advance
    LaunchedEffect(currentPage) {
        canAdvance = false
        kotlinx.coroutines.delay(2000L)
        canAdvance = true
    }

    val pages = listOf(
        GuidePageData(
            emoji       = "",
            title       = "Your storage at a glance",
            description = "SpaceMint shows how full your phone is and tracks how much space you free over time.",
            bgColor     = MintGreen,
            content     = {}
        ),
        GuidePageData(
            emoji       = "",
            title       = "Keep or delete — you decide",
            description = "Each file is shown one by one. Tap Delete to remove it or Keep to leave it. Simple as that.",
            bgColor     = MintGreen,
            content     = {}
        ),
        GuidePageData(
            emoji       = "",
            title       = "Two reminders a day",
            description = "A morning reminder and an evening reminder. Tap the notification and you land directly on the review screen.",
            bgColor     = MintGreen,
            content     = {}
        ),
        GuidePageData(
            emoji       = "",
            title       = "Small habit. Real results.",
            description = "5 files a day does not sound like much. But slowly, with time, SpaceMint will clean your entire phone without you even noticing.",
            bgColor     = MintGreen,
            content     = {}
        )
    )

    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MintGreen)
            .clickable {
                // only advance if 2 seconds have passed
                if (canAdvance) {
                    if (currentPage < totalPages - 1) {
                        currentPage++
                    } else {
                        OnboardingGuide.markShown(context)
                        onFinished()
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── PROGRESS DOTS ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 52.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(totalPages) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                if (index <= currentPage)
                                    Color.White
                                else
                                    Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            // ── SKIP ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    OnboardingGuide.markShown(context)
                    onFinished()
                }) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── PHONE MOCKUP ──────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(210.dp)
                        .height(380.dp)
                        .background(Color(0xFF0F6E56), RoundedCornerShape(32.dp))
                        .border(
                            width = 3.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(196.dp)
                            .height(366.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color(0xFFF8FAF8)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        when (currentPage) {

                            // ── PAGE 1 — Home ─────────
                            0 -> Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "SpaceMint",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintGreen,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                Text(
                                    text = "Your daily space cleaner",
                                    fontSize = 7.sp,
                                    color = Color(0xFF999999),
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                // storage card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(text = "Phone storage", fontSize = 6.sp, color = Color(0xFF999999))
                                        Text(text = "116.5 GB of 240 GB used", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFEEEEEE), RoundedCornerShape(99.dp))) {
                                            Box(modifier = Modifier.fillMaxWidth(0.48f).height(4.dp).background(MintGreen, RoundedCornerShape(99.dp)))
                                        }
                                        Text(text = "124.0 GB free", fontSize = 6.sp, color = MintGreen)
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // session card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(58.dp)
                                        .background(MintGreen, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(text = "Session ready", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "5 files - under 60 seconds", fontSize = 6.sp, color = Color.White.copy(alpha = 0.8f))
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Box(modifier = Modifier.background(Color.White, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                            Text(text = "Start reviewing", fontSize = 6.sp, color = MintGreen, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // stats
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf("61 MB\nfreed", "35\ndeleted", "1\nday streak").forEach { stat ->
                                        Box(modifier = Modifier.weight(1f).height(32.dp).background(Color.White, RoundedCornerShape(6.dp)).border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                                            Text(text = stat, fontSize = 6.sp, textAlign = TextAlign.Center, color = Color(0xFF1A1A1A), lineHeight = 9.sp)
                                        }
                                    }
                                }
                            }

                            // ── PAGE 2 — Review ───────
                            1 -> Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                            ) {
                                Text(text = "Review session", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                Text(text = "3 of 5", fontSize = 7.sp, color = Color(0xFF999999))
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    listOf(MintGreen, MintGreen, Color(0xFFEF9F27), Color(0xFFE0E0E0), Color(0xFFE0E0E0)).forEach { c ->
                                        Box(modifier = Modifier.weight(1f).height(3.dp).background(c, RoundedCornerShape(99.dp)))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // photo area
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .background(Color(0xFFDDE8DD), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Box(modifier = Modifier.size(42.dp).background(Color(0xFFB8D4B8), RoundedCornerShape(6.dp)))
                                            Box(modifier = Modifier.size(42.dp).background(Color(0xFFD4C4A8), RoundedCornerShape(6.dp)))
                                            Box(modifier = Modifier.size(42.dp).background(Color(0xFFA8C4D4), RoundedCornerShape(6.dp)))
                                        }
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(text = "Tap to view full screen", fontSize = 5.sp, color = Color(0xFF999999))
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(text = "IMG_20240901.jpg", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                        Text(text = "1 Sept 2024", fontSize = 6.sp, color = Color(0xFF999999))
                                    }
                                    Box(modifier = Modifier.background(Color(0xFFFFEDED), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp)) {
                                        Text(text = "4.2 MB", fontSize = 7.sp, color = Color(0xFFA32D2D), fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(5.dp))
                                Box(modifier = Modifier.fillMaxWidth().background(Color(0xFFFAEEDA), RoundedCornerShape(6.dp)).padding(5.dp)) {
                                    Text(text = "Old photo — consider if you still need this", fontSize = 6.sp, color = Color(0xFF854F0B))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    Box(modifier = Modifier.weight(1f).height(30.dp).background(Color(0xFFFFEDED), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Text(text = "Delete", fontSize = 7.sp, color = Color(0xFFA32D2D), fontWeight = FontWeight.Bold)
                                    }
                                    Box(modifier = Modifier.weight(1f).height(30.dp).background(Color(0xFFE8F7F1), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        Text(text = "Keep", fontSize = 7.sp, color = MintGreen, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // ── PAGE 3 — Notifications ─
                            2 -> Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(10.dp))
                                        .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Box(modifier = Modifier.size(12.dp).background(MintGreen, RoundedCornerShape(3.dp)))
                                            Text(text = "SpaceMint", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                            Text(text = "now", fontSize = 6.sp, color = Color(0xFF999999))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "Good morning. Time to clean up.", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                                        Text(text = "5 files waiting. Takes under 2 minutes.", fontSize = 6.sp, color = Color(0xFF777777))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            Box(modifier = Modifier.background(MintGreen, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                                Text(text = "Review Now", fontSize = 6.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Box(modifier = Modifier.background(Color(0xFFEEEEEE), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                                Text(text = "Later", fontSize = 6.sp, color = Color(0xFF777777))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(text = "Tap Review Now", fontSize = 7.sp, color = Color.White.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "↓", fontSize = 20.sp, color = Color.White.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(55.dp)
                                        .background(MintGreen.copy(alpha = 0.7f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = "Review session opens", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(text = "No home screen. Straight to files.", fontSize = 6.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                    listOf("Morning  10 - 12 AM", "Evening  5 - 6 PM").forEach { time ->
                                        Box(modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(6.dp)).border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp)).padding(6.dp), contentAlignment = Alignment.Center) {
                                            Text(text = time, fontSize = 6.sp, color = Color(0xFF1A1A1A), textAlign = TextAlign.Center, lineHeight = 9.sp)
                                        }
                                    }
                                }
                            }

                            // ── PAGE 4 — Concept ──────
                            3 -> Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                listOf(
                                    Pair("5", "files reviewed every day"),
                                    Pair("365", "days in a year"),
                                    Pair("1,825", "files in 5 years")
                                ).forEachIndexed { index, stat ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .background(
                                                MintGreen.copy(alpha = if (index == 2) 0.25f else 0.12f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stat.first,
                                                fontSize = if (index == 2) 16.sp else 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1A1A1A)
                                            )
                                            Text(
                                                text = stat.second,
                                                fontSize = 7.sp,
                                                color = Color(0xFF555555)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "You will not feel it happening. But slowly, with time, your phone will get cleaner every single day.",
                                        fontSize = 7.sp,
                                        color = Color(0xFF555555),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 11.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // notch
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 6.dp)
                                .width(40.dp)
                                .height(6.dp)
                                .background(Color(0xFF0F6E56), RoundedCornerShape(99.dp))
                        )
                    }
                }
            }

            // ── BOTTOM TEXT ───────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = page.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = page.description,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                // tap hint — shows when unlocked
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (canAdvance) {
                        if (currentPage == totalPages - 1) "Tap anywhere to get started"
                        else "Tap anywhere to continue"
                    } else {
                        "..."
                    },
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = if (canAdvance) 0.6f else 0.3f)
                )

                if (currentPage == totalPages - 1 && canAdvance) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            OnboardingGuide.markShown(context)
                            onFinished()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor   = MintGreen
                        )
                    ) {
                        Text(
                            text = "Let's get started",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


data class GuidePageData(
    val emoji: String,
    val title: String,
    val description: String,
    val bgColor: Color,
    val content: @Composable () -> Unit
)

// ── PAGE CONTENT COMPOSABLES ──────────────────────────────

@Composable
fun StoragePageContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // mock storage card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Phone storage",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "116 GB of 240 GB used",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.48f)
                            .fillMaxHeight()
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "124 GB free",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // mock stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Pair("61 MB", "freed"),
                Pair("35", "deleted"),
                Pair("🔥 7", "streak")
            ).forEach { (num, label) ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = num,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewPageContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // mock file card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // mock thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📸", fontSize = 48.sp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Tap to view full",
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IMG_2024_photo.jpg",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = "4.2 MB", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // mock delete keep buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🗑", fontSize = 18.sp)
                    Text(
                        text = "Delete",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "✓", fontSize = 18.sp, color = Color.White)
                    Text(
                        text = "Keep",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationPageContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // mock notification
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🌿", fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Good morning! Time to breathe 🌿",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "5 files waiting. Takes under 60 seconds!",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.25f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Review Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Later",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // arrow pointing down
        Text(
            text = "↓ tap to open 5 files instantly",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun ConceptPageContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // big numbers
        data class StatItem(val num: String, val label: String)
        val statItems = listOf(
            StatItem("5", "files reviewed daily"),
            StatItem("365", "days in a year"),
            StatItem("1,825", "files reviewed — forever clean")
        )
        statItems.forEachIndexed { index, stat ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(
                        alpha = if (index == 2) 0.25f else 0.15f
                    )
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.num,
                        fontSize = if (index == 2) 26.sp else 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stat.label,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Small habit. Permanent result.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}

@Composable
fun AppNavigation(startDestination: String = "splash") {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(onGetStarted = {
                navController.navigate("guide") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }
        composable("guide") {
            GuideScreen(onFinished = {
                navController.navigate("onboarding") {
                    popUpTo("guide") { inclusive = true }
                }
            })
        }
        composable("onboarding") {
            OnboardingScreen(onDone = {
                navController.navigate("home") {
                    popUpTo("onboarding") { inclusive = true }
                }
            })
        }
        composable("home") {
            HomeScreen(
                onStartReview = { navController.navigate("review") },
                onViewBin     = { navController.navigate("bin") }
            )
        }
        composable("review") {
            ReviewScreen(onFinished = {
                navController.navigate("bin") {
                    popUpTo("review") { inclusive = true }
                }
            })
        }
        composable("bin") {
            BinScreen(onBack = {
                navController.navigate("home") {
                    popUpTo("bin") { inclusive = true }
                }
            })
        }
        composable("splash") {
            val context = androidx.compose.ui.platform.LocalContext.current
            SplashScreen(onGetStarted = {
                // check if guide already shown
                val guideShown = OnboardingGuide.isShown(context)
                if (guideShown) {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("guide") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            })
        }
    }
}
//somthing related with bin
@Composable
fun BinRunGame() {
    AndroidView(
        factory = { context ->
            android.webkit.WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                loadDataWithBaseURL(
                    null,
                    getBinRunHTML(),
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

fun getBinRunHTML(): String = """
<!DOCTYPE html><html><head><meta name="viewport" content="width=device-width,initial-scale=1">
<style>*{margin:0;padding:0;box-sizing:border-box;}body{background:#F0FBF6;overflow:hidden;}canvas{display:block;width:100vw;height:100vh;}</style>
</head><body><canvas id="g"></canvas>
<script>
const cv=document.getElementById('g'),ct=cv.getContext('2d');
function rs(){cv.width=window.innerWidth;cv.height=window.innerHeight;}rs();
const GND=()=>cv.height-18,GRAV=0.50,J1=-12.5,J2=-10.0,BW=22,BH=28,BX=50;
let S='idle',sc=0,best=0,fr=0,spd=0,gOff=0,bY=0,bVY=0,onG=true,jmps=0,lp=0,cr=false;
let objs=[],pts=[],spT=0,spI=110,dMsg='';
function init(){bY=GND()-BH;bVY=0;onG=true;jmps=0;lp=0;objs=[];pts=[];sc=0;fr=0;spd=1.8;spT=0;spI=110;gOff=0;dMsg='';cr=false;}
init();
function jump(){if(jmps<2){bVY=jmps===0?J1:J2;onG=false;jmps++;}}
function update(){
  if(S!=='running')return;fr++;sc=~~(fr/6);if(sc>best)best=sc;
  spd=1.8+fr*.0016;gOff=(gOff+spd)%(cv.width/8);
  const eH=cr?BH*.5:BH,gY=GND()-eH;bVY+=GRAV;bY+=bVY;
  if(bY>=gY){bY=gY;bVY=0;onG=true;jmps=0;}if(onG)lp+=.28;
  spT++;if(spT>=spI){spawnObj();spT=0;spI=60+~~(Math.random()*60);}
  objs.forEach(o=>{o.x-=spd;});
  const bL=BX-BW*.5+3,bR=BX+BW*.5-3,bTop=bY+4,bBot=bY+eH-2;
  objs.forEach(o=>{
    if(o.t==='o'&&!o.dead){
      const oL=o.x+o.w*.12,oR=o.x+o.w*.88;
      if(bR>oL&&bL<oR&&bBot>o.y+4&&bTop<o.y+o.h){
        o.dead=true;S='dead';dMsg='Game Over!';
      }
    }
    if(o.t==='c'&&!o.got){if(bR>o.x+2&&bL<o.x+o.w-2&&bBot>o.y+2&&bTop<o.y+o.h-2){o.got=true;sc+=5;}}
  });
  objs=objs.filter(o=>o.x>-80);pts=pts.filter(p=>p.life>0);
}
function spawnObj(){
  const W=cv.width;
  const shapes=[{w:28,h:32},{w:40,h:36},{w:32,h:30}];
  const s=shapes[~~(Math.random()*shapes.length)];
  if(Math.random()<.6)objs.push({t:'o',x:W+10,y:GND()-s.h,w:s.w,h:s.h});
  else objs.push({t:'c',x:W+10,y:GND()-BH-Math.random()*14-4,w:14,h:14,got:false});
}
function draw(){
  const w=cv.width,h=cv.height;
  ct.fillStyle='#F0FBF6';ct.fillRect(0,0,w,h);
  ct.fillStyle='#C8EDE0';ct.fillRect(0,GND(),w,h-GND());ct.fillStyle='#9FE1CB';ct.fillRect(0,GND(),w,2);
  objs.forEach(o=>{
    if(o.t==='o'){ct.fillStyle='#9E9690';ct.fillRect(o.x,o.y,o.w,o.h);}
    else if(!o.got){ct.fillStyle='rgba(239,159,39,.8)';ct.beginPath();ct.arc(o.x+7,o.y+7,7,0,Math.PI*2);ct.fill();}
  });
  const dead=S==='dead',bh=cr&&!dead?BH*.5:BH;
  ct.save();ct.translate(BX-BW/2,bY);
  ct.beginPath();ct.moveTo(-BW/2+2,5);ct.lineTo(-BW/2,bh);ct.lineTo(BW/2,bh);ct.lineTo(BW/2-2,5);ct.closePath();
  ct.fillStyle=dead?'#E24B4A':'#1D9E75';ct.fill();
  ct.beginPath();ct.roundRect(-BW/2-2,0,BW+4,8,3);ct.fillStyle=dead?'#A32D2D':'#0F6E56';ct.fill();
  ct.restore();
  ct.font='bold 10px sans-serif';ct.textAlign='right';ct.fillStyle='rgba(15,110,86,.7)';ct.fillText(sc+'',w-6,14);
  ct.font='8px sans-serif';ct.fillStyle='rgba(15,110,86,.4)';ct.fillText('best '+best,w-6,24);
  if(S==='idle'){ct.font='bold 11px sans-serif';ct.textAlign='center';ct.fillStyle='#0F6E56';ct.fillText('TAP TO START',w/2,h/2);}
  if(S==='dead'){ct.fillStyle='rgba(0,0,0,.35)';ct.fillRect(0,0,w,h);ct.font='bold 11px sans-serif';ct.textAlign='center';ct.fillStyle='#fff';ct.fillText(sc+' pts · tap to retry',w/2,h/2);}
}
function loop(){update();draw();requestAnimationFrame(loop);}
let hTmr=null,didCr=false;
function pS(){if(S!=='running'){if(S==='dead'){S='idle';init();}else S='running';return;}didCr=false;hTmr=setTimeout(()=>{cr=true;didCr=true;},150);}
function pE(){clearTimeout(hTmr);cr=false;if(!didCr&&S==='running')jump();}
document.addEventListener('touchstart',e=>{e.preventDefault();pS();},{passive:false});
document.addEventListener('touchend',e=>{e.preventDefault();pE();},{passive:false});
document.addEventListener('mousedown',pS);document.addEventListener('mouseup',pE);
window.addEventListener('resize',()=>{rs();init();});
loop();
</script></body></html>
""".trimIndent()


// ─── SCREEN 1: SPLASH ────────────────────────────────────────
@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1500L)
        onGetStarted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MintGreen),
        contentAlignment = Alignment.Center
    ) {
        // main content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // app icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Color.White.copy(alpha = 0.15f),
                        RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌿", fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SpaceMint",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your daily space cleaner",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.weight(1f))

            // dedication line at bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                // thin divider line
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.4f))
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "devoted to mankind",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 2.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "credited to a stranger",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.55f),
                    letterSpacing = 1.5.sp
                )
            }
        }
    }
}

// ─── SCREEN 2: ONBOARDING ─────────────────────────────────────
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var targetMB by remember {
        mutableStateOf(
            context.getSharedPreferences("spacemint_target", android.content.Context.MODE_PRIVATE)
                .getFloat("target_mb", 50f)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF8))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Your phone is full\nof forgotten files",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Old screenshots. Duplicate photos.\nDownloads you never opened.\nSpaceMint helps you clear them — slowly, safely, every day.",
            fontSize = 15.sp,
            color = Color(0xFF777777),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ── TARGET SLIDER ─────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, Color(0xFFE0E0E0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily clean target",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Box(
                        modifier = Modifier
                            .background(MintGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "~${targetMB.toInt()} MB / day",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = targetMB,
                    onValueChange = { targetMB = it },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor        = MintGreen,
                        activeTrackColor  = MintGreen,
                        inactiveTrackColor= Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "10 MB",
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA)
                    )
                    Text(
                        text = "100 MB",
                        fontSize = 11.sp,
                        color = Color(0xFFAAAAAA)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // description based on target
                Text(
                    text = when {
                        targetMB < 25f -> "Light — mostly screenshots and small photos"
                        targetMB < 50f -> "Moderate — mix of photos and documents"
                        targetMB < 75f -> "Active — includes larger photos and short videos"
                        else           -> "Aggressive — targets large videos and files"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF777777),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // save target to prefs
                val prefs = context.getSharedPreferences(
                    "spacemint_target", android.content.Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putFloat("target_mb", targetMB)
                    .putFloat("current_target_mb", targetMB)
                    .apply()
                onDone()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MintGreen,
                contentColor   = Color.White
            )
        ) {
            Text(
                text = "Let's clean it up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── SCREEN 3: HOME ───────────────────────────────────────────
@Composable
fun HomeScreen(
    onStartReview: () -> Unit,
    onViewBin: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val imagesGranted = permissions[android.Manifest.permission.READ_MEDIA_IMAGES] ?: false
        val videosGranted = permissions[android.Manifest.permission.READ_MEDIA_VIDEO] ?: false
        if (!imagesGranted || !videosGranted) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        val perms = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_MEDIA_IMAGES
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) perms.add(android.Manifest.permission.READ_MEDIA_IMAGES)

            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_MEDIA_VIDEO
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) perms.add(android.Manifest.permission.READ_MEDIA_VIDEO)

            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) perms.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (perms.isNotEmpty()) {
            permLauncher.launch(perms.toTypedArray())
        }
    }

    // ── LIMITED ACCESS DIALOG ─────────────────────────────────
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = {
                Text(
                    text = "Full access needed",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "SpaceMint needs full access to your photos and videos to show all your files for review.\n\nPlease tap 'Open Settings' → tap 'Permissions' → set Photos and Videos to 'Allow all'.",
                    color = Color(0xFF777777),
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        ).apply {
                            data = android.net.Uri.fromParts(
                                "package", context.packageName, null
                            )
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintGreen,
                        contentColor   = Color.White
                    )
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Later", color = Color(0xFF999999))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF8))
    ) {
        // ── HEADER ───────────────────────────────────
        var showSettings by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SpaceMint",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintGreen
                )
                Text(
                    text = "Your daily space cleaner",
                    fontSize = 14.sp,
                    color = Color(0xFF777777)
                )
            }
            // gear icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                    .clickable { showSettings = true },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚙", fontSize = 20.sp)
            }
        }

// settings bottom sheet
        if (showSettings) {
            SettingsScreen(onDismiss = { showSettings = false })
        }

        // ── STORAGE CARD ──────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = Color(0xFFE0E0E0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Phone storage",
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${StorageHelper.formatBytes(StorageHelper.getUsedStorage())} of ${StorageHelper.formatBytes(StorageHelper.getTotalStorage())} used",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { StorageHelper.getUsedPercent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = when {
                        StorageHelper.getUsedPercent() > 0.85f -> Color(0xFFE24B4A)
                        StorageHelper.getUsedPercent() > 0.65f -> Color(0xFFEF9F27)
                        else                                   -> MintGreen
                    },
                    trackColor = Color(0xFFEEEEEE)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        StorageHelper.getUsedPercent() > 0.85f -> "Critical — clean up now!"
                        StorageHelper.getUsedPercent() > 0.65f -> "Getting full — clean now"
                        else -> "${StorageHelper.formatBytes(StorageHelper.getFreeStorage())} free"
                    },
                    fontSize = 12.sp,
                    color = when {
                        StorageHelper.getUsedPercent() > 0.85f -> Color(0xFFE24B4A)
                        StorageHelper.getUsedPercent() > 0.65f -> Color(0xFFBA7517)
                        else                                   -> MintGreen
                    }
                )
            }
        }

        // ── SESSION CARD ──────────────────────────────
        // ── SESSION CARD ──────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MintGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ready to clean",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "5 files waiting — takes 60 seconds",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Text(text = "🌿", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BIG PROMINENT BUTTON
                Button(
                    onClick = onStartReview,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor   = MintGreen
                    ),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Text(
                        text = "Start Reviewing  →",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ── STATS ROW ────────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                number   = StorageHelper.formatBytes(
                    StorageHelper.getTotalFreedBytes(context)
                ),
                label    = "freed total",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                number   = "${StorageHelper.getTotalDeleted(context)}",
                label    = "files deleted",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                number      = "🔥 ${StorageHelper.getStreak(context)}",
                label       = "day streak",
                modifier    = Modifier.weight(1f),
                numberColor = MintGreen
            )
        }

        // ── DAILY FACT ───────────────────────────────
        FactCard()

        // ── BIN BUTTON ────────────────────────────────
        // ── BIN BUTTON — only show when bin has items ──
        if (BinManager.count() > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onViewBin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEDED),
                    contentColor   = Color(0xFFA32D2D)
                )
            ) {
                Text(
                    text = "Recycle bin — ${BinManager.count()} items waiting",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        // ── DAILY TARGET BAR ─────────────────────────
        var targetMB by remember {
            mutableStateOf(
                context.getSharedPreferences(
                    "spacemint_target", android.content.Context.MODE_PRIVATE
                ).getFloat("target_mb", 50f)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, Color(0xFFE0E0E0)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily clean target",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Box(
                        modifier = Modifier
                            .background(MintGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "~${targetMB.toInt()} MB / day",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = targetMB,
                    onValueChange = {
                        targetMB = it
                        TargetManager.setUserTarget(context, it)
                    },
                    valueRange = 10f..100f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor         = MintGreen,
                        activeTrackColor   = MintGreen,
                        inactiveTrackColor = Color(0xFFE0E0E0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "10 MB", fontSize = 10.sp, color = Color(0xFFAAAAAA))
                    Text(text = "100 MB", fontSize = 10.sp, color = Color(0xFFAAAAAA))
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = when {
                        targetMB < 25f -> "Light — mostly screenshots and small photos"
                        targetMB < 50f -> "Moderate — mix of photos and documents"
                        targetMB < 75f -> "Active — includes larger photos and short videos"
                        else           -> "Aggressive — targets large videos and files"
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }



        Spacer(modifier = Modifier.height(8.dp))


        // ── BINRUN GAME ───────────────────────────────
        BinRunGame()

        Spacer(modifier = Modifier.height(16.dp))
    }
}
@Composable
fun SettingsScreen(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // morning and evening times
    var morningHour by remember {
        mutableStateOf(NotificationScheduler.getMorningTime(context).first)
    }
    var eveningHour by remember {
        mutableStateOf(NotificationScheduler.getEveningTime(context).first)
    }
    var targetMB by remember {
        mutableStateOf(TargetManager.getCurrentTarget(context))
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAF8)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // ── HEADER ────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(99.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", fontSize = 14.sp, color = Color(0xFF777777))
                    }
                }

                // ── NOTIFICATIONS ─────────────────────
                SettingsSection(title = "Notifications") {
                    // morning time
                    SettingsRow(
                        title = "Morning reminder",
                        subtitle = "Currently $morningHour:00 AM"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(9, 10, 11, 12).forEach { hour ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (morningHour == hour) MintGreen
                                            else Color(0xFFEEEEEE),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            morningHour = hour
                                            NotificationScheduler.setMorningTime(context, hour, 0)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${hour}AM",
                                        fontSize = 12.sp,
                                        color = if (morningHour == hour)
                                            Color.White else Color(0xFF555555),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // evening time
                    SettingsRow(
                        title = "Evening reminder",
                        subtitle = "Currently $eveningHour:00 PM"
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(17, 18, 19, 20).forEach { hour ->
                                val label = "${hour - 12}PM"
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (eveningHour == hour) MintGreen
                                            else Color(0xFFEEEEEE),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            eveningHour = hour
                                            NotificationScheduler.setEveningTime(
                                                context, hour, 0
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        color = if (eveningHour == hour)
                                            Color.White else Color(0xFF555555),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // ── DAILY TARGET ──────────────────────
                SettingsSection(title = "Daily Clean Target") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target per session",
                            fontSize = 14.sp,
                            color = Color(0xFF1A1A1A)
                        )
                        Box(
                            modifier = Modifier
                                .background(MintGreen, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "~${targetMB.toInt()} MB",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = targetMB,
                        onValueChange = {
                            targetMB = it
                            TargetManager.setUserTarget(context, it)
                        },
                        valueRange = 10f..100f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor         = MintGreen,
                            activeTrackColor   = MintGreen,
                            inactiveTrackColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "10 MB", fontSize = 10.sp, color = Color(0xFFAAAAAA))
                        Text(text = "100 MB", fontSize = 10.sp, color = Color(0xFFAAAAAA))
                    }
                }

                // ── SHARE APP ─────────────────────────
                SettingsSection(title = "Share") {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_SEND
                            ).apply {
                                type = "text/plain"
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "I have been using SpaceMint to slowly clean my phone storage — 5 files a day. Really useful app. Check it out on Play Store — search SpaceMint."
                                )
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(intent, "Share SpaceMint")
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8F7F1),
                            contentColor   = MintGreen
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = "Share SpaceMint with friends",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ── PRIVACY POLICY ────────────────────
                SettingsSection(title = "Legal") {
                    TextButton(
                        onClick = {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(
                                    "https://bafflingraman.github.io/SpaceMint/privacy_policy"
                                )
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Text(
                            text = "Privacy Policy",
                            fontSize = 14.sp,
                            color = MintGreen,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        )
                    }
                }

                // ── ABOUT ─────────────────────────────
                SettingsSection(title = "About") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SpaceMint",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0",
                            fontSize = 13.sp,
                            color = Color(0xFF999999)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Made by Raman",
                            fontSize = 13.sp,
                            color = Color(0xFF777777)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "devoted to mankind",
                            fontSize = 11.sp,
                            color = Color(0xFFAAAAAA),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "credited to a stranger",
                            fontSize = 10.sp,
                            color = Color(0xFFCCCCCC),
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// ── SETTINGS HELPER COMPOSABLES ───────────────────────────────
@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFAAAAAA),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(0.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, Color(0xFFE8E8E8)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

// ── REUSABLE STAT CARD ────────────────────────────────────────
@Composable
fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier,
    numberColor: Color = Color(0xFF1A1A1A)
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp, color = Color(0xFFE8E8E8)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = number, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = numberColor)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = label, fontSize = 10.sp, color = Color(0xFF999999), textAlign = TextAlign.Center)
        }
    }
}

// ── SCREEN 4: REVIEW ──────────────────────────────────────────
@Composable
fun ReviewScreen(onFinished: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }
    var deletedCount by remember { mutableStateOf(0) }
    var showUndo by remember { mutableStateOf(false) }
    var lastDeletedFile by remember { mutableStateOf<ReviewFile?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasFullAccess by remember { mutableStateOf(true) }
    var showAccessDialog by remember { mutableStateOf(false) }

    // check full access every time review screen opens
    LaunchedEffect(Unit) {
        val hasImages = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_MEDIA_IMAGES
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        val hasVideos = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.READ_MEDIA_VIDEO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasImages || !hasVideos) {
            hasFullAccess = false
            showAccessDialog = true
        }
    }

    // ── FULL ACCESS DIALOG ────────────────────────────────────
    if (showAccessDialog) {
        AlertDialog(
            onDismissRequest = { },  // cannot dismiss — must take action
            title = {
                Text(
                    text = "Full access required",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You have given SpaceMint limited access. This means only a few photos are visible and the same files will repeat.\n\nTo review your full gallery and actually free storage, please allow full access.\n\nTap Open Settings → Permissions → Photos and Videos → Allow all.",
                    color = Color(0xFF777777),
                    lineHeight = 22.sp,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccessDialog = false
                        // open app settings directly
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        ).apply {
                            data = android.net.Uri.fromParts(
                                "package", context.packageName, null
                            )
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintGreen,
                        contentColor   = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Open Settings",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAccessDialog = false
                        onFinished() // go back
                    }
                ) {
                    Text(
                        text = "Maybe later",
                        color = Color(0xFF999999)
                    )
                }
            }
        )
        // ── UNDO BAR ──────────────────────────────
        if (showUndo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .background(
                        Color(0xFF1A1A1A),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Added to bin",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    TextButton(
                        onClick = {
                            lastDeletedFile?.let {
                                BinManager.restore(BinManager.items.lastOrNull() ?: return@TextButton)
                                if (deletedCount > 0) deletedCount--
                                if (currentIndex > 0) currentIndex--
                                showUndo = false
                                lastDeletedFile = null
                            }
                        }
                    ) {
                        Text(
                            text = "Undo",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                    }
                }
            }
        }
    }

    // if no full access — show warning screen instead of review
    if (!hasFullAccess) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAF8))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Limited access detected",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "SpaceMint can only see a few photos right now. The same files will keep repeating and very little storage will be freed.\n\nAllow full access so SpaceMint can review your entire gallery.",
                fontSize = 14.sp,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    showAccessDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintGreen,
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text = "Allow full access",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onFinished) {
                Text(
                    text = "Go back",
                    color = Color(0xFF999999)
                )
            }
        }
        return
    }


    // ── REST OF REVIEWSCREEN CONTINUES BELOW ──────────────────
    // ... your existing ReviewScreen code from here


    val files = remember {
        QueueManager.getNextBatch(context, batchSize = 5).ifEmpty {
            listOf(ReviewFile(
                name = "No files found",
                date = "", size = "", type = "Photo",
                hint = "Grant storage permission and reopen the app"
            ))
        }
    }


    var freedMB      by remember { mutableStateOf(0.0) }

    if (currentIndex >= files.size) {
        ReviewDoneScreen(
            deletedCount = deletedCount,
            freedMB      = freedMB,
            onGoHome     = onFinished
        )
        return
    }

    val file = files[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF8))
    ) {

        // ── TOP BAR ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Review session",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${currentIndex + 1} of ${files.size}",
                fontSize = 14.sp,
                color = Color(0xFF999999)
            )
        }

        // ── PROGRESS DOTS ─────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            files.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(
                            when {
                                index < currentIndex  -> MintGreen
                                index == currentIndex -> Color(0xFFEF9F27)
                                else                  -> Color(0xFFE0E0E0)
                            }
                        )
                )
            }
        }

        // ── FILE CARD ─────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp, Color(0xFFE0E0E0)
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ── REAL THUMBNAIL ────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(Color(0xFF1A1A1A))
                        .clickable {
                            file.uri?.let { uri ->
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW
                                ).apply {
                                    setDataAndType(
                                        uri,
                                        if (file.type == "Video") "video/*" else "image/*"
                                    )
                                    addFlags(
                                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                                context.startActivity(intent)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (file.uri != null) {
                        if (file.type == "Video") {
                            // ── REAL VIDEO THUMBNAIL ──────────────────
                            val thumbnail = remember(file.uri) {
                                try {
                                    val retriever = android.media.MediaMetadataRetriever()
                                    retriever.setDataSource(context, file.uri)
                                    val bitmap = retriever.getFrameAtTime(
                                        1_000_000, // get frame at 1 second
                                        android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                                    )
                                    retriever.release()
                                    bitmap
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            if (thumbnail != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = thumbnail.asImageBitmap(),
                                    contentDescription = file.name,
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // fallback if frame extraction fails
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF1A1A2A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🎥", fontSize = 52.sp)
                                }
                            }

                            // play button overlay
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.6f),
                                        RoundedCornerShape(99.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "▶", fontSize = 26.sp, color = Color.White)
                            }

                        } else {
                            // ── PHOTO THUMBNAIL ───────────────────────
                            coil.compose.AsyncImage(
                                model = coil.request.ImageRequest.Builder(context)
                                    .data(file.uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = file.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // tap to view label
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.45f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Tap to view full",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }

                    } else {
                        Text(
                            text = when (file.type) {
                                "Video"      -> "🎥"
                                "PDF"        -> "📄"
                                "Screenshot" -> "📋"
                                else         -> "📸"
                            },
                            fontSize = 52.sp
                        )
                    }
                }

                // ── FILE INFO ROW ─────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = file.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A1A),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = file.date,
                            fontSize = 11.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFEDED), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = file.size,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA32D2D)
                        )
                    }
                }
            }
        }

        // ── SMART HINT ────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .background(Color(0xFFFAEEDA), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "💡", fontSize = 14.sp)
            Text(
                text = file.hint,
                fontSize = 12.sp,
                color = Color(0xFF854F0B),
                lineHeight = 18.sp
            )
        }



        Spacer(modifier = Modifier.height(12.dp))

        // ── DELETE / KEEP BUTTONS ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // DELETE
            // DELETE
            Button(
                onClick = {
                    lastDeletedFile = file
                    BinManager.addToBin(file)
                    deletedCount++
                    val num = file.size
                        .replace(" MB", "")
                        .replace(" GB", "")
                        .replace(" KB", "")
                        .toDoubleOrNull() ?: 0.0
                    freedMB += when {
                        file.size.contains("GB") -> num * 1000
                        file.size.contains("KB") -> num / 1000
                        else                     -> num
                    }
                    currentIndex++
                    showUndo = true
                    scope.launch {
                        kotlinx.coroutines.delay(3000L)
                        showUndo = false
                        lastDeletedFile = null
                    }
                },

                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEDED),
                    contentColor   = Color(0xFFA32D2D)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "🗑", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Delete",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = file.size,
                        fontSize = 11.sp,
                        color = Color(0xFFCC4444)
                    )
                }
            }

            // KEEP
            Button(
                onClick = { currentIndex++ },
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8F7F1),
                    contentColor   = Color(0xFF0F6E56)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "✓", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keep",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Keep it",
                        fontSize = 11.sp,
                        color = Color(0xFF0F6E56).copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))



    }
}

// ── REVIEW DONE SCREEN ────────────────────────────────────────


// ── REVIEW DONE SCREEN ────────────────────────────────────────
@Composable
fun ReviewDoneScreen(
    deletedCount: Int,
    freedMB: Double,
    onGoHome: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val freedBytes = (freedMB * 1_000_000).toLong()
        StorageHelper.recordSession(context, deletedCount, freedBytes)
        NotificationScheduler.markSessionComplete(context)
        NotificationScheduler.cancelEveningIfDone(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF8))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (deletedCount == 0) {
            // ── ALL KEPT — no deletions ───────────────
            Text(text = "👍", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "All files kept!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You reviewed 5 files and decided to keep them all. That is perfectly fine — SpaceMint will show you new ones tomorrow.",
                fontSize = 14.sp,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    BinManager.isComingFromReview = false
                    onGoHome()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintGreen,
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text = "Back to home",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        } else {
            // ── FILES DELETED — celebration ───────────
            Text(text = "🎉", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Session complete!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your phone has a little more room now.",
                fontSize = 14.sp,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(32.dp))

            // session stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    number   = "$deletedCount",
                    label    = "deleted this session",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    number      = "%.1f MB".format(freedMB),
                    label       = "space freed",
                    modifier    = Modifier.weight(1f),
                    numberColor = MintGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // all time stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    number   = "${StorageHelper.getTotalDeleted(context)}",
                    label    = "total deleted",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    number      = "🔥 ${StorageHelper.getStreak(context)}",
                    label       = "day streak",
                    modifier    = Modifier.weight(1f),
                    numberColor = MintGreen
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    BinManager.isComingFromReview = true
                    onGoHome()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE24B4A),
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text = "Review ${deletedCount} deleted files",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = {
                BinManager.isComingFromReview = false
                onGoHome()
            }) {
                Text(
                    text = "Skip — keep in bin for now",
                    fontSize = 13.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

// ── SCREEN 5: BIN ─────────────────────────────────────────────


// ── BIN ITEM CARD ─────────────────────────────────────────────
@Composable
fun BinScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var binItems   by remember { mutableStateOf(BinManager.items) }
    var showDialog by remember { mutableStateOf(false) }

    // auto show confirm dialog when arriving from review
    LaunchedEffect(Unit) {
        if (binItems.isNotEmpty()) {
            kotlinx.coroutines.delay(300L)
            showDialog = true
        }
    }

    // system delete permission launcher
    val deleteLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts
            .StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            BinManager.emptyBin()
            binItems = BinManager.items
            // close app after deletion — like Gmail after sending
            (context as? android.app.Activity)?.finish()
        }
    }

    // ── AUTO CONFIRM DIALOG ───────────────────────────────────
    if (showDialog && binItems.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Delete ${binItems.size} files permanently?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "${BinManager.totalSize()} will be freed from your phone forever.",
                        color = Color(0xFF777777),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // thumbnails preview row
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        binItems.take(4).forEach { item ->
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1A1A1A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.uri != null) {
                                    if (item.type == "Video") {
                                        // extract real video frame
                                        val thumbnail = remember(item.uri) {
                                            try {
                                                val retriever = android.media.MediaMetadataRetriever()
                                                retriever.setDataSource(context, item.uri)
                                                val bitmap = retriever.getFrameAtTime(
                                                    1_000_000,
                                                    android.media.MediaMetadataRetriever
                                                        .OPTION_CLOSEST_SYNC
                                                )
                                                retriever.release()
                                                bitmap
                                            } catch (e: Exception) { null }
                                        }
                                        if (thumbnail != null) {
                                            androidx.compose.foundation.Image(
                                                bitmap = thumbnail.asImageBitmap(),
                                                contentDescription = item.name,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0xFF2A2A2A)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "Video", fontSize = 8.sp, color = Color.White)
                                            }
                                        }
                                        // play icon overlay
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.6f),
                                                    RoundedCornerShape(99.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "▶", fontSize = 8.sp, color = Color.White)
                                        }
                                    } else {
                                        // photo
                                        coil.compose.AsyncImage(
                                            model = item.uri,
                                            contentDescription = item.name,
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                } else {
                                    Text(
                                        text = if (item.type == "Video") "Video" else "Photo",
                                        fontSize = 8.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        if (binItems.size > 4) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8EBE8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${binItems.size - 4}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF555555)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        BinManager.deleteAllFromStorage(
                            context = context,
                            onNeedPermission = { intentSender ->
                                deleteLauncher.launch(
                                    androidx.activity.result.IntentSenderRequest
                                        .Builder(intentSender).build()
                                )
                            }
                        )
                        binItems = BinManager.items
                        // Android 10 and below — close directly
                        if (BinManager.count() == 0) {
                            (context as? android.app.Activity)?.finish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE24B4A),
                        contentColor   = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "🗑  Yes, delete all permanently",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Keep in bin for now", color = MintGreen)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF8))
    ) {

        // ── TOP BAR ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor   = Color(0xFF1A1A1A)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "← Home", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Recycle bin",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )


            if (binItems.isNotEmpty()) {
                TextButton(onClick = { showDialog = true }) {
                    Text("Empty", fontSize = 13.sp, color = Color(0xFFE24B4A))
                }
            }
        }

        Text(
            text = if (binItems.isEmpty()) "Bin is empty"
            else "${binItems.size} items · ${BinManager.totalSize()}",
            fontSize = 12.sp,
            color = Color(0xFF999999),
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
        )

        // ── BIG DELETE BUTTON ─────────────────────────
        if (binItems.isNotEmpty()) {
            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE24B4A),
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text = "🗑  Delete all ${binItems.size} files permanently",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // ── EMPTY STATE ───────────────────────────────
        if (binItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🗑", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Bin is empty",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Files you delete during review\nwill appear here.",
                    fontSize = 14.sp,
                    color = Color(0xFF777777),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        } else {
            // ── BIN ITEMS LIST ────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                binItems.forEach { item ->
                    BinItemCard(
                        item = item,
                        onRestore = {
                            BinManager.restore(item)
                            binItems = BinManager.items
                        },
                        onDeleteNow = {
                            BinManager.deleteFromStorage(
                                context   = context,
                                item      = item,
                                onSuccess = {
                                    BinManager.restore(item)
                                    binItems = BinManager.items
                                    if (BinManager.count() == 0) {
                                        (context as? android.app.Activity)?.finish()
                                    }
                                },
                                onNeedPermission = { intentSender ->
                                    deleteLauncher.launch(
                                        androidx.activity.result.IntentSenderRequest
                                            .Builder(intentSender).build()
                                    )
                                    BinManager.restore(item)
                                    binItems = BinManager.items
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BinItemCard(
    item: BinItem,
    onRestore: () -> Unit,
    onDeleteNow: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp, Color(0xFFE8E8E8)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── THUMBNAIL — tap to open ───────────────
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0FBF6))
                    .clickable {
                        item.uri?.let { uri ->
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW
                            ).apply {
                                setDataAndType(
                                    uri,
                                    if (item.type == "Video") "video/*" else "image/*"
                                )
                                addFlags(
                                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            }
                            context.startActivity(intent)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (item.uri != null) {
                    if (item.type == "Video") {
                        val thumbnail = remember(item.uri) {
                            try {
                                val retriever = android.media.MediaMetadataRetriever()
                                retriever.setDataSource(context, item.uri)
                                val bitmap = retriever.getFrameAtTime(
                                    1_000_000,
                                    android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                                )
                                retriever.release()
                                bitmap
                            } catch (e: Exception) { null }
                        }
                        if (thumbnail != null) {
                            androidx.compose.foundation.Image(
                                bitmap = thumbnail.asImageBitmap(),
                                contentDescription = item.name,
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(text = "Video", fontSize = 10.sp, color = Color(0xFF777777))
                        }
                        // play icon
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(99.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "▶", fontSize = 8.sp, color = Color.White)
                        }
                    } else {
                        coil.compose.AsyncImage(
                            model = item.uri,
                            contentDescription = item.name,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text(
                        text = when(item.type) {
                            "Video" -> "Vid"
                            "PDF"   -> "PDF"
                            else    -> "Img"
                        },
                        fontSize = 10.sp,
                        color = Color(0xFF777777)
                    )
                }
            }

            // ── FILE INFO ─────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.size,
                    fontSize = 12.sp,
                    color = Color(0xFFA32D2D),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Tap photo to view",
                    fontSize = 10.sp,
                    color = Color(0xFFAAAAAA)
                )
            }

            // ── BUTTONS — vertical stack ──────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Restore button
                Button(
                    onClick = onRestore,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE8F7F1),
                        contentColor   = MintGreen
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical   = 8.dp
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Restore",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delete button
                Button(
                    onClick = onDeleteNow,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEDED),
                        contentColor   = Color(0xFFA32D2D)
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical   = 8.dp
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
// ── DAILY FACT CARD ───────────────────────────────────────────
@Composable
fun FactCard() {
    // remember picks a new fact every time the screen opens
    val fact = remember { FactsData.getRandom() }

    // category colour mapping
    val categoryColor = when(fact.category) {
        "Universe"    -> Color(0xFF185FA5)
        "Animals"     -> Color(0xFF0F6E56)
        "Geography"   -> Color(0xFF854F0B)
        "Human Body"  -> Color(0xFFA32D2D)
        "History"     -> Color(0xFF534AB7)
        "Science"     -> Color(0xFF993C1D)
        "India"       -> Color(0xFF1D6B99)
        else          -> Color(0xFF555555)
    }

    val categoryBg = when(fact.category) {
        "Universe"    -> Color(0xFFE6F1FB)
        "Animals"     -> Color(0xFFE1F5EE)
        "Geography"   -> Color(0xFFFAEEDA)
        "Human Body"  -> Color(0xFFFCEBEB)
        "History"     -> Color(0xFFEEEDFE)
        "Science"     -> Color(0xFFFAECE7)
        "India"       -> Color(0xFFE1F0FA)
        else          -> Color(0xFFF1EFE8)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = Color(0xFFE0E0E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // header row — emoji + category pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Text(text = fact.emoji, fontSize = 22.sp)

                Box(
                    modifier = Modifier
                        .background(categoryBg, RoundedCornerShape(99.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = fact.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Did you know?",
                    fontSize = 10.sp,
                    color = Color(0xFFAAAAAA)
                )
            }

            // the fact text
            Text(
                text = fact.fact,
                fontSize = 13.sp,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
        }
    }
}