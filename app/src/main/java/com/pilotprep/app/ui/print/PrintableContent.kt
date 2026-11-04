package com.pilotprep.app.ui.print

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity

data class PrintablePayload(
    val html: String?,
    val jobName: String
)

data class PrintOptions(
    val a4Landscape: Boolean = false,
    val marginMm: Int = 18,
    val themeDark: Boolean = false,
    val headerTitle: String? = null,
    val headerLogoUrl: String? = null,
    val headerColorHex: String? = null,
    val headerBorderPx: Int = 1,
    val footerText: String? = null
)

typealias PrintableProvider = () -> PrintablePayload?

object PrintableContentRegistry {
    @Volatile
    private var provider: PrintableProvider? = null

    fun setProvider(newProvider: PrintableProvider?) {
        provider = newProvider
    }

    fun getPayload(): PrintablePayload? = provider?.invoke()
}

private fun mmToMils(mm: Int): Int = (mm * 39.37).toInt()

private fun injectStylesAndHeader(originalHtml: String, options: PrintOptions): String {
    val style = buildString {
        append("<style>")
        append("@page{margin:")
        append(options.marginMm).append("mm;")
        append("}")
        append("body{color:")
        append(if (options.themeDark) "#E0E0E0" else "#111111")
        append(";background:")
        append(if (options.themeDark) "#000000" else "#FFFFFF")
        append(";}")
        val headerColor = options.headerColorHex ?: (if (options.themeDark) "#333333" else "#dddddd")
        val borderPx = options.headerBorderPx.coerceIn(0, 8)
        append("header.print-header{display:block;border-bottom:")
        append(borderPx).append("px solid ")
        append(headerColor)
        append(";padding:6pt 0;margin:0 0 10pt 0;font-size:10pt;color:")
        append(if (options.themeDark) "#aaaaaa" else "#666666")
        append("}")
        append(".print-header img{height:16pt;vertical-align:middle;margin-right:6pt}")
        append(".print-header .title{vertical-align:middle;font-weight:bold}")
        append("</style>")
    }

    val withHead = if (originalHtml.contains("</head>", ignoreCase = true)) {
        originalHtml.replace(Regex("</head>", RegexOption.IGNORE_CASE), "$style</head>")
    } else {
        "<html><head>$style</head><body>" + originalHtml + "</body></html>"
    }

    val headerHtml = if (options.headerTitle != null || options.headerLogoUrl != null) {
        buildString {
            append("<header class=\"print-header\">")
            options.headerLogoUrl?.let { append("<img src=\"").append(it).append("\" alt=\"logo\"/>") }
            options.headerTitle?.let { append("<span class=\"title\">").append(it).append("</span>") }
            append("</header>")
        }
    } else ""

    val bodyTagRegex = Regex("<body(.*?)>", RegexOption.IGNORE_CASE)
    val bodyMatch = bodyTagRegex.find(withHead)
    var result = if (bodyMatch != null) {
        val bodyTag = bodyMatch.value
        val themedBodyTag = if (withHead.contains("class=", ignoreCase = true)) bodyTag else bodyTag.replace(">", " class=\"" + (if (options.themeDark) "dark" else "light") + "\">")
        withHead.replaceFirst(bodyTagRegex, themedBodyTag)
            .replaceFirst(Regex("<body[^>]*>", RegexOption.IGNORE_CASE), bodyMatch.value + headerHtml)
    } else {
        headerHtml + withHead
    }

    // Injecte un footer si demandé
    options.footerText?.let { footer ->
        val footerHtml = "<footer style='font-size:10pt; color:" + (if (options.themeDark) "#aaaaaa" else "#666666") + "; margin-top:12pt;'>" + footer + "</footer>"
        result = result.replace(Regex("</body>", RegexOption.IGNORE_CASE), footerHtml + "</body>")
    }
    return result
}

fun printHtml(activity: ComponentActivity, html: String, jobName: String, options: PrintOptions = PrintOptions()) {
    val finalHtml = injectStylesAndHeader(html, options)
    val webView = WebView(activity)
    webView.settings.javaScriptEnabled = false
    webView.loadDataWithBaseURL(
        null,
        finalHtml,
        "text/html",
        "UTF-8",
        null
    )
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = activity.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val adapter = webView.createPrintDocumentAdapter(jobName)
            val media = if (options.a4Landscape) PrintAttributes.MediaSize.ISO_A4.asLandscape() else PrintAttributes.MediaSize.ISO_A4
            val attributes = PrintAttributes.Builder()
                .setMediaSize(media)
                .setResolution(PrintAttributes.Resolution("pdf", "pdf", 300, 300))
                .setMinMargins(PrintAttributes.Margins(mmToMils(options.marginMm), mmToMils(options.marginMm), mmToMils(options.marginMm), mmToMils(options.marginMm)))
                .build()
            printManager.print(jobName, adapter, attributes)
        }
    }
}


