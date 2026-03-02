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

            int contador = 0;

            while (true) {

                Socket socket = serverSocket.accept();
                contador++;

                String ipCliente = socket.getInetAddress().getHostAddress();
                int puertoCliente = socket.getPort();

                System.out.println("Cliente #" + contador
                        + " conectado desde IP: " + ipCliente
                        + " | Puerto: " + puertoCliente);

                new Thread(() -> {
                    try {
                        socket.setSoTimeout(10000); // ⏱️ 10 segundos para el mensaje inicial

                        BufferedReader entrada = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        PrintWriter salida = new PrintWriter(
                                socket.getOutputStream(), true);

                        // 🟢 Handshake con timeout
                        boolean mensajeRecibido = atenderMensaje(
                                entrada, salida, socket, ipCliente, puertoCliente);

                        if (!mensajeRecibido) {
                            System.out.println("Cerrando conexión por falta de mensaje inicial -> IP: "
                                    + ipCliente + " | Puerto: " + puertoCliente);
                            socket.close();
                            return; // TERMINA el hilo aquí
                        }

                        // 🔓 Quitamos el timeout para la comunicación normal
                        socket.setSoTimeout(0); // 0 = sin límite de tiempo

                        // 🔵 Ahora sí: protocolo normal de conversiones
                        atenderCliente(entrada, salida, ipCliente, puertoCliente);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException ignored) {}

                        System.out.println("Cliente desconectado -> IP: "
                                + ipCliente + " | Puerto: " + puertoCliente);
                    }
                }).start();
            }

        } catch (IOException e) {
            System.out.println("Error al iniciar el servidor.");
        }
    }

    private static boolean atenderMensaje(BufferedReader entrada,
                                          PrintWriter salida,
                                          Socket socket,
                                          String ip,
                                          int puerto) {

        try {
            System.out.println("Esperando mensaje inicial del cliente -> IP: "
                    + ip + " | Puerto: " + puerto);

            String mensajeInicial = entrada.readLine();

            if (mensajeInicial == null) {
                System.out.println("Cliente no envió mensaje inicial.");
                return false;
            }

            System.out.println("Mensaje inicial recibido -> IP: "
                    + ip + " | Puerto: " + puerto);
            System.out.println("Contenido: " + mensajeInicial);

            salida.println("Mensaje inicial recibido correctamente.");
            return true;

        } catch (java.net.SocketTimeoutException e) {
            System.out.println("Tiempo de espera agotado (10s). Cliente no envió mensaje inicial -> IP: "
                    + ip + " | Puerto: " + puerto);
            return false;

        } catch (IOException e) {
            System.out.println("Error al recibir mensaje inicial: " + e.getMessage());
            return false;
        }
    }

    private static void atenderCliente(BufferedReader entrada,
                                       PrintWriter salida,
                                       String ip,
                                       int puerto) throws IOException {

        String mensaje;

        while ((mensaje = entrada.readLine()) != null) {

            System.out.println("Solicitud recibida -> IP: "
                    + ip + " | Puerto: " + puerto);
            System.out.println("Datos: " + mensaje);

            // 🔥 NUEVO: detectar protocolo multilínea
            if (mensaje.equals("7")) {

                System.out.println("Modo mensaje multilínea activado -> IP: "
                        + ip + " | Puerto: " + puerto);

                StringBuilder bloque = new StringBuilder();
                String linea;

                // Leer hasta que llegue "FIN"
                while ((linea = entrada.readLine()) != null) {

                    if (linea.equals("FIN")) {
                        break;
                    }

                    bloque.append(linea).append("\n");
                }

                salida.println("MENSAJE MULTILINEA RECIBIDO:");

                // Enviar línea por línea
                String[] lineas = bloque.toString().split("\n");
                for (String l : lineas) {
                    salida.println(l);
                }

                // Delimitador FINAL (CLAVE)
                salida.println("FIN_RESPUESTA");

                continue; // 🔥 vuelve al menú normal sin pasar por el parser
            }

            // 🔵 Protocolo normal de conversiones (intacto)
            String respuesta = procesarSolicitud(mensaje);

            System.out.println("Respuesta enviada: " + respuesta);
            salida.println(respuesta);
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