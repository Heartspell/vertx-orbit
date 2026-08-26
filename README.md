<div align="center">

# Vert.x Orbit

**A focused lifecycle companion for Vert.x Kotlin projects in IntelliJ IDEA.**

[![JetBrains Plugins](https://img.shields.io/badge/JetBrains%20Marketplace-Vert.x%20Orbit-000000?style=for-the-badge&logo=jetbrains&logoColor=white)](https://plugins.jetbrains.com/plugin/33185-vert-x-orbit)

[![GitHub release](https://img.shields.io/github/v/release/heartspell/vertx-orbit?style=flat-square&color=blue)](https://github.com/heartspell/vertx-orbit/releases)
[![GitHub stars](https://img.shields.io/github/stars/heartspell/vertx-orbit?style=flat-square&color=gold)](https://github.com/heartspell/vertx-orbit/stargazers)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg?style=flat-square)](LICENSE)

---

</div>

## What It Shows

- Verticle classes in the active Kotlin file.
- `deployVerticle(...)` calls that point to those classes.
- Lifecycle signals grouped by `Start`, `Running`, and `Stop`.
- Warnings for suspicious lifecycle code, such as promises that may not complete or coroutine scopes that are not cancelled.
- Vert.x-managed resources such as event bus consumers, timers, and HTTP servers.
- A bottom panel with a graph, a short description, and a recommendation for the selected row.

Orbit works best as a focused companion while reading one file. It does not try to replace Find Usages, debugger traces, or project-wide architecture diagrams.

## Using It

Open a Kotlin file with Vert.x verticles, then open the `Vert.x Orbit` tool window from the right side of the IDE.

Double-click a class, deployment, or lifecycle row to jump back to the source.

The display mode is available in:

```text
Settings | Tools | Vert.x Orbit
```

Modes:

- `Full`: tree, lifecycle graph, description, and recommendation.
- `Tiny`: tree plus a compact bottom summary.
- `Trace`: tree only.

## Install From ZIP

Build the plugin:

```bash
./gradlew buildPlugin
```

Install the generated archive:

```text
Settings | Plugins | gear icon | Install Plugin from Disk...
```

Pick:

```text
build/distributions/vertx-orbit-*.zip
```

Restart IntelliJ IDEA after installation.

## Development

Run the plugin in a sandbox IDE:

```bash
./gradlew runIde
```

The sample file is here:

```text
examples/SampleVerticle.kt
```

Useful checks before packaging:

```bash
./gradlew clean buildPlugin
./gradlew verifyPluginProjectConfiguration
```

## License

MIT License. See [LICENSE](LICENSE).
