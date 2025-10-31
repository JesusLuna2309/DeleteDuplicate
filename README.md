# 🧹 Duplicate File Remover

> 🇪🇸 **Versión en español abajo**

---

<p align="center">
  <img src="https://img.shields.io/badge/Java-21+-orange?logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaFX-21-blue?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Status-In%20Development-yellow" />
  <img src="https://img.shields.io/badge/License-GPLv3-blue" />
</p>

---

## 🇬🇧 English

### 🧠 Overview

**Duplicate File Remover** is a desktop app built with **JavaFX** that scans directories to detect and remove duplicate files based on their **content hash**.  
It provides a **dark and modern UI**, fast performance with **parallel processing**, and a clean user experience for data organization.

---

### ✨ Features

- 🗂️ Folder selection via file chooser  
- ⚡ Fast duplicate detection using file hashing (MD5/SHA algorithms)  
- 🧮 Parallelized file scanning for better performance  
- 🧹 Safe file deletion with user confirmation  
- 🌙 Dark, modern interface built with JavaFX and CSS  
- 🧾 Real-time logs on console

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

### 🖼️ Screenshot (Placeholder)

![App Screenshot](resources/screenshots/main_window.png)

---

### ⚙️ Built With

- 🧱 **Java 21+**
- 💠 **JavaFX 21**
- 🎨 **FXML** for the UI
- 🧰 **Eclipse IDE**
- 🧾 Optional: **Gradle / Java Modules**

---

### 🚀 How to Run

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/DuplicateFileRemover.git
   ```
2. Open it in **Eclipse** (or IntelliJ / VS Code).  
3. Ensure JavaFX is properly configured in the module path.  
4. Run the main class:
   ```
   application.MainApp
   ```
5. Select a directory → Scan → Review duplicates → Delete safely.

---

### 📁 Project Structure

```
DuplicateFileRemover/
│
├── src/
│   ├── application/        # Main Java source code
│   └── resources/
│       ├── icons/          # Icons and app images
│       ├── styles/         # Dark theme CSS
│       └── fxml/           # UI layouts
│
├── bin/                    # Compiled classes
├── README.md               # This file
└── module-info.java        # Module declaration
```

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

- 🗂️ Selección de carpeta con explorador de archivos  
- ⚡ Detección rápida de duplicados mediante hash (MD5/SHA)  
- 🧮 Escaneo de archivos en paralelo  
- 🧹 Eliminación segura con confirmación del usuario  
- 🌙 Interfaz oscura y moderna  
- 🧾 Registro de acciones en la consola

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

### ⚙️ Tecnologías utilizadas

- 🧱 **Java 21+**
- 💠 **JavaFX 21**
- 🎨 **FXML** para la interfaz  
- 🧰 **Eclipse IDE**
- 🧾 Opcional: **Gradle / módulos Java**

---

### 🚀 Cómo ejecutar el proyecto

1. Clona este repositorio:
   ```bash
   git clone https://github.com/your-username/DuplicateFileRemover.git
   ```
2. Ábrelo en **Eclipse** o en tu IDE preferido.  
3. Configura correctamente las librerías de JavaFX.  
4. Ejecuta la clase principal:
   ```
   application.MainApp
   ```
5. Selecciona una carpeta → Analiza → Revisa duplicados → Elimina con seguridad.

---

### 📁 Estructura del proyecto

```
DuplicateFileRemover/
│
├── src/
│   ├── application/        # Código fuente Java
│   └── resources/
│       ├── icons/          # Iconos e imágenes
│       ├── styles/         # Estilos CSS
│       └── fxml/           # Vistas gráficas
│
├── bin/                    # Archivos compilados
├── README.md               # Este archivo
└── module-info.java        # Configuración del módulo
```

---

### 👨‍💻 Autor

Desarrollado por **Jesús Luna Romero**  
📧 [jesuslunaromero230902@gmail.com](mailto:jesuslunaromero230902@gmail.com)

---

⭐ *If you like this project, give it a star on GitHub!*
