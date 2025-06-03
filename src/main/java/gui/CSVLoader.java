// package gui;

// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;

// public class CSVLoader {

//     public static List<GeoPosition> loadStopCoordinatesFromCSV(String csvFilePath) {
//         List<GeoPosition> stopCoordinates = new ArrayList<>();
//         String csvDelimiterPattern = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

//         try (BufferedReader fileReader = new BufferedReader(new FileReader(csvFilePath))) {
//             fileReader.readLine();
//             String line;
//             while ((line = fileReader.readLine()) != null) {
//                 String[] columns = line.split(csvDelimiterPattern, -1);
//                 if (columns.length < 4) continue;
//                 try {
//                     double latitude = Double.parseDouble(columns[2]);
//                     double longitude = Double.parseDouble(columns[3]);
//                     stopCoordinates.add(new GeoPosition(latitude, longitude));
//                 } catch (NumberFormatException ignored) {}
//             }
//         } catch (IOException exception) {
//             System.err.println(exception.getMessage());
//         }

//         return stopCoordinates;
//     }
// }

