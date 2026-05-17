package net.enelson.sopli.lib.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_MESSAGE_GRADIENT_PATTERN = Pattern.compile(
            "<(gradient|transition):([^>]+)>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MINI_MESSAGE_TAG_PATTERN = Pattern.compile(
            "<(/)?(?:"
                    + "#[A-Fa-f0-9]{6}"
                    + "|black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|grey|dark_gray"
                    + "|blue|green|aqua|red|light_purple|yellow|white"
                    + "|reset|bold|b|italic|i|underlined|u|strikethrough|st|obfuscated|obf"
                    + "|gradient|transition|rainbow|color|newline|br|hover|click|lang|font|insert|key|selector|score|nbt"
                    + ")(:[^>]*)?>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern GRADIENT_BLOCK_PATTERN = Pattern.compile(
            "<gradient:([^>]+)>(.*?)</gradient>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern COLOR_BLOCK_PATTERN = Pattern.compile(
            "<color:(#[A-Fa-f0-9]{6})>(.*?)</color>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern HEX_OPEN_TAG_PATTERN = Pattern.compile(
            "<#([A-Fa-f0-9]{6})>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern HEX_CLOSE_TAG_PATTERN = Pattern.compile(
            "</#([A-Fa-f0-9]{6})>",
            Pattern.CASE_INSENSITIVE
    );
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('\u00A7')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final Map<String, String> SIMPLE_TAG_REPLACEMENTS = createSimpleTagReplacements();

    public String color(String input) {
        if (input == null) {
            return "";
        }

        String miniNormalized = normalizeMiniMessageTags(input);

        Matcher matcher = HEX_PATTERN.matcher(miniNormalized);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);

        String normalized = ChatColor.translateAlternateColorCodes('&', buffer.toString());
        if (!containsMiniMessage(normalized)) {
            return normalized;
        }

        try {
            net.kyori.adventure.text.minimessage.MiniMessage miniMessage = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage();
            Component component = miniMessage.deserialize(normalized.replace('\u00A7', '&'));
            return LEGACY_SERIALIZER.serialize(component);
        } catch (Throwable ignored) {
            return applyLegacyMiniMessageFallback(normalized);
        }
    }

    public List<String> color(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(lines.size());
        for (String line : lines) {
            result.add(color(line));
        }
        return result;
    }

    private boolean containsMiniMessage(String input) {
        return input != null && MINI_MESSAGE_TAG_PATTERN.matcher(input).find();
    }

    private String normalizeMiniMessageTags(String input) {
        if (input == null || input.indexOf('<') < 0 || input.indexOf('>') < 0) {
            return input;
        }

        Matcher matcher = MINI_MESSAGE_GRADIENT_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        boolean replaced = false;
        while (matcher.find()) {
            String type = matcher.group(1);
            String arguments = matcher.group(2).replaceAll("(?i)(#[a-f0-9]{6})(?=#)", "$1:");
            matcher.appendReplacement(result, Matcher.quoteReplacement("<" + type + ":" + arguments + ">"));
            replaced = true;
        }
        if (!replaced) {
            return input;
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String applyLegacyMiniMessageFallback(String input) {
        String resolved = input;
        resolved = resolveGradientBlocks(resolved);
        resolved = resolveColorBlocks(resolved);
        resolved = resolveHexTags(resolved);
        resolved = resolveSimpleTags(resolved);
        resolved = stripUnsupportedTags(resolved);
        return resolved;
    }

    private String resolveGradientBlocks(String input) {
        Matcher matcher = GRADIENT_BLOCK_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        boolean replaced = false;
        while (matcher.find()) {
            String[] stops = matcher.group(1).split(":");
            String content = matcher.group(2);
            String replacement = renderGradient(content, stops);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            replaced = true;
        }
        if (!replaced) {
            return input;
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String resolveColorBlocks(String input) {
        Matcher matcher = COLOR_BLOCK_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        boolean replaced = false;
        while (matcher.find()) {
            String color = matcher.group(1);
            String content = matcher.group(2);
            String replacement = ChatColor.of(color).toString() + content + ChatColor.RESET;
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            replaced = true;
        }
        if (!replaced) {
            return input;
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String resolveHexTags(String input) {
        Matcher matcher = HEX_OPEN_TAG_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(result, Matcher.quoteReplacement(ChatColor.of("#" + matcher.group(1)).toString()));
        }
        matcher.appendTail(result);
        return HEX_CLOSE_TAG_PATTERN.matcher(result.toString()).replaceAll("");
    }

    private String resolveSimpleTags(String input) {
        String resolved = input;
        for (Map.Entry<String, String> entry : SIMPLE_TAG_REPLACEMENTS.entrySet()) {
            resolved = resolved.replace(entry.getKey(), entry.getValue());
        }
        return resolved;
    }

    private String stripUnsupportedTags(String input) {
        return input.replaceAll("(?i)</?(hover|click|lang|font|insert|key|selector|score|nbt|transition|rainbow)(:[^>]+)?>", "");
    }

    private String renderGradient(String content, String[] stops) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        List<int[]> colors = new ArrayList<int[]>();
        for (String stop : stops) {
            String normalized = stop == null ? "" : stop.trim();
            if (!normalized.startsWith("#") || normalized.length() != 7) {
                continue;
            }
            colors.add(parseHexColor(normalized));
        }
        if (colors.isEmpty()) {
            return content;
        }
        if (colors.size() == 1) {
            return ChatColor.of(toHex(colors.get(0))).toString() + content;
        }

        StringBuilder out = new StringBuilder();
        int visibleCharacters = content.length();
        for (int i = 0; i < visibleCharacters; i++) {
            double progress = visibleCharacters == 1 ? 0.0D : (double) i / (double) (visibleCharacters - 1);
            int[] rgb = interpolate(colors, progress);
            out.append(ChatColor.of(toHex(rgb))).append(content.charAt(i));
        }
        return out.toString();
    }

    private int[] interpolate(List<int[]> colors, double progress) {
        int segments = colors.size() - 1;
        double scaled = progress * segments;
        int index = Math.min(segments - 1, (int) Math.floor(scaled));
        double local = scaled - index;
        int[] start = colors.get(index);
        int[] end = colors.get(index + 1);
        return new int[]{
                blend(start[0], end[0], local),
                blend(start[1], end[1], local),
                blend(start[2], end[2], local)
        };
    }

    private int blend(int start, int end, double progress) {
        return (int) Math.round(start + ((end - start) * progress));
    }

    private int[] parseHexColor(String hex) {
        return new int[]{
                Integer.parseInt(hex.substring(1, 3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16)
        };
    }

    private String toHex(int[] rgb) {
        return String.format("#%02X%02X%02X", rgb[0], rgb[1], rgb[2]);
    }

    private static Map<String, String> createSimpleTagReplacements() {
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("<black>", ChatColor.BLACK.toString());
        replacements.put("</black>", ChatColor.RESET.toString());
        replacements.put("<dark_blue>", ChatColor.DARK_BLUE.toString());
        replacements.put("</dark_blue>", ChatColor.RESET.toString());
        replacements.put("<dark_green>", ChatColor.DARK_GREEN.toString());
        replacements.put("</dark_green>", ChatColor.RESET.toString());
        replacements.put("<dark_aqua>", ChatColor.DARK_AQUA.toString());
        replacements.put("</dark_aqua>", ChatColor.RESET.toString());
        replacements.put("<dark_red>", ChatColor.DARK_RED.toString());
        replacements.put("</dark_red>", ChatColor.RESET.toString());
        replacements.put("<dark_purple>", ChatColor.DARK_PURPLE.toString());
        replacements.put("</dark_purple>", ChatColor.RESET.toString());
        replacements.put("<gold>", ChatColor.GOLD.toString());
        replacements.put("</gold>", ChatColor.RESET.toString());
        replacements.put("<gray>", ChatColor.GRAY.toString());
        replacements.put("</gray>", ChatColor.RESET.toString());
        replacements.put("<grey>", ChatColor.GRAY.toString());
        replacements.put("</grey>", ChatColor.RESET.toString());
        replacements.put("<dark_gray>", ChatColor.DARK_GRAY.toString());
        replacements.put("</dark_gray>", ChatColor.RESET.toString());
        replacements.put("<blue>", ChatColor.BLUE.toString());
        replacements.put("</blue>", ChatColor.RESET.toString());
        replacements.put("<green>", ChatColor.GREEN.toString());
        replacements.put("</green>", ChatColor.RESET.toString());
        replacements.put("<aqua>", ChatColor.AQUA.toString());
        replacements.put("</aqua>", ChatColor.RESET.toString());
        replacements.put("<red>", ChatColor.RED.toString());
        replacements.put("</red>", ChatColor.RESET.toString());
        replacements.put("<light_purple>", ChatColor.LIGHT_PURPLE.toString());
        replacements.put("</light_purple>", ChatColor.RESET.toString());
        replacements.put("<yellow>", ChatColor.YELLOW.toString());
        replacements.put("</yellow>", ChatColor.RESET.toString());
        replacements.put("<white>", ChatColor.WHITE.toString());
        replacements.put("</white>", ChatColor.RESET.toString());
        replacements.put("<bold>", ChatColor.BOLD.toString());
        replacements.put("</bold>", ChatColor.RESET.toString());
        replacements.put("<b>", ChatColor.BOLD.toString());
        replacements.put("</b>", ChatColor.RESET.toString());
        replacements.put("<italic>", ChatColor.ITALIC.toString());
        replacements.put("</italic>", ChatColor.RESET.toString());
        replacements.put("<i>", ChatColor.ITALIC.toString());
        replacements.put("</i>", ChatColor.RESET.toString());
        replacements.put("<underlined>", ChatColor.UNDERLINE.toString());
        replacements.put("</underlined>", ChatColor.RESET.toString());
        replacements.put("<u>", ChatColor.UNDERLINE.toString());
        replacements.put("</u>", ChatColor.RESET.toString());
        replacements.put("<strikethrough>", ChatColor.STRIKETHROUGH.toString());
        replacements.put("</strikethrough>", ChatColor.RESET.toString());
        replacements.put("<st>", ChatColor.STRIKETHROUGH.toString());
        replacements.put("</st>", ChatColor.RESET.toString());
        replacements.put("<obfuscated>", ChatColor.MAGIC.toString());
        replacements.put("</obfuscated>", ChatColor.RESET.toString());
        replacements.put("<obf>", ChatColor.MAGIC.toString());
        replacements.put("</obf>", ChatColor.RESET.toString());
        replacements.put("<reset>", ChatColor.RESET.toString());
        replacements.put("</reset>", "");
        replacements.put("<newline>", "\n");
        replacements.put("</newline>", "");
        replacements.put("<br>", "\n");
        replacements.put("</br>", "");
        return replacements;
    }
}
