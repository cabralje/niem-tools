# AGENTS.md

This repository also contains a more detailed assistant guide in CLAUDE.md.
When present, treat CLAUDE.md as the primary “source of truth” for project
structure, conventions, and workflows.

## Quick rules (don’t skip)

- Build/test: run `mvn test` after code changes (use `mvn package` only if packaging is required).
- Do not edit generated BOUML wrappers in `src/main/java/fr/bouml/`.
- Most real code changes belong in `src/main/java/org/cabral/niemtools/`.
- JavaFX is optional at runtime; avoid unconditional JavaFX class loading (use the existing reflection-based patterns).
- Tests are JUnit 4 (not JUnit 5).
- Property keys and FXML ids/fields use PascalCase (e.g., `ExportProjectDir`).
