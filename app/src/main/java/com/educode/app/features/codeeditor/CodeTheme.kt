package com.educode.app.features.codeeditor

import androidx.compose.ui.graphics.Color

enum class CodeThemeType(val displayName: String) {
    VS_CODE_DARK("VS Code Dark"),
    VS_CODE_LIGHT("VS Code Light"),
    CYBERPUNK_PREMIUM("Cyberpunk Premium"),
    NEON_GLOW("Neon Glow")
}

data class EditorThemeColors(
    val background: Color,
    val gutterBackground: Color,
    val gutterText: Color,
    val text: Color,
    val keyword: Color,
    val comment: Color,
    val stringColor: Color,
    val number: Color,
    val type: Color,
    val builtin: Color,
    val cursor: Color,
    val matchedBracket: Color,
    val error: Color,
    val searchHighlight: Color
) {
    companion object {
        val VS_CODE_DARK = EditorThemeColors(
            background = Color(0xFF1E1E1E),
            gutterBackground = Color(0xFF1E1E1E),
            gutterText = Color(0xFF858585),
            text = Color(0xFFD4D4D4),
            keyword = Color(0xFF569CD6),
            comment = Color(0xFF6A9955),
            stringColor = Color(0xFFCE9178),
            number = Color(0xFFB5CEA8),
            type = Color(0xFF4EC9B0),
            builtin = Color(0xFFDCDCAA),
            cursor = Color(0xFFAEAFAD),
            matchedBracket = Color(0xFF3B5B75),
            error = Color(0xFFF44336),
            searchHighlight = Color(0xFF614E1A)
        )

        val VS_CODE_LIGHT = EditorThemeColors(
            background = Color(0xFFFFFFFF),
            gutterBackground = Color(0xFFF3F3F3),
            gutterText = Color(0xFF237893),
            text = Color(0xFF000000),
            keyword = Color(0xFF0000FF),
            comment = Color(0xFF008000),
            stringColor = Color(0xFFA31515),
            number = Color(0xFF098658),
            type = Color(0xFF267F99),
            builtin = Color(0xFF795E26),
            cursor = Color(0xFF000000),
            matchedBracket = Color(0xFFE8E8E8),
            error = Color(0xFFFF1744),
            searchHighlight = Color(0xFFFFEB3B)
        )

        val CYBERPUNK_PREMIUM = EditorThemeColors(
            background = Color(0xFF0F121E),
            gutterBackground = Color(0xFF1A1F30),
            gutterText = Color(0xFF6B21A8),
            text = Color(0xFFFFFFFF),
            keyword = Color(0xFFFF007F), // Vivid Pink
            comment = Color(0xFF6D7993), // Cyber Slate Gray
            stringColor = Color(0xFF00E5FF), // Cyber Cyan
            number = Color(0xFF39FF14), // Radioactive Green
            type = Color(0xFF9D4EDD), // Hologram Purple
            builtin = Color(0xFFFFD500), // Laser Yellow
            cursor = Color(0xFF00E5FF),
            matchedBracket = Color(0xFF3A0CA3),
            error = Color(0xFFFF1744),
            searchHighlight = Color(0xFF5A189A)
        )

        val NEON_GLOW = EditorThemeColors(
            background = Color(0xFF000000),
            gutterBackground = Color(0xFF0D0D0D),
            gutterText = Color(0xFF39FF14), // Radioactive Green
            text = Color(0xFFE5E5E5),
            keyword = Color(0xFF00FFCC), // Electric Mint
            comment = Color(0xFF444444), // Dim gray
            stringColor = Color(0xFFFF3366), // Neon Red/Pink
            number = Color(0xFFFFCC00), // Sunshine gold
            type = Color(0xFF9400D3), // Dark Violet
            builtin = Color(0xFF33CCFF), // Electric Sky Blue
            cursor = Color(0xFF39FF14),
            matchedBracket = Color(0xFF222222),
            error = Color(0xFFFF073A),
            searchHighlight = Color(0xFF39FF14)
        )

        fun forType(type: CodeThemeType): EditorThemeColors {
            return when (type) {
                CodeThemeType.VS_CODE_DARK -> VS_CODE_DARK
                CodeThemeType.VS_CODE_LIGHT -> VS_CODE_LIGHT
                CodeThemeType.CYBERPUNK_PREMIUM -> CYBERPUNK_PREMIUM
                CodeThemeType.NEON_GLOW -> NEON_GLOW
            }
        }
    }
}
