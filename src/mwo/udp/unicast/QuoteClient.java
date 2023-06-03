package mwo.udp.unicast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import mwo.psr.proto.GradesRequestProtos.*;

/**
 * Ta klasa reprezentuje klienta w komunikacji. Tworzy ona wiadomo�� GradesRequest z imieniem studenta i przedmiotem,
 * serializuje j� do tablicy bajt�w, a nast�pnie wysy�a j� do serwera za pomoc� DatagramSocket przez protok� UDP.
 * Nast�pnie oczekuje na otrzymanie pakietu z odpowiedzi� od serwera, parsuje odpowied� z powrotem do wiadomo�ci
 * GradesResponse i wy�wietla oceny na konsoli. Je�li odpowied� zawiera oceny, s� one wy�wietlane wraz z ich numerem.
 * Je�li odpowied� nie zawiera �adnych ocen, wy�wietlany jest odpowiedni komunikat.
 */
public class QuoteClient {
    public static void main(String[] args) throws IOException {

        GradesRequest request = GradesRequest.newBuilder()
                .setStudent(
                        Student.newBuilder()
                                .setName("Jan Kowalski"))
                .setSubject(Subject.JEZYK_POLSKI)
                .build();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        request.writeTo(output);
        byte[] buf1 = output.toByteArray();

        // Print data as hex values
        // for (byte b : buf1) {
        //    System.out.print(String.format("%02X", b));
        // }

        String studentName = request.getStudent().getName();
        String subject = String.valueOf(request.getSubject());
        InetAddress address = InetAddress.getByName("127.0.0.7");
        int port = 4445;

        DatagramSocket socket = new DatagramSocket();

        // Send request
        DatagramPacket packet = new DatagramPacket(buf1, buf1.length, address, port);
        System.out.println("Sending packet...");
        System.out.println("Get grades. Student: " + studentName + ", Subject: " + subject);
        socket.send(packet);
        System.out.println("Sent.");

        // Get response
        byte[] buf2 = new byte[256];
        packet = new DatagramPacket(buf2, buf2.length);
        System.out.println("Waiting for a response...");
        socket.receive(packet);

        ByteArrayInputStream input = new ByteArrayInputStream(packet.getData(), packet.getOffset(), packet.getLength());
        GradesResponse response = GradesResponse.parseFrom(input);

        // Display response
        System.out.println("Received response:\n");
        if (response.getGradesList().isEmpty()) {
            System.out.println("No grades found for the requested subject");
        } else {
            System.out.println("Grades: ");
            for (int i = 0; i < response.getGradesList().size(); i++) {
                Grade grade = response.getGradesList().get(i);
                System.out.println("Grade " + (i + 1) + ": " + grade);
            }
        }
        socket.close();
    }
}
