package io.softwarebyandroid.tool.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.softwarebyandroid.tool.R

@Composable fun EmptyContent(symbol: Symbol, title: Int, detail: Int, modifier: Modifier = Modifier) {
    Column(modifier.padding(Space.section), horizontalAlignment=Alignment.CenterHorizontally, verticalArrangement=Arrangement.Center) {
        Surface(color=MaterialTheme.colorScheme.primaryContainer, shape=MaterialTheme.shapes.large) {
            ToolIcon(symbol, modifier=Modifier.padding(Space.section).size(40.dp),tint=MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Spacer(Modifier.height(Space.section))
        Text(stringResource(title),style=MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(Space.sm))
        Text(stringResource(detail),style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
@Composable fun FileHeading(name: String, detail: String, symbol: Symbol) {
    var show by remember { mutableStateOf(false) }
    Surface(onClick={ show=true }, color=MaterialTheme.colorScheme.surfaceContainerLow,shape=MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(Space.page),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(Space.md)) {
            ToolIcon(symbol,modifier=Modifier.size(28.dp),tint=MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(name,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(detail,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ToolIcon(Symbol.Info,stringResource(R.string.details))
        }
    }
    if(show) AlertDialog(onDismissRequest={show=false},title={Text(stringResource(R.string.details))},
        text={Text("$name\n$detail")},confirmButton={TextButton(onClick={show=false}){Text(stringResource(R.string.close))}})
}
@Composable fun ErrorNotice(error: Int?) {
    if(error!=null) Surface(color=MaterialTheme.colorScheme.errorContainer,shape=MaterialTheme.shapes.small) {
        Text(stringResource(error),modifier=Modifier.fillMaxWidth().padding(Space.md),color=MaterialTheme.colorScheme.onErrorContainer,style=MaterialTheme.typography.bodyMedium)
    }
}
@Composable fun BusyNotice(busy: Boolean, label: Int = R.string.loading) {
    if(busy) Row(Modifier.fillMaxWidth().padding(Space.sm),horizontalArrangement=Arrangement.spacedBy(Space.md),verticalAlignment=Alignment.CenterVertically) {
        CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp)
        Text(stringResource(label),style=MaterialTheme.typography.bodyMedium)
    }
}
fun DrawScope.checkerboard() {
    val cell=12.dp.toPx()
    drawRect(Color(0xFFF4F4F4))
    for(y in 0..(size.height/cell).toInt()) for(x in 0..(size.width/cell).toInt()) {
        if((x+y)%2==0) drawRect(Color(0xFFE5E5E5),Offset(x*cell,y*cell),Size(cell,cell))
    }
}
