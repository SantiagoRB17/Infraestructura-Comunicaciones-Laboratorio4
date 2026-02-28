package Cliente;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class RFC_Cliente {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try (
                Socket socket = new Socket("localhost", 5000);
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {

            System.out.println("=================================");
            System.out.println("   CLIENTE CONVERSION NUMERICA");
            System.out.println("=================================");

            while (true) {

                mostrarMenu();
                String opcion = sc.nextLine();

                if (opcion.equals("0")) {
                    System.out.println("Cerrando cliente...");
                    break;
                }

                String mensaje = construirMensaje(opcion, sc);

                if (mensaje == null) {
                    System.out.println("Opción inválida.\n");
                    continue;
                }

                // Enviar al servidor
                salida.println(mensaje);

                // Recibir respuesta
                String respuesta = entrada.readLine();

                System.out.println("Resultado recibido: " + respuesta);
                System.out.println();
            }

        } catch (IOException e) {
            System.out.println("No se pudo conectar con el servidor.");
        }
    }

    private static void mostrarMenu() {
        System.out.println("1. Decimal -> Binario");
        System.out.println("2. Binario -> Decimal");
        System.out.println("3. Decimal -> Hexadecimal");
        System.out.println("4. Hexadecimal -> Decimal");
        System.out.println("5. Binario -> Hexadecimal");
        System.out.println("6. Hexadecimal -> Binario");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static String construirMensaje(String opcion, Scanner sc) {

        switch (opcion) {

            case "1":
                System.out.print("Ingrese numero decimal: ");
                String dec1 = sc.nextLine();
                System.out.print("Longitud en bits: ");
                String bits = sc.nextLine();
                return "1;" + dec1 + ";" + bits;

            case "2":
                System.out.print("Ingrese numero binario: ");
                String bin = sc.nextLine();
                return "2;" + bin;

            case "3":
                System.out.print("Ingrese numero decimal: ");
                String dec2 = sc.nextLine();
                System.out.print("Ancho en digitos hex: ");
                String ancho = sc.nextLine();
                return "3;" + dec2 + ";" + ancho;

            case "4":
                System.out.print("Ingrese numero hexadecimal: ");
                String hex = sc.nextLine();
                return "4;" + hex;

            case "5":
                System.out.print("Ingrese numero binario: ");
                String bin2 = sc.nextLine();
                System.out.print("Ancho en digitos hex: ");
                String anchoHex = sc.nextLine();
                return "5;" + bin2 + ";" + anchoHex;

            case "6":
                System.out.print("Ingrese numero hexadecimal: ");
                String hex2 = sc.nextLine();
                return "6;" + hex2;

            default:
                return null;
        }
    }
}

