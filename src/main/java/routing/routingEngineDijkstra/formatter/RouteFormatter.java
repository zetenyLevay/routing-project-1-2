package routing.routingEngineDijkstra.formatter;

import routing.routingEngineDijkstra.adiModels.AdiRouteInfo;
import routing.routingEngineDijkstra.adiModels.AdiRouteStep;
import routing.routingEngineModels.*;

import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RouteFormatter {
    private static final Pattern INPUT_PATTERN = Pattern.compile(
            "\\{\\s*\"routeFrom\"\\s*:\\s*\\{\\s*\"lat\"\\s*:\\s*([\\d.]+)\\s*,\\s*\"lon\"\\s*:\\s*([\\d.]+)\\s*}\\s*,\\s*" +
                    "\"to\"\\s*:\\s*\\{\\s*\"lat\"\\s*:\\s*([\\d.]+)\\s*,\\s*\"lon\"\\s*:\\s*([\\d.]+)\\s*}\\s*,\\s*" +
                    "\"startingAt\"\\s*:\\s*\"(\\d{2}:\\d{2})\"\\s*}"
    );

    public static InputJourney parseInput(String jsonInput) throws IllegalArgumentException {
        Matcher matcher = INPUT_PATTERN.matcher(jsonInput.replaceAll("\\s", ""));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid input format");
        }

        try {
            double fromLat = Double.parseDouble(matcher.group(1));
            double fromLon = Double.parseDouble(matcher.group(2));
            double toLat = Double.parseDouble(matcher.group(3));
            double toLon = Double.parseDouble(matcher.group(4));
            String[] timeParts = matcher.group(5).split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            return new InputJourney(
                    new Coordinates(fromLat, fromLon),
                    new Coordinates(toLat, toLon),
                    LocalTime.of(hour, minute)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse input values", e);
        }
    }

    public static String formatResult(FinalRoute route, LocalTime startTime) {
        if (route == null || route.getRouteSteps() == null) {
            return "{\"error\":\"No route found\"}";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("{\"ok\":[");

        List<AdiRouteStep> steps = route.getRouteSteps();
        LocalTime currentTime = startTime;
        boolean first = true;

        for (AdiRouteStep step : steps) {
            if (!first) {
                builder.append(",");
            }
            first = false;

            builder.append(formatRouteStep(step, currentTime));
            currentTime = currentTime.plusSeconds((long)(step.getDuration() * 60));
        }

        builder.append("]}");
        return builder.toString();
    }

    private static String formatRouteStep(AdiRouteStep step, LocalTime startTime) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("\"mode\":\"").append(step.getMode()).append("\",");
        builder.append("\"to\":").append(formatPoint(step.getTo())).append(",");
        builder.append("\"duration\":").append(Math.round(step.getDuration())).append(",");
        builder.append("\"startTime\":\"").append(formatTime(startTime)).append("\"");

        if ("ride".equals(step.getMode())) {
            builder.append(",");
            builder.append("\"stop\":\"").append(escapeJson(step.getStop())).append("\",");
            builder.append("\"route\":").append(formatRouteInfo(step.getRoute()));
        }

        builder.append("}");
        return builder.toString();
    }

    private static String formatRouteInfo(AdiRouteInfo routeInfo) {
        if (routeInfo == null) {
            return "null";
        }
        return String.format(
                "{\"operator\":\"%s\",\"shortName\":\"%s\",\"longName\":\"%s\",\"headSign\":\"%s\"}",
                escapeJson(routeInfo.getOperator()),
                escapeJson(routeInfo.getShortName()),
                escapeJson(routeInfo.getLongName()),
                escapeJson(routeInfo.getHeadSign())
        );
    }

    private static String formatPoint(Coordinates point) {
        return String.format("{\"lat\":%.6f,\"lon\":%.6f}", point.getLatitude(), point.getLongitude());
    }

    private static String formatTime(LocalTime time) {
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}