package com.example.data.model

data class TrendingNiche(
    val id: String,
    val title: String,
    val category: String,
    val trendingScore: Int, // e.g. 99%
    val viralGrowth: String, // e.g. "+380% viral surge"
    val sampleHooks: List<String>,
    val suggestedTopics: List<String>,
    val defaultHashtags: List<String>,
    val primaryColorHex: Long = 0xFF00D2FF,
    val accentColorHex: Long = 0xFFFF1E44,
    val coverDrawableName: String = "niche_supercar"
)

object TrendingNichesRepository {
    val curatedNiches = listOf(
        TrendingNiche(
            id = "niche_gym_anime_beast",
            title = "⚡ Anime Beast Motivation & Gym Arc",
            category = "Fitness & Mindset",
            trendingScore = 99,
            viralGrowth = "+420% Phonk surge",
            sampleHooks = listOf(
                "🔥 They laughed at your start. Now make them fear your discipline!",
                "⚔️ Enter your villain training arc: 3 rules to build an unbreakable physique!",
                "⚡ When you feel like quitting, remember why you began!"
            ),
            suggestedTopics = listOf(
                "Anime Villain Training Arc",
                "Phonk Gym Motivation Routine",
                "Unbreakable Mental Discipline",
                "Dopamine Detox Transformation"
            ),
            defaultHashtags = listOf("#GymTok", "#AnimeMotivation", "#PhonkWorkout", "#Grindset", "#Discipline"),
            primaryColorHex = 0xFF00D2FF,
            accentColorHex = 0xFFFF1E44,
            coverDrawableName = "niche_gym_anime"
        ),
        TrendingNiche(
            id = "niche_supercars_hypercars",
            title = "🏎️ Supercars & Midnight Drift ASMR",
            category = "Automotive & Speed",
            trendingScore = 98,
            viralGrowth = "+360% FYP engagement",
            sampleHooks = listOf(
                "🔊 Twin-turbo V12 spooling at 3:00 AM on the empty highway...",
                "🏁 0 to 200 MPH in pure silence before the exhaust screams!",
                "🚀 The rarest hypercar on Earth that only 5 people can buy!"
            ),
            suggestedTopics = listOf(
                "Twin Turbo V12 Acceleration",
                "Midnight Tokyo Highway Drift",
                "Hypercar Aero Engineering",
                "Custom Carbon Widebody Build"
            ),
            defaultHashtags = listOf("#CarTok", "#Supercar", "#TwinTurbo", "#JDMDrift", "#Hypercar"),
            primaryColorHex = 0xFFFF1E44,
            accentColorHex = 0xFFFFB800,
            coverDrawableName = "niche_supercar"
        ),
        TrendingNiche(
            id = "niche_cyberpunk_esports",
            title = "🎮 Cyberpunk Gaming & Stealth Battlestations",
            category = "Gaming & Tech",
            trendingScore = 97,
            viralGrowth = "+290% TikTok views",
            sampleHooks = listOf(
                "🎯 The 1v5 clutch moment that made the entire enemy lobby rage quit!",
                "⚡ Building the ultimate $10,000 stealth matte black gaming setup!",
                "🕹️ Secret tactical settings pro FPS players don't want you to know!"
            ),
            suggestedTopics = listOf(
                "Pro Esports Clutch Highlights",
                "Stealth Matte Black Battlestation",
                "Tactical FPS Reflex Secrets",
                "Cyberpunk 2077 Max RTX Mode"
            ),
            defaultHashtags = listOf("#GamingTok", "#Battlestation", "#Esports", "#Cyberpunk", "#FPSClutch"),
            primaryColorHex = 0xFF00FF87,
            accentColorHex = 0xFF00D2FF,
            coverDrawableName = "niche_supercar"
        ),
        TrendingNiche(
            id = "niche_ai_future_tech",
            title = "🚀 Future AI Robotics & Sci-Fi Power",
            category = "Future Tech",
            trendingScore = 96,
            viralGrowth = "+310% virality",
            sampleHooks = listOf(
                "🤖 Humanoid robots just learned how to sprint at 25 MPH!",
                "🧠 Quantum processors just solved an equation in 3 seconds!",
                "🌌 Humanity's blueprint to build a Dyson Sphere by 2050!"
            ),
            suggestedTopics = listOf(
                "Humanoid Robot Speed Test",
                "Quantum Supercomputing Era",
                "Space Fortress Mega-Structures",
                "Cybernetic Neural Interfaces"
            ),
            defaultHashtags = listOf("#FutureTech", "#AIRevolution", "#QuantumTech", "#SciFi", "#Robotics"),
            primaryColorHex = 0xFF00D2FF,
            accentColorHex = 0xFF0066FF,
            coverDrawableName = "niche_supercar"
        ),
        TrendingNiche(
            id = "niche_sigma_stoic_mindset",
            title = "🐺 Stoic Power & Sigma Discipline",
            category = "Philosophy & Focus",
            trendingScore = 95,
            viralGrowth = "+275% watch retention",
            sampleHooks = listOf(
                "🛡️ Marcus Aurelius: The brutal truth about controlling your emotions in chaotic times!",
                "🧠 4 harsh psychological rules that will instantly put you ahead of 99% of people!",
                "⚔️ Silence is power: Why dangerous men never brag about their next move!"
            ),
            suggestedTopics = listOf(
                "Marcus Aurelius Stoic Rules",
                "Dark Psychology & Focus",
                "Mastering Emotional Control",
                "Unstoppable Solo Grind"
            ),
            defaultHashtags = listOf("#Stoicism", "#SigmaMindset", "#Discipline", "#MarcusAurelius", "#MenMindset"),
            primaryColorHex = 0xFFFFB800,
            accentColorHex = 0xFFFF1E44,
            coverDrawableName = "niche_gym_anime"
        ),
        TrendingNiche(
            id = "niche_hustle_wealth_empires",
            title = "💰 High-Stakes Wealth & Tactical Empires",
            category = "Wealth & Strategy",
            trendingScore = 94,
            viralGrowth = "+230% algorithm push",
            sampleHooks = listOf(
                "📈 How one calculated risk turned a $500 idea into a $10M empire!",
                "🏢 The hidden financial strategies of the world's most powerful families!",
                "💼 Stop trading time for money: Build systems that generate cash while you sleep!"
            ),
            suggestedTopics = listOf(
                "Billionaire Investment Strategy",
                "Building Scalable Tech Empires",
                "High-Stakes Negotiation Tactics",
                "Automated Wealth Cashflow"
            ),
            defaultHashtags = listOf("#WealthTok", "#HustleHard", "#BusinessStrategy", "#FinancialFreedom", "#Empire"),
            primaryColorHex = 0xFF00FF87,
            accentColorHex = 0xFFFFB800,
            coverDrawableName = "niche_gym_anime"
        )
    )
}
