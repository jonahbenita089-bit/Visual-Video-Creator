package com.example.data

import com.example.model.AnimationStyle
import com.example.model.ColorPalettePreset
import com.example.model.GraphicStyle
import com.example.model.TemplateCategory
import com.example.model.VideoAspectRatio
import com.example.model.VideoScene
import com.example.model.VideoTemplate

object TemplateRepository {

    val palettes = listOf(
        ColorPalettePreset(
            id = "cyber_neon",
            name = "Cyber Neon",
            bgStart = 0xFF0B0F19,
            bgEnd = 0xFF161F38,
            primary = 0xFF00E5FF,
            secondary = 0xFF7C4DFF,
            textPrimary = 0xFFFFFFFF,
            accent = 0xFFFF007F
        ),
        ColorPalettePreset(
            id = "sunset_gold",
            name = "Sunset Glow",
            bgStart = 0xFF1A0B2E,
            bgEnd = 0xFF3D1635,
            primary = 0xFFFF6E40,
            secondary = 0xFFFFAB00,
            textPrimary = 0xFFFFF8E7,
            accent = 0xFFFF1744
        ),
        ColorPalettePreset(
            id = "corporate_slate",
            name = "Executive Slate",
            bgStart = 0xFF0F172A,
            bgEnd = 0xFF1E293B,
            primary = 0xFF38BDF8,
            secondary = 0xFF818CF8,
            textPrimary = 0xFFF8FAFC,
            accent = 0xFF34D399
        ),
        ColorPalettePreset(
            id = "emerald_nature",
            name = "Emerald Forest",
            bgStart = 0xFF06281E,
            bgEnd = 0xFF0E4435,
            primary = 0xFF10B981,
            secondary = 0xFF34D399,
            textPrimary = 0xFFECFDF5,
            accent = 0xFFA7F3D0
        ),
        ColorPalettePreset(
            id = "celebration_warm",
            name = "Celebration Warmth",
            bgStart = 0xFF2D132C,
            bgEnd = 0xFF801336,
            primary = 0xFFEE4540,
            secondary = 0xFFF9A826,
            textPrimary = 0xFFFFF4EC,
            accent = 0xFFFFD166
        ),
        ColorPalettePreset(
            id = "monochrome_minimal",
            name = "Minimal Onyx",
            bgStart = 0xFF121212,
            bgEnd = 0xFF242424,
            primary = 0xFFE0E0E0,
            secondary = 0xFF9E9E9E,
            textPrimary = 0xFFFFFFFF,
            accent = 0xFF757575
        )
    )

    val templates: List<VideoTemplate> = listOf(
        // DANCING DOG FEATURED TEMPLATE
        VideoTemplate(
            id = "social_dancing_dog",
            title = "Dancing Dog Groove",
            description = "Viral animated dance reel featuring a joyful dancing dog with tapping paws, ear bobbing, and disco floor waves.",
            category = TemplateCategory.SOCIAL_MEDIA,
            defaultAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
            palette = palettes[1], // Sunset Glow
            suggestedMusicMood = "Funky Disco Beats & Upbeat Rhythm",
            scenes = listOf(
                VideoScene(
                    id = "sdd_s1",
                    title = "Meet the Dancing Dog",
                    subtitle = "Dropping the freshest paw moves on the dance floor.",
                    badge = "Dance Mode",
                    caption = "Turn up the volume and watch him groove",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.DANCING_DOG,
                    placeholderGraphicDesc = "Dancing cartoon dog with tapping paws and wagging tail"
                ),
                VideoScene(
                    id = "sdd_s2",
                    title = "Catch the Rhythm",
                    subtitle = "Ear bobbing and tail wagging in sync with the beat.",
                    badge = "Step One",
                    caption = "Tap along with the beat",
                    durationSeconds = 3.8f,
                    animation = AnimationStyle.KINETIC_BOUNCE,
                    graphicStyle = GraphicStyle.DANCING_DOG,
                    placeholderGraphicDesc = "Dog spinning and tapping paws to harmonic disco waves"
                ),
                VideoScene(
                    id = "sdd_s3",
                    title = "Freestyle Dance Solo",
                    subtitle = "Bouncing high with disco lights and musical notes.",
                    badge = "Party Vibe",
                    caption = "Double tap if your dog loves music",
                    durationSeconds = 3.2f,
                    animation = AnimationStyle.GLITCH_SHIFT,
                    graphicStyle = GraphicStyle.DANCING_DOG,
                    placeholderGraphicDesc = "Energetic freestyle dance moves with floating musical notes"
                ),
                VideoScene(
                    id = "sdd_s4",
                    title = "Take a Bow",
                    subtitle = "Share this happy dancing dog with your friends.",
                    badge = "Encore",
                    caption = "Follow for more daily dog dance videos",
                    durationSeconds = 3.0f,
                    animation = AnimationStyle.ZOOM_KEN_BURNS,
                    graphicStyle = GraphicStyle.DANCING_DOG,
                    placeholderGraphicDesc = "Final bowing salute on the glowing dance floor"
                )
            )
        ),
        // SOCIAL MEDIA TEMPLATES
        VideoTemplate(
            id = "social_product_launch",
            title = "Product Drop Teaser",
            description = "High-energy portrait reel designed for viral product launches and feature drops.",
            category = TemplateCategory.SOCIAL_MEDIA,
            defaultAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
            palette = palettes[0], // Cyber Neon
            suggestedMusicMood = "Upbeat Electronic / Synthwave",
            scenes = listOf(
                VideoScene(
                    id = "spl_s1",
                    title = "Something New Is Here",
                    subtitle = "Engineered for creators who demand perfection.",
                    badge = "Official Reveal",
                    caption = "Swipe up to discover the revolution",
                    durationSeconds = 3.2f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Dynamic concentric energy rings centering on hero product emblem"
                ),
                VideoScene(
                    id = "spl_s2",
                    title = "Next-Gen Architecture",
                    subtitle = "Twice the speed. Half the latency. Zero compromises.",
                    badge = "Key Specs",
                    caption = "Benchmark tested across real workflows",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.TECH_GRID,
                    placeholderGraphicDesc = "Isometric glowing grid with metric nodes and data callouts"
                ),
                VideoScene(
                    id = "spl_s3",
                    title = "Available Worldwide",
                    subtitle = "Pre-orders open today with exclusive member benefits.",
                    badge = "Limited Batch",
                    caption = "Tap link in bio to secure yours",
                    durationSeconds = 3.0f,
                    animation = AnimationStyle.KINETIC_BOUNCE,
                    graphicStyle = GraphicStyle.NEON_HEXAGONS,
                    placeholderGraphicDesc = "Pulsing neon honeycomb pattern highlighting call-to-action badge"
                )
            )
        ),
        VideoTemplate(
            id = "social_daily_quote",
            title = "Kinetic Motivation Quote",
            description = "Elegant typography with ambient waves for shareable mindset and wisdom shorts.",
            category = TemplateCategory.SOCIAL_MEDIA,
            defaultAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
            palette = palettes[1], // Sunset Glow
            suggestedMusicMood = "Inspiring Ambient Piano",
            scenes = listOf(
                VideoScene(
                    id = "sdq_s1",
                    title = "Master Your Focus",
                    subtitle = "Where your attention flows, your momentum follows.",
                    badge = "Daily Thought",
                    caption = "Discipline over temporary motivation",
                    durationSeconds = 3.8f,
                    animation = AnimationStyle.ZOOM_KEN_BURNS,
                    graphicStyle = GraphicStyle.DYNAMIC_WAVE,
                    placeholderGraphicDesc = "Harmonic fluid wave crests undulating calmly behind typography"
                ),
                VideoScene(
                    id = "sdq_s2",
                    title = "Small Steps Compound",
                    subtitle = "A one percent improvement every single day creates massive transformation.",
                    badge = "Mindset Shift",
                    caption = "Save this reminder for later",
                    durationSeconds = 4.0f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.AURORA_GRADIENT,
                    placeholderGraphicDesc = "Radial golden aura bloom expanding smoothly through the background"
                ),
                VideoScene(
                    id = "sdq_s3",
                    title = "Begin Today",
                    subtitle = "The best moment to take action was yesterday. The second best is now.",
                    badge = "Action Prompt",
                    caption = "Tag someone who needs to hear this",
                    durationSeconds = 3.2f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Centering golden orbital rings pulsing on key call-to-action phrase"
                )
            )
        ),
        VideoTemplate(
            id = "social_flash_sale",
            title = "Weekend Flash Sale Promo",
            description = "Urgent high-impact square promo for retail discounts and weekend campaigns.",
            category = TemplateCategory.SOCIAL_MEDIA,
            defaultAspectRatio = VideoAspectRatio.SQUARE_1_1,
            palette = palettes[4], // Celebration Warm
            suggestedMusicMood = "Energetic Bass & Driving Beats",
            scenes = listOf(
                VideoScene(
                    id = "sfs_s1",
                    title = "Flash Sale Alert",
                    subtitle = "48 Hours Only. Up to 50% off sitewide on all catalog items.",
                    badge = "Mega Event",
                    caption = "Clock is ticking down fast",
                    durationSeconds = 2.8f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.CELEBRATION_BURST,
                    placeholderGraphicDesc = "Vibrant burst rays radiating outward from center discount badge"
                ),
                VideoScene(
                    id = "sfs_s2",
                    title = "Top Picks Going Fast",
                    subtitle = "Premium materials and crafted accessories selling out quickly.",
                    badge = "High Demand",
                    caption = "Free worldwide express shipping included",
                    durationSeconds = 3.0f,
                    animation = AnimationStyle.GLITCH_SHIFT,
                    graphicStyle = GraphicStyle.DYNAMIC_WAVE,
                    placeholderGraphicDesc = "Accelerating dynamic frequency waves tracking promotional momentum"
                ),
                VideoScene(
                    id = "sfs_s3",
                    title = "Use Code: FLASH50",
                    subtitle = "Checkout before Sunday midnight to claim your VIP discount.",
                    badge = "Redeem Now",
                    caption = "Visit our official store today",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.KINETIC_BOUNCE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Pulsing discount code container with glowing perimeter highlights"
                )
            )
        ),

        // PRESENTATION TEMPLATES
        VideoTemplate(
            id = "pres_business_pitch",
            title = "Quarterly Business Pitch",
            description = "Clean corporate widescreen video for executive summaries, investor decks, and roadmap reviews.",
            category = TemplateCategory.PRESENTATIONS,
            defaultAspectRatio = VideoAspectRatio.LANDSCAPE_16_9,
            palette = palettes[2], // Corporate Slate
            suggestedMusicMood = "Sophisticated Corporate Tech",
            scenes = listOf(
                VideoScene(
                    id = "pbp_s1",
                    title = "Q4 Strategic Overview",
                    subtitle = "Transforming customer engagement and scaling enterprise revenue.",
                    badge = "Executive Briefing",
                    caption = "Presented by Leadership Team",
                    durationSeconds = 4.0f,
                    animation = AnimationStyle.PAN_SIDEWAYS,
                    graphicStyle = GraphicStyle.TECH_GRID,
                    placeholderGraphicDesc = "Subtle isometric matrix grid with clean architectural perspective lines"
                ),
                VideoScene(
                    id = "pbp_s2",
                    title = "Key Growth Indicators",
                    subtitle = "Year-over-year expansion across primary international target markets.",
                    badge = "Performance Data",
                    caption = "+142% Retention Rate Recorded",
                    durationSeconds = 4.2f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.METRICS_CHART,
                    placeholderGraphicDesc = "Ascending geometric bar graph with vector trendline trajectory"
                ),
                VideoScene(
                    id = "pbp_s3",
                    title = "Three Pillars for 2027",
                    subtitle = "Operational automation, international distribution, and AI platform integration.",
                    badge = "Strategic Focus",
                    caption = "Clear execution milestones underway",
                    durationSeconds = 4.0f,
                    animation = AnimationStyle.ZOOM_KEN_BURNS,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Three intersecting orbital node rings representing core pillars"
                ),
                VideoScene(
                    id = "pbp_s4",
                    title = "Thank You for Partnering",
                    subtitle = "Questions, discussions, and technical roadmap documentation available.",
                    badge = "Next Steps",
                    caption = "Contact investor-relations@enterprise.com",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.AURORA_GRADIENT,
                    placeholderGraphicDesc = "Subtle cyan-indigo gradient glow framing company conclusion mark"
                )
            )
        ),
        VideoTemplate(
            id = "pres_tech_keynote",
            title = "Tech Keynote Showcase",
            description = "Futuristic presentation showcasing new software architecture and developer tools.",
            category = TemplateCategory.PRESENTATIONS,
            defaultAspectRatio = VideoAspectRatio.LANDSCAPE_16_9,
            palette = palettes[0], // Cyber Neon
            suggestedMusicMood = "Deep Electronic Minimalist",
            scenes = listOf(
                VideoScene(
                    id = "ptk_s1",
                    title = "Introducing Nova Platform",
                    subtitle = "The cloud runtime designed for ultra-low latency compute.",
                    badge = "Version 3.0",
                    caption = "Global keynote address",
                    durationSeconds = 4.0f,
                    animation = AnimationStyle.ZOOM_KEN_BURNS,
                    graphicStyle = GraphicStyle.NEON_HEXAGONS,
                    placeholderGraphicDesc = "Luminous honeycomb array symbolizing modular microservices mesh"
                ),
                VideoScene(
                    id = "ptk_s2",
                    title = "Sub-Millisecond Processing",
                    subtitle = "Benchmarked against leading distributed database engines worldwide.",
                    badge = "Speed Benchmark",
                    caption = "Zero cold starts across edge nodes",
                    durationSeconds = 4.0f,
                    animation = AnimationStyle.GLITCH_SHIFT,
                    graphicStyle = GraphicStyle.METRICS_CHART,
                    placeholderGraphicDesc = "High-contrast speed comparator graph with highlighted performance delta"
                ),
                VideoScene(
                    id = "ptk_s3",
                    title = "Deploy Anywhere in Seconds",
                    subtitle = "Open source SDK and turnkey CLI integrations available today.",
                    badge = "Developer Release",
                    caption = "Read documentation at dev.nova.io",
                    durationSeconds = 3.8f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.TECH_GRID,
                    placeholderGraphicDesc = "Perspective grid transitioning into terminal command icon highlight"
                )
            )
        ),

        // PERSONAL GREETINGS TEMPLATES
        VideoTemplate(
            id = "greet_birthday_celebration",
            title = "Joyful Birthday Celebration",
            description = "Festive and colorful greeting card video to celebrate milestone birthdays.",
            category = TemplateCategory.GREETINGS,
            defaultAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
            palette = palettes[4], // Celebration Warm
            suggestedMusicMood = "Joyful Acoustic Celebration",
            scenes = listOf(
                VideoScene(
                    id = "gbc_s1",
                    title = "Happy Birthday",
                    subtitle = "Wishing you a year filled with grand adventures and boundless joy.",
                    badge = "Special Day",
                    caption = "Cheers to your celebration",
                    durationSeconds = 3.6f,
                    animation = AnimationStyle.KINETIC_BOUNCE,
                    graphicStyle = GraphicStyle.CELEBRATION_BURST,
                    placeholderGraphicDesc = "Multicolor radiant confetti beams and celebratory sparkle bursts"
                ),
                VideoScene(
                    id = "gbc_s2",
                    title = "Cherished Memories",
                    subtitle = "Thank you for the laughter, warmth, and inspiration you bring to everyone.",
                    badge = "Milestone Moments",
                    caption = "Reflecting on great times together",
                    durationSeconds = 3.8f,
                    animation = AnimationStyle.ZOOM_KEN_BURNS,
                    graphicStyle = GraphicStyle.AURORA_GRADIENT,
                    placeholderGraphicDesc = "Soft warm golden aura with floating light particles"
                ),
                VideoScene(
                    id = "gbc_s3",
                    title = "Here is to What Comes Next",
                    subtitle = "May every dream you pursue this year turn into reality.",
                    badge = "Best Wishes Always",
                    caption = "With love and warmest congratulations",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Heartwarming concentric golden rings centering closing blessing"
                )
            )
        ),
        VideoTemplate(
            id = "greet_wedding_invitation",
            title = "Elegant Wedding Invitation",
            description = "Romantic and timeless video card for wedding announcements and Save The Date invites.",
            category = TemplateCategory.GREETINGS,
            defaultAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
            palette = palettes[1], // Sunset Glow
            suggestedMusicMood = "Cinematic Strings & Romantic Harp",
            scenes = listOf(
                VideoScene(
                    id = "gwi_s1",
                    title = "Save the Date",
                    subtitle = "We invite you to share our happiness and witness our union.",
                    badge = "Love Story",
                    caption = "Saturday, September 18th",
                    durationSeconds = 3.8f,
                    animation = AnimationStyle.ZOOM_KEN_BURNS,
                    graphicStyle = GraphicStyle.AURORA_GRADIENT,
                    placeholderGraphicDesc = "Luminous rose-gold ambient gradient bloom with subtle dust motes"
                ),
                VideoScene(
                    id = "gwi_s2",
                    title = "Ceremony and Reception",
                    subtitle = "Rosewood Estate Gardens followed by dinner, music, and dancing under the stars.",
                    badge = "Event Schedule",
                    caption = "Four o'clock in the afternoon",
                    durationSeconds = 4.0f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.ABSTRACT_SPHERE,
                    placeholderGraphicDesc = "Intertwined double golden wedding ring geometry softly rotating"
                ),
                VideoScene(
                    id = "gwi_s3",
                    title = "RSVP Details",
                    subtitle = "Kindly confirm your presence by August 1st via our wedding page.",
                    badge = "Joyfully Invited",
                    caption = "We cannot wait to celebrate together",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Ornate concentric laurel border framing RSVP web address"
                )
            )
        ),
        VideoTemplate(
            id = "greet_thank_you_gratitude",
            title = "Sincere Thank You Message",
            description = "Warm personal appreciation video for friends, mentors, colleagues, or community supporters.",
            category = TemplateCategory.GREETINGS,
            defaultAspectRatio = VideoAspectRatio.SQUARE_1_1,
            palette = palettes[3], // Emerald Nature
            suggestedMusicMood = "Gentle Acoustic Guitar",
            scenes = listOf(
                VideoScene(
                    id = "gty_s1",
                    title = "Heartfelt Gratitude",
                    subtitle = "Your kindness, guidance, and generous support made all the difference.",
                    badge = "Thank You",
                    caption = "Deeply appreciated",
                    durationSeconds = 3.6f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.DYNAMIC_WAVE,
                    placeholderGraphicDesc = "Calm organic botanical sine waves flowing across lower third"
                ),
                VideoScene(
                    id = "gty_s2",
                    title = "Every Effort Counts",
                    subtitle = "We could not have accomplished this milestone without your dedication.",
                    badge = "Community Strength",
                    caption = "Proud to collaborate with you",
                    durationSeconds = 3.8f,
                    animation = AnimationStyle.ZOOM_KEN_BURNS,
                    graphicStyle = GraphicStyle.AURORA_GRADIENT,
                    placeholderGraphicDesc = "Emerald green ambient light pool highlighting central gratitude statement"
                ),
                VideoScene(
                    id = "gty_s3",
                    title = "Wishing You Continued Success",
                    subtitle = "Looking forward to our next chapter together.",
                    badge = "Warmest Regards",
                    caption = "From all of us",
                    durationSeconds = 3.2f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Symmetrical badge frame centering signature sign-off"
                )
            )
        ),

        // MARKETING & TECH TEMPLATES
        VideoTemplate(
            id = "mkt_podcast_teaser",
            title = "Podcast Episode Teaser",
            description = "Square audio-visualizer template for podcast clips, interview highlights, and episode drops.",
            category = TemplateCategory.MARKETING,
            defaultAspectRatio = VideoAspectRatio.SQUARE_1_1,
            palette = palettes[0], // Cyber Neon
            suggestedMusicMood = "Podcast Intro Jingle & Voice Track",
            scenes = listOf(
                VideoScene(
                    id = "mpt_s1",
                    title = "Episode 42 Drops Now",
                    subtitle = "Unpacking the next ten years of artificial intelligence with Dr. Marcus Vance.",
                    badge = "New Release",
                    caption = "Available on all streaming platforms",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.DYNAMIC_WAVE,
                    placeholderGraphicDesc = "Animated sound frequency visualizer bars dancing in center frame"
                ),
                VideoScene(
                    id = "mpt_s2",
                    title = "The Quote of the Week",
                    subtitle = "The question is not if automation arrives, but what humans choose to create next.",
                    badge = "Soundbite",
                    caption = "Minute 24:15 in the conversation",
                    durationSeconds = 4.0f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.NEON_HEXAGONS,
                    placeholderGraphicDesc = "Floating neon audio waveform crests illuminating quotation text"
                ),
                VideoScene(
                    id = "mpt_s3",
                    title = "Listen Everywhere",
                    subtitle = "Subscribe and leave a five-star review on Apple Podcasts and Spotify.",
                    badge = "Stream Now",
                    caption = "Search The Future Dialogue Podcast",
                    durationSeconds = 3.2f,
                    animation = AnimationStyle.KINETIC_BOUNCE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Circular microphone badge surrounded by active broadcast ripples"
                )
            )
        ),
        VideoTemplate(
            id = "tech_course_explainer",
            title = "Educational Micro-Lesson",
            description = "Widescreen explainer template for course teasers, tutorial intros, and bite-sized learning.",
            category = TemplateCategory.TECH,
            defaultAspectRatio = VideoAspectRatio.LANDSCAPE_16_9,
            palette = palettes[2], // Corporate Slate
            suggestedMusicMood = "Clean Ambient Learning Pulse",
            scenes = listOf(
                VideoScene(
                    id = "tce_s1",
                    title = "Master Modern State Management",
                    subtitle = "Step-by-step principles for reactive user interfaces in Android Kotlin.",
                    badge = "Lesson 01",
                    caption = "Beginner to Advanced Track",
                    durationSeconds = 3.8f,
                    animation = AnimationStyle.PAN_SIDEWAYS,
                    graphicStyle = GraphicStyle.TECH_GRID,
                    placeholderGraphicDesc = "Clean architectural blueprint showing StateFlow data streams"
                ),
                VideoScene(
                    id = "tce_s2",
                    title = "Unidirectional Data Flow",
                    subtitle = "Events flow up. State flows down. Predictable screens at every lifecycle phase.",
                    badge = "Core Principle",
                    caption = "Avoid shared mutable state pitfalls",
                    durationSeconds = 4.2f,
                    animation = AnimationStyle.SLIDE_UP,
                    graphicStyle = GraphicStyle.METRICS_CHART,
                    placeholderGraphicDesc = "Looping flowchart diagram connecting ViewModel to Compose UI tree"
                ),
                VideoScene(
                    id = "tce_s3",
                    title = "Build Your First Project",
                    subtitle = "Download starter repository and follow the interactive coding walkthrough.",
                    badge = "Hands-On Lab",
                    caption = "Full source code and exercises included",
                    durationSeconds = 3.5f,
                    animation = AnimationStyle.PULSE_SCALE,
                    graphicStyle = GraphicStyle.CONCENTRIC_RINGS,
                    placeholderGraphicDesc = "Interactive code bracket insignia with pulsing completion halo"
                )
            )
        )
    )
}
