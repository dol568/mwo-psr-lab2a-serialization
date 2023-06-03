package mwo.psr.serialization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.google.protobuf.Timestamp;
import mwo.psr.proto.WeatherForecastProtos.*;

public class ProtoSerialization {

    public static void main(String[] args) {
        try {
            new ProtoSerialization().weatherForecast();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Metoda tworzy obiekt WeatherForcast, serializuje go, zapisuje do pliku, nastepnie odczytuje plik,
     * deserializuje z powrotem do nowego obiektu WeatherForecast i wyswietla zdeserializowane dane w konsoli
     *
     * @throws IOException Jeœli wyst¹pi b³¹d operacji wejœcia-wyjœcia podczas zapisywania/odczytywania pliku.
     */
    public void weatherForecast() throws IOException {

        // Utworzenie obiektu WeatherForecast i wypelnienie go przykladowymi danymi
        LocalDateTime now = LocalDateTime.now();
        WeatherForecast forecast = WeatherForecast.newBuilder()
                .addDailyForecasts(
                        DailyForecast.newBuilder()
                                .setDate(Timestamp.newBuilder()
                                        .setSeconds(now.toEpochSecond(ZoneOffset.UTC))
                                        .setNanos(now.getNano()).build())
                                .addCityForecasts(
                                        DailyForecast.CityForecast.newBuilder()
                                                .setCityName("Krakow")
                                                .setTemperature(22.0f)
                                                .setHumidity(55f)
                                                .setCloudiness(44f)
                                                .setPressure(1015f))
                                .addCityForecasts(
                                        DailyForecast.CityForecast.newBuilder()
                                                .setCityName("Warszawa")
                                                .setTemperature(20.0f)
                                                .setHumidity(35f)
                                                .setCloudiness(5f)
                                                .setPressure(1013f)))
                .addDailyForecasts(
                        DailyForecast.newBuilder()
                                .setDate(Timestamp.newBuilder()
                                        .setSeconds(now.plusDays(1).toEpochSecond(ZoneOffset.UTC))
                                        .setNanos(now.plusDays(1).getNano()).build())
                                .addCityForecasts(
                                        DailyForecast.CityForecast.newBuilder()
                                                .setCityName("Krakow")
                                                .setTemperature(16.5f)
                                                .setHumidity(20f)
                                                .setCloudiness(89f)
                                                .setPressure(1010f))
                                .addCityForecasts(
                                        DailyForecast.CityForecast.newBuilder()
                                                .setCityName("Warszawa")
                                                .setTemperature(17.5f)
                                                .setHumidity(75f)
                                                .setCloudiness(2f)
                                                .setPressure(1017f)))
                                .build();

        // Serializacja obiektu WeatherForecast
        byte[] forecastser = forecast.toByteArray();
        //print data as hex values
//        for (byte b : forecastser) {
//            System.out.print(String.format("%02X", b));
//        }

        // Zserializowane dane s¹ zapisywane do pliku o nazwie "forecast.ser" za pomoc¹ klasy FileOutputStream
        String filename = "forecast.ser";
        try (FileOutputStream file = new FileOutputStream(filename)) {
            file.write(forecastser);
        }

        // Wczeœniej zapisany plik jest odczytywany za pomoc¹ klasy FileInputStream
        // Dane sa zdeserializowane do nowego obiektu WeatherForecast
        // a nastepnie wyswietlane w konsoli
        try (FileInputStream input = new FileInputStream(filename)) {
            WeatherForecast deserializedForecast = WeatherForecast.parseFrom(input);
            System.out.println("\nWczytane dane z pliku:");

            // Porownanie oryginalnego obiektu z obiektem po deserializacji
            boolean objectsEqual = forecast.equals(deserializedForecast);
            System.out.println("Obiekty sa sobie rowne po deserializacji: " + objectsEqual);

            for (DailyForecast dailyForecast : deserializedForecast.getDailyForecastsList()) {

                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(
                        dailyForecast.getDate().getSeconds(),
                        dailyForecast.getDate().getNanos(),
                        ZoneOffset.UTC
                );

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String formattedDate = dateTime.format(dateFormatter);

                System.out.println("\nData: " + formattedDate);
                for (DailyForecast.CityForecast cityForecast : dailyForecast.getCityForecastsList()) {
                    System.out.println("\n\tMiasto: " + cityForecast.getCityName() +
                            "\n\t\tTemperatura: " + cityForecast.getTemperature() +"°C" +
                            "\n\t\tWilgotnosc: " + cityForecast.getHumidity() + "%" +
                            "\n\t\tZachmurzenie: " + cityForecast.getCloudiness() + "%" +
                            "\n\t\tCisnienie atmosferyczne: " + cityForecast.getPressure() + " hPa");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
