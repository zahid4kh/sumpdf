-dontwarn kotlinx.serialization.**

# Sun/Swing warnings
-dontwarn sun.misc.**
-dontwarn sun.swing.SwingUtilities2$AATextInfo
-dontwarn net.miginfocom.swing.MigLayout
-dontwarn sun.java2d.cmm.**

# Suppress notes
-dontnote kotlinx.serialization.**
-dontnote META-INF.**
-dontnote kotlinx.serialization.internal.PlatformKt

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep all serializable classes
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}

# Keep serializers
-keepclasseswithmembers class **$$serializer {
    static **$$serializer INSTANCE;
}

# Keep serializable classes and their properties
-if @kotlinx.serialization.Serializable class **
-keep class <1> {
    static <1>$Companion Companion;
}

# Keep serialization classes
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class kotlinx.serialization.descriptors.** { *; }

# Keep data classes
-keep class combiner.AppSettings { *; }
-keep class combiner.AppSettings$$serializer { *; }
-keep class converter.ConverterSettings { *; }
-keep class converter.ConverterSettings$$serializer { *; }
-keep class converter.ConversionTask { *; }
-keep class converter.ConversionTask$$serializer { *; }

# XML and DOM - Handle duplicate definitions
-dontnote javax.xml.**
-dontnote org.w3c.dom.**
-dontnote org.xml.sax.**
-dontwarn javax.xml.**
-dontwarn org.w3c.dom.**
-dontwarn org.xml.sax.**

# Apache Commons
-dontwarn org.objectweb.asm.**
-dontwarn org.brotli.dec.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.tukaani.xz.**
-dontwarn org.apache.commons.compress.archivers.sevenz.**
-dontwarn org.apache.commons.compress.compressors.**
-dontwarn org.apache.commons.compress.harmony.**
-dontnote org.apache.commons.compress.**
-dontnote org.apache.commons.io.**
-dontnote org.apache.commons.lang3.**

# Apache Commons Logging
-dontwarn org.apache.commons.logging.**
-dontnote org.apache.commons.logging.**
-dontwarn javax.servlet.**
-dontwarn org.apache.avalon.framework.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.log.**
-dontnote org.apache.log4j.**
-dontnote org.apache.log.**

# SLF4J
-dontwarn org.slf4j.**
-dontnote org.slf4j.**
-keep class org.slf4j.LoggerFactory { *; }
-keep interface org.slf4j.Logger { *; }
-keep class org.slf4j.simple.SimpleServiceProvider implements org.slf4j.spi.SLF4JServiceProvider { *; }
-keepnames class * implements org.slf4j.spi.SLF4JServiceProvider
-keep interface org.slf4j.spi.SLF4JServiceProvider { *; }

# Apache PDFBox
-dontnote org.apache.pdfbox.**
-dontnote org.apache.fontbox.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.pdfbox.io.IOUtils
-dontwarn org.apache.fontbox.**
-keep class org.apache.pdfbox.** { *; }
-keep class org.apache.fontbox.** { *; }
-keepnames class * implements org.apache.pdfbox.pdmodel.graphics.image.ImageReader
-keepnames class * implements org.apache.pdfbox.pdmodel.font.FontProvider

# Deskit UI Library
-dontwarn com.github.zahid4kh.deskit.**
-dontnote com.github.zahid4kh.deskit.**
-keep public class com.github.zahid4kh.deskit.** {
    public protected *;
}
-keep public interface com.github.zahid4kh.deskit.** {
    public protected *;
}
-keepclassmembers enum com.github.zahid4kh.deskit.** {
    *;
}

# JodConverter - Keep only what used
-dontwarn org.jodconverter.**
-dontwarn com.sun.star.**
-dontwarn java.lang.ProcessBuilder

# Keep core JodConverter classes
-keep class org.jodconverter.core.** { *; }
-keep class org.jodconverter.local.LocalConverter { *; }
-keep class org.jodconverter.local.LocalConverter$* { *; }
-keep class org.jodconverter.local.office.LocalOfficeManager { *; }
-keep class org.jodconverter.local.office.LocalOfficeManager$* { *; }
-keep class org.jodconverter.local.office.LocalOfficeUtils { *; }
-keep class org.jodconverter.local.office.OfficeConnection { *; }
-keep interface org.jodconverter.local.** { *; }

-dontnote org.jodconverter.local.filter.**

# Keep only the necessary com.sun.star classes
-dontnote com.sun.star.**
-keep interface com.sun.star.** { *; }
-keep class com.sun.star.lib.uno.helper.UnoUrl { *; }
-keep class com.sun.star.comp.** { *; }
-keep class com.sun.star.lib.** { *; }
-keep class com.sun.star.uno.** { *; }

# Apache Batik - Fixed the warnings
-dontwarn org.apache.batik.**
-dontwarn org.mozilla.javascript.**
-dontwarn org.python.**
-dontwarn org.apache.fop.**

# Suppress Batik dynamic loading notes
-dontnote org.apache.batik.**
-dontnote org.w3c.css.sac.helpers.ParserFactory
-dontnote javax.xml.datatype.DatatypeConfigurationException
-dontnote javax.xml.transform.TransformerException

# Keep Batik classes actually used
-keep class org.apache.batik.transcoder.** { *; }
-keep class org.apache.batik.transcoder.image.PNGTranscoder { *; }
-keep class org.apache.batik.bridge.** { *; }
-keep class org.apache.batik.dom.** { *; }
-keep class org.apache.batik.util.** { *; }
-keep class org.apache.batik.apps.** { *; }
-keep class org.apache.batik.ext.** { *; }
-keep class org.apache.batik.gvt.** { *; }
-keep class org.apache.xmlgraphics.** { *; }
-keep interface org.apache.batik.** { *; }

# Keep Batik service implementations
-keepnames class * implements org.apache.batik.ext.awt.image.spi.ImageWriter
-keepnames class * implements org.apache.batik.ext.awt.image.spi.ImageTranscoder
-keepnames class * implements org.apache.batik.gvt.font.FontFamily

# Fix for Batik's BridgeContext methods
-keepclassmembers class org.apache.batik.bridge.BatikWrapFactory {
    void setJavaPrimitiveWrap(boolean);
}
-keepclassmembers class org.apache.batik.bridge.EventTargetWrapper {
    java.lang.Object unwrap();
}
-keepclassmembers class org.apache.batik.bridge.GlobalWrapper {
    void defineFunctionProperties(java.lang.String[], java.lang.Class, int);
}
-keepclassmembers class org.apache.batik.bridge.WindowWrapper {
    void defineFunctionProperties(java.lang.String[], java.lang.Class, int);
    void defineProperty(java.lang.String, java.lang.Class, int);
}

# Don't warn about optional dependencies
-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn com.google.gson.**
-dontnote com.google.gson.**

# PreCompose
-dontwarn moe.tlaster.precompose.**
-keep class moe.tlaster.precompose.** { *; }
-keep interface moe.tlaster.precompose.** { *; }
-keep class * extends moe.tlaster.precompose.viewmodel.ViewModel { *; }
-keepnames class * implements moe.tlaster.precompose.navigation.Route
-dontnote moe.tlaster.precompose.navigation.Route

# Keep ViewModels and other app classes
-keep class combiner.CombinerViewModel { *; }
-keep class converter.ConverterViewModel { *; }
-keep class converter.PDFConverter { *; }
-keep class Database { *; }
-keep class SumPDF { *; }

# Suppress dynamic loading notes
-dontnote org.apache.batik.apps.svgbrowser.JSVGViewerFrame$Debugger
-dontnote org.apache.batik.apps.svgbrowser.Main
-dontnote org.apache.batik.apps.rasterizer.DestinationType
-dontnote org.apache.batik.ext.awt.image.spi.ImageTranscoder
-dontnote org.apache.batik.gvt.font.FontFamily

# Keep reflection access needed by your app
-keepclassmembers class * {
    @com.sun.star.lib.uno.typeinfo.TypeInfo <fields>;
}