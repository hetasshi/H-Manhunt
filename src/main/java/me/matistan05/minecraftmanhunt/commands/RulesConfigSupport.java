package me.matistan05.minecraftmanhunt.commands;

import me.matistan05.minecraftmanhunt.Main;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

final class RulesConfigSupport {
    private RulesConfigSupport() {
    }

    enum RuleType {
        BOOLEAN,
        INTEGER,
        DECIMAL,
        UNSUPPORTED
    }

    static boolean isEditableRule(Main main, String key) {
        if (!main.getConfig().contains(key) || main.getConfig().isConfigurationSection(key)) {
            return false;
        }
        return resolveRuleType(main, key) != RuleType.UNSUPPORTED;
    }

    static RuleType resolveRuleType(Main main, String key) {
        Object referenceValue = getReferenceValue(main, key);
        if (referenceValue instanceof Boolean) {
            return RuleType.BOOLEAN;
        }
        if (referenceValue instanceof Integer || referenceValue instanceof Long
                || referenceValue instanceof Short || referenceValue instanceof Byte) {
            return RuleType.INTEGER;
        }
        if (referenceValue instanceof Double || referenceValue instanceof Float) {
            return RuleType.DECIMAL;
        }
        return RuleType.UNSUPPORTED;
    }

    static String expectedValueHint(RuleType ruleType) {
        return switch (ruleType) {
            case BOOLEAN -> "true или false";
            case INTEGER -> "целым числом";
            case DECIMAL -> "десятичным числом";
            case UNSUPPORTED -> "поддерживаемым типом";
        };
    }

    static Object parseValue(Main main, String key, String rawValue) {
        Object referenceValue = getReferenceValue(main, key);
        if (referenceValue instanceof Integer) {
            return Integer.parseInt(rawValue);
        }
        if (referenceValue instanceof Long) {
            return Long.parseLong(rawValue);
        }
        if (referenceValue instanceof Short) {
            return Short.parseShort(rawValue);
        }
        if (referenceValue instanceof Byte) {
            return Byte.parseByte(rawValue);
        }
        if (referenceValue instanceof Double) {
            return Double.parseDouble(rawValue);
        }
        if (referenceValue instanceof Float) {
            return Float.parseFloat(rawValue);
        }
        if (referenceValue instanceof Boolean) {
            if (!rawValue.equalsIgnoreCase("true") && !rawValue.equalsIgnoreCase("false")) {
                throw new IllegalArgumentException("BOOLEAN");
            }
            return Boolean.parseBoolean(rawValue);
        }
        throw new IllegalArgumentException("UNSUPPORTED");
    }

    static List<String> getValueSuggestions(Main main, String key, String userInput) {
        List<String> suggestions = new ArrayList<>();
        RuleType ruleType = resolveRuleType(main, key);
        if (ruleType == RuleType.BOOLEAN) {
            if (startsWith("true", userInput)) {
                suggestions.add("true");
            }
            if (startsWith("false", userInput)) {
                suggestions.add("false");
            }
            return suggestions;
        }

        if (ruleType == RuleType.INTEGER || ruleType == RuleType.DECIMAL) {
            String currentValue = String.valueOf(main.getConfig().get(key));
            if (startsWith(currentValue, userInput)) {
                suggestions.add(currentValue);
            }
        }

        return suggestions;
    }

    private static Object getReferenceValue(Main main, String key) {
        FileConfiguration config = main.getConfig();
        Configuration defaults = config.getDefaults();
        if (defaults != null && defaults.contains(key) && !defaults.isConfigurationSection(key)) {
            return defaults.get(key);
        }
        return config.get(key);
    }

    private static boolean startsWith(String value, String prefix) {
        if (prefix.length() > value.length()) {
            return false;
        }
        for (int i = 0; i < prefix.length(); i++) {
            if (Character.toLowerCase(prefix.charAt(i)) != Character.toLowerCase(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
