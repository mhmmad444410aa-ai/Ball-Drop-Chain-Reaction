package com.mhmmad.balldropchainreaction

object Texts {
    private val strings = mapOf(
        "title" to listOf("BALL DROP", "سقوط الكرات", "QUEDA DE BOLAS"),
        "subtitle" to listOf("CHAIN REACTION", "التفاعل المتسلسل", "REAÇÃO EM CADEIA"),
        "play" to listOf("PLAY", "العب الآن", "JOGAR"),
        "continue" to listOf("CONTINUE", "متابعة", "CONTINUAR"),
        "worlds" to listOf("WORLDS", "العوالم", "MUNDOS"),
        "levels" to listOf("LEVELS", "المراحل", "FASES"),
        "settings" to listOf("SETTINGS", "الإعدادات", "CONFIGURAÇÕES"),
        "about" to listOf("ABOUT", "حول اللعبة", "SOBRE"),
        "back" to listOf("BACK", "رجوع", "VOLTAR"),
        "run" to listOf("RUN", "تشغيل", "INICIAR"),
        "pause" to listOf("PAUSE", "إيقاف مؤقت", "PAUSAR"),
        "reset" to listOf("RESET", "إعادة", "REINICIAR"),
        "undo" to listOf("UNDO", "تراجع", "DESFAZER"),
        "hint" to listOf("HINT", "تلميح", "DICA"),
        "rotate" to listOf("ROTATE", "تدوير", "GIRAR"),
        "next" to listOf("NEXT LEVEL", "المرحلة التالية", "PRÓXIMA FASE"),
        "complete" to listOf("LEVEL COMPLETE!", "اكتملت المرحلة!", "FASE CONCLUÍDA!"),
        "failed" to listOf("TRY AGAIN", "حاول مجددًا", "TENTE DE NOVO"),
        "language" to listOf("Language", "اللغة", "Idioma"),
        "sound" to listOf("Sound effects", "المؤثرات الصوتية", "Efeitos sonoros"),
        "sound_note" to listOf("Sound effects only — no music", "مؤثرات فقط — دون موسيقى", "Apenas efeitos — sem música"),
        "objective" to listOf("Guide the ball to the glowing target", "وجّه الكرة نحو الهدف المضيء", "Leve a bola até o alvo brilhante"),
        "place" to listOf("Choose a tool, then tap or drag onto the board", "اختر أداة ثم المس اللوحة أو اسحب عليها", "Escolha uma peça e toque ou arraste para o tabuleiro"),
        "score" to listOf("Stars earned", "النجوم المكتسبة", "Estrelas ganhas"),
        "offline" to listOf("Physics puzzles • Offline play • No music", "ألغاز فيزيائية • دون إنترنت • دون موسيقى", "Enigmas físicos • Offline • Sem música"),
        "privacy" to listOf("Privacy policy URL must be configured before publishing. The current build has no ads or Internet permission.",
            "يجب إعداد رابط سياسة الخصوصية قبل النشر. هذه النسخة دون إعلانات أو صلاحية إنترنت.",
            "A política de privacidade deve ser configurada antes de publicar. Esta versão não tem anúncios nem permissão de Internet."),
        "about_text" to listOf("Original offline puzzle game. Build a machine, release a ball and watch real rigid-body interactions. Version 1.0.0 (development build).",
            "لعبة ألغاز أصلية تعمل دون إنترنت. ابنِ سلسلة آليات وشاهد حركة الأجسام وتفاعلها الفيزيائي. الإصدار التطويري 1.0.0.",
            "Jogo de física original e offline. Construa uma máquina e veja interações reais entre corpos. Versão de desenvolvimento 1.0.0."),
        "locked" to listOf("LOCKED", "مغلقة", "BLOQUEADA"),
        "tool" to listOf("TOOLS", "الأدوات", "FERRAMENTAS"),
        "finish" to listOf("Well done! The ball reached its target.", "أحسنت! وصلت الكرة إلى الهدف.", "Parabéns! A bola chegou ao alvo."),
        "try" to listOf("Reposition your tools and run the simulation again.", "غيّر أماكن الأدوات ثم أعد تشغيل المحاكاة.", "Reposicione as peças e tente novamente.")
    )
    private val worldNames = listOf(
        listOf("Gravity Basics","Domino Reactions","Spring Mechanics","Moving Machines","Chain Master"),
        listOf("أساسيات الجاذبية","تفاعلات الدومينو","آليات النوابض","الآلات المتحركة","سيد السلسلة"),
        listOf("Fundamentos da Gravidade","Reações de Dominó","Mecânica das Molas","Máquinas em Movimento","Mestre das Reações")
    )
    private val levelNames = listOf(
        listOf("First Fall","Bridge the Gap","Corner Drop","Double Slope","Domino Arrival","Topple Together","Domino Descent","Tipping Point","Bounce One","Spring Valley","High Bounce","Spring Timing","Lever Lift","Pendulum Push","Machine Crossing","Mechanical Loop","Grand Cascade","Chain Timing","Triple Mechanism","Chain Master"),
        listOf("السقوط الأول","عبور الفجوة","سقوط الزاوية","منحدران","وصول الدومينو","سقوط متتابع","انحدار الدومينو","نقطة الانقلاب","الارتداد الأول","وادي النوابض","ارتداد عالٍ","توقيت النابض","رفع الرافعة","دفعة البندول","عبور الآلة","الدائرة الميكانيكية","الشلال الكبير","توقيت السلسلة","الآلية الثلاثية","سيد التفاعل"),
        listOf("Primeira Queda","Ponte sobre a Lacuna","Queda na Curva","Rampa Dupla","Chegada do Dominó","Queda em Conjunto","Descida de Dominós","Ponto de Virada","Primeiro Salto","Vale das Molas","Salto Alto","Tempo da Mola","Alavanca","Impulso do Pêndulo","Travessia Mecânica","Circuito Mecânico","Grande Cascata","Sincronia em Cadeia","Mecanismo Triplo","Mestre da Cadeia")
    )
    private fun index(lang: String): Int = when(lang) {"ar"->1;"pt"->2;else->0}
    fun world(lang: String, id: Int): String = worldNames[index(lang)][id.coerceIn(0,4)]
    fun level(lang: String, id: Int): String = levelNames[index(lang)][(id-1).coerceIn(0,19)]
    fun get(lang: String, key: String): String {
        val i = when(lang){"ar" -> 1; "pt" -> 2; else -> 0}
        return strings[key]?.get(i) ?: key
    }
}
