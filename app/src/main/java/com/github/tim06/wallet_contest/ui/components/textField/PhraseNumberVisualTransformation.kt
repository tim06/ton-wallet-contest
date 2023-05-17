package com.github.tim06.wallet_contest.ui.components.textField

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.github.tim06.wallet_contest.ui.theme.RobotoRegular
import com.github.tim06.wallet_contest.ui.theme.SecondaryTextColor

class PhraseNumberVisualTransformation(private val prefix: String) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        fontFamily = RobotoRegular,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = SecondaryTextColor
                    )
                ) {
                    append(prefix)
                }
                append(text)
            },
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = offset + prefix.length

                override fun transformedToOriginal(offset: Int): Int {
                    if (offset < prefix.length) return 0
                    return offset - prefix.length
                }
            }
        )
    }
}