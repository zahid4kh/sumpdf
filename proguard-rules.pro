-dontwarn kotlinx.serialization.**

# Updated to be more general for sun.misc.Unsafe etc.
-dontwarn sun.misc.**
-dontwarn sun.swing.SwingUtilities2$AATextInfo
-dontwarn net.miginfocom.swing.MigLayout

-dontnote kotlinx.serialization.**
-dontnote META-INF.**
-dontnote kotlinx.serialization.internal.PlatformKt

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep all serializable classes with their @Serializable annotation
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

# Keep specific serializer classes
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep serialization descriptors
-keep class kotlinx.serialization.descriptors.** { *; }

# Specifically keep AppSettings and its serializer
-keep class AppSettings { *; }
-keep class AppSettings$$serializer { *; }


# --- Apache Commons Compress ---
-dontwarn org.objectweb.asm.**
-dontwarn org.brotli.dec.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.tukaani.xz.**

# Apache Commons Compress optional formats
-dontwarn org.apache.commons.compress.archivers.sevenz.**
-dontwarn org.apache.commons.compress.compressors.lzma.**
-dontwarn org.apache.commons.compress.compressors.xz.**
-dontwarn org.apache.commons.compress.harmony.pack200.**
-dontwarn org.apache.commons.compress.harmony.unpack200.**

-keep class org.apache.commons.compress.archivers.zip.** { *; }
-keep class org.apache.commons.compress.archivers.ArchiveEntry { *; }
-keep class org.apache.commons.compress.archivers.ArchiveOutputStream { *; }

# Keep Pack200 internal references
-keep class org.apache.commons.compress.harmony.pack200.Pack200ClassReader {
    byte[] b;
}
-keep class org.apache.commons.compress.harmony.pack200.NewAttribute {
    java.lang.String type;
}
-keep class org.apache.commons.compress.harmony.pack200.Segment$SegmentAnnotationVisitor {
    org.objectweb.asm.AnnotationVisitor av;
}

# Suppress notes about dynamic class loading in Apache Commons
-dontnote org.apache.commons.compress.**
-dontnote org.apache.commons.io.**
-dontnote org.apache.commons.lang3.**

# --- Apache Commons Logging (Transitive dependency from PDFBox) ---
# PDFBox brings in commons-logging, which has many optional dependencies.
-dontwarn org.apache.commons.logging.**
-dontnote org.apache.commons.logging.**
-keep interface org.apache.commons.logging.Log { *; }
-keep class org.apache.commons.logging.LogFactory { *; }

# Optional logging backends/dependencies for commons-logging:
-dontwarn javax.servlet.**
-dontwarn org.apache.avalon.framework.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.log.**
-dontnote org.apache.log4j.**
-dontnote org.apache.log.**

# --- SLF4J (org.slf4j:slf4j-api and org.slf4j:slf4j-simple) ---
-dontwarn org.slf4j.**
-dontnote org.slf4j.**

# Keep essential SLF4J API classes and interfaces.
-keep class org.slf4j.LoggerFactory { *; }
-keep interface org.slf4j.Logger { *; }

# SLF4J 2.x uses java.util.ServiceLoader to find SLF4JServiceProvider.
-keep class org.slf4j.simple.SimpleServiceProvider implements org.slf4j.spi.SLF4JServiceProvider { *; }
-keepnames class * implements org.slf4j.spi.SLF4JServiceProvider
-keep interface org.slf4j.spi.SLF4JServiceProvider { *; }

##############################################################

# --- Apache PDFBox (org.apache.pdfbox:pdfbox) ---
-dontnote org.apache.pdfbox.**
-dontnote org.apache.fontbox.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.pdfbox.io.IOUtils
-dontwarn org.apache.fontbox.**
-dontwarn sun.java2d.cmm.**

# Keep all classes and members within PDFBox and FontBox.
-keep class org.apache.pdfbox.** { *; }
-keep class org.apache.fontbox.** { *; }

# PDFBox uses ServiceLoader for plugins.
-keepnames class * implements org.apache.pdfbox.pdmodel.graphics.image.ImageReader
-keepnames class * implements org.apache.pdfbox.pdmodel.font.FontProvider

# --- Deskit UI Library (com.github.zahid4kh:deskit) ---
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
