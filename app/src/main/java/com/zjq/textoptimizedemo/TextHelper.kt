package com.zjq.textoptimizedemo

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.UnderlineSpan

object TextHelper {
    private const val REPLACE_TAG = "<n>"
    private const val DELETE_TAG = "<d>"
    private const val MODIFY_TAG = "<o>"
    private const val GARY_TAG = "<g>"
    private const val LIGHT_TAG = "<l>"
    private const val STRIKE_TAG = "<s>"
    private const val UNDER_TAG = "<u>"
    private const val PRE_BRACKET = "("
    private const val LAST_BRACKET = ")"
    private const val TAG_LENGTH = 3
    private const val WORD_MARK = ' '
    private val SPECIAL_CHAR = arrayOf('.',',')

    private fun recordClosingTagIndices(text: String, tag: String): List<RecordeTagIndex> {
        val records: MutableList<RecordeTagIndex> = ArrayList()
        var startIndex = 0
        while (startIndex < text.length) {
            val openIndex = text.indexOf(tag, startIndex)
            if (openIndex == -1) break
            val closeIndex = text.indexOf(tag, openIndex)
            if (closeIndex == -1) break
            startIndex = closeIndex + tag.length
            records.add(RecordeTagIndex(openIndex, closeIndex))
        }
        return records
    }

    fun outputRichText(processText: String, lightColor: Int, grayColor: Int): String {
        val recordWordLightIndexs = recordClosingTagIndices(processText, LIGHT_TAG)
        val recordWordGrayIndexs = recordClosingTagIndices(processText, GARY_TAG)
        val recordStrikeIndexs = recordClosingTagIndices(processText, STRIKE_TAG)
        val recordUnderlineIndexs = recordClosingTagIndices(processText, UNDER_TAG)
        val spannableString = SpannableString(processText)
        val lightForegroundColorSpan = ForegroundColorSpan(lightColor)
        val grayForegroundColorSpan = ForegroundColorSpan(grayColor)
        val strikethroughSpan = StrikethroughSpan()
        val underlineSpan = UnderlineSpan()
        for (lightTagRecorde in recordWordLightIndexs) {
            spannableString.setSpan(lightForegroundColorSpan, lightTagRecorde.startIndex, lightTagRecorde.endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        for (grayTagRecorde in recordWordGrayIndexs) {
            spannableString.setSpan(grayForegroundColorSpan, grayTagRecorde.startIndex, grayTagRecorde.endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        for (strikeTagRecorde in recordStrikeIndexs) {
            spannableString.setSpan(strikethroughSpan, strikeTagRecorde.startIndex, strikeTagRecorde.endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        for (underlineTagRecorde in recordUnderlineIndexs) {
            spannableString.setSpan(underlineSpan, underlineTagRecorde.startIndex, underlineTagRecorde.endIndex, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        return spannableString.toString()
    }

    fun getFullText(originText: String, optimizeText: String): Pair<String, String> {
        val uiSb = StringBuffer()
        val copySb = StringBuffer()
        var originIndex = 0
        var optimizeIndex = 0
        while (originText.length > originIndex && optimizeText.length > optimizeIndex) {
            val origin = originText[originIndex]
            val optimize = optimizeText[optimizeIndex]
            if (origin == optimize) {
                originIndex += 1
                optimizeIndex += 1
                uiSb.append(origin)
                copySb.append(origin)
            }
            if (origin != optimize && optimizeText.length > optimizeIndex + TAG_LENGTH) {
                val tag = optimizeText.substring(optimizeIndex, optimizeIndex + TAG_LENGTH)
                when (tag) {
                    REPLACE_TAG -> {
                        val endIndex = optimizeText.indexOf(REPLACE_TAG, optimizeIndex + TAG_LENGTH)
                        val range = endIndex - (optimizeIndex + TAG_LENGTH)
                        val opt = optimizeText.substring(optimizeIndex + TAG_LENGTH, endIndex)
                        uiSb.append(LIGHT_TAG)
                        uiSb.append(opt)
                        uiSb.append(LIGHT_TAG)
                        uiSb.append(GARY_TAG)
                        uiSb.append(PRE_BRACKET)
                        uiSb.append(UNDER_TAG)
                        if (originText.length > (originIndex + range)) {
                            uiSb.append(originText.substring(originIndex, originIndex + range))
                        }
                        uiSb.append(UNDER_TAG)
                        uiSb.append(LAST_BRACKET)
                        uiSb.append(GARY_TAG)
                        copySb.append(opt)
                        originIndex += range
                        optimizeIndex = endIndex + TAG_LENGTH
                    }
                    DELETE_TAG -> {
                        val endIndex = optimizeText.indexOf(DELETE_TAG, optimizeIndex + TAG_LENGTH)
                        val range = endIndex - (optimizeIndex + TAG_LENGTH)
                        val opt = optimizeText.substring(optimizeIndex + TAG_LENGTH, endIndex)
                        uiSb.append(GARY_TAG)
                        uiSb.append(PRE_BRACKET)
                        uiSb.append(STRIKE_TAG)
                        uiSb.append(opt)
                        uiSb.append(STRIKE_TAG)
                        uiSb.append(LAST_BRACKET)
                        uiSb.append(GARY_TAG)
                        originIndex += range
                        optimizeIndex = endIndex + TAG_LENGTH
                    }
                    MODIFY_TAG -> {
                        val endIndex = optimizeText.indexOf(MODIFY_TAG, optimizeIndex + TAG_LENGTH)
                        val range = endIndex - (optimizeIndex + TAG_LENGTH)
                        val opt = optimizeText.substring(optimizeIndex + TAG_LENGTH, endIndex)
                        uiSb.append(LIGHT_TAG)
                        uiSb.append(opt)
                        uiSb.append(LIGHT_TAG)
                        copySb.append(opt)
                        val bracketStartIndex =
                            originText.substring(0, originIndex).lastIndexOf(WORD_MARK)
                        val originStart = originIndex
                        optimizeIndex = endIndex + TAG_LENGTH
                        originIndex += range

                        var bracketEndIndex = optimizeText.indexOf(WORD_MARK, optimizeIndex)
                        bracketEndIndex = if (bracketEndIndex != -1) {
                            bracketEndIndex
                        } else {
                            originText.length
                        }
                        val startIndex = optimizeIndex
                        for (i in startIndex until bracketEndIndex) {
                            if ((originIndex < originText.length) && originText[originIndex] == optimizeText[i]) {
                                uiSb.append(originText[originIndex])
                                copySb.append(originText[originIndex])
                                originIndex += 1
                                optimizeIndex += 1
                            }
                        }
                        uiSb.append(GARY_TAG)
                        uiSb.append(PRE_BRACKET)
                        uiSb.append(originText.substring(bracketStartIndex + 1, originStart))
                        uiSb.append(UNDER_TAG)
                        uiSb.append(originText.substring(originStart, originStart + range))
                        uiSb.append(UNDER_TAG)
                        uiSb.append(originText.substring(originStart + range, originIndex))
                        uiSb.append(LAST_BRACKET)
                        uiSb.append(GARY_TAG)
                    }
                }
            }
        }
        return Pair(uiSb.toString(), copySb.toString())
    }
}