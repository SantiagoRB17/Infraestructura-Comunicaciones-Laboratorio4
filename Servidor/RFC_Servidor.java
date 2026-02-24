package Servidor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class RFC_Servidor {

    private static final int PUERTO = 5000;

    public static void main(String[] args) {
        iniciarServidor();
    }

    private static void iniciarServidor() {

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {

            System.out.println("=================================");
            System.out.println("  SERVIDOR CONVERSION NUMERICA");
            System.out.println("=================================");
            System.out.println("Servidor iniciado en puerto " + PUERTO);
            System.out.println("Esperando conexiones...\n");

            while (true) {

                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado desde: "
                        + socket.getInetAddress().getHostAddress());

                atenderCliente(socket);

                System.out.println("Cliente desconectado.\n");
            }

        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor.");
        }
    }

    private static void atenderCliente(Socket socket) {

        try (
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter salida = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {

            String mensaje;

            while ((mensaje = entrada.readLine()) != null) {

                System.out.println("Solicitud recibida: " + mensaje);

                String respuesta = procesarSolicitud(mensaje);

                salida.println(respuesta);

                System.out.println("Respuesta enviada: " + respuesta);
            }

        } catch (IOException e) {
            System.out.println("Error en la comunicación con el cliente.");
        }
    }

    private static String procesarSolicitud(String mensaje) {

        try {

            String[] protocolo = mensaje.split(";");

            int opcion = Integer.parseInt(protocolo[0]);

            switch (opcion) {

                case 1: // Decimal -> Binario
                    if (protocolo.length < 3) return "ERROR: Parametros insuficientes";

                    int decimal1 = Integer.parseInt(protocolo[1]);
                    int bits = Integer.parseInt(protocolo[2]);

                    String binario = Integer.toBinaryString(decimal1);

                    return String.format("%" + bits + "s", binario)
                            .replace(' ', '0');

                case 2: // Binario -> Decimal
                    return String.valueOf(
                            Integer.parseInt(protocolo[1], 2));

                case 3: // Decimal -> Hexadecimal
                    if (protocolo.length < 3) return "ERROR: Parametros insuficientes";

                    int decimal2 = Integer.parseInt(protocolo[1]);
                    int ancho = Integer.parseInt(protocolo[2]);

                    String hex = Integer.toHexString(decimal2).toUpperCase();

                    return String.format("%" + ancho + "s", hex)
                            .replace(' ', '0');

                case 4: // Hexadecimal -> Decimal
                    return String.valueOf(
                            Integer.parseInt(protocolo[1], 16));

                case 5: // Binario -> Hexadecimal
                    if (protocolo.length < 3) return "ERROR: Parametros insuficientes";

                    int decimal3 = Integer.parseInt(protocolo[1], 2);
                    int anchoHex = Integer.parseInt(protocolo[2]);

                    String hex2 = Integer.toHexString(decimal3).toUpperCase();

                    return String.format("%" + anchoHex + "s", hex2)
                            .replace(' ', '0');

                case 6: // Hexadecimal -> Binario
                    int decimal4 = Integer.parseInt(protocolo[1], 16);
                    return Integer.toBinaryString(decimal4);

                default:
                    return "ERROR: Operacion no valida";
            }

        } catch (Exception e) {
            return "ERROR: Formato invalido";
        }
    }
}