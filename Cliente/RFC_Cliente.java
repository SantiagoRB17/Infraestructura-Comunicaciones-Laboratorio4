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

            // 🔥 NUEVO: mensaje inmediato al conectarse
            System.out.print("Ingrese un mensaje inicial para el servidor (10 segundos): ");

            String mensajeInicial = leerConTimeout(sc, 10);

            // ⏱️ Si el usuario NO escribió nada en 10 segundos
            if (mensajeInicial == null || mensajeInicial.trim().isEmpty()) {
                System.out.println("\n⚠ Tiempo límite alcanzado (10 segundos).");
                System.out.println("Desconectado por inactividad.");
                socket.close();
                return; // Termina el cliente inmediatamente
            }

            // Si sí escribió, se envía normal
            salida.println(mensajeInicial);

            // 🔥 CLAVE: consumir la respuesta del servidor (handshake)
            String respuestaInicial = entrada.readLine();

            if (respuestaInicial == null) {
                System.out.println("El servidor cerró la conexión.");
                socket.close();
                return;
            }

            System.out.println("Servidor: " + respuestaInicial);
            System.out.println();

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

                try {
                    // Enviar al servidor
                    salida.println(mensaje);

                    // 🔹 Si es opción 7 → respuesta multilínea
                    if (opcion.equals("7")) {

                        System.out.println("\nResultado recibido:");

                        String lineaRespuesta;

                        while (true) {
                            lineaRespuesta = entrada.readLine();

                            if (lineaRespuesta == null) {
                                System.out.println("\n⚠ El servidor cerró la conexión.");
                                break;
                            }

                            // Delimitador de fin del bloque
                            if (lineaRespuesta.equals("FIN_RESPUESTA")) {
                                break;
                            }

                            System.out.println(lineaRespuesta);
                        }

                        System.out.println();

                    } else {

                        // 🔹 Respuesta normal (una sola línea)
                        String respuesta = entrada.readLine();

                        if (respuesta == null) {
                            System.out.println("\n⚠ Desconectado del servidor.");
                            break;
                        }

                        System.out.println("Resultado recibido: " + respuesta);
                        System.out.println();
                    }

                } catch (IOException e) {
                    System.out.println("\n⚠ Conexión cerrada por el servidor (posible inactividad).");
                    break;
                }
            }

        } catch (IOException e) {
            System.out.println("No se pudo conectar con el servidor.");
        }
    }

    private static String leerConTimeout(Scanner sc, int segundos) {
        final String[] resultado = new String[1];

        Thread hiloEntrada = new Thread(() -> {
            try {
                if (sc.hasNextLine()) {
                    resultado[0] = sc.nextLine();
                }
            } catch (Exception ignored) {}
        });

        hiloEntrada.setDaemon(true);
        hiloEntrada.start();

        try {
            hiloEntrada.join(segundos * 1000L); // Espera X segundos
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return resultado[0]; // Será null si no escribió nada a tiempo
    }

    private static void mostrarMenu() {
        System.out.println("1. Decimal -> Binario");
        System.out.println("2. Binario -> Decimal");
        System.out.println("3. Decimal -> Hexadecimal");
        System.out.println("4. Hexadecimal -> Decimal");
        System.out.println("5. Binario -> Hexadecimal");
        System.out.println("6. Hexadecimal -> Binario");
        System.out.println("7. Enviar mensaje multilínea");
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

            case "7":
                System.out.println("Ingrese su mensaje multilínea.");
                System.out.println("Escriba 'FIN' en una línea nueva para terminar:");

                StringBuilder mensajeMultilinea = new StringBuilder();
                String linea;

                while (true) {
                    linea = sc.nextLine();

                    if (linea.equals("FIN")) {
                        break;
                    }

                    mensajeMultilinea.append(linea).append("\n");
                }

                // Enviamos primero el comando 7 y luego el bloque completo
                return "7\n" + mensajeMultilinea.toString() + "FIN";

            default:
                return null;
        }
    }
}

