package com.example.data.model

data class VoiceOption(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String = "🎙️"
)

data class LanguageOption(
    val code: String,
    val name: String,
    val country: String,
    val flagEmoji: String,
    val sampleHook: String,
    val voices: List<VoiceOption>
)

object SupportedLanguages {
    val all = listOf(
        LanguageOption(
            code = "pt-BR",
            name = "Português",
            country = "Brasil",
            flagEmoji = "🇧🇷",
            sampleHook = "🔥 Pare tudo! Enquanto todos dormem, é assim que você constrói uma disciplina inabalável!",
            voices = listOf(
                VoiceOption("pt_phonk", "Narrador Grave Phonk BR", "Voz profunda com sub-grave e pegada sombria", "⚡"),
                VoiceOption("pt_coach", "Coach Motivacional Extremo", "Tom agressivo e inspirador de alta energia", "🔥"),
                VoiceOption("pt_cyber", "Operador Tático IA BR", "Dicção cibernética, rápida e precisa", "🤖"),
                VoiceOption("pt_stoic", "Filósofo Estoico BR", "Tom calmo, compenetrado e autoritário", "🛡️"),
                VoiceOption("pt_tiktok", "Apresentador Viral TikTok BR", "Entrega rápida, dinamismo e retenção máx", "🚀")
            )
        ),
        LanguageOption(
            code = "en-US",
            name = "English",
            country = "United States",
            flagEmoji = "🇺🇸",
            sampleHook = "🔥 Stop scrolling right now! This is the exact blueprint to dominate your field in 2026!",
            voices = listOf(
                VoiceOption("en_phonk", "Deep Phonk Narrator (US)", "Heavy sub-bass, dark gritty cadence", "⚡"),
                VoiceOption("en_coach", "Unstoppable Motivation Coach", "Aggressive beast mindset delivery", "🔥"),
                VoiceOption("en_cyber", "Cyberpunk AI Operative", "Cold tactical analysis and crisp tone", "🤖"),
                VoiceOption("en_stoic", "Stoic Philosopher (US)", "Deep, resolute, unbreakable focus", "🛡️"),
                VoiceOption("en_host", "High-Octane TikTok Host", "Fast punchy hook delivery", "🚀")
            )
        ),
        LanguageOption(
            code = "es-LA",
            name = "Español",
            country = "Latinoamérica / España",
            flagEmoji = "🇪🇸",
            sampleHook = "🔥 ¡Detén el scroll ahora mismo! ¡Así es como construyes una disciplina legendaria!",
            voices = listOf(
                VoiceOption("es_phonk", "Narrador Grave Phonk Latino", "Voz profunda con impacto y poder", "⚡"),
                VoiceOption("es_coach", "Coach Motivación Extrema", "Energía imparable y contundente", "🔥"),
                VoiceOption("es_cyber", "Operador Táctico IA ES", "Análisis frío y precisión tecnológica", "🤖"),
                VoiceOption("es_stoic", "Filósofo Estoico Latino", "Serenidad, firmeza y convicción", "🛡️"),
                VoiceOption("es_tiktok", "Locutor Viral TikTok ES", "Ritmo acelerado para máxima retención", "🚀")
            )
        ),
        LanguageOption(
            code = "ja-JP",
            name = "日本語",
            country = "日本 (Japan)",
            flagEmoji = "🇯🇵",
            sampleHook = "🔥 今すぐ画面を止めて！これが限界を突破する究極の規律だ！",
            voices = listOf(
                VoiceOption("ja_anime", "Shonen Hero Voice (熱血)", "熱血で力強い主人公ボイス", "⚔️"),
                VoiceOption("ja_cyber", "Tokyo Cyber Operator (電脳)", "クールで精密な近未来AIナレーション", "🤖"),
                VoiceOption("ja_phonk", "Deep Bass Samurai (重厚)", "重厚で低音の効いた侍スピリット", "⚡"),
                VoiceOption("ja_stoic", "Zen Stoic Master (禅・沈着)", "静けさと圧倒的意志の力", "🛡️")
            )
        ),
        LanguageOption(
            code = "de-DE",
            name = "Deutsch",
            country = "Deutschland",
            flagEmoji = "🇩🇪",
            sampleHook = "🔥 Hör sofort auf zu scrollen! So baust du unzerstörbare Disziplin auf!",
            voices = listOf(
                VoiceOption("de_phonk", "Tiefe Phonk-Stimme", "Mächtiger Subbass und maskuliner Ton", "⚡"),
                VoiceOption("de_coach", "Extremer Motivationscoach", "Direkt, kompromisslos und kraftvoll", "🔥"),
                VoiceOption("de_cyber", "Taktischer KI-Sprecher", "Präzise High-Tech Analyse", "🤖"),
                VoiceOption("de_stoic", "Stoischer Meister", "Ruhige, unerschütterliche Autorität", "🛡️")
            )
        ),
        LanguageOption(
            code = "fr-FR",
            name = "Français",
            country = "France",
            flagEmoji = "🇫🇷",
            sampleHook = "🔥 Arrête de scroller maintenant ! Voici le secret d'une discipline inébranlable !",
            voices = listOf(
                VoiceOption("fr_phonk", "Voix Grave Phonk FR", "Basses profondes et impact percutant", "⚡"),
                VoiceOption("fr_coach", "Coach Motivation Intense", "Énergie brute et détermination totale", "🔥"),
                VoiceOption("fr_cyber", "Opérateur Tactique IA", "Analyse futuriste précise et fluide", "🤖"),
                VoiceOption("fr_stoic", "Philosophe Stoïque FR", "Calme absolu et maîtrise de soi", "🛡️")
            )
        ),
        LanguageOption(
            code = "it-IT",
            name = "Italiano",
            country = "Italia",
            flagEmoji = "🇮🇹",
            sampleHook = "🔥 Fermati subito! Ecco come forgiare una disciplina e un focus indistruttibili!",
            voices = listOf(
                VoiceOption("it_phonk", "Voce Profonda Phonk IT", "Tono basso e potente con ritmo oscuro", "⚡"),
                VoiceOption("it_coach", "Coach Motivazionale Intenso", "Carica esplosiva e grinta vincente", "🔥"),
                VoiceOption("it_cyber", "Operatore Tattico IA", "Freddezza analitica e stile moderno", "🤖"),
                VoiceOption("it_stoic", "Filosofo Stoico IT", "Fermezza, equilibrio e autorità", "🛡️")
            )
        )
    )

    val defaultLanguage = all.first() // Portuguese BR by default, with instant switching to any
}
