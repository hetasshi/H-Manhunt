package me.matistan05.minecraftmanhunt.managers;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.generator.structure.Structure;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.loot.LootTable;
import org.bukkit.util.Vector;
import org.bukkit.util.StructureSearchResult;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class MatchWorldEvaluator {
    private final Main main;

    public MatchWorldEvaluator(Main main) {
        this.main = main;
    }

    public MatchWorldCandidate evaluate(World world, long seed) {
        Location spawn = world.getSpawnLocation().clone();
        StringBuilder summary = new StringBuilder("seed=").append(seed);

        int score = scoreSpawnBiome(world, spawn, summary);
        score += scoreImmediateSpawnSafety(world, spawn, summary);
        score += scoreWoodAvailability(world, spawn, summary);
        score += scoreRelief(world, spawn, summary);

        StructureData village = locateVillage(world, spawn, summary);
        StructureData ruinedPortal = locateRuinedPortal(world, spawn, summary);
        StructureData shipwreck = locateShipwreck(world, spawn, summary);
        StructureData rareStructure = locateRareStructure(world, spawn, summary);
        score += village.score() + ruinedPortal.score() + shipwreck.score() + rareStructure.score();

        StructureData anchor = pickRandomAnchor(village, ruinedPortal, shipwreck, rareStructure);
        Location startLocation = findStartLocation(world, spawn, anchor);
        String runnerHintMessage = buildRunnerHint(world, startLocation, anchor);

        return new MatchWorldCandidate(world, startLocation, seed, score, summary.toString(), runnerHintMessage);
    }

    private int scoreSpawnBiome(World world, Location spawn, StringBuilder summary) {
        Biome biome = world.getBiome(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
        String biomeKey = biome.getKey().getKey().toLowerCase(Locale.ROOT);
        int score;
        if (biomeKey.contains("plains") || biomeKey.contains("forest") || biomeKey.contains("savanna")) {
            score = 25;
        } else if (biomeKey.contains("jungle") || biomeKey.contains("taiga") || biomeKey.contains("cherry")) {
            score = 18;
        } else if (biomeKey.contains("desert") || biomeKey.contains("badlands")) {
            score = 8;
        } else if (biomeKey.contains("ocean") || biomeKey.contains("river")) {
            score = -25;
        } else {
            score = 10;
        }
        summary.append(", biome=").append(biome.getKey().getKey()).append("(").append(score).append(")");
        return score;
    }

    private int scoreImmediateSpawnSafety(World world, Location spawn, StringBuilder summary) {
        Location safeSurface = findNearestSafeSurface(world, spawn, 12);
        if (safeSurface == null) {
            summary.append(", safe_spawn=missing(-15)");
            return -15;
        }

        int verticalShift = Math.abs(safeSurface.getBlockY() - spawn.getBlockY());
        int score = Math.max(4, 16 - verticalShift);
        summary.append(", safe_spawn=").append(verticalShift).append("(").append(score).append(")");
        return score;
    }

    private int scoreWoodAvailability(World world, Location spawn, StringBuilder summary) {
        int logs = 0;
        int radius = 16;
        int originX = spawn.getBlockX();
        int originZ = spawn.getBlockZ();
        for (int x = -radius; x <= radius; x += 4) {
            for (int z = -radius; z <= radius; z += 4) {
                int blockX = originX + x;
                int blockZ = originZ + z;
                if (!world.isChunkLoaded(blockX >> 4, blockZ >> 4)) {
                    continue;
                }

                int y = world.getHighestBlockYAt(blockX, blockZ);
                for (int sampleY = Math.max(world.getMinHeight(), y - 6); sampleY <= y; sampleY++) {
                    String blockName = world.getBlockAt(blockX, sampleY, blockZ).getType().getKey().getKey();
                    if (blockName.endsWith("_log") || blockName.endsWith("_wood")) {
                        logs++;
                        break;
                    }
                }
            }
        }
        int score = Math.min(20, logs * 2);
        summary.append(", wood=").append(logs).append("(").append(score).append(")");
        return score;
    }

    private int scoreRelief(World world, Location spawn, StringBuilder summary) {
        int centerX = spawn.getBlockX();
        int centerZ = spawn.getBlockZ();
        if (!world.isChunkLoaded(centerX >> 4, centerZ >> 4)) {
            summary.append(", relief=unknown(0)");
            return 0;
        }

        int centerY = world.getHighestBlockYAt(centerX, centerZ);
        int maxDiff = 0;
        int radius = 32;
        for (int x = -radius; x <= radius; x += 8) {
            for (int z = -radius; z <= radius; z += 8) {
                int sampleX = centerX + x;
                int sampleZ = centerZ + z;
                if (!world.isChunkLoaded(sampleX >> 4, sampleZ >> 4)) {
                    continue;
                }

                int sampleY = world.getHighestBlockYAt(sampleX, sampleZ);
                maxDiff = Math.max(maxDiff, Math.abs(sampleY - centerY));
            }
        }
        int score = Math.min(15, maxDiff / 2);
        summary.append(", relief=").append(maxDiff).append("(").append(score).append(")");
        return score;
    }

    private StructureData locateVillage(World world, Location spawn, StringBuilder summary) {
        Structure[] villages = {
                Structure.VILLAGE_PLAINS,
                Structure.VILLAGE_DESERT,
                Structure.VILLAGE_SAVANNA,
                Structure.VILLAGE_SNOWY,
                Structure.VILLAGE_TAIGA
        };

        int radiusChunks = Math.max(8,
                main.getConfig().getInt("matchWorlds.autoGenerate.structureSearchRadiusChunks", 18));
        boolean preferVillageWithSmithBuilding = main.getConfig().getBoolean(
                "matchWorlds.autoGenerate.preferVillageWithSmithBuilding", true);
        boolean requireVillageWithSmithBuilding = main.getConfig().getBoolean(
                "matchWorlds.autoGenerate.requireVillageWithSmithBuilding", false);
        int smithSearchRadiusBlocks = Math.max(24, main.getConfig().getInt(
                "matchWorlds.autoGenerate.villageSmithSearchRadiusBlocks", 64));
        StructureData bestVillage = null;
        StructureData bestSmithVillage = null;
        for (Structure villageStructure : villages) {
            StructureSearchResult result = world.locateNearestStructure(spawn, villageStructure, radiusChunks, false);
            if (result == null) {
                continue;
            }

            int distance = (int) spawn.distance(result.getLocation());
            int score = Math.max(0, 70 - (distance / 12));
            SmithBuildingData smithBuilding = findVillageSmithBuilding(world, result.getLocation(),
                    smithSearchRadiusBlocks);
            score += smithBuilding.scoreBonus();

            StructureData current = new StructureData(
                    "village",
                    result.getLocation(),
                    distance,
                    score,
                    smithBuilding.structureLabel());
            if (bestVillage == null || current.score() > bestVillage.score()) {
                bestVillage = current;
            }
            if (smithBuilding.present() && (bestSmithVillage == null || current.score() > bestSmithVillage.score())) {
                bestSmithVillage = current;
            }
        }

        StructureData selectedVillage = requireVillageWithSmithBuilding && bestSmithVillage != null
                ? bestSmithVillage
                : preferVillageWithSmithBuilding && bestSmithVillage != null
                        ? bestSmithVillage
                        : bestVillage;

        if (selectedVillage == null) {
            summary.append(", village=none");
            return new StructureData("village", null, Integer.MIN_VALUE, 0);
        }

        if (selectedVillage.smithStructureLabel() == null && requireVillageWithSmithBuilding) {
            summary.append(", village=no_smith_building");
            return new StructureData("village", null, Integer.MIN_VALUE, 0);
        }

        summary.append(", village=").append(selectedVillage.distanceBlocks()).append("b(")
                .append(selectedVillage.score()).append(")");
        if (selectedVillage.smithStructureLabel() != null) {
            summary.append(", smith=").append(selectedVillage.smithStructureLabel());
        } else {
            summary.append(", smith=none");
        }
        return selectedVillage;
    }

    private StructureData locateRuinedPortal(World world, Location spawn, StringBuilder summary) {
        int radiusChunks = Math.max(8,
                main.getConfig().getInt("matchWorlds.autoGenerate.structureSearchRadiusChunks", 18));
        StructureSearchResult result = world.locateNearestStructure(spawn, StructureType.RUINED_PORTAL, radiusChunks,
                false);
        if (result == null) {
            summary.append(", ruined_portal=none");
            return new StructureData("ruined_portal", null, Integer.MIN_VALUE, 0);
        }

        int distance = (int) spawn.distance(result.getLocation());
        int score = Math.max(0, 56 - (distance / 12));
        summary.append(", ruined_portal=").append(distance).append("b(").append(score).append(")");
        return new StructureData("ruined_portal", result.getLocation(), distance, score);
    }

    private StructureData locateShipwreck(World world, Location spawn, StringBuilder summary) {
        int radiusChunks = Math.max(8,
                main.getConfig().getInt("matchWorlds.autoGenerate.structureSearchRadiusChunks", 18));
        StructureData bestShipwreck = null;
        Structure[] shipwreckStructures = {
                Structure.SHIPWRECK,
                Structure.SHIPWRECK_BEACHED
        };

        for (Structure shipwreckStructure : shipwreckStructures) {
            StructureSearchResult result = world.locateNearestStructure(spawn, shipwreckStructure, radiusChunks, false);
            if (result == null) {
                continue;
            }

            int distance = (int) spawn.distance(result.getLocation());
            int score = Math.max(0, 40 - (distance / 14));
            String structureKey = shipwreckStructure == Structure.SHIPWRECK_BEACHED
                    ? "beached_shipwreck"
                    : "shipwreck";
            StructureData current = new StructureData(structureKey, result.getLocation(), distance, score);
            if (bestShipwreck == null || current.score() > bestShipwreck.score()) {
                bestShipwreck = current;
            }
        }

        if (bestShipwreck == null) {
            summary.append(", shipwreck=none");
            return new StructureData("shipwreck", null, Integer.MIN_VALUE, 0);
        }

        summary.append(", shipwreck=").append(bestShipwreck.distanceBlocks()).append("b(")
                .append(bestShipwreck.score()).append(")");
        return bestShipwreck;
    }

    private StructureData locateRareStructure(World world, Location spawn, StringBuilder summary) {
        int radiusChunks = Math.max(16,
                main.getConfig().getInt("matchWorlds.autoGenerate.rareStructureSearchRadiusChunks", 32));
        StructureData mansion = locateRareCandidate(
                world,
                spawn,
                Structure.MANSION,
                "woodland_mansion",
                24,
                28,
                radiusChunks);
        StructureData outpost = locateRareCandidate(
                world,
                spawn,
                Structure.PILLAGER_OUTPOST,
                "pillager_outpost",
                18,
                24,
                radiusChunks);

        StructureData bestRare = pickBestScoredStructure(mansion, outpost);
        if (bestRare == null || bestRare.location() == null) {
            summary.append(", rare=none");
            return new StructureData("rare", null, Integer.MIN_VALUE, 0);
        }

        summary.append(", rare=").append(bestRare.structureKey()).append(":")
                .append(bestRare.distanceBlocks()).append("b(").append(bestRare.score()).append(")");
        return bestRare;
    }

    private StructureData locateRareCandidate(World world, Location spawn, Structure structure, String key,
            int maxScore, int divisor, int radiusChunks) {
        StructureSearchResult result = world.locateNearestStructure(spawn, structure, radiusChunks, false);
        if (result == null) {
            return new StructureData(key, null, Integer.MIN_VALUE, 0);
        }

        int distance = (int) spawn.distance(result.getLocation());
        int score = Math.max(0, maxScore - (distance / divisor));
        return new StructureData(key, result.getLocation(), distance, score);
    }

    private StructureData pickBestScoredStructure(StructureData... anchors) {
        StructureData best = null;
        for (StructureData anchor : anchors) {
            if (anchor.location() == null) {
                continue;
            }
            if (best == null || anchor.score() > best.score()) {
                best = anchor;
            }
        }
        return best;
    }

    private StructureData pickRandomAnchor(StructureData... anchors) {
        int totalWeight = 0;
        for (StructureData anchor : anchors) {
            if (anchor.location() == null) {
                continue;
            }
            totalWeight += calculateAnchorWeight(anchor);
        }

        if (totalWeight <= 0) {
            return pickBestScoredStructure(anchors);
        }

        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int current = 0;
        for (StructureData anchor : anchors) {
            if (anchor.location() == null) {
                continue;
            }

            current += calculateAnchorWeight(anchor);
            if (roll < current) {
                return anchor;
            }
        }

        return pickBestScoredStructure(anchors);
    }

    private int calculateAnchorWeight(StructureData anchor) {
        int baseWeight = switch (anchor.structureKey()) {
            case "village" ->
                Math.max(1, main.getConfig().getInt("matchWorlds.autoGenerate.anchorWeights.village", 100));
            case "ruined_portal" ->
                Math.max(1, main.getConfig().getInt("matchWorlds.autoGenerate.anchorWeights.ruinedPortal", 70));
            case "shipwreck", "beached_shipwreck" ->
                Math.max(1, main.getConfig().getInt("matchWorlds.autoGenerate.anchorWeights.shipwreck", 45));
            case "pillager_outpost" ->
                Math.max(1, main.getConfig().getInt("matchWorlds.autoGenerate.anchorWeights.pillagerOutpost", 18));
            case "woodland_mansion" ->
                Math.max(1, main.getConfig().getInt("matchWorlds.autoGenerate.anchorWeights.woodlandMansion", 10));
            default -> 1;
        };

        int scoreContribution = Math.max(0, anchor.score());
        return Math.max(1, baseWeight + scoreContribution);
    }

    private Location findStartLocation(World world, Location naturalSpawn, StructureData anchor) {
        if (anchor == null || anchor.location() == null) {
            Location safeLocation = findNearestSafeSurface(world, naturalSpawn, 20);
            return safeLocation == null ? naturalSpawn.toCenterLocation() : safeLocation.toCenterLocation();
        }

        int desiredDistance = Math.max(40,
                main.getConfig().getInt("matchWorlds.autoGenerate.startDistanceFromAnchor", 100));
        Location anchorLocation = anchor.location().clone();
        Vector direction = naturalSpawn.toVector().subtract(anchorLocation.toVector()).setY(0);
        if (direction.lengthSquared() < 1.0) {
            direction = new Vector(1, 0, 0);
        }

        Location candidate = anchorLocation.clone().add(direction.normalize().multiply(desiredDistance));
        Location safeLocation = findNearestSafeSurface(world, candidate, 24);
        return safeLocation == null ? naturalSpawn.toCenterLocation() : safeLocation.toCenterLocation();
    }

    private Location findNearestSafeSurface(World world, Location candidate, int spread) {
        int[][] offsets = {
                { 0, 0 }, { spread, 0 }, { -spread, 0 }, { 0, spread }, { 0, -spread },
                { spread, spread }, { -spread, spread }, { spread, -spread }, { -spread, -spread }
        };
        for (int[] offset : offsets) {
            int x = candidate.getBlockX() + offset[0];
            int z = candidate.getBlockZ() + offset[1];
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
                continue;
            }
            int y = world.getHighestBlockYAt(x, z);
            Location surface = new Location(world, x + 0.5, y + 1, z + 0.5);
            if (!surface.getBlock().isLiquid() && !surface.clone().subtract(0, 1, 0).getBlock().isLiquid()) {
                return surface;
            }
        }
        return null;
    }

    private String buildRunnerHint(World world, Location startLocation, StructureData anchor) {
        if (anchor != null && anchor.location() != null) {
            String sourceName = switch (anchor.structureKey()) {
                case "village" -> "Где-то рядом есть деревня с кузницей";
                case "ruined_portal" -> "Где-то рядом есть разрушенный портал";
                case "shipwreck", "beached_shipwreck" -> "Где-то рядом есть корабль";
                case "woodland_mansion" -> "Где-то рядом есть особняк илладжеров";
                case "pillager_outpost" -> "Где-то рядом есть башня илладжеров";
                default -> "Где-то рядом есть полезное место";
            };
            Location location = anchor.location();
            return sourceName + ". Координаты: X " + location.getBlockX()
                    + ", Y " + location.getBlockY()
                    + ", Z " + location.getBlockZ()
                    + ". Направление (привет илья хуй жопа): " + directionFromTo(startLocation, location) + ".";
        }

        HintTarget bestHint = null;
        int[][] directions = {
                { 0, -48 }, { 48, -48 }, { 48, 0 }, { 48, 48 },
                { 0, 48 }, { -48, 48 }, { -48, 0 }, { -48, -48 }
        };

        for (int[] direction : directions) {
            int sampleX = startLocation.getBlockX() + direction[0];
            int sampleZ = startLocation.getBlockZ() + direction[1];
            if (!world.isChunkLoaded(sampleX >> 4, sampleZ >> 4)) {
                continue;
            }

            Biome biome = world.getBiome(sampleX, startLocation.getBlockY(), sampleZ);
            String biomeKey = biome.getKey().getKey().toLowerCase(Locale.ROOT);
            int score = scoreHintBiome(biomeKey);
            boolean hasWood = hasWoodNearby(world, sampleX, sampleZ, 8);
            boolean hasWater = hasWaterNearby(world, sampleX, sampleZ, 8);

            if (hasWood) {
                score += 8;
            }
            if (hasWater) {
                score += 4;
            }

            String featureText = describeFeature(biomeKey, hasWood, hasWater);
            HintTarget current = new HintTarget(featureText, score);
            if (bestHint == null || current.score() > bestHint.score()) {
                bestHint = current;
            }
        }

        if (bestHint == null) {
            return "Осмотрись у спавна и держись более безопасной суши.";
        }

        return bestHint.featureText() + ".";
    }

    private String directionFromTo(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        String northSouth = dz < -12 ? "north" : dz > 12 ? "south" : "";
        String westEast = dx > 12 ? "east" : dx < -12 ? "west" : "";
        if (!northSouth.isEmpty() && !westEast.isEmpty()) {
            return northSouth + "-" + westEast;
        }
        if (!northSouth.isEmpty()) {
            return northSouth;
        }
        if (!westEast.isEmpty()) {
            return westEast;
        }
        return "here";
    }

    private int scoreHintBiome(String biomeKey) {
        if (biomeKey.contains("forest") || biomeKey.contains("plains") || biomeKey.contains("savanna")) {
            return 12;
        }
        if (biomeKey.contains("taiga") || biomeKey.contains("jungle") || biomeKey.contains("cherry")) {
            return 10;
        }
        if (biomeKey.contains("river") || biomeKey.contains("beach")) {
            return 7;
        }
        if (biomeKey.contains("ocean")) {
            return 2;
        }
        if (biomeKey.contains("desert") || biomeKey.contains("badlands")) {
            return 4;
        }
        return 6;
    }

    private boolean hasWoodNearby(World world, int centerX, int centerZ, int radius) {
        for (int x = -radius; x <= radius; x += 4) {
            for (int z = -radius; z <= radius; z += 4) {
                int sampleX = centerX + x;
                int sampleZ = centerZ + z;
                if (!world.isChunkLoaded(sampleX >> 4, sampleZ >> 4)) {
                    continue;
                }

                int y = world.getHighestBlockYAt(sampleX, sampleZ);
                for (int sampleY = Math.max(world.getMinHeight(), y - 6); sampleY <= y; sampleY++) {
                    String blockName = world.getBlockAt(sampleX, sampleY, sampleZ).getType().getKey().getKey();
                    if (blockName.endsWith("_log") || blockName.endsWith("_wood")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasWaterNearby(World world, int centerX, int centerZ, int radius) {
        for (int x = -radius; x <= radius; x += 4) {
            for (int z = -radius; z <= radius; z += 4) {
                int sampleX = centerX + x;
                int sampleZ = centerZ + z;
                if (!world.isChunkLoaded(sampleX >> 4, sampleZ >> 4)) {
                    continue;
                }

                int y = world.getHighestBlockYAt(sampleX, sampleZ);
                String blockName = world.getBlockAt(sampleX, y - 1, sampleZ).getType().getKey().getKey();
                if (blockName.contains("water")) {
                    return true;
                }
            }
        }
        return false;
    }

    private SmithBuildingData findVillageSmithBuilding(World world, Location villageCenter, int radius) {
        SmithBuildingData bestMatch = SmithBuildingData.none();

        int minChunkX = Math.floorDiv(villageCenter.getBlockX() - radius, 16);
        int maxChunkX = Math.floorDiv(villageCenter.getBlockX() + radius, 16);
        int minChunkZ = Math.floorDiv(villageCenter.getBlockZ() - radius, 16);
        int maxChunkZ = Math.floorDiv(villageCenter.getBlockZ() + radius, 16);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    world.getChunkAt(chunkX, chunkZ);
                }
            }
        }

        int centerX = villageCenter.getBlockX();
        int centerZ = villageCenter.getBlockZ();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                int highestY = world.getHighestBlockYAt(x, z);
                int minY = Math.max(world.getMinHeight(), highestY - 14);
                int maxY = Math.min(world.getMaxHeight() - 1, highestY + 4);
                for (int y = minY; y <= maxY; y++) {
                    Material type = world.getBlockAt(x, y, z).getType();
                    if (type != Material.LAVA) {
                        continue;
                    }

                    SmithBuildingData currentMatch = findSmithLootChestNearLava(world, x, y, z);
                    if (currentMatch.present() && currentMatch.scoreBonus() > bestMatch.scoreBonus()) {
                        bestMatch = currentMatch;
                    }
                }
            }
        }

        return bestMatch;
    }

    private SmithBuildingData findSmithLootChestNearLava(World world, int lavaX, int lavaY, int lavaZ) {
        int chestRadius = 6;
        int minX = lavaX - chestRadius;
        int maxX = lavaX + chestRadius;
        int minY = Math.max(world.getMinHeight(), lavaY - 4);
        int maxY = Math.min(world.getMaxHeight() - 1, lavaY + 5);
        int minZ = lavaZ - chestRadius;
        int maxZ = lavaZ + chestRadius;

        SmithBuildingData bestMatch = SmithBuildingData.none();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (world.getBlockAt(x, y, z).getType() != Material.CHEST) {
                        continue;
                    }

                    SmithBuildingData currentMatch = mapSmithLootChest(world.getBlockAt(x, y, z).getState());
                    if (currentMatch.present() && currentMatch.scoreBonus() > bestMatch.scoreBonus()) {
                        bestMatch = currentMatch;
                    }
                }
            }
        }

        return bestMatch;
    }

    private SmithBuildingData mapSmithLootChest(BlockState state) {
        if (!(state instanceof Chest chest)) {
            return SmithBuildingData.none();
        }

        LootTable lootTable = chest.getLootTable();
        if (lootTable == null) {
            return SmithBuildingData.none();
        }

        NamespacedKey lootTableKey = lootTable.getKey();
        String fullKey = lootTableKey.getNamespace() + ":" + lootTableKey.getKey();
        if (fullKey.endsWith("village/village_weaponsmith")) {
            return new SmithBuildingData(true, "weaponsmith_loot", 28);
        }
        if (fullKey.endsWith("village/village_toolsmith")) {
            return new SmithBuildingData(true, "toolsmith_loot", 18);
        }
        return SmithBuildingData.none();
    }

    private String describeFeature(String biomeKey, boolean hasWood, boolean hasWater) {
        if (hasWood && hasWater) {
            return "Похоже, рядом есть деревья и вода";
        }
        if (hasWood) {
            return "Похоже, рядом есть деревья и открытая суша";
        }
        if (biomeKey.contains("plains")) {
            return "Похоже, рядом открытая равнина";
        }
        if (biomeKey.contains("forest")) {
            return "Похоже, рядом лес";
        }
        if (biomeKey.contains("savanna")) {
            return "Похоже, рядом саванна";
        }
        if (biomeKey.contains("river") || hasWater) {
            return "Похоже, рядом вода";
        }
        if (biomeKey.contains("desert")) {
            return "Похоже, рядом пустыня";
        }
        return "Похоже, рядом более удобный маршрут";
    }

    private record HintTarget(String featureText, int score) {
    }

    private record SmithBuildingData(boolean present, String structureLabel, int scoreBonus) {
        private static SmithBuildingData none() {
            return new SmithBuildingData(false, null, 0);
        }
    }

    private record StructureData(String structureKey, Location location, int distanceBlocks, int score,
            String smithStructureLabel) {
        private StructureData(String structureKey, Location location, int distanceBlocks, int score) {
            this(structureKey, location, distanceBlocks, score, null);
        }
    }
}
