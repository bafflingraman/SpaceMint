package com.spacemint.app

data class Fact(
    val emoji: String,
    val category: String,
    val fact: String
)

object FactsData {

    val facts = listOf(

        // ── UNIVERSE ──────────────────────────────────
        Fact("🌌", "Universe", "There are more stars in the universe than grains of sand on all of Earth's beaches combined."),
        Fact("🕳", "Universe", "A black hole called TON 618 is 66 billion times the mass of our Sun. Our entire solar system would fit inside it like a grain of sand in a swimming pool."),
        Fact("⚡", "Universe", "Lightning on Jupiter is 3 times more powerful than lightning on Earth — and the storms that create it are wider than our entire planet."),
        Fact("🌞", "Universe", "The Sun is so large that 1.3 million Earths could fit inside it. Yet it is considered a medium-sized star — some stars are 1,700 times bigger."),
        Fact("🌑", "Universe", "There is a planet called 55 Cancri e where it literally rains diamonds. The surface temperature is around 2,400°C."),
        Fact("🚀", "Universe", "If you could drive a car to the Moon at highway speed it would take about 6 months. To the Sun — 177 years."),
        Fact("🌀", "Universe", "The Milky Way galaxy is 100,000 light years wide. Even at the speed of light it would take 100,000 years to cross it."),
        Fact("❄", "Universe", "Space is not completely empty. It has about 1 atom per cubic meter — an almost perfect vacuum, but not zero."),
        Fact("🪐", "Universe", "Saturn's rings are only about 10 metres thick on average — but they are 282,000 km wide. Thinner than a piece of paper scaled to the planet."),
        Fact("💥", "Universe", "The Big Bang happened 13.8 billion years ago. If that were compressed into one year, humans appeared at 11:59 PM on December 31st."),
        Fact("🌍", "Universe", "The observable universe is 93 billion light years across — but the actual universe may be infinitely larger than what we can see."),
        Fact("🔭", "Universe", "When you look at a star, you are looking into the past. The light from distant stars left them thousands of years ago. Some stars you see tonight may no longer exist."),

        // ── ANIMALS ───────────────────────────────────
        Fact("🐙", "Animals", "Octopuses have three hearts, blue blood, and nine brains — one central brain and one in each of their eight arms."),
        Fact("🦒", "Animals", "A giraffe's tongue is 45–50 cm long and is dark bluish-purple in colour — believed to protect it from sunburn while eating."),
        Fact("🐘", "Animals", "Elephants are the only animals known to have death rituals. They mourn their dead, return to the bones of deceased relatives, and have been seen crying."),
        Fact("🦈", "Animals", "Sharks are older than trees. Sharks have existed for 450 million years. Trees only appeared around 360 million years ago."),
        Fact("🐬", "Animals", "Dolphins have names for each other. They use unique whistle signatures to identify individuals — essentially calling each other by name."),
        Fact("🦦", "Animals", "Sea otters hold hands while sleeping so they don't drift apart. This is called a 'raft'."),
        Fact("🐜", "Animals", "Ants never sleep. They have no sleep cycle. Instead they take hundreds of tiny power naps of about 1 minute throughout the day."),
        Fact("🦅", "Animals", "A mantis shrimp can punch with the force of a bullet. It can accelerate its punch at 10,000 g and shatter aquarium glass."),
        Fact("🐳", "Animals", "Blue whales have hearts the size of a small car. Their heartbeat can be detected from 2 miles away."),
        Fact("🦋", "Animals", "When a caterpillar becomes a butterfly, it completely dissolves into liquid inside the chrysalis before rebuilding itself from scratch."),
        Fact("🐧", "Animals", "Penguins propose to each other with pebbles. A male will search for the smoothest, most perfect pebble to present to a female."),
        Fact("🐊", "Animals", "Crocodiles cannot stick out their tongue. Their tongue is fused to the roof of their mouth."),
        Fact("🦜", "Animals", "African grey parrots can understand the concept of zero — a level of abstract thinking once thought to be uniquely human."),
        Fact("🐝", "Animals", "Bees can recognise human faces. They use the same technique as humans — processing faces as a whole rather than individual features."),
        Fact("🦁", "Animals", "Lions sleep up to 20 hours a day. They are among the laziest of all big cats, conserving energy for explosive hunting bursts."),
        Fact("🐠", "Animals", "Clownfish can change sex. All clownfish are born male. When the dominant female of a group dies, the dominant male changes sex to replace her."),
        Fact("🦟", "Animals", "Mosquitoes are the deadliest animal on Earth — responsible for over 700,000 human deaths per year through disease transmission."),
        Fact("🐋", "Animals", "Sperm whales sleep vertically, bobbing near the surface like giant logs. Scientists discovered this by accident when a boat sailed into a sleeping pod."),

        // ── GEOGRAPHY ─────────────────────────────────
        Fact("🗺", "Geography", "Canada has more lakes than all other countries combined. It has over 60% of the world's fresh water lakes."),
        Fact("🏔", "Geography", "Mount Everest is not the closest point to space. That is Mount Chimborazo in Ecuador, because Earth bulges at the equator."),
        Fact("🌊", "Geography", "The Pacific Ocean is larger than all of Earth's landmass combined. It covers more than 30% of the Earth's surface."),
        Fact("🏜", "Geography", "The Sahara Desert is smaller than Russia, Canada, the USA, China, and Brazil. It is large — but not the largest thing on Earth."),
        Fact("🧊", "Geography", "Antarctica is the world's largest desert. A desert is defined by low precipitation — Antarctica gets less rainfall than the Sahara."),
        Fact("🌋", "Geography", "Hawaii is moving toward Japan at about 10 cm per year — the same speed as your fingernails grow — due to tectonic plate movement."),
        Fact("🏞", "Geography", "The Amazon River discharges 20% of all the fresh water that flows into the world's oceans — more than the next seven largest rivers combined."),
        Fact("⛰", "Geography", "There are more pyramids in Sudan than in Egypt. Sudan has around 200–255 pyramids, Egypt has around 130."),
        Fact("🌐", "Geography", "Russia spans 11 time zones. When it is Monday morning in one end of Russia, it is already Tuesday morning at the other end."),
        Fact("🏝", "Geography", "Indonesia is an archipelago of over 17,000 islands — more than any other country in the world."),
        Fact("🗻", "Geography", "The Mariana Trench is deeper than Mount Everest is tall. If Everest were placed at the bottom, its peak would still be over 1 mile underwater."),
        Fact("💧", "Geography", "Lake Baikal in Russia contains about 20% of the world's unfrozen surface fresh water. It is the deepest lake on Earth at 1,642 metres."),
        Fact("🌵", "Geography", "The Atacama Desert in Chile is so dry that some weather stations there have never recorded rain. Parts of it may not have had rain in 500 years."),

        // ── HUMAN BODY ────────────────────────────────
        Fact("🧠", "Human Body", "Your brain generates about 23 watts of power when awake — enough to power a small LED lightbulb."),
        Fact("❤", "Human Body", "Your heart beats approximately 100,000 times a day — about 35 million times a year and 2.5 billion times in a lifetime."),
        Fact("🦴", "Human Body", "Babies are born with 270 bones. Adults have only 206. The extra bones fuse together during childhood."),
        Fact("👁", "Human Body", "The human eye can detect a single photon of light in complete darkness — it is sensitive enough to see a candle flame from 48 km away."),
        Fact("🧬", "Human Body", "If all the DNA in your body were uncoiled and stretched out, it would reach from Earth to the Sun and back — 600 times."),
        Fact("👃", "Human Body", "Humans can smell over 1 trillion different scents. For decades scientists thought we could only detect 10,000."),
        Fact("🦷", "Human Body", "Tooth enamel is the hardest substance in the human body — harder than steel. But unlike bone, it cannot repair itself."),
        Fact("🫁", "Human Body", "The surface area of human lungs, if spread flat, would cover a tennis court."),
        Fact("🩸", "Human Body", "Red blood cells complete a full circuit of the body in about 20 seconds. In one day they travel roughly 19,000 km."),
        Fact("🧪", "Human Body", "You share 60% of your DNA with a banana, 85% with a mouse, and 99% with a chimpanzee."),

        // ── HISTORY ───────────────────────────────────
        Fact("🏛", "History", "Cleopatra lived closer in time to the Moon landing than to the construction of the Great Pyramid of Giza."),
        Fact("⚔", "History", "The shortest war in history lasted 38–45 minutes. It was between Britain and Zanzibar in 1896."),
        Fact("🗽", "History", "Oxford University is older than the Aztec Empire. Teaching began at Oxford in 1096. The Aztec Empire was founded in 1428."),
        Fact("🦖", "History", "Woolly mammoths were still alive when the Great Pyramid of Giza was being built. They went extinct around 1650 BC — the pyramid was built around 2560 BC."),
        Fact("📜", "History", "Nintendo was founded in 1889 — when it made playing cards. It existed for nearly 90 years before making video games."),
        Fact("🌍", "History", "For most of human history, the idea of washing hands before surgery was considered absurd. The doctor who proposed it in 1847 was fired and ridiculed."),
        Fact("🏺", "History", "Ancient Egyptians used toothpaste made from ox hooves, ashes, burnt eggshells, and pumice stone."),
        Fact("🗼", "History", "The Eiffel Tower was originally intended to be a temporary structure — built for the 1889 World Fair and scheduled for demolition in 1909."),

        // ── SCIENCE ───────────────────────────────────
        Fact("⚛", "Science", "If you removed all the empty space from atoms in the human body, all 8 billion humans on Earth would fit into a sugar cube."),
        Fact("🔥", "Science", "Fire is not actually a substance — it is a chemical reaction. Flames have no mass and are not made of matter."),
        Fact("🌡", "Science", "Hot water can freeze faster than cold water under certain conditions. This is called the Mpemba effect and scientists still do not fully understand why."),
        Fact("💡", "Science", "A single bolt of lightning contains enough energy to toast 100,000 slices of bread."),
        Fact("🧲", "Science", "If two pieces of metal touch in outer space, they permanently weld together. This is called cold welding. There is no oxygen to form the oxide layer that prevents it on Earth."),
        Fact("🌈", "Science", "A rainbow is actually a full circle — you only see an arc because the ground gets in the way. Pilots sometimes see complete circular rainbows."),
        Fact("🫧", "Science", "Water doesn't actually make a bubbling sound — it is the air pockets collapsing inside the water that create the sound."),
        Fact("🧊", "Science", "Ice is less dense than liquid water, which is why it floats. If ice sank, oceans would freeze from the bottom up and nearly all aquatic life would die."),

        // ── INDIA ─────────────────────────────────────
        Fact("🇮🇳", "India", "India invented the number zero. The concept of zero as a number was first developed by mathematician Brahmagupta in 628 AD."),
        Fact("♟", "India", "Chess was invented in India around the 6th century AD. It was called 'Chaturanga' and spread to Persia, then to Europe."),
        Fact("🌙", "India", "India's Chandrayaan-1 discovered water on the Moon in 2008 — a finding that changed our understanding of lunar geology."),
        Fact("💰", "India", "India was the world's richest country for most of recorded history — accounting for over 25% of global GDP before British colonisation."),
        Fact("🏏", "India", "The game of Snakes and Ladders originated in India in the 13th century as a moral game called Moksha Patam about karma and ethics."),
        Fact("🌿", "India", "Yoga is over 5,000 years old. It originated in the Indus Valley Civilisation — among the oldest civilisations on Earth."),
    )

    fun getRandom(): Fact = facts.random()

    fun getByCategory(category: String): Fact =
        facts.filter { it.category == category }.random()
}