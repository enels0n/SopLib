package net.enelson.sopli.lib.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final char SECTION_CHAR = '\u00A7';
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public String color(String input) {
        if (input == null) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);

        String normalized = ChatColor.translateAlternateColorCodes('&', buffer.toString());
        if (containsMiniMessage(normalized)) {
            Component component = MINI_MESSAGE.deserialize(normalized.replace(SECTION_CHAR, '&'));
            return LEGACY_SERIALIZER.serialize(component);
        }
        return normalized;
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
        return input.contains("<") && input.contains(">");
    }
}
