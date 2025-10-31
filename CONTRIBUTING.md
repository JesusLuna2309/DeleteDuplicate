# Contributing to Duplicate File Remover

First off, thank you for considering contributing to Duplicate File Remover! It's people like you that make this tool better for everyone.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

This project adheres to a code of conduct that we expect all contributors to follow. Please be respectful and constructive in all interactions.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce** the behavior
- **Expected behavior**
- **Actual behavior**
- **Environment details** (OS, Java version, etc.)
- **Screenshots** if applicable

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- **Clear title and description**
- **Current behavior** and **desired behavior**
- **Why this enhancement would be useful**
- **Possible implementation approach** (optional)

### Pull Requests

We actively welcome pull requests! Here's the process:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Make your changes
4. Add or update tests as needed
5. Ensure all tests pass
6. Commit your changes (see Commit Guidelines)
7. Push to your fork (`git push origin feature/AmazingFeature`)
8. Open a Pull Request

## Development Setup

### Prerequisites

- **Java 21** or higher
- **Maven 3.9+**
- **Git**

### Initial Setup

1. Clone your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Duplicate_File_Remover.git
   cd Duplicate_File_Remover
   ```

2. Compile the project:
   ```bash
   mvn clean compile
   ```

3. Run tests:
   ```bash
   mvn test
   ```

4. Run the application:
   ```bash
   mvn javafx:run
   ```

### Project Structure

```
Duplicate_File_Remover/
├── src/main/java/              # Java source code
│   └── com/jesusluna/duplicateremover/
├── src/main/resources/         # Resources (icons, i18n, config)
├── src/test/java/              # Unit tests
└── pom.xml                     # Maven configuration
```

## Coding Standards

### Java Style

- Follow the `.editorconfig` settings in the project
- Use **4 spaces** for indentation (no tabs)
- **Maximum line length**: 120 characters
- Use meaningful variable and method names
- Add JavaDoc comments for public methods and classes

### Best Practices

- **Single Responsibility**: Each class should have one clear purpose
- **DRY Principle**: Don't Repeat Yourself
- **SOLID Principles**: Follow object-oriented design principles
- **Error Handling**: Use appropriate exception handling
- **Logging**: Use SLF4J for logging, not System.out

### Code Example

```java
/**
 * Calculates the SHA-256 hash of a file.
 * 
 * @param file the file to hash
 * @return the hex-encoded hash string
 * @throws IOException if file cannot be read
 */
public String calculateFileHash(File file) throws IOException {
    logger.debug("Calculating hash for: {}", file.getAbsolutePath());
    
    try (InputStream input = new FileInputStream(file)) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int read;
        
        while ((read = input.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        
        return bytesToHex(digest.digest());
    } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("SHA-256 algorithm not available", e);
    }
}
```

## Commit Guidelines

We follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages:

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples

```bash
feat(ui): add progress bar for file scanning

Add a progress bar that shows the current file being scanned
and overall progress percentage.

Closes #42
```

```bash
fix(hash): handle large files with streaming

Use streaming approach for files larger than 100MB to
avoid memory issues.

Fixes #38
```

## Pull Request Process

1. **Update Documentation**: If you change functionality, update the README or other docs
2. **Add Tests**: Ensure your changes are tested
3. **Update CHANGELOG**: Add an entry describing your changes (if applicable)
4. **Run All Tests**: `mvn test` must pass
5. **Follow Code Style**: Ensure code follows project conventions
6. **Squash Commits**: Keep your PR history clean (optional but preferred)
7. **Write Clear Description**: Explain what your PR does and why

### PR Template

When opening a PR, please include:

- **Description**: What does this PR do?
- **Motivation**: Why is this change needed?
- **Testing**: How was this tested?
- **Screenshots**: If UI changes, include before/after screenshots
- **Checklist**: 
  - [ ] Tests pass
  - [ ] Code follows style guidelines
  - [ ] Documentation updated
  - [ ] No breaking changes (or clearly documented)

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=MainAppTest

# Run tests with coverage (future)
mvn test jacoco:report
```

### Writing Tests

- Use **JUnit 5** for unit tests
- Test file naming: `*Test.java`
- Use descriptive test method names: `testCalculateHashForLargeFile()`
- Include positive and negative test cases
- Mock external dependencies when appropriate

## Questions?

If you have questions, feel free to:

- Open an issue with the `question` label
- Contact the maintainer: jesuslunaromero230902@gmail.com

## License

By contributing, you agree that your contributions will be licensed under the GPL v3 License.

---

Thank you for contributing! 🎉
