import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Registro interactivo de múltiples cargas de uva durante una jornada. */
public class RecepcionPackingUva {
    private static final double PESO_NOMINAL_KG = 8.2;
    private static final double PESO_MAXIMO_KG = PESO_NOMINAL_KG * 1.02;
    private static final int CAJAS_POR_PALLET = 114;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static int correlativoPpa = 0;
    private static int correlativoPma = 0;

    private static final List<String> CALIBRES = List.of("Jumbo", "Extra Large", "Large");

    public static void main(String[] args) {
        List<ResultadoCarga> registrosDeSesion = new ArrayList<>();
        Path archivoDiario = archivoDiario();

        System.out.println("SISTEMA DE GESTIÓN Y TRAZABILIDAD PARA PACKING DE UVA");
        System.out.println("Los registros de hoy se guardarán en: " + archivoDiario.toAbsolutePath());

        try (Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8)) {
            boolean continuar = true;
            while (continuar) {
                System.out.printf("%n========== NUEVA CARGA #%d ==========%n", registrosDeSesion.size() + 1);
                DatosRecepcion datos = capturarRecepcion(entrada);
                ResultadoCarga resultado;

                String observacionSanitaria = validarRecepcion(datos);
                if (observacionSanitaria != null) {
                    resultado = new ResultadoCarga(datos.carga, observacionSanitaria, 0, 0, 0, 0, 0);
                    System.out.println("Resultado: " + observacionSanitaria);
                    System.out.println("No se registra producción porque la carga no está autorizada para empaque.");
                } else {
                    datos = capturarEmpaque(entrada, datos);
                    resultado = procesarEmpaque(datos.carga);
                }

                registrosDeSesion.add(resultado);
                mostrarResultado(resultado);
                try {
                    guardarRegistroDiario(archivoDiario, resultado);
                    System.out.println("Carga guardada en el registro diario.");
                } catch (IOException ex) {
                    System.out.println("No se pudo escribir el archivo diario: " + ex.getMessage());
                    System.out.println("El registro se conserva en el resumen de esta sesión.");
                }
                continuar = preguntarSiNo(entrada, "¿Desea registrar otra carga? (S/N): ");
            }
        }

        mostrarResumen(registrosDeSesion, archivoDiario);
    }

    private static DatosRecepcion capturarRecepcion(Scanner entrada) {
        System.out.println("Datos de recepción");
        String camion = leerTexto(entrada, "Correlativo o código del camión: ");
        String guia = leerTexto(entrada, "Número de guía de tránsito/remisión: ");
        String fundo = leerTexto(entrada, "Nombre del fundo o proveedor: ");
        String varietal = leerTexto(entrada, "Variedad de uva: ");
        String clp = leerTexto(entrada, "Código de Lugar de Producción (CLP): ");
        String origen = leerOrigen(entrada);
        boolean clpVigente = preguntarSiNo(entrada, "¿El CLP está vigente? (S/N): ");
        double mtd = leerDecimal(entrada, "Índice MTD registrado: ");
        boolean lmrConforme = preguntarSiNo(entrada, "¿El análisis de LMR/inocuidad es conforme? (S/N): ");
        double pesoBruto = leerDecimalPositivo(entrada, "Peso bruto recibido en kg: ");

        Carga carga = new Carga(LocalDateTime.now(), camion, guia, fundo, varietal, clp,
                origen, clpVigente, mtd, lmrConforme, pesoBruto,
                Map.of(), Map.of());
        return new DatosRecepcion(carga, new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    private static String validarRecepcion(DatosRecepcion datos) {
        Carga carga = datos.carga;
        if (carga.getClp().isBlank() || !carga.isClpVigente()) {
            return "CUARENTENA: CLP vacío o vencido.";
        }
        if (!validarMtd(carga.getOrigen(), carga.getMtd())) {
            return "CUARENTENA/RECHAZO: MTD fuera del límite para " + carga.getOrigen() + ".";
        }
        if (!carga.isLmrConforme()) {
            return "RECHAZADO: revisión de LMR/inocuidad no conforme.";
        }
        System.out.println("Recepción sanitaria aprobada. Ingrese los datos del empaque.");
        return null;
    }

    private static DatosRecepcion capturarEmpaque(Scanner entrada, DatosRecepcion datos) {
        System.out.println("Producción por calibre (ingrese 0 si no se produjo ese calibre)");
        int total = 0;
        for (String calibre : CALIBRES) {
            int cantidad = leerEnteroNoNegativo(entrada, "Cantidad de cajas " + calibre + ": ");
            datos.cajasPorCalibre.put(calibre, cantidad);
            total += cantidad;
        }
        while (total == 0) {
            System.out.println("La carga debe contener al menos una caja para procesar el empaque.");
            total = 0;
            for (String calibre : CALIBRES) {
                int cantidad = leerEnteroNoNegativo(entrada, "Cantidad de cajas " + calibre + ": ");
                datos.cajasPorCalibre.put(calibre, cantidad);
                total += cantidad;
            }
        }

        System.out.println("Ingrese el peso medido de cada caja. Rango permitido: 8.200 a 8.364 kg.");
        for (String calibre : CALIBRES) {
            int cantidad = datos.cajasPorCalibre.get(calibre);
            List<Double> pesos = new ArrayList<>();
            for (int i = 1; i <= cantidad; i++) {
                pesos.add(leerDecimalPositivo(entrada,
                        String.format("Peso caja %s %d/%d (kg): ", calibre, i, cantidad)));
            }
            datos.pesosPorCalibre.put(calibre, pesos);
        }

        Carga anterior = datos.carga;
        datos.carga = new Carga(anterior.getFechaHoraIngreso(), anterior.getCamion(), anterior.getGuia(),
                anterior.getFundo(), anterior.getVarietal(), anterior.getClp(), anterior.getOrigen(),
                anterior.isClpVigente(), anterior.getMtd(), anterior.isLmrConforme(),
                anterior.getPesoBrutoKg(), datos.cajasPorCalibre, datos.pesosPorCalibre);
        return datos;
    }

    private static ResultadoCarga procesarEmpaque(Carga carga) {
        int totalCajas = 0;
        int cajasFueraDeRango = 0;
        for (List<Double> pesos : carga.getPesosPorCalibre().values()) {
            totalCajas += pesos.size();
            for (double peso : pesos) {
                if (peso < PESO_NOMINAL_KG || peso > PESO_MAXIMO_KG) {
                    cajasFueraDeRango++;
                }
            }
        }
        if (cajasFueraDeRango > 0) {
            return new ResultadoCarga(carga, "OBSERVADO: cajas fuera del rango de peso", totalCajas,
                    cajasFueraDeRango, 0, 0, 0);
        }

        int remanentes = 0;
        int palletsPpa = 0;
        List<String> etiquetas = new ArrayList<>();
        for (Map.Entry<String, Integer> entrada : carga.getCajasPorCalibre().entrySet()) {
            int cantidad = entrada.getValue();
            int completos = cantidad / CAJAS_POR_PALLET;
            remanentes += cantidad % CAJAS_POR_PALLET;
            for (int i = 0; i < completos; i++) {
                correlativoPpa++;
                String etiqueta = crearEtiqueta("PPA", entrada.getKey(), correlativoPpa, carga);
                etiquetas.add(etiqueta);
                System.out.println(etiqueta);
                palletsPpa++;
            }
        }
        int palletsPma = remanentes / CAJAS_POR_PALLET;
        for (int i = 0; i < palletsPma; i++) {
            correlativoPma++;
            String etiqueta = crearEtiqueta("PMA", "Mixto", correlativoPma, carga);
            etiquetas.add(etiqueta);
            System.out.println(etiqueta);
        }
        int puchos = remanentes % CAJAS_POR_PALLET;
        return new ResultadoCarga(carga, "APROBADO PARA DESPACHO / CÁMARA DE FRÍO",
                totalCajas, 0, palletsPpa, palletsPma, puchos, etiquetas);
    }

    private static void mostrarResultado(ResultadoCarga resultado) {
        System.out.println("--- Resultado de la carga ---");
        System.out.printf("Camión %s | Guía %s | CLP %s | Fundo %s%n",
                resultado.carga.getCamion(), resultado.carga.getGuia(),
                resultado.carga.getClp(), resultado.carga.getFundo());
        System.out.println("Estado: " + resultado.estado);
        if (resultado.totalCajas > 0) {
            System.out.printf("Cajas: %d | Fuera de rango: %d | Pallets PPA: %d | Pallets PMA: %d | Puchos: %d%n",
                    resultado.totalCajas, resultado.cajasFueraDeRango,
                    resultado.palletsPpa, resultado.palletsPma, resultado.puchos);
        }
    }

    private static void mostrarResumen(List<ResultadoCarga> registros, Path archivoDiario) {
        System.out.println("\n========== RESUMEN DE LA SESIÓN ==========");
        int aprobadas = 0;
        int observadasORechazadas = 0;
        double pesoBrutoTotal = 0;
        for (ResultadoCarga resultado : registros) {
            System.out.printf("%s | Camión %s | Guía %s | %s%n",
                    resultado.carga.getFechaHoraIngreso().format(FECHA_HORA),
                    resultado.carga.getCamion(), resultado.carga.getGuia(), resultado.estado);
            pesoBrutoTotal += resultado.carga.getPesoBrutoKg();
            if (resultado.estado.startsWith("APROBADO")) aprobadas++;
            else observadasORechazadas++;
        }
        System.out.printf("Cargas registradas: %d | Aprobadas: %d | Observadas/rechazadas: %d | Peso bruto total: %.2f kg%n",
                registros.size(), aprobadas, observadasORechazadas, pesoBrutoTotal);
        System.out.println("Archivo diario: " + archivoDiario.toAbsolutePath());
    }

    private static boolean validarMtd(String origen, double mtd) {
        if (origen.equalsIgnoreCase("Arequipa")) return mtd < 0.3;
        if (origen.equalsIgnoreCase("Ica")) return mtd < 0.1;
        return false;
    }

    private static String crearEtiqueta(String tipo, String calibre, int numero, Carga carga) {
        return String.format("Etiqueta %s-%05d | Calibre: %s | CLP: %s | Camión: %s",
                tipo, numero, calibre, carga.getClp(), carga.getCamion());
    }

    private static Path archivoDiario() {
        return Path.of("registros", "cargas_" + LocalDate.now() + ".csv");
    }

    private static void guardarRegistroDiario(Path archivo, ResultadoCarga resultado) throws IOException {
        Files.createDirectories(archivo.getParent());
        boolean escribirCabecera = !Files.exists(archivo) || Files.size(archivo) == 0;
        StringBuilder fila = new StringBuilder();
        Carga carga = resultado.carga;
        agregarCampo(fila, carga.getFechaHoraIngreso().format(FECHA_HORA));
        agregarCampo(fila, carga.getCamion());
        agregarCampo(fila, carga.getGuia());
        agregarCampo(fila, carga.getFundo());
        agregarCampo(fila, carga.getVarietal());
        agregarCampo(fila, carga.getClp());
        agregarCampo(fila, carga.getOrigen());
        agregarCampo(fila, String.valueOf(carga.getMtd()));
        agregarCampo(fila, carga.isClpVigente() ? "Sí" : "No");
        agregarCampo(fila, carga.isLmrConforme() ? "Conforme" : "No conforme");
        agregarCampo(fila, String.valueOf(carga.getPesoBrutoKg()));
        agregarCampo(fila, String.valueOf(resultado.totalCajas));
        Map<String, Integer> cajas = carga.getCajasPorCalibre();
        agregarCampo(fila, String.valueOf(cajas.getOrDefault("Jumbo", 0)));
        agregarCampo(fila, String.valueOf(cajas.getOrDefault("Extra Large", 0)));
        agregarCampo(fila, String.valueOf(cajas.getOrDefault("Large", 0)));
        agregarCampo(fila, serializarPesos(carga.getPesosPorCalibre()));
        agregarCampo(fila, String.valueOf(resultado.cajasFueraDeRango));
        agregarCampo(fila, String.valueOf(resultado.palletsPpa));
        agregarCampo(fila, String.valueOf(resultado.palletsPma));
        agregarCampo(fila, String.valueOf(resultado.puchos));
        agregarCampo(fila, String.join(" | ", resultado.etiquetas));
        agregarCampo(fila, resultado.estado);
        fila.setLength(fila.length() - 1);

        String cabecera = "fecha_hora;camion;guia;fundo;varietal;clp;origen;mtd;clp_vigente;lmr;"
                + "peso_bruto_kg;cajas_total;cajas_jumbo;cajas_extra_large;cajas_large;pesos_por_calibre_kg;"
                + "fuera_rango;pallets_ppa;pallets_pma;puchos;etiquetas;estado" + System.lineSeparator();
        if (escribirCabecera) Files.writeString(archivo, cabecera, StandardCharsets.UTF_8);
        Files.writeString(archivo, fila + System.lineSeparator(), StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.APPEND);
    }

    private static String serializarPesos(Map<String, List<Double>> pesosPorCalibre) {
        List<String> detalles = new ArrayList<>();
        pesosPorCalibre.forEach((calibre, pesos) -> detalles.add(calibre + "=" +
                pesos.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","))));
        return String.join(" | ", detalles);
    }

    private static void agregarCampo(StringBuilder fila, String valor) {
        String seguro = valor.replace("\"", "\"\"");
        fila.append('"').append(seguro).append("\";");
    }

    private static String leerOrigen(Scanner entrada) {
        while (true) {
            System.out.print("Origen (1 = Arequipa, 2 = Ica): ");
            String opcion = entrada.nextLine().trim();
            if (opcion.equals("1")) return "Arequipa";
            if (opcion.equals("2")) return "Ica";
            System.out.println("Seleccione 1 o 2.");
        }
    }

    private static String leerTexto(Scanner entrada, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = entrada.nextLine().trim();
            if (!valor.isBlank()) return valor;
            System.out.println("El dato es obligatorio.");
        }
    }

    private static boolean preguntarSiNo(Scanner entrada, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String respuesta = entrada.nextLine().trim();
            if (respuesta.equalsIgnoreCase("s") || respuesta.equalsIgnoreCase("si")
                    || respuesta.equalsIgnoreCase("sí")) return true;
            if (respuesta.equalsIgnoreCase("n") || respuesta.equalsIgnoreCase("no")) return false;
            System.out.println("Responda S o N.");
        }
    }

    private static double leerDecimal(Scanner entrada, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = entrada.nextLine().trim().replace(',', '.');
            try {
                double numero = Double.parseDouble(valor);
                if (Double.isFinite(numero) && numero >= 0) return numero;
            } catch (NumberFormatException ignored) {
                // Se vuelve a solicitar el dato con un mensaje claro.
            }
            System.out.println("Ingrese un número válido igual o mayor que cero.");
        }
    }

    private static double leerDecimalPositivo(Scanner entrada, String mensaje) {
        while (true) {
            double valor = leerDecimal(entrada, mensaje);
            if (valor > 0) return valor;
            System.out.println("El peso debe ser mayor que cero.");
        }
    }

    private static int leerEnteroNoNegativo(Scanner entrada, String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String valor = entrada.nextLine().trim();
            try {
                int numero = Integer.parseInt(valor);
                if (numero >= 0) return numero;
            } catch (NumberFormatException ignored) {
                // Se vuelve a solicitar el dato con un mensaje claro.
            }
            System.out.println("Ingrese un número entero igual o mayor que cero.");
        }
    }

    private static class DatosRecepcion {
        private Carga carga;
        private final Map<String, Integer> cajasPorCalibre;
        private final Map<String, List<Double>> pesosPorCalibre;

        private DatosRecepcion(Carga carga, Map<String, Integer> cajasPorCalibre,
                               Map<String, List<Double>> pesosPorCalibre) {
            this.carga = carga;
            this.cajasPorCalibre = cajasPorCalibre;
            this.pesosPorCalibre = pesosPorCalibre;
        }
    }

    private static class ResultadoCarga {
        private final Carga carga;
        private final String estado;
        private final int totalCajas;
        private final int cajasFueraDeRango;
        private final int palletsPpa;
        private final int palletsPma;
        private final int puchos;
        private final List<String> etiquetas;

        private ResultadoCarga(Carga carga, String estado, int totalCajas,
                               int cajasFueraDeRango, int palletsPpa, int palletsPma, int puchos) {
            this(carga, estado, totalCajas, cajasFueraDeRango, palletsPpa, palletsPma, puchos, List.of());
        }

        private ResultadoCarga(Carga carga, String estado, int totalCajas,
                               int cajasFueraDeRango, int palletsPpa, int palletsPma,
                               int puchos, List<String> etiquetas) {
            this.carga = carga;
            this.estado = estado;
            this.totalCajas = totalCajas;
            this.cajasFueraDeRango = cajasFueraDeRango;
            this.palletsPpa = palletsPpa;
            this.palletsPma = palletsPma;
            this.puchos = puchos;
            this.etiquetas = new ArrayList<>(etiquetas);
        }
    }
}
