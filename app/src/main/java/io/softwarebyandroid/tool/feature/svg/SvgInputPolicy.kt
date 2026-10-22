package io.softwarebyandroid.tool.feature.svg

import android.util.Xml
import io.softwarebyandroid.tool.R
import io.softwarebyandroid.tool.core.data.ToolFailure
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.xmlpull.v1.XmlPullParser
import java.io.StringReader

/** Validate before AndroidSVG can decode any embedded bitmap. No regex-based element matching. */
internal suspend fun validateSvgInput(source: String) {
    val parser=Xml.newPullParser()
    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,true)
    parser.setInput(StringReader(source))
    try {
        while(true) {
            currentCoroutineContext().ensureActive()
            when(parser.nextToken()) {
                XmlPullParser.END_DOCUMENT -> return
                XmlPullParser.DOCDECL -> throw ToolFailure(R.string.svg_entities)
                XmlPullParser.START_TAG -> if(parser.name=="image") throw ToolFailure(R.string.svg_embedded_image)
            }
        }
    } catch(e: org.xmlpull.v1.XmlPullParserException) { throw ToolFailure(R.string.svg_invalid) }
}
