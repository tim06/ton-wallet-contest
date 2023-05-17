package com.github.tim06.wallet_contest.ui.components.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleEventObserver
import com.github.tim06.wallet_contest.R
import com.github.tim06.wallet_contest.ton.TonWalletClient
import com.github.tim06.wallet_contest.ui.components.BackIcon
import com.github.tim06.wallet_contest.ui.feature.scan.CameraPermissionScreen
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.telegram.ImageLoader
import org.telegram.messenger.Utilities

@Composable
fun TonWalletCameraScreen(
    tonWalletClient: TonWalletClient,
    onBackClick: () -> Unit,
    onResult: (String) -> Unit
) {
    BackHandler(onBack = onBackClick)
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasStoragePermission by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        hasStoragePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            hasCameraPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
            hasStoragePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasStoragePermission = granted
        }
    )
    val qrCodeReader = remember { QRCodeReader() }
    val imagePicker = rememberLauncherForActivityResult(
        contract = PickPhotoActivityContract(),
        onResult = { data ->
            data?.let {
                val screenSize = Utilities.getRealScreenSize(context)
                val bitmap: Bitmap = ImageLoader.loadBitmap(
                    context, null, data,
                    screenSize.x.toFloat(), screenSize.y.toFloat(), true
                )
                val intArray = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                try {
                    val result = qrCodeReader.decode(
                        BinaryBitmap(
                            GlobalHistogramBinarizer(
                                RGBLuminanceSource(
                                    bitmap.width, bitmap.height, intArray
                                )
                            )
                        )
                    )
                    if (result.text.isEmpty().not() && result.text.startsWith("ton://transfer/")) {
                        val uri = Uri.parse(result.text)
                        val path = uri.path?.replace("/", "")
                        if (path != null && tonWalletClient.isValidWalletAddress(path)) {
                            onResult.invoke(result.text.split("/").last())
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )
    LaunchedEffect(key1 = true) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var torchEnabled by remember { mutableStateOf(false) }
    var onResultTriggered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (hasCameraPermission) {
            ScanQrScreen(
                tonWalletClient = tonWalletClient,
                torchEnabled = torchEnabled,
                onCodeScanResult = { result ->
                    if (!onResultTriggered) {
                        onResult.invoke(result)
                        onResultTriggered = true
                    }
                }
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = -((256.dp / 2) + 70.dp).roundToPx()
                        )
                    },
                text = stringResource(id = R.string.scan_qr_title),
                style = MaterialTheme.typography.body1.copy(
                    color = Color.White
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = 256.dp.roundToPx()
                        )
                    },
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconBtn(iconRes = R.drawable.ic_gallery) {
                    if (hasStoragePermission) {
                        imagePicker.launch(Unit)
                    } else {
                        storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
                IconBtn(
                    iconRes = R.drawable.ic_torch,
                    checked = torchEnabled
                ) {
                    torchEnabled = !torchEnabled
                }
            }
        } else {
            CameraPermissionScreen(onBackClick)
        }
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = {},
            navigationIcon = {
                BackIcon(iconColor = Color.White, click = onBackClick)
            },
            backgroundColor = Color.Transparent,
            elevation = 0.dp
        )
    }
}

@Composable
fun ScanQrScreen(
    tonWalletClient: TonWalletClient,
    torchEnabled: Boolean = false,
    onCodeScanResult: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }
    var camera by remember {
        mutableStateOf<androidx.camera.core.Camera?>(null)
    }
    AndroidView(
        factory = { context1 ->
            val previewView = PreviewView(context1)
            val preview = Preview.Builder().build()
            val selector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(
                    android.util.Size(
                        700,
                        500
                    )
                )
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(context1),
                QrCodeAnalyzer(
                    tonWalletClient = tonWalletClient,
                    onQrCodeScanned = onCodeScanResult
                )
            )
            try {
                camera = cameraProviderFuture.get().bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            previewView
        },
        modifier = Modifier.fillMaxSize(),
        update = {
            camera?.cameraControl?.enableTorch(torchEnabled)
        }
    )

    val path = remember { Path() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                drawContent()
                drawRect(
                    color = Color.Black,
                    alpha = 0.5f
                )
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(
                        x = center.x - (256.dp / 2).toPx(),
                        y = center.y - (256.dp / 2).toPx()
                    ),
                    size = androidx.compose.ui.geometry.Size(256.dp.toPx(), 256.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    blendMode = BlendMode.Src
                )

                val x = center.x - (256.dp / 2).toPx()
                val y = center.y - (256.dp / 2).toPx()
                val size1 = 256.dp.toPx()
                path.reset()
                path.moveTo(x, y + 40.dp.toPx())
                path.cubicTo(x, y - 6.dp.toPx(), x - 6.dp.toPx(), y, x + 40.dp.toPx(), y)
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                path.reset()
                path.moveTo((x + size1), y + 40.dp.toPx())
                path.cubicTo(
                    (x + size1),
                    y - 6.dp.toPx(),
                    (x + size1) + 6.dp.toPx(),
                    y,
                    x + size1 - 40.dp.toPx(),
                    y
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                path.reset()
                path.moveTo(x, y + size1 - 40.dp.toPx())
                path.cubicTo(
                    x,
                    (y + size1) + 6.dp.toPx(),
                    x - 6.dp.toPx(),
                    (y + size1),
                    x + 40.dp.toPx(),
                    (y + size1)
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                path.reset()
                path.moveTo((x + size1), y + size1 - 40.dp.toPx())
                path.cubicTo(
                    (x + size1),
                    (y + size1) + 6.dp.toPx(),
                    (x + size1) + 6.dp.toPx(),
                    (y + size1),
                    x + size1 - 40.dp.toPx(),
                    (y + size1)
                )
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
    )
}

@Composable
private fun IconBtn(
    @DrawableRes iconRes: Int,
    checked: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                brush = SolidColor(Color.White),
                alpha = if (checked) 0.8f else 0.4f,
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "Action",
            tint = Color.White
        )
    }
}