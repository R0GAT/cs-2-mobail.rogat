package com.example.model

import androidx.compose.ui.graphics.Color

object CS2SkinCatalog {

    val KNIVES: List<WeaponSkin> = listOf(
        WeaponSkin(
            id = "knife_karambit_fade",
            name = "Karambit | Fade",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.KARAMBIT,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFFF2A85),
            secondaryColor = Color(0xFF7A00FF),
            accentColor = Color(0xFFFFD700),
            description = "Легендарний градієнт Fade з вигнутим лезом «кіготь тигра»"
        ),
        WeaponSkin(
            id = "knife_karambit_blue_gem",
            name = "Karambit | Case Hardened (Blue Gem #661)",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.KARAMBIT,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFF00C3FF),
            secondaryColor = Color(0xFF0051FF),
            accentColor = Color(0xFFFFD23F),
            description = "Рідкісний 100% Blue Gem паттерн небесно-блакитного загартування"
        ),
        WeaponSkin(
            id = "knife_karambit_crimson",
            name = "Karambit | Crimson Web",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.KARAMBIT,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFD60000),
            secondaryColor = Color(0xFF220000),
            accentColor = Color(0xFF111111),
            description = "Криваво-червоне матове покриття з візерунком павутини"
        ),
        WeaponSkin(
            id = "knife_karambit_doppler",
            name = "Karambit | Doppler (Phase 2 Pink Galaxy)",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.KARAMBIT,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFFF1493),
            secondaryColor = Color(0xFF4B0082),
            accentColor = Color(0xFF00F0FF),
            description = "Космічна галактика з яскравими рожево-фіолетовими спалахами"
        ),
        WeaponSkin(
            id = "knife_butterfly_fade",
            name = "Butterfly Knife | Fade",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.BUTTERFLY,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFFF3366),
            secondaryColor = Color(0xFF9933FF),
            accentColor = Color(0xFFFFCC00),
            description = "Балісонг з плавною анімацією обертання та градієнтом Fade"
        ),
        WeaponSkin(
            id = "knife_butterfly_marble_fade",
            name = "Butterfly Knife | Marble Fade (Fire & Ice)",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.BUTTERFLY,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFFF1E00),
            secondaryColor = Color(0xFF0077FE),
            accentColor = Color(0xFFFFE600),
            description = "Мармуровий градієнт полум'я та льоду"
        ),
        WeaponSkin(
            id = "knife_butterfly_lore",
            name = "Butterfly Knife | Lore",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.BUTTERFLY,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFFFD700),
            secondaryColor = Color(0xFF3B6829),
            accentColor = Color(0xFFF3E5AB),
            description = "Золоте гравіювання дракона в кельтському стилі"
        ),
        WeaponSkin(
            id = "knife_m9_autotronic",
            name = "M9 Bayonet | Autotronic",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.M9_BAYONET,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFFF2222),
            secondaryColor = Color(0xFFDCDCDC),
            accentColor = Color(0xFF222222),
            description = "Важкий штик-ніж зі сталевою пилкою та червоною сіткою"
        ),
        WeaponSkin(
            id = "knife_m9_doppler_sapphire",
            name = "M9 Bayonet | Doppler (Sapphire)",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.M9_BAYONET,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFF0055FF),
            secondaryColor = Color(0xFF00C8FF),
            accentColor = Color(0xFF002288),
            description = "Найчистіший сапфіровий кристал з дзеркальним відблиском"
        ),
        WeaponSkin(
            id = "knife_skeleton_fade",
            name = "Skeleton Knife | Fade",
            weaponType = WeaponType.KNIFE,
            knifeType = KnifeType.SKELETON,
            rarity = SkinRarity.EXTRAORDINARY,
            primaryColor = Color(0xFFFF007F),
            secondaryColor = Color(0xFF7B1FA2),
            accentColor = Color(0xFFFFB300),
            description = "Легкий скелетний клинок з отвором під палець для трюків"
        )
    )

    val RIFLES: List<WeaponSkin> = listOf(
        WeaponSkin(
            id = "ak47_case_hardened",
            name = "AK-47 | Case Hardened (Blue Gem)",
            weaponType = WeaponType.AK47,
            rarity = SkinRarity.CLASSIFIED,
            primaryColor = Color(0xFF00B4D8),
            secondaryColor = Color(0xFF0077B6),
            accentColor = Color(0xFFFFB703),
            description = "Загартований у вогні автомат з рідкісним синім верхом ствольної коробки"
        ),
        WeaponSkin(
            id = "ak47_vulcan",
            name = "AK-47 | Vulcan",
            weaponType = WeaponType.AK47,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFF00F0FF),
            secondaryColor = Color(0xFF1E232A),
            accentColor = Color(0xFFFFFFFF),
            description = "Спортивний високотехнологічний дизайн у блакитно-білих тонах"
        ),
        WeaponSkin(
            id = "ak47_asiimov",
            name = "AK-47 | Asiimov",
            weaponType = WeaponType.AK47,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFFFF6600),
            secondaryColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFF1C1C1C),
            description = "Футуристичний науково-фантастичний стиль за мотивами Азімова"
        ),
        WeaponSkin(
            id = "ak47_fire_serpent",
            name = "AK-47 | Fire Serpent",
            weaponType = WeaponType.AK47,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFF38B000),
            secondaryColor = Color(0xFF5A4D41),
            accentColor = Color(0xFFFF5400),
            description = "Священний змій майя, що вивергає яскраві язики полум'я"
        ),
        WeaponSkin(
            id = "m4a4_howl",
            name = "M4A4 | Howl (Виття)",
            weaponType = WeaponType.M4A4,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFFFF1A1A),
            secondaryColor = Color(0xFF8B0000),
            accentColor = Color(0xFFFF8C00),
            description = "Єдиний у своєму роді контрабандний скін із палаючим вовком"
        ),
        WeaponSkin(
            id = "m4a1s_printstream",
            name = "M4A1-S | Printstream",
            weaponType = WeaponType.M4A4,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFFF8F9FA),
            secondaryColor = Color(0xFF111111),
            accentColor = Color(0xFF00F5D4),
            description = "Перламутрове біле покриття з переливними голографічними написами"
        )
    )

    val SNIPERS: List<WeaponSkin> = listOf(
        WeaponSkin(
            id = "awp_dragon_lore",
            name = "AWP | Dragon Lore (Історія про дракона)",
            weaponType = WeaponType.AWP,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFFFFC300),
            secondaryColor = Color(0xFFD00000),
            accentColor = Color(0xFF38B000),
            description = "Найвідоміша та найдорожча гвинтівка з вогнедишним драконом"
        ),
        WeaponSkin(
            id = "awp_asiimov",
            name = "AWP | Asiimov",
            weaponType = WeaponType.AWP,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFFFF6B00),
            secondaryColor = Color(0xFFFFFFFF),
            accentColor = Color(0xFF262626),
            description = "Чистий білий корпус з помаранчевими та чорними геометричними лініями"
        ),
        WeaponSkin(
            id = "awp_gungnir",
            name = "AWP | Gungnir",
            weaponType = WeaponType.AWP,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF003F88),
            accentColor = Color(0xFFFDF0D5),
            description = "Легендарний спис Одіна вкритий крижаними скандинавськими рунами"
        )
    )

    val PISTOLS: List<WeaponSkin> = listOf(
        WeaponSkin(
            id = "deagle_blaze",
            name = "Desert Eagle | Blaze (Полум'я)",
            weaponType = WeaponType.DEAGLE,
            rarity = SkinRarity.RESTRICTED,
            primaryColor = Color(0xFFFF4500),
            secondaryColor = Color(0xFF181818),
            accentColor = Color(0xFFFFD700),
            description = "Матово-чорний пістолет з палаючими язиками вогню на стволі"
        ),
        WeaponSkin(
            id = "deagle_printstream",
            name = "Desert Eagle | Printstream",
            weaponType = WeaponType.DEAGLE,
            rarity = SkinRarity.COVERT,
            primaryColor = Color(0xFFFFFFFF),
            secondaryColor = Color(0xFF1A1A1A),
            accentColor = Color(0xFFE040FB),
            description = "Монохромна перламутрова поверхня з геометричними символами"
        )
    )

    val ALL_SKINS: List<WeaponSkin> = KNIVES + RIFLES + SNIPERS + PISTOLS

    val CASES: List<CS2Case> = listOf(
        CS2Case(
            id = "case_csgo_weapon",
            name = "CS:GO Weapon Case #1",
            price = 500,
            badgeColor = Color(0xFFFF9900),
            items = listOf(
                KNIVES[0], // Karambit Fade
                KNIVES[1], // Karambit Blue Gem
                RIFLES[0], // AK47 Case Hardened
                SNIPERS[0], // AWP Dragon Lore
                PISTOLS[0] // Deagle Blaze
            )
        ),
        CS2Case(
            id = "case_dreams_nightmares",
            name = "Dreams & Nightmares Case",
            price = 350,
            badgeColor = Color(0xFF9933FF),
            items = listOf(
                KNIVES[4], // Butterfly Fade
                KNIVES[6], // Butterfly Lore
                RIFLES[1], // AK47 Vulcan
                SNIPERS[1], // AWP Asiimov
                PISTOLS[1] // Deagle Printstream
            )
        ),
        CS2Case(
            id = "case_revolution",
            name = "Revolution Case",
            price = 300,
            badgeColor = Color(0xFFFF0066),
            items = listOf(
                KNIVES[7], // M9 Autotronic
                KNIVES[8], // M9 Sapphire
                RIFLES[4], // M4A4 Howl
                RIFLES[5], // M4A1-S Printstream
                SNIPERS[2] // AWP Gungnir
            )
        )
    )
}
