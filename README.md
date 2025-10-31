# 🧹 Duplicate File Remover

> 🇪🇸 **Versión en español abajo**

---

<p align="center">
  <img src="https://img.shields.io/badge/Java-21+-orange?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaFX-21-blue?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Maven-3.9+-blue?logo=apachemaven&logoColor=white" />
  <img src="https://img.shields.io/badge/Status-In%20Development-yellow" />
  <img src="https://img.shields.io/badge/License-GPLv3-blue" />
</p>

---

## 🇬🇧 English

### 🧠 Overview

**Duplicate File Remover** is a desktop application built with **JavaFX** that scans directories to detect and remove duplicate files based on their **content hash**.  
It provides a **dark and modern UI**, fast performance with **parallel processing**, comprehensive logging, and multi-language support for a clean user experience in data organization.

---

### ✨ Features

- 🗂️ **Folder selection** via intuitive file chooser
- ⚡ **Fast duplicate detection** using efficient file hashing algorithms (SHA-256)
- 🧮 **Parallelized file scanning** for optimal performance
- 🧹 **Safe file operations** with user confirmation and dry-run mode
- 🌙 **Dark, modern interface** built with JavaFX and custom CSS
- 🌍 **Multi-language support** (Spanish and English)
- 📝 **Comprehensive logging** with SLF4J and Logback
- 🔄 **Progress tracking** with cancellation support
- 📊 **Detailed results table** with sortable columns
- 🚀 **Maven-based build** for easy compilation and packaging

---

### 🧩 Technical Overview (Mermaid Diagram)

```mermaid
flowchart TD
    A[User selects folder] --> B[Scan files recursively]
    B --> C[Compute hash of each file]
    C --> D{Hash already exists?}
    D -->|No| E[Store hash in memory]
    D -->|Yes| F[Mark as duplicate]
    F --> G[Show duplicates in UI]
    G --> H{User confirms deletion?}
    H -->|Yes| I[Delete duplicates safely]
    H -->|No| J[Keep all files]
    I --> K[Show summary and finish]
```

---

### 📋 Requirements

- **Java 21** or higher
- **Maven 3.9+** (for building)
- **Operating System**: Windows, macOS, or Linux with JavaFX support

---

### 🚀 Installation & Usage

#### Option 1: Using Maven (Recommended)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/JesusLuna2309/Duplicate_File_Remover.git
   cd Duplicate_File_Remover
   ```

2. **Build the project:**
   ```bash
   mvn clean package
   ```

3. **Run the application:**
   ```bash
   mvn javafx:run
   ```

#### Option 2: Running the JAR

After building with Maven, you can run the generated JAR:
```bash
java -jar target/duplicate-file-remover-1.0.0-SNAPSHOT.jar
```

#### Usage Steps

1. **Launch** the application
2. **Select a folder** using the "Elegir carpeta" button
3. **Choose options** (include subfolders if needed)
4. **Start scanning** with the "Empezar limpieza" button
5. **Review duplicates** in the results table
6. **Take action** on duplicate files (delete, move to trash, etc.)

---

### 🛠️ Development

#### Building from Source

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Package as JAR
mvn clean package

# Run directly with JavaFX plugin
mvn javafx:run
```

#### Project Structure

```
Duplicate_File_Remover/
│
├── src/
│   ├── main/
│   │   ├── java/com/jesusluna/duplicateremover/
│   │   │   ├── MainApp.java              # Main application entry point
│   │   │   └── module-info.java          # Java module descriptor
│   │   └── resources/
│   │       ├── icons/                    # Application icons
│   │       ├── styles/                   # CSS stylesheets
│   │       ├── i18n/                     # Internationalization bundles
│   │       │   ├── messages.properties
│   │       │   ├── messages_es.properties
│   │       │   └── messages_en.properties
│   │       └── logback.xml               # Logging configuration
│   └── test/
│       ├── java/                         # Unit tests
│       └── resources/                    # Test resources
├── .github/
│   └── workflows/
│       └── build.yml                     # CI/CD workflow
├── pom.xml                               # Maven project configuration
├── .gitignore                            # Git ignore rules
├── .gitattributes                        # Git attributes for line endings
├── .editorconfig                         # Editor configuration
├── LICENSE                               # GPL v3 license
└── README.md                             # This file
```

---

### 🔒 Security Considerations

- **File deletion** is a destructive operation. Always review duplicates before deletion.
- **Dry-run mode** allows you to preview actions without making changes.
- **Logging** tracks all operations for audit purposes.
- **Confirmation dialogs** prevent accidental data loss.

---

### 🗺️ Roadmap

- [x] Maven-based build system
- [x] Multi-language support (ES/EN)
- [x] Comprehensive logging
- [x] CI/CD with GitHub Actions
- [ ] Complete duplicate detection engine with hash comparison
- [ ] Results table with sortable columns
- [ ] File preview for images
- [ ] Export results to CSV/JSON
- [ ] Move to trash instead of permanent deletion
- [ ] Progress bar with cancellation support
- [ ] Hash caching for faster re-scans
- [ ] Advanced filtering options

---

### 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please ensure:
- Code follows the project's style guidelines (.editorconfig)
- Tests pass (`mvn test`)
- Documentation is updated as needed

---

### 📄 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

---

### 👨‍💻 Author

Developed by **Jesús Luna Romero**  
📧 [jesuslunaromero230902@gmail.com](mailto:jesuslunaromero230902@gmail.com)

---

## 🇪🇸 Español

### 🧠 Descripción general

**Duplicate File Remover** es una aplicación de escritorio creada con **JavaFX** que permite analizar carpetas para detectar y eliminar archivos duplicados según su **hash de contenido**.  
Ofrece una **interfaz moderna y oscura**, un rendimiento rápido mediante **procesamiento paralelo**, y una experiencia limpia para mantener tu sistema ordenado.

---

### ✨ Características

- 🗂️ **Selección de carpetas** mediante un explorador intuitivo
- ⚡ **Detección rápida de duplicados** usando algoritmos eficientes de hash (SHA-256)
- 🧮 **Escaneo de archivos en paralelo** para un rendimiento óptimo
- 🧹 **Operaciones seguras** con confirmación del usuario y modo simulación
- 🌙 **Interfaz moderna y oscura** construida con JavaFX y CSS personalizado
- 🌍 **Soporte multiidioma** (español e inglés)
- 📝 **Registro completo** con SLF4J y Logback
- 🔄 **Seguimiento de progreso** con soporte de cancelación
- 📊 **Tabla de resultados detallada** con columnas ordenables
- 🚀 **Construcción basada en Maven** para fácil compilación y empaquetado

---

### 🧩 Vista técnica (Diagrama Mermaid)

```mermaid
flowchart TD
    A[Usuario selecciona carpeta] --> B[Escaneo recursivo de archivos]
    B --> C[Cálculo del hash de cada archivo]
    C --> D{¿Hash ya existente?}
    D -->|No| E[Guardar hash en memoria]
    D -->|Sí| F[Marcar como duplicado]
    F --> G[Mostrar duplicados en la interfaz]
    G --> H{¿Eliminar duplicados?}
    H -->|Sí| I[Eliminar de forma segura]
    H -->|No| J[Conservar todos]
    I --> K[Mostrar resumen final]
```

---

### 📋 Requisitos

- **Java 21** o superior
- **Maven 3.9+** (para compilar)
- **Sistema Operativo**: Windows, macOS o Linux con soporte para JavaFX

---

### 🚀 Instalación y Uso

#### Opción 1: Usando Maven (Recomendado)

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/JesusLuna2309/Duplicate_File_Remover.git
   cd Duplicate_File_Remover
   ```

2. **Compilar el proyecto:**
   ```bash
   mvn clean package
   ```

3. **Ejecutar la aplicación:**
   ```bash
   mvn javafx:run
   ```

#### Opción 2: Ejecutar el JAR

Después de compilar con Maven, puedes ejecutar el JAR generado:
```bash
java -jar target/duplicate-file-remover-1.0.0-SNAPSHOT.jar
```

#### Pasos de Uso

1. **Inicia** la aplicación
2. **Selecciona una carpeta** usando el botón "Elegir carpeta"
3. **Elige opciones** (incluir subcarpetas si es necesario)
4. **Inicia el escaneo** con el botón "Empezar limpieza"
5. **Revisa los duplicados** en la tabla de resultados
6. **Toma acción** sobre los archivos duplicados (eliminar, mover a papelera, etc.)

---

### 🔒 Consideraciones de Seguridad

- La **eliminación de archivos** es una operación destructiva. Siempre revisa los duplicados antes de eliminar.
- El **modo simulación** permite previsualizar acciones sin realizar cambios.
- El **registro (logging)** rastrea todas las operaciones con fines de auditoría.
- Los **diálogos de confirmación** previenen pérdida accidental de datos.

---

### 🗺️ Hoja de Ruta

- [x] Sistema de construcción basado en Maven
- [x] Soporte multiidioma (ES/EN)
- [x] Registro completo (logging)
- [x] CI/CD con GitHub Actions
- [ ] Motor completo de detección de duplicados con comparación de hash
- [ ] Tabla de resultados con columnas ordenables
- [ ] Vista previa de archivos de imagen
- [ ] Exportar resultados a CSV/JSON
- [ ] Mover a papelera en lugar de eliminación permanente
- [ ] Barra de progreso con soporte de cancelación
- [ ] Caché de hash para re-escaneos más rápidos
- [ ] Opciones avanzadas de filtrado

---

### 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor sigue estos pasos:

1. Haz un fork del repositorio
2. Crea una rama de característica (`git checkout -b feature/CaracteristicaAsombrosa`)
3. Haz commit de tus cambios (`git commit -m 'Agregar alguna CaracteristicaAsombrosa'`)
4. Haz push a la rama (`git push origin feature/CaracteristicaAsombrosa`)
5. Abre un Pull Request

Por favor asegúrate de que:
- El código sigue las guías de estilo del proyecto (.editorconfig)
- Las pruebas pasan (`mvn test`)
- La documentación se actualiza según sea necesario

---

### 📄 Licencia

Este proyecto está licenciado bajo la **Licencia Pública General de GNU v3.0** - consulta el archivo [LICENSE](LICENSE) para más detalles.

---

### 👨‍💻 Autor

Desarrollado por **Jesús Luna Romero**  
📧 [jesuslunaromero230902@gmail.com](mailto:jesuslunaromero230902@gmail.com)

---

⭐ *Si te gusta este proyecto, ¡dale una estrella en GitHub!*
