package routing.csa.model.route;

public enum RouteType {
    TRAM(0, "Tram"),
    SUBWAY(1, "Subway"),
    RAIL(2, "Rail"),
    BUS(3, "Bus"),
    FERRY(4, "Ferry"),
    CABLE_TRAM(5, "Cable Tram"),
    AERIAL_LIFT(6, "Aerial Lift"),
    FUNICULAR(7, "Funicular"),
    TROLLEYBUS(11, "Trolleybus"),
    MONORAIL(12, "Monorail"),
    RAILWAY_SERVICE(100, "Railway Service"),
    HIGH_SPEED_RAIL(101, "High Speed Rail Service"),
    LONG_DISTANCE_TRAIN(102, "Long Distance Trains"),
    INTER_REGIONAL_RAIL(103, "Inter Regional Rail Service"),
    CAR_TRANSPORT_RAIL(104, "Car Transport Rail Service"),
    SLEEPER_RAIL(105, "Sleeper Rail Service"),
    REGIONAL_RAIL(106, "Regional Rail Service"),
    TOURIST_RAILWAY(107, "Tourist Railway Service"),
    RAIL_SHUTTLE(108, "Rail Shuttle (Within Complex)"),
    SUBURBAN_RAILWAY(109, "Suburban Railway"),
    REPLACEMENT_RAIL(110, "Replacement Rail Service"),
    SPECIAL_RAIL(111, "Special Rail Service"),
    LORRY_TRANSPORT_RAIL(112, "Lorry Transport Rail Service"),
    ALL_RAIL_SERVICES(113, "All Rail Services"),
    CROSS_COUNTRY_RAIL(114, "Cross-Country Rail Service"),
    VEHICLE_TRANSPORT_RAIL(115, "Vehicle Transport Rail Service"),
    RACK_PINION_RAILWAY(116, "Rack and Pinion Railway"),
    ADDITIONAL_RAIL(117, "Additional Rail Service"),
    COACH_SERVICE(200, "Coach Service"),
    INTERNATIONAL_COACH(201, "International Coach Service"),
    NATIONAL_COACH(202, "National Coach Service"),
    SHUTTLE_COACH(203, "Shuttle Coach Service"),
    REGIONAL_COACH(204, "Regional Coach Service"),
    SPECIAL_COACH(205, "Special Coach Service"),
    SIGHTSEEING_COACH(206, "Sightseeing Coach Service"),
    TOURIST_COACH(207, "Tourist Coach Service"),
    COMMUTER_COACH(208, "Commuter Coach Service"),
    ALL_COACH_SERVICES(209, "All Coach Services"),
    URBAN_RAILWAY(400, "Urban Railway Service"),
    METRO(401, "Metro Service"),
    UNDERGROUND(402, "Underground Service"),
    URBAN_RAILWAY_SERVICE(403, "Urban Railway Service"),
    ALL_URBAN_RAILWAY(404, "All Urban Railway Services"),
    URBAN_MONORAIL(405, "Urban Monorail"),
    BUS_SERVICE(700, "Bus Service"),
    REGIONAL_BUS(701, "Regional Bus Service"),
    EXPRESS_BUS(702, "Express Bus Service"),
    STOPPING_BUS(703, "Stopping Bus Service"),
    LOCAL_BUS(704, "Local Bus Service"),
    NIGHT_BUS(705, "Night Bus Service"),
    POST_BUS(706, "Post Bus Service"),
    SPECIAL_NEEDS_BUS(707, "Special Needs Bus"),
    MOBILITY_BUS(708, "Mobility Bus Service"),
    MOBILITY_BUS_DISABLED(709, "Mobility Bus for Registered Disabled"),
    SIGHTSEEING_BUS(710, "Sightseeing Bus"),
    SHUTTLE_BUS(711, "Shuttle Bus"),
    SCHOOL_BUS(712, "School Bus"),
    SCHOOL_PUBLIC_BUS(713, "School and Public Service Bus"),
    RAIL_REPLACEMENT_BUS(714, "Rail Replacement Bus Service"),
    DEMAND_RESPONSE_BUS(715, "Demand and Response Bus Service"),
    ALL_BUS_SERVICES(716, "All Bus Services"),
    TROLLEYBUS_SERVICE(800, "Trolleybus Service"),
    TRAM_SERVICE(900, "Tram Service"),
    CITY_TRAM(901, "City Tram Service"),
    LOCAL_TRAM(902, "Local Tram Service"),
    REGIONAL_TRAM(903, "Regional Tram Service"),
    SIGHTSEEING_TRAM(904, "Sightseeing Tram Service"),
    SHUTTLE_TRAM(905, "Shuttle Tram Service"),
    ALL_TRAM_SERVICES(906, "All Tram Services"),
    WATER_TRANSPORT(1000, "Water Transport Service"),
    AIR_SERVICE(1100, "Air Service"),
    FERRY_SERVICE(1200, "Ferry Service"),
    ALT_AERIAL_LIFT(1300, "Alternative Aerial Lift Service"),
    TELECABIN(1301, "Telecabin Service"),
    CABLE_CAR(1302, "Cable Car Service"),
    ELEVATOR(1303, "Elevator Service"),
    CHAIR_LIFT(1304, "Chair Lift Service"),
    DRAG_LIFT(1305, "Drag Lift Service"),
    SMALL_TELECABIN(1306, "Small Telecabin Service"),
    ALL_TELECABIN(1307, "All Telecabin Services"),
    FUNICULAR_SERVICE(1400, "Funicular Service"),
    TAXI_SERVICE(1500, "Taxi Service"),
    COMMUNAL_TAXI(1501, "Communal Taxi Service"),
    WATER_TAXI(1502, "Water Taxi Service"),
    RAIL_TAXI(1503, "Rail Taxi Service"),
    BIKE_TAXI(1504, "Bike Taxi Service"),
    LICENSED_TAXI(1505, "Licensed Taxi Service"),
    PRIVATE_HIRE(1506, "Private Hire Service Vehicle"),
    ALL_TAXI_SERVICES(1507, "All Taxi Services"),
    MISCELLANEOUS_SERVICE(1700, "Miscellaneous Service"),
    HORSE_DRAWN(1702, "Horse-drawn Carriage"),
    UNDEFINED(-1, "UNDEFINED");

    private final int code;
    private final String name;

    RouteType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static RouteType getNameFromCode(int code) {
        for (RouteType routeType : RouteType.values()) {
            if (routeType.code == code) {
                return routeType;
            }
        }
        return UNDEFINED;
    }

    public String showDisplayName() {
        return name;
    }

}
