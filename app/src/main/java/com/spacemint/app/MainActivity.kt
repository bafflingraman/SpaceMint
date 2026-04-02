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

val MintGreen = Color(0xFF1D9E75)
val MintDark  = Color(0xFF0F6E56)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // ── TEMP TEST — remove after testing ──────────
       // android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
         //   NotificationHelper.sendReminder(this, isMorning = true)
      //  }, 5000L) // fires after 5 seconds
    }

} // ← class ends here

@Composable
fun AppNavigation(startDestination: String = "splash") {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(onGetStarted = {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
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
        kotlinx.coroutines.delay(2800L)
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
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Old screenshots. Duplicate photos.\nDownloads you never opened.\nSpaceMint helps you clear them — slowly, safely, every day.",
            fontSize = 15.sp,
            color = Color(0xFF777777),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(60.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MintGreen,
                contentColor = Color.White
            )
        ) {
            Text(text = "Let's clean it up", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 8.dp)
        ) {
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MintGreen),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Session ready",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "5 files · under 60 seconds",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onStartReview,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor   = MintGreen
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical   = 10.dp
                        )
                    ) {
                        Text(
                            text = "Start reviewing →",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(text = "🌿", fontSize = 48.sp)
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
                text = "🗑 View bin (${BinManager.count()} items)",
                fontSize = 14.sp
            )
        }
        // ── BANNER IMAGE ──────────────────────────────
        Spacer(modifier = Modifier.height(12.dp))
        Image(
            painter = painterResource(id = R.drawable.spacemint_banner),
            contentDescription = "SpaceMint",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))


        // ── BINRUN GAME ───────────────────────────────
        BinRunGame()

        Spacer(modifier = Modifier.height(16.dp))
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
    val context = androidx.compose.ui.platform.LocalContext.current

    val files = remember {
        QueueManager.getNextBatch(context, batchSize = 5).ifEmpty {
            listOf(ReviewFile(
                name = "No files found",
                date = "", size = "", type = "Photo",
                hint = "Grant storage permission and reopen the app"
            ))
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var deletedCount by remember { mutableStateOf(0) }
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
                        .background(Color(0xFFF0FBF6))
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

                        coil.compose.AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(file.uri)
                                .crossfade(true)
                                .build(),
                            contentDescription = file.name,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // video play button overlay
                        if (file.type == "Video") {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        Color.Black.copy(alpha = 0.5f),
                                        RoundedCornerShape(99.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "▶",
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                            }
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

        Spacer(modifier = Modifier.weight(1f))

        // ── DELETE / KEEP BUTTONS ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // DELETE
            Button(
                onClick = {
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
                },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEDED),
                    contentColor   = Color(0xFFA32D2D)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🗑", fontSize = 20.sp)
                    Text(
                        text = "Delete",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // KEEP
            Button(
                onClick = { currentIndex++ },
                modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8F7F1),
                    contentColor   = Color(0xFF0F6E56)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "✓", fontSize = 20.sp)
                    Text(
                        text = "Keep",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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

    // record session stats + mark complete
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

        // stats row
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

        // go to bin button
        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (deletedCount > 0) Color(0xFFE24B4A) else MintGreen,
                contentColor   = Color.White
            )
        ) {
            Text(
                text = if (deletedCount > 0)
                    "🗑 Review ${deletedCount} deleted files"
                else
                    "Back to home",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // skip bin — go home directly
        if (deletedCount > 0) {
            TextButton(onClick = {
                // skip bin — mark as going home
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
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF0FBF6)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (item.uri != null) {
                                    coil.compose.AsyncImage(
                                        model = item.uri,
                                        contentDescription = item.name,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Text(
                                        text = when(item.type) {
                                            "Video" -> "🎥"
                                            else    -> "📸"
                                        },
                                        fontSize = 20.sp
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
            Spacer(modifier = Modifier.weight(1f))

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

// ── BIN ITEM CARD ─────────────────────────────────────────────
@Composable
fun BinItemCard(
    item: BinItem,
    onRestore: () -> Unit,
    onDeleteNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // thumbnail
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF0FBF6)),
                contentAlignment = Alignment.Center
            ) {
                if (item.uri != null) {
                    coil.compose.AsyncImage(
                        model = item.uri,
                        contentDescription = item.name,
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = when(item.type) {
                            "Video"      -> "🎥"
                            "PDF"        -> "📄"
                            "Screenshot" -> "📋"
                            else         -> "📸"
                        },
                        fontSize = 22.sp
                    )
                }
            }

            // file info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = "Deleted ${item.deletedDate} · ${item.size}",
                    fontSize = 11.sp,
                    color = Color(0xFF999999),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // restore button
            Button(
                onClick = onRestore,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8F7F1),
                    contentColor   = MintGreen
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "Restore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // delete now button
            Button(
                onClick = onDeleteNow,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEDED),
                    contentColor   = Color(0xFFA32D2D)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(text = "Delete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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