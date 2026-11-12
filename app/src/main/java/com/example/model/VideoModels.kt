package com.example.model

enum class TemplateCategory(val title: String) {
    ALL("All Templates"),
    SOCIAL_MEDIA("Social Media"),
    PRESENTATIONS("Presentations"),
    GREETINGS("Personal Greetings"),
    MARKETING("Marketing"),
    TECH("Tech & Product"),
    CINEMATIC("Cinematic & Film"),
    ENTERTAINMENT("Music & Creative")
}

enum class VideoAspectRatio(val ratio: Float, val label: String) {
    PORTRAIT_9_16(9f / 16f, "9:16 Reel"),
    LANDSCAPE_16_9(16f / 9f, "16:9 Deck"),
    SQUARE_1_1(1f, "1:1 Feed")
}

enum class AnimationStyle(val displayName: String, val description: String) {
    ZOOM_KEN_BURNS("Ken Burns Zoom", "Gentle continuous zoom for cinematic depth"),
    SLIDE_UP("Slide Up & Rise", "Elements glide smoothly from bottom into focus"),
    PAN_SIDEWAYS("Horizontal Pan", "Sweeping pan motion across the frame"),
    PULSE_SCALE("Rhythmic Pulse", "Energetic pulsing scale timed to rhythm"),
    KINETIC_BOUNCE("Kinetic Bounce", "Dynamic snappy entrance with elastic settle"),
    GLITCH_SHIFT("Digital Shift", "Fast sleek cybernetic coordinate shift")
}

enum class GraphicStyle(val displayName: String, val description: String) {
    CONCENTRIC_RINGS("Concentric Rings", "Rotating layered energy circles and orbital arcs"),
    DYNAMIC_WAVE("Dynamic Audio Waves", "Sine wave oscillations with harmonic crests"),
    TECH_GRID("Tech Blueprint Grid", "Subtle perspective lines with glowing nodes"),
    AURORA_GRADIENT("Aurora Bloom", "Dreamy fluid light sweeps and radial glows"),
    CELEBRATION_BURST("Celebration Burst", "Radiant geometric confetti and sparkle beams"),
    METRICS_CHART("Metrics & Growth Chart", "Animated bar graphs and upward trajectory vectors"),
    ABSTRACT_SPHERE("Abstract Orbit Sphere", "Layered geometric sphere with rotating rings"),
    NEON_HEXAGONS("Neon Hex Pattern", "Honeycomb matrix with ambient pulse lighting"),
    DANCING_DOG("Dancing Dog Character", "Animated dancing dog with bobbing ears, tapping paws, and wagging tail"),
    RETRO_SYNTHWAVE("Retro 80s Synthwave", "Neon grid highway with segmented glowing horizon sun"),
    CINEMATIC_LETTERBOX("Cinematic Flare", "Anamorphic lens flares, vignette aura, and floating dust motes"),
    CARTOON_SPEED_LINES("Cartoon Manga Lines", "High-velocity radial speed bursts and comic impact rays"),
    LUXURY_MINIMAL("Luxury Geometric", "Delicate gold wireframe facets and soft radial luminescence"),
    CYBER_HUD_TARGET("Sci-Fi HUD Reticle", "Holographic targeting brackets, rotation dials, and telemetry nodes"),
    AUDIO_SPECTRUM("EDM Audio Spectrum", "Pulsing frequency equalizer columns with central audio ripple"),
    NATURE_HORIZON("Nature Horizon", "Layered mountain terrain silhouettes and solar atmospheric glow")
}

enum class VideoGenre(
    val displayName: String,
    val description: String,
    val defaultGraphicStyle: GraphicStyle,
    val paletteId: String
) {
    CINEMATIC("Cinematic Movie", "Moody widescreen atmosphere with anamorphic lens flares", GraphicStyle.CINEMATIC_LETTERBOX, "monochrome_minimal"),
    RETRO_80S("Retro Synthwave", "80s cyberpunk neon grid with glowing horizon sun", GraphicStyle.RETRO_SYNTHWAVE, "cyber_neon"),
    ANIME_COMIC("Cartoon & Manga", "High energy radial action speed lines and comic bursts", GraphicStyle.CARTOON_SPEED_LINES, "sunset_gold"),
    SCI_FI_CYBERPUNK("Sci-Fi HUD & Tech", "Futuristic holographic target reticles and telemetry", GraphicStyle.CYBER_HUD_TARGET, "cyber_neon"),
    LUXURY_EDITORIAL("Luxury & Fashion", "Elegant gold wireframe diamonds and refined shimmer", GraphicStyle.LUXURY_MINIMAL, "sunset_gold"),
    AUDIO_EDM("Music Visualizer", "Rhythmic sound spectrum equalizer and beat halos", GraphicStyle.AUDIO_SPECTRUM, "cyber_neon"),
    NATURE_ORGANIC("Nature & Travel", "Atmospheric horizon silhouettes and organic auroras", GraphicStyle.NATURE_HORIZON, "emerald_nature"),
    CORPORATE_DATA("Business & Deck", "Professional growth charts and architecture grids", GraphicStyle.METRICS_CHART, "corporate_slate"),
    VIRAL_PARTY("Celebration & Fun", "Sparkle bursts, festive confetti, and dancing characters", GraphicStyle.DANCING_DOG, "celebration_warm")
}

data class ColorPalettePreset(
    val id: String,
    val name: String,
    val bgStart: Long,
    val bgEnd: Long,
    val primary: Long,
    val secondary: Long,
    val textPrimary: Long,
    val accent: Long
)

data class VideoScene(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String = "",
    val caption: String = "",
    val durationSeconds: Float = 3.5f,
    val animation: AnimationStyle = AnimationStyle.ZOOM_KEN_BURNS,
    val graphicStyle: GraphicStyle = GraphicStyle.CONCENTRIC_RINGS,
    val placeholderGraphicDesc: String = "Graphic placeholder"
)

data class VideoTemplate(
    val id: String,
    val title: String,
    val description: String,
    val category: TemplateCategory,
    val defaultAspectRatio: VideoAspectRatio,
    val palette: ColorPalettePreset,
    val suggestedMusicMood: String,
    val scenes: List<VideoScene>
)

object VideoLimits {
    const val MAX_SCENES = 700
    const val MAX_SCENE_DURATION_SECONDS = 30.0f
    const val MIN_SCENE_DURATION_SECONDS = 1.0f
}

fun formatVideoDuration(totalSeconds: Float): String {
    val sec = totalSeconds.toInt()
    val hours = sec / 3600
    val minutes = (sec % 3600) / 60
    val seconds = sec % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else if (minutes > 0) {
        String.format("%02d:%02d", minutes, seconds)
    } else {
        String.format("%.1fs", totalSeconds)
    }
}
