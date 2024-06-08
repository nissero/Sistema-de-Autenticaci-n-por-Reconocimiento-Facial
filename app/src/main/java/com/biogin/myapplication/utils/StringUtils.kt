package com.biogin.myapplication.utils

class StringUtils {

    fun normalizeAndSentenceCase(string: String): String {
        return normalize(sentenceCase(string))
    }
    private fun normalize(string: String): String {
        var normalizedString = ""

        for(c in string) {
            normalizedString += when(c) {
                'á' -> 'a'
                'é' -> 'e'
                'í' -> 'i'
                'ó' -> 'o'
                'ú' -> 'u'
                'Á' -> 'A'
                'É' -> 'E'
                'Í' -> 'I'
                'Ó' -> 'O'
                'Ú' -> 'I'
                else -> c
            }
        }

        return normalizedString
    }

    private fun sentenceCase(string: String): String {
        return string.lowercase().replaceFirstChar(Char::titlecase)
    }
}