package com.github.tim06.wallet_contest.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.R

val RobotoMedium = FontFamily(Font(R.font.roboto_medium))
val RobotoRegular = FontFamily(Font(R.font.roboto_regular))
val RobotoMonoRegular = FontFamily(Font(R.font.robotomono_regular))
val InterRegular = FontFamily(Font(R.font.inter_regular))
val InterSemibold = FontFamily(Font(R.font.inter_semibold))
val SansMedium = FontFamily(Font(R.font.open_sans_medium))

val Typography = Typography(
    h6 = TextStyle(
        fontFamily = RobotoRegular,
        fontWeight = FontWeight.Medium,
        lineHeight = 24.sp,
        fontSize = 20.sp,
        color = PrimaryTextColor
    ),
    body1 = TextStyle(
        fontFamily = RobotoMedium,
        fontWeight = FontWeight.Medium,
        lineHeight = 28.sp,
        fontSize = 24.sp,
        color = TitleTextColor
    ),
    body2 = TextStyle(
        fontFamily = RobotoRegular,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        fontSize = 15.sp,
        color = PrimaryTextColor
    ),
    button = TextStyle(
        fontFamily = RobotoRegular,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        fontSize = 15.sp,
        letterSpacing = 0.1.sp,
        color = Color.White
    ),
    caption = TextStyle(
        fontFamily = RobotoRegular,
        fontWeight = FontWeight.Medium,
        lineHeight = 18.sp,
        fontSize = 14.sp,
        color = TonBlue2
    )
)

val MainWalletAddressTextStyle = TextStyle(
    fontFamily = RobotoRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 18.sp,
    color = Color.White
)

val BalanceBigTextStyle = TextStyle(
    fontFamily = SansMedium,
    fontWeight = FontWeight.Medium,
    fontSize = 44.sp,
    lineHeight = 56.sp,
    color = Color.White
)

val BalanceInUsdTextStyle = TextStyle(
    fontFamily = InterRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 16.sp,
    color = BalanceInUsdColor
)

val BalanceAdditionTextStyle = TextStyle(
    fontFamily = RobotoRegular,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    color = Color.White
)