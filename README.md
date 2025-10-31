# taktx-worker-examples (collection)

This repository is a collection of small example modules that demonstrate different ways to use and build components for TaktX. 
The root project centralizes common Gradle configuration and helper tasks so modules can be lightweight and focused on their framework.

Modules live under the repository root (for example `client-workers-quarkus/`). Each module should provide its own `README.md` with framework-specific instructions. 
The root README documents global commands and how to add new modules.

The sources on the master branch work with the current TaktX release.

## Current modules

- `client-workers-quarkus` — Quarkus-based example workers 
- `client-swagger-ui` — A simple static web server serving the Swagger UI to interact with TaktX using the TaktXClient.
- `docker/docker-compose-full` — Docker Compose setup for a local full TaktX environment with all components.

## License
This repository is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.