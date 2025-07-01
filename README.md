# SumPDF

**SumPDF** is a free and open-source desktop application built with [Compose for Desktop](https://github.com/JetBrains/compose-multiplatform?tab=readme-ov-file#desktop) that allows you to effortlessly  **combine** multiple PDF files and **convert** various document types (SVG, PNG, JPG, JPEG, ODT, DOC, DOCX) to PDF; Enhance your PDF workflow by **extracting** specific pages, **splitting** files by range, **deleting** selected pages, **reordering** pages to your preference, and **merging** the remaining pages into a new document. All features are free and open-source.
## 🆕 What's New in v1.3.0

### 🔥 Advanced PDF Splitting
- **📄 Complete Page Extraction**: Split any PDF into individual pages with real-time preview
- **🎯 Range-Based Splitting**: Extract specific page ranges (e.g., pages 5-15) for focused document creation
- **🎛️ Interactive Page Management**: Reorder, delete, and merge extracted pages with intuitive interface
- **⚡ Real-time UI Updates**: Watch pages appear as interactive cards during the extraction process

### 🎨 Enhanced User Experience  
- **🌟 Dynamic Main Screen**: New staggered grid layout with smooth spring animations
- **🎬 Polished Animations**: Smooth transitions for page operations and navigation
- **📊 Better Visual Feedback**: Clear progress indicators and responsive UI elements
- **🔍 Comprehensive Logging**: Detailed operation tracking for better troubleshooting

[View Full Release Notes →](https://github.com/zahid4kh/sumpdf/releases/tag/1.3.0)

## ✨ Features

### 📄 Combine PDFs

- Merge multiple PDF files into a single document
- Drag & drop support for easy file selection
- Custom output file naming and location
- Preserves original document quality

### 🔄 Convert to PDF

- **Text files** (`.txt`) - Instant conversion with proper formatting
- **Images** (`.png`, `.jpg`, `.jpeg`) - Maintains aspect ratio and quality
- **Vector graphics** (`.svg`) - Rasterization via Apache Batik
- **Documents** (`.doc`, `.docx`, `.odt`) - Professional conversion via LibreOffice

### ✂️ Split PDFs (New!)

- **Complete Splitting**: Extract all pages into individual files
- **Range Extraction**: Extract specific page ranges (e.g., pages 10-25)
- **Interactive Management**: Reorder, delete, and merge extracted pages
- **Real-time Preview**: See extracted pages as they're processed

### 🎨 User Experience

- **Modern UI** with dark/light mode support
- **Real-time progress tracking** with file-by-file feedback
- **Drag & drop interface** for intuitive file management
- **Recent folders** for quick access to output locations
- **Batch processing** - convert multiple files simultaneously
- **Smooth animations** and responsive interactions

### Document Conversion (doc/docx/odt)

- **LibreOffice** must be installed separately
  - **Windows**: Download from [LibreOffice.org](https://www.libreoffice.org/download/download/)
  - **Linux**: `sudo apt install libreoffice` (Ubuntu/Debian) or equivalent
  - **macOS**: Install via Homebrew `brew install --cask libreoffice`

## ⚡ Performance Expectations

| File Type                           | Conversion Speed        | Notes                        |
| ----------------------------------- | ----------------------- | ---------------------------- |
| Text (`.txt`)                       | Instant                 | < 1 second                   |
| Images (`.png`, `.jpg`, `.jpeg`)    | Instant                 | < 1 second                   |
| Vector (`.svg`)                     | Almost instant          | ~ 1 second                   |
| Documents (`.doc`, `.docx`, `.odt`) | **~9 seconds per file** | Requires LibreOffice startup |
| PDF Splitting                       | **~150ms per page**     | Real-time preview available  |

> **Note**: Document conversions are slower due to LibreOffice process management. The first conversion may take longer as LibreOffice initializes.

## 🖥️ Screenshots

### Home Screen

![Home Screen](screenshots/home.png)

### Combine PDFs

![Combine PDFs](screenshots/combine.png)

### Convert to PDF

![Convert to PDF](screenshots/convert.png)

## 🚀 Installation

### Linux Installation (Recommended)

**Easy installation via APT repository:**

📦 **[Install from my APT Repository](https://github.com/zahid4kh/my-apt-repo)**

This is the recommended method for Ubuntu/Debian users as it provides automatic updates and dependency management.

### Download Releases

1. Go to [Releases](https://github.com/zahid4kh/sumpdf/releases)
2. Download the appropriate installer:

- **Windows**: `.exe` or `.msi` installer
- **Linux**: `.deb` package (manual installation)

### Manual Linux Installation

```bash
sudo dpkg -i sumpdf_1.3.1-1_amd64-wm.deb

sudo apt install -f
```

### Windows Installation

- Run the `.exe` installer and follow the setup wizard
- Administrator privileges may be required

## 🔧 Build from Source

### Prerequisites

- **JDK 17+**
- **Gradle 8.0+**

### Build Steps

```bash
# Clone the repository
git clone https://github.com/zahid4kh/sumpdf.git
cd sumpdf

# Run the application
./gradlew :run

# Or run with hot-reload for development
./gradlew :runHot --mainClass SumPDF --auto

# Create distribution packages
./gradlew packageDeb          # Linux .deb
./gradlew packageMsi          # Windows .msi
./gradlew packageExe          # Windows .exe
./gradlew packageDmg          # macOS .dmg
```

## 🛠️ Technology Stack

- **UI Framework**: [Compose for Desktop](https://github.com/JetBrains/compose-multiplatform?tab=readme-ov-file#desktop)
- **Language**: Kotlin
- **PDF Processing**: Apache PDFBox 3.0.3
- **Document Conversion**: JodConverter + LibreOffice
- **SVG Processing**: Apache Batik
- **Dependency Injection**: Koin
- **Serialization**: Kotlinx Serialization

## 🐛 Known Issues & Limitations

- **LibreOffice Required**: Document conversion (`.doc`, `.docx`, `.odt`) requires LibreOffice installation
- **Conversion Speed**: Document conversion is slower (~9 seconds per file) due to LibreOffice overhead

## 🤝 Contributing

Contributions are welcome! Please feel free to submit issues, feature requests, or pull requests.

### Development Setup

1. Install LibreOffice for testing document conversion
2. Use `./gradlew :runHot` for hot-reload development
3. Test on multiple platforms before submitting PRs

## 📝 License

This project is licensed under the **Apache 2.0 License**. See [LICENSE](LICENSE) for more details.

---

**Made with ❤️ using Kotlin and Compose for Desktop**
