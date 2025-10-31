graph TD
    A[DuplicateFileRemover/] --> B[src/]
    A --> F[bin/]
    A --> G[README.md]
    A --> H[module-info.java]

    B --> B1[application/ # Código Java principal]
    B --> B2[resources/]

    B2 --> B2a[icons/ # Iconos e imágenes]
    B2 --> B2b[styles/ # Estilos CSS]
    B2 --> B2c[fxml/ # Interfaces gráficas]
