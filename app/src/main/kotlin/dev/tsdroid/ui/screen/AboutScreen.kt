package dev.tsdroid.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tsdroid.han.R

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repoUrl = "https://github.com/YUAXI/TS6_Droid_CN"
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
        ) {
            Text(
                text = stringResource(R.string.about_back),
                color = Color(0xFF1976D2),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onBack() }.padding(8.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = stringResource(R.string.about_software),
                style = MaterialTheme.typography.titleLarge.copy(color = Color(0xFF121212), fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(text = stringResource(R.string.about_credits_title), fontWeight = FontWeight.Bold, color = Color(0xFF121212), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.about_credits_desc),
            color = Color(0x8A000000), fontSize = 14.sp, lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(R.string.about_enhancement_title), fontWeight = FontWeight.Bold, color = Color(0xFF121212), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.about_enhancement_desc),
            color = Color(0x8A000000), fontSize = 14.sp, lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(R.string.about_license_title), fontWeight = FontWeight.Bold, color = Color(0xFF121212), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.about_license_desc),
            color = Color(0x8A000000), fontSize = 14.sp, lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(R.string.about_repo_title), fontWeight = FontWeight.Bold, color = Color(0xFF121212), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.about_repo_desc),
            color = Color(0x8A000000), fontSize = 14.sp, lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.about_repo_link),
            color = Color(0xFF1976D2),
            fontSize = 14.sp,
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
                    context.startActivity(intent)
                } catch (e: Exception) {
                }
            }
        )

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider(color = Color(0x1A000000), thickness = 1.dp)
        Spacer(modifier = Modifier.height(28.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x0AFF5252)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.about_warning_title),
                    color = Color(0xFFD32F2F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.about_warning_desc),
                    color = Color(0xFFE57373),
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
