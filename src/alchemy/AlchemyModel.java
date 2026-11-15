package alchemy;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Data-driven, order-independent recipes and persistent discovery progress. */
final class AlchemyModel {
    record Element(String name, String category, String symbol) {}

    record Recipe(String first, String second, String result) {}

    record Discovery(String result, boolean fresh) {}

    final Map<String, Element> elements = new LinkedHashMap<>();
    final List<Recipe> recipes = new ArrayList<>();
    final Set<String> discovered = new LinkedHashSet<>();
    private final Map<String, String> combinations = new HashMap<>();
    private final Path saveFile;
    String saveWarning = "";
    static final List<String> STARTERS = List.of("City", "Ball", "Water", "Land", "Sun", "Snow");

    AlchemyModel(Path saveFile) {
        this.saveFile = saveFile;
        add("City", "Basics", "CT");
        add("Ball", "Basics", "●");
        add("Water", "Basics", "≈");
        add("Land", "Basics", "▲");
        add("Sun", "Basics", "☀");
        add("Snow", "Basics", "❄");
        recipe("City", "City", "Metropolis", "Places", "MT");
        recipe("Land", "Land", "Park", "Places", "PK");
        recipe("Water", "Land", "Coast", "Places", "CO");
        recipe("Snow", "Land", "Mountain", "Places", "MN");
        recipe("Sun", "Land", "Desert", "Places", "DS");
        recipe("Water", "Water", "Ocean", "Places", "OC");
        recipe("Land", "Ocean", "Island", "Places", "IS");
        recipe("Water", "Mountain", "River", "Places", "RV");
        recipe("City", "Park", "Stadium", "Places", "ST");
        recipe("City", "Metropolis", "Tower", "Places", "TW");
        recipe("Sun", "City", "Energy", "Culture", "EN");
        recipe("City", "Energy", "Technology", "Culture", "TX");
        recipe("City", "Land", "History", "Culture", "HI");
        recipe("History", "Tower", "Temple", "Places", "TM");
        recipe("City", "Ball", "Basketball", "Sports", "BK");
        recipe("Ball", "Land", "Soccer", "Sports", "SC");
        recipe("Ball", "Water", "Water Polo", "Sports", "WP");
        recipe("Ball", "Sun", "Tennis", "Sports", "TN");
        recipe("Ball", "Snow", "Ice Hockey", "Sports", "HK");
        recipe("Park", "Ball", "Baseball", "Sports", "BB");
        recipe("Mountain", "Snow", "Skiing", "Sports", "SK");
        recipe("Coast", "Ball", "Volleyball", "Sports", "VB");
        recipe("Ocean", "Sun", "Surfing", "Sports", "SF");
        recipe("Water", "Energy", "Swimming", "Sports", "SW");
        recipe("Land", "Energy", "Running", "Sports", "RN");
        recipe("Running", "Technology", "Cycling", "Sports", "CY");
        recipe("River", "Energy", "Rowing", "Sports", "RW");
        recipe("Park", "Sun", "Golf", "Sports", "GF");
        recipe("History", "Ball", "Cricket", "Sports", "CR");
        recipe("Soccer", "Technology", "Esports", "Sports", "ES");
        recipe("City", "Soccer", "Fans", "Culture", "FN");
        recipe("Fans", "Fans", "Crowd", "Culture", "CW");
        recipe("Fans", "Energy", "Music", "Culture", "MU");
        recipe("Music", "Sun", "Carnival", "Culture", "CV");
        recipe("Soccer", "Stadium", "Match Day", "Events", "MD");
        recipe("Basketball", "Stadium", "Arena", "Places", "AR");
        recipe("Tennis", "Park", "Tennis Court", "Places", "TC");
        recipe("Swimming", "Stadium", "Aquatics Center", "Places", "AC");
        recipe("Snow", "Stadium", "Ice Rink", "Places", "IR");
        recipe("Running", "Park", "Track", "Places", "TR");
        recipe("Cycling", "Stadium", "Velodrome", "Places", "VE");
        recipe("City", "Tower", "Paris", "Cities", "PA");
        recipe("Metropolis", "Tower", "New York", "Cities", "NY");
        recipe("City", "Coast", "Los Angeles", "Cities", "LA");
        recipe("City", "History", "Rome", "Cities", "RO");
        recipe("City", "Temple", "Tokyo", "Cities", "TK");
        recipe("City", "Carnival", "Rio", "Cities", "RI");
        recipe("City", "Desert", "Dubai", "Cities", "DU");
        recipe("City", "Island", "Sydney", "Cities", "SY");
        recipe("City", "Mountain", "Denver", "Cities", "DE");
        recipe("City", "Snow", "Oslo", "Cities", "OS");
        recipe("City", "Technology", "Seoul", "Cities", "SE");
        recipe("History", "River", "London", "Cities", "LD");
        recipe("City", "Music", "Nashville", "Cities", "NA");
        recipe("City", "River", "Cairo", "Cities", "CA");
        recipe("Coast", "History", "Barcelona", "Cities", "BC");
        recipe("Paris", "Tennis", "Clay Court Classic", "Events", "CC");
        recipe("London", "Tennis", "Grass Court Classic", "Events", "GC");
        recipe("New York", "Tennis", "Big Apple Open", "Events", "BA");
        recipe("New York", "Running", "City Marathon", "Events", "MA");
        recipe("Los Angeles", "Basketball", "Showtime", "Events", "SH");
        recipe("Tokyo", "Esports", "Neon Tournament", "Events", "NT");
        recipe("Rio", "Volleyball", "Beach Games", "Events", "BG");
        recipe("Sydney", "Surfing", "Surf Festival", "Events", "SU");
        recipe("Oslo", "Skiing", "Winter Games", "Events", "WG");
        recipe("Denver", "Cycling", "Mountain Challenge", "Events", "MC");
        recipe("Dubai", "Golf", "Desert Invitational", "Events", "DI");
        recipe("Seoul", "Esports", "World Finals", "Events", "WF");
        recipe("Rome", "Running", "Ancient City Run", "Events", "AU");
        recipe("Barcelona", "Soccer", "Derby Night", "Events", "DN");
        recipe("Nashville", "Match Day", "Stadium Concert", "Events", "SC");
        recipe("Cairo", "Rowing", "River Regatta", "Events", "RR");
        recipe("City Marathon", "Winter Games", "World Games", "Events", "★");
        recipe("World Games", "Crowd", "Opening Ceremony", "Events", "★");
        recipe("World Finals", "World Games", "City of Champions", "Events", "★");
        discovered.addAll(STARTERS);
        if (saveFile != null && Files.exists(saveFile))
            try {
                for (String name : Files.readAllLines(saveFile))
                    if (elements.containsKey(name)) discovered.add(name);
            } catch (IOException ignored) {
                saveWarning = "Progress could not be loaded.";
            }
    }

    private void add(String name, String category, String symbol) {
        elements.put(name, new Element(name, category, symbol));
    }

    private void recipe(String a, String b, String result, String category, String symbol) {
        if (!elements.containsKey(a) || !elements.containsKey(b))
            throw new IllegalArgumentException("Unknown ingredient");
        String key = key(a, b);
        if (combinations.putIfAbsent(key, result) != null)
            throw new IllegalArgumentException("Duplicate recipe: " + key);
        add(result, category, symbol);
        recipes.add(new Recipe(a, b, result));
    }

    private static String key(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    Discovery combine(String a, String b) {
        if (!discovered.contains(a) || !discovered.contains(b)) return null;
        String result = combinations.get(key(a, b));
        if (result == null) return null;
        boolean fresh = discovered.add(result);
        if (fresh) save();
        return new Discovery(result, fresh);
    }

    Recipe hint() {
        return recipes.stream()
                .filter(r ->
                        discovered.contains(r.first)
                                && discovered.contains(r.second)
                                && !discovered.contains(r.result))
                .findFirst()
                .orElse(null);
    }

    List<Element> library(String query, String category) {
        String needle = query.toLowerCase(Locale.ROOT).trim();
        return discovered.stream()
                .map(elements::get)
                .filter(e ->
                        e.name.toLowerCase(Locale.ROOT).contains(needle)
                                && (category.equals("All") || e.category.equals(category)))
                .sorted(Comparator.comparing(Element::name))
                .toList();
    }

    void reset() {
        discovered.clear();
        discovered.addAll(STARTERS);
        save();
    }

    private void save() {
        if (saveFile == null) return;
        Path temporary = saveFile.resolveSibling(saveFile.getFileName() + ".tmp");
        try {
            Files.write(temporary, discovered);
            try {
                Files.move(
                        temporary,
                        saveFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, saveFile, StandardCopyOption.REPLACE_EXISTING);
            }
            saveWarning = "";
        } catch (IOException ignored) {
            saveWarning = "Progress is in memory; saving unavailable.";
        }
    }
}
