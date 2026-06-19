package com.educode.app.features.codeeditor

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.regex.Pattern

class CodeSyntaxHighlighter(
    private val language: ProgrammingLanguage,
    private val themeColors: EditorThemeColors
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val annotatedString = highlight(originalText)
        return TransformedText(annotatedString, OffsetMapping.Identity)
    }

    private fun highlight(code: String): AnnotatedString {
        val builder = AnnotatedString.Builder(code)
        
        // Let's set default code text color
        builder.addStyle(SpanStyle(color = themeColors.text, fontFamily = FontFamily.Monospace), 0, code.length)
        
        if (code.isEmpty()) return builder.toAnnotatedString()

        try {
            // Retrieve patterns for specific language
            val (keywords, types, builtins, commentRegex, stringRegex, numberRegex) = getPatternsForLanguage(language)

            // Compile regexes
            val keywordPattern = if (keywords.isNotEmpty()) {
                Pattern.compile("\\b(${keywords.joinToString("|")})\\b")
            } else null

            val typePattern = if (types.isNotEmpty()) {
                Pattern.compile("\\b(${types.joinToString("|")})\\b")
            } else null

            val builtinPattern = if (builtins.isNotEmpty()) {
                Pattern.compile("\\b(${builtins.joinToString("|")})\\b")
            } else null

            // Highlight Numbers first (overwritten by keywords if appropriate)
            val numPat = Pattern.compile(numberRegex)
            val numMatcher = numPat.matcher(code)
            while (numMatcher.find()) {
                builder.addStyle(
                    SpanStyle(color = themeColors.number),
                    numMatcher.start(),
                    numMatcher.end()
                )
            }

            // Highlight Types
            if (typePattern != null) {
                val matcher = typePattern.matcher(code)
                while (matcher.find()) {
                    builder.addStyle(
                        SpanStyle(color = themeColors.type, fontWeight = FontWeight.Bold),
                        matcher.start(),
                        matcher.end()
                    )
                }
            }

            // Highlight Builtins
            if (builtinPattern != null) {
                val matcher = builtinPattern.matcher(code)
                while (matcher.find()) {
                    builder.addStyle(
                        SpanStyle(color = themeColors.builtin),
                        matcher.start(),
                        matcher.end()
                    )
                }
            }

            // Highlight Keywords
            if (keywordPattern != null) {
                val matcher = keywordPattern.matcher(code)
                while (matcher.find()) {
                    builder.addStyle(
                        SpanStyle(color = themeColors.keyword, fontWeight = FontWeight.Bold),
                        matcher.start(),
                        matcher.end()
                    )
                }
            }

            // Highlight Strings (Should stamp over keyword/builtins)
            val strPat = Pattern.compile(stringRegex)
            val strMatcher = strPat.matcher(code)
            while (strMatcher.find()) {
                builder.addStyle(
                    SpanStyle(color = themeColors.stringColor),
                    strMatcher.start(),
                    strMatcher.end()
                )
            }

            // Highlight Comments (Stamped over everything else)
            val commentPat = Pattern.compile(commentRegex, Pattern.MULTILINE)
            val commentMatcher = commentPat.matcher(code)
            while (commentMatcher.find()) {
                builder.addStyle(
                    SpanStyle(color = themeColors.comment),
                    commentMatcher.start(),
                    commentMatcher.end()
                )
            }

        } catch (e: Exception) {
            // Fallback graceful handling
        }

        return builder.toAnnotatedString()
    }

    private data class LangPatterns(
        val keywords: List<String>,
        val types: List<String>,
        val builtins: List<String>,
        val commentRegex: String,
        val stringRegex: String,
        val numberRegex: String
    )

    private fun getPatternsForLanguage(lang: ProgrammingLanguage): LangPatterns {
        val numberRegex = "\\b\\d+(\\.\\d+)?\\b"
        val stdStringRegex = "\"(\\\\.|[^\"])*\"|'(\\\\.|[^'])*'"

        return when (lang) {
            ProgrammingLanguage.HTML -> LangPatterns(
                keywords = listOf("html", "head", "body", "title", "meta", "link", "script", "style", "div", "p", "span", "ul", "li", "h1", "h2", "h3", "h4", "h5", "h6", "table", "tr", "td", "th", "a", "img", "button", "input"),
                types = listOf("class", "id", "href", "src", "type", "rel", "charset", "lang", "style", "onclick"),
                builtins = listOf("doctype", "DOCTYPE"),
                commentRegex = "<!--.*?-->",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
            ProgrammingLanguage.CSS -> LangPatterns(
                keywords = listOf("@media", "@import", "@keyframes", "body", "p", "div", "span", "a", "h1", "h2", "h3"),
                types = listOf("color", "background-color", "font-size", "margin", "padding", "border", "display", "position", "width", "height", "box-shadow", "transition", "transform", "justify-content", "align-items"),
                builtins = listOf("px", "em", "rem", "vh", "vw", "rgba", "rgb", "url", "sans-serif", "linear"),
                commentRegex = "/\\*.*?\\*/",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
            ProgrammingLanguage.JAVASCRIPT -> LangPatterns(
                keywords = listOf("break", "case", "catch", "class", "const", "continue", "debugger", "default", "delete", "do", "else", "export", "extends", "finally", "for", "function", "if", "import", "in", "instanceof", "new", "return", "super", "switch", "this", "throw", "try", "typeof", "var", "void", "while", "with", "yield", "let"),
                types = listOf("true", "false", "null", "undefined", "NaN", "Infinity", "prototype", "window", "document"),
                builtins = listOf("console", "log", "error", "warn", "alert", "prompt", "parseInt", "parseFloat", "Math", "sqrt", "push", "join", "split", "map", "filter"),
                commentRegex = "//.*|/\\*.*?\\*/",
                stringRegex = "`.*?`|$stdStringRegex",
                numberRegex = numberRegex
            )
            ProgrammingLanguage.PYTHON -> LangPatterns(
                keywords = listOf("False", "None", "True", "and", "as", "assert", "async", "await", "break", "class", "continue", "def", "del", "elif", "else", "except", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"),
                types = listOf("int", "float", "str", "bool", "list", "dict", "tuple", "set", "object"),
                builtins = listOf("print", "len", "range", "input", "str", "sum", "min", "max", "open", "abs", "round", "type"),
                commentRegex = "#.*",
                stringRegex = "\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|$stdStringRegex",
                numberRegex = numberRegex
            )
            ProgrammingLanguage.C -> LangPatterns(
                keywords = listOf("auto", "break", "case", "const", "continue", "default", "do", "else", "enum", "extern", "for", "goto", "if", "register", "return", "sizeof", "static", "struct", "switch", "typedef", "union", "volatile", "while"),
                types = listOf("char", "double", "float", "int", "long", "short", "signed", "unsigned", "void", "bool"),
                builtins = listOf("printf", "scanf", "malloc", "free", "exit", "strlen", "fopen", "fclose", "NULL", "define", "include"),
                commentRegex = "//.*|/\\*.*?\\*/",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
            ProgrammingLanguage.CPP -> LangPatterns(
                keywords = listOf("alignas", "alignof", "and", "and_eq", "asm", "atomic_cancel", "atomic_commit", "atomic_noexcept", "auto", "bitand", "bitor", "break", "case", "catch", "class", "compl", "concept", "const", "consteval", "constexpr", "constinit", "const_cast", "continue", "co_await", "co_return", "co_yield", "decltype", "default", "delete", "do", "dynamic_cast", "else", "enum", "explicit", "export", "extern", "for", "friend", "goto", "if", "inline", "mutable", "namespace", "new", "noexcept", "not", "not_eq", "nullptr", "operator", "or", "or_eq", "private", "protected", "public", "reflexpr", "register", "reinterpret_cast", "requires", "return", "sizeof", "static", "static_assert", "static_cast", "struct", "switch", "template", "this", "thread_local", "throw", "try", "typedef", "typeid", "typename", "union", "using", "virtual", "volatile", "while", "xor", "xor_eq"),
                types = listOf("char", "char8_t", "char16_t", "char32_t", "double", "float", "int", "long", "short", "signed", "unsigned", "void", "bool", "wchar_t", "vector", "string", "map", "set", "pair"),
                builtins = listOf("cout", "cin", "cerr", "clog", "endl", "std", "printf", "scanf", "include", "define"),
                commentRegex = "//.*|/\\*.*?\\*/",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
            ProgrammingLanguage.CSHARP -> LangPatterns(
                keywords = listOf("abstract", "as", "base", "break", "case", "catch", "checked", "class", "const", "continue", "default", "delegate", "do", "else", "enum", "event", "explicit", "extern", "finally", "fixed", "for", "foreach", "goto", "if", "implicit", "in", "interface", "internal", "is", "lock", "namespace", "new", "null", "operator", "out", "override", "params", "private", "protected", "public", "readonly", "ref", "return", "sealed", "sizeof", "stackalloc", "static", "struct", "switch", "this", "throw", "try", "typeof", "unchecked", "unsafe", "using", "virtual", "void", "volatile", "while", "get", "set", "var"),
                types = listOf("bool", "byte", "char", "decimal", "double", "float", "int", "long", "object", "sbyte", "short", "string", "uint", "ulong", "ushort"),
                builtins = listOf("Console", "WriteLine", "ReadLine", "Write", "Convert", "Math"),
                commentRegex = "//.*|/\\*.*?\\*/",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
            ProgrammingLanguage.JAVA -> LangPatterns(
                keywords = listOf("abstract", "assert", "break", "case", "catch", "class", "const", "continue", "default", "do", "else", "enum", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "instanceof", "interface", "native", "new", "package", "private", "protected", "public", "return", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "volatile", "while", "var"),
                types = listOf("boolean", "byte", "char", "double", "float", "int", "long", "short", "void", "String", "ArrayList", "HashMap", "Integer", "Double", "Boolean", "List"),
                builtins = listOf("System", "out", "println", "print", "err", "Math", "Arrays", "Collections", "Main"),
                commentRegex = "//.*|/\\*.*?\\*/",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
            ProgrammingLanguage.PHP -> LangPatterns(
                keywords = listOf("abstract", "and", "array", "as", "break", "callable", "case", "catch", "class", "clone", "const", "continue", "declare", "default", "die", "do", "echo", "else", "elseif", "empty", "enddeclare", "endfor", "endforeach", "endif", "endswitch", "endwhile", "eval", "exit", "extends", "final", "finally", "fn", "for", "foreach", "function", "global", "goto", "if", "implements", "include", "include_once", "instanceof", "insteadof", "interface", "isset", "list", "match", "namespace", "new", "or", "print", "private", "protected", "public", "require", "require_once", "return", "static", "switch", "throw", "trait", "try", "unset", "use", "var", "while", "xor", "yield"),
                types = listOf("int", "float", "string", "bool", "array", "object", "callable", "iterable"),
                builtins = listOf("phpinfo", "count", "define", "isset", "empty", "__construct", "this"),
                commentRegex = "//.*|#.*|/\\*.*?\\*/",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
            ProgrammingLanguage.RUST -> LangPatterns(
                keywords = listOf("as", "async", "await", "break", "const", "continue", "crate", "dyn", "else", "enum", "extern", "false", "fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub", "ref", "return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "union", "unsafe", "use", "where", "while"),
                types = listOf("i8", "i16", "i32", "i64", "i128", "isize", "u8", "u16", "u32", "u64", "u128", "usize", "f32", "f64", "bool", "char", "str", "String", "Option", "Result", "Vec"),
                builtins = listOf("println!", "print!", "format!", "panic!", "vec!", "unwrap", "expect", "as_ref", "main"),
                commentRegex = "//.*|/\\*.*?\\*/",
                stringRegex = stdStringRegex,
                numberRegex = numberRegex
            )
        }
    }
}
