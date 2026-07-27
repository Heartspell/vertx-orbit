package com.heartspell.vertxorbit.settings

import com.heartspell.vertxorbit.lifecycle.LifecycleFinding
import com.heartspell.vertxorbit.lifecycle.LifecyclePhase

object OrbitMessages {
    private val language: VertxOrbitSettings.OrbitLanguage
        get() = VertxOrbitSettings.getInstance().language

    fun activeEditor(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Active editor"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Активный редактор"
    }

    fun refreshActiveFile(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Refresh active file"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Обновить активный файл"
    }

    fun noVerticle(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "No Vert.x verticle in active file."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "В активном файле нет Vert.x verticle."
    }

    fun deployments(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Deployments"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Деплой"
    }

    fun noSignals(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "No lifecycle signals or issues found."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Lifecycle-сигналы и проблемы не найдены."
    }

    fun ok(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "OK"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "OK"
    }

    fun review(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "review"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "проверить"
    }

    fun needsReview(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Needs review"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Нужно проверить"
    }

    fun openClassHint(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Double-click to open class."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Двойной клик откроет класс."
    }

    fun openChildrenHint(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Double-click child rows to navigate."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Двойной клик по дочерней строке откроет код."
    }

    fun openDeployHint(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Double-click to open deploy call."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Двойной клик откроет deploy-вызов."
    }

    fun openSourceHint(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Double-click to open source."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Двойной клик откроет исходный код."
    }

    fun lifecycleSignals(count: Int): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "lifecycle signal(s)"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> if (count == 1) "lifecycle-сигнал" else "lifecycle-сигналов"
    }

    fun phaseTitle(phase: LifecyclePhase): String {
        return when (language) {
            VertxOrbitSettings.OrbitLanguage.ENGLISH -> phase.title
            VertxOrbitSettings.OrbitLanguage.RUSSIAN -> when (phase) {
                LifecyclePhase.START -> "Старт"
                LifecyclePhase.RUNNING -> "Работа"
                LifecyclePhase.STOP -> "Стоп"
            }
        }
    }

    fun phaseShort(phase: LifecyclePhase): String {
        return when (language) {
            VertxOrbitSettings.OrbitLanguage.ENGLISH -> phase.title
            VertxOrbitSettings.OrbitLanguage.RUSSIAN -> when (phase) {
                LifecyclePhase.START -> "Старт"
                LifecyclePhase.RUNNING -> "Работа"
                LifecyclePhase.STOP -> "Стоп"
            }
        }
    }

    fun runningStatus(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "run"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "run"
    }

    fun description(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Description"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Описание"
    }

    fun recommendation(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Recommendation"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Рекомендация"
    }

    fun selectedVerticleDescription(className: String): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "$className is the selected verticle in the active editor."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "$className выбран в активном редакторе."
    }

    fun selectedFindingDescription(message: String): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> message
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> message
    }

    fun selectedDeploymentDescription(target: String): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "deployVerticle target: $target"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Цель deployVerticle: $target"
    }

    fun okRecommendation(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "No lifecycle warning is visible for this selection."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Для выбранного элемента lifecycle-предупреждений не видно."
    }

    fun warningRecommendation(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "Review this before deploy; double-click the row to jump to the source."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Проверь это перед деплоем; двойной клик откроет место в коде."
    }

    fun dynamicDeployRecommendation(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "If possible, prefer a resolvable verticle class so Orbit can connect deploy calls to lifecycle signals."
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Если возможно, используй разрешимый класс verticle, чтобы Orbit связал deploy с lifecycle."
    }

    fun settingsIntro(language: VertxOrbitSettings.OrbitLanguage = this.language): String = text(
        language,
        "Kotlin Vert.x verticle lifecycle, deployment, and resource navigator for IntelliJ IDEA.",
        "Навигатор lifecycle, deployVerticle и ресурсов Vert.x verticle для Kotlin в IntelliJ IDEA."
    )

    fun languageLabel(language: VertxOrbitSettings.OrbitLanguage = this.language): String = text(
        language,
        "Language:",
        "Язык:"
    )

    fun designModeLabel(language: VertxOrbitSettings.OrbitLanguage = this.language): String = text(
        language,
        "Design mode:",
        "Режим интерфейса:"
    )

    fun authorLabel(language: VertxOrbitSettings.OrbitLanguage = this.language): String = text(
        language,
        "Author:",
        "Автор:"
    )

    fun repositoryLabel(language: VertxOrbitSettings.OrbitLanguage = this.language): String = text(
        language,
        "Repository:",
        "Репозиторий:"
    )

    fun aboutLabel(language: VertxOrbitSettings.OrbitLanguage = this.language): String = text(
        language,
        "About:",
        "О плагине:"
    )

    fun aboutText(language: VertxOrbitSettings.OrbitLanguage = this.language): String = text(
        language,
        "Vert.x Orbit keeps the current editor focused: it finds Kotlin verticles, deployVerticle calls, lifecycle signals, and risky start/stop patterns without leaving IntelliJ IDEA.",
        "Vert.x Orbit работает с текущим редактором: находит Kotlin verticle, deployVerticle-вызовы, lifecycle-сигналы и рискованные start/stop-паттерны прямо внутри IntelliJ IDEA."
    )

    fun languageName(value: VertxOrbitSettings.OrbitLanguage): String {
        return when (value) {
            VertxOrbitSettings.OrbitLanguage.ENGLISH -> "English"
            VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Русский"
        }
    }

    fun displayModeName(
        mode: VertxOrbitSettings.DisplayMode,
        language: VertxOrbitSettings.OrbitLanguage = this.language
    ): String {
        return when (language) {
            VertxOrbitSettings.OrbitLanguage.ENGLISH -> when (mode) {
                VertxOrbitSettings.DisplayMode.FULL -> "Full"
                VertxOrbitSettings.DisplayMode.TINY -> "Tiny"
                VertxOrbitSettings.DisplayMode.TRACE -> "Trace"
            }

            VertxOrbitSettings.OrbitLanguage.RUSSIAN -> when (mode) {
                VertxOrbitSettings.DisplayMode.FULL -> "Полный"
                VertxOrbitSettings.DisplayMode.TINY -> "Короткий"
                VertxOrbitSettings.DisplayMode.TRACE -> "Трассировка"
            }
        }
    }

    fun findingMessage(finding: LifecycleFinding): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> finding.message
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> when {
            finding.message.contains("promise may never complete") -> {
                "Promise ${phaseGenitive(finding.phase)} может не завершиться"
            }
            finding.message == "Coroutine scope is not cancelled" -> "CoroutineScope не отменяется"
            finding.message.startsWith("Event bus consumer") -> {
                "Потребитель event bus: ${finding.message.substringAfter(": ").ifBlank { "динамический адрес" }}"
            }
            finding.message == "timer timer" -> "Одноразовый таймер"
            finding.message == "periodic timer" -> "Периодический таймер"
            finding.message == "HTTP server" -> "HTTP-сервер"
            finding.message == "Potential event-loop blocking call" -> "Возможная блокировка event loop"
            finding.message == "Event bus request failure path is not visible" -> "Не видна обработка ошибки event bus request"
            else -> finding.message
        }
    }

    fun findingDetail(finding: LifecycleFinding): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> finding.detail
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> {
            val detail = finding.detail
            when {
                detail.startsWith("Complete or fail") -> {
                    "Заверши или отклони promise на каждом асинхронном пути ${phaseGenitive(finding.phase)}."
                }
                detail.startsWith("Cancel `") -> {
                    val name = detail.substringAfter("Cancel `").substringBefore("`")
                    "Отмени `$name` в `stop()`, чтобы coroutine-задачи следовали lifecycle verticle."
                }
                detail.startsWith("Vert.x automatically unregisters event bus handlers") -> {
                    managedDetail("Vert.x автоматически снимает event bus handlers, созданные внутри verticle, при undeploy.", detail)
                }
                detail.startsWith("Vert.x automatically cancels timers") -> {
                    managedDetail("Vert.x автоматически отменяет таймеры, созданные внутри verticle, при undeploy.", detail)
                }
                detail.startsWith("Vert.x automatically closes HTTP servers") -> {
                    managedDetail("Vert.x автоматически закрывает HTTP-серверы, созданные внутри verticle, при undeploy.", detail)
                }
                detail.startsWith("Review `") -> {
                    val call = detail.substringAfter("Review `").substringBefore("`")
                    "Проверь `$call` внутри verticle; блокирующую работу лучше выполнять в worker context."
                }
                detail.startsWith("Add `onFailure`") -> {
                    "Добавь `onFailure`, `onComplete` или обработку исключений coroutine вокруг этого request."
                }
                else -> detail
            }
        }
    }

    private fun phaseGenitive(phase: LifecyclePhase): String {
        return when (phase) {
            LifecyclePhase.START -> "запуска"
            LifecyclePhase.RUNNING -> "работы"
            LifecyclePhase.STOP -> "остановки"
        }
    }

    fun dynamicDeployTarget(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "dynamic deployVerticle target"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "динамическая цель deployVerticle"
    }

    fun noActiveEditor(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "No active editor"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "Нет активного редактора"
    }

    fun currentFile(): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "current file"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "текущий файл"
    }

    fun line(number: Int): String = when (language) {
        VertxOrbitSettings.OrbitLanguage.ENGLISH -> "line $number"
        VertxOrbitSettings.OrbitLanguage.RUSSIAN -> "строка $number"
    }

    private fun managedDetail(prefix: String, original: String): String {
        val storage = when {
            original.contains("Handle stored.") -> "Handle сохранен."
            original.contains("Handle not stored.") -> "Handle не сохранен."
            else -> ""
        }
        return "$prefix $storage".trim()
    }

    private fun text(language: VertxOrbitSettings.OrbitLanguage, english: String, russian: String): String {
        return when (language) {
            VertxOrbitSettings.OrbitLanguage.ENGLISH -> english
            VertxOrbitSettings.OrbitLanguage.RUSSIAN -> russian
        }
    }
}
