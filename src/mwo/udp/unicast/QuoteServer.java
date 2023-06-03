package mwo.udp.unicast;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

import mwo.psr.proto.GradesRequestProtos.*;

/**
 * Klasa QuoteServer jest odpowiedzialna za uruchomienie serwera UDP,  który nasłuchuje na żądania od klientów
 * i odpowiada na nie zgodnie z logiką biznesową. W metodzie run, w pętli while serwer odbiera pakiety, parsuje
 * żądanie GradesRequest przesłane przez klienta, odczytuje nazwę studenta i nazwę przedmiotu z żądania. Następnie
 * serwer wywołuje metodę getStudentGradesForSubject, która pobiera oceny studenta dla danego przedmiotu.
 * Oceny są generowane losowo za pomocą metody getRandomGrades. Można dostosować tę logikę i odczytać oceny z
 * prawdziwej bazy danych lub innego źródła. Następnie serwer tworzy odpowiedź GradesResponse zawierającą listę ocen
 * i wysyła ją do klienta poprzez gniazdo DatagramSocket. Odpowiedź jest przesyłana na adres i port, z którego
 * otrzymano żądanie.
 */
public class QuoteServer {
    public static void main(String[] args) throws IOException {
        new QuoteServerThread().start();
    }
}

class QuoteServerThread extends Thread {

    protected DatagramSocket socket;

    public QuoteServerThread() throws IOException {
        this("QuoteServerThread");
    }

    public QuoteServerThread(String name) throws IOException {
        super(name);
        socket = new DatagramSocket(4445);
        System.out.println("Server started. Listening for requests...");
    }

    public void run() {

        while (true) {
            try {
                byte[] buf = new byte[256];

                // Receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
                GradesRequest request = GradesRequest.parseFrom(input);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                String studentName = request.getStudent().getName();
                String subject = request.getSubject().toString();

                System.out.println("Received packet from: " + address.getHostAddress() + "/" + port +
                        "\nStudent: " + studentName + "\nSubject: " + subject);

                List<Grade> grades = getStudentGradeForSubject(studentName, subject);
                GradesResponse response = GradesResponse.newBuilder()
                        .addAllGrades(grades)
                        .build();

                ByteArrayOutputStream output = new ByteArrayOutputStream();
                response.writeTo(output);
                byte[] responseData = output.toByteArray();

                packet = new DatagramPacket(responseData, responseData.length, address, port);
                socket.send(packet);
                System.out.println("Response sent.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Grade[] getRandomGrades(int num) {
        Grade[] result = new Grade[num];
        Grade[] grades = {Grade.NDST, Grade.DST, Grade.DB, Grade.BDB};
        for (int i = 0; i < result.length; i++) {
            int randomIndex = (int) (Math.random() * grades.length);
            result[i] = grades[randomIndex];
        }
        return result;
    }

    private List<Grade> getStudentGradeForSubject(String name, String subject) {
        Database database = Database.newBuilder()
                .putData("Jan Kowalski",
                        StudentData.newBuilder()
                                .putSubjects(Subject.JEZYK_POLSKI.toString(),
                                        SubjectGrades.newBuilder().addAllGrades(Arrays.asList(getRandomGrades(7)))
                                                .build())
                                .putSubjects(Subject.MATEMATYKA.toString(),
                                        SubjectGrades.newBuilder().addAllGrades(Arrays.asList(getRandomGrades(5)))
                                                .build())
                                .build()).build();

        StudentData studentData = database.getDataMap().get(name);
        if (studentData != null) {
            SubjectGrades subGrades = studentData.getSubjectsMap().get(subject);
            if (subGrades != null) {
                return subGrades.getGradesList();
            } else {
                System.out.println("Nie znaleziono ocen dla przedmiotu " + subject);
            }
        } else {
            System.out.println("Nie znaleziono studenta " + name + " w 'bazie danych'");
        }
        return Collections.emptyList();
    }
}



