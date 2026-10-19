import java.util.Scanner;

/* Version basica del sistema de trazabilidad. */
class CodigoBasico {
    public static void main(String[] args) {
        Scanner entrada = new Scanner(System.in);

        int cargasRegistradas = 0;
        int cargasAprobadas = 0;
        int cargasObservadas = 0;
        int correlativoPPA = 0;
        int correlativoPMA = 0;
        double pesoBrutoTotal = 0;
        String respuesta = "S";

        System.out.println("SISTEMA BASICO DE RECEPCION DE UVA");
        System.out.println("Ingrese los datos en la terminal. Use punto o coma para decimales.");

        while (respuesta.equalsIgnoreCase("S")) {
            System.out.println("\n===== NUEVA CARGA =====");

            System.out.print("Correlativo o codigo del camion: ");
            String camion = entrada.nextLine();

            System.out.print("Numero de guia: ");
            String guia = entrada.nextLine();

            System.out.print("Nombre del fundo: ");
            String fundo = entrada.nextLine();

            System.out.print("Variedad de uva: ");
            String varietal = entrada.nextLine();

            System.out.print("Codigo CLP: ");
            String clp = entrada.nextLine();

            System.out.print("Origen (Arequipa o Ica): ");
            String origen = entrada.nextLine();

            System.out.print("El CLP esta vigente? (S/N): ");
            String respuestaClp = entrada.nextLine();
            boolean clpVigente = respuestaClp.equalsIgnoreCase("S");

            System.out.print("Indice MTD: ");
            double mtd = Double.parseDouble(entrada.nextLine().replace(',', '.'));

            System.out.print("El resultado de LMR/inocuidad es conforme? (S/N): ");
            String respuestaLmr = entrada.nextLine();
            boolean lmrConforme = respuestaLmr.equalsIgnoreCase("S");

            System.out.print("Peso bruto recibido en kg: ");
            double pesoBruto = Double.parseDouble(entrada.nextLine().replace(',', '.'));

            cargasRegistradas++;
            pesoBrutoTotal = pesoBrutoTotal + pesoBruto;

            boolean mtdConforme = false;
            if (origen.equalsIgnoreCase("Arequipa") && mtd < 0.3) {
                mtdConforme = true;
            } else if (origen.equalsIgnoreCase("Ica") && mtd < 0.1) {
                mtdConforme = true;
            }

            if (clpVigente && mtdConforme && lmrConforme && !clp.isEmpty()) {
                System.out.println("Recepcion aprobada. Ingrese los datos del empaque.");

                System.out.print("Cantidad de cajas Jumbo: ");
                int cajasJumbo = Integer.parseInt(entrada.nextLine());

                System.out.print("Cantidad de cajas Extra Large: ");
                int cajasExtraLarge = Integer.parseInt(entrada.nextLine());

                System.out.print("Cantidad de cajas Large: ");
                int cajasLarge = Integer.parseInt(entrada.nextLine());

                int totalCajas = cajasJumbo + cajasExtraLarge + cajasLarge;
                int cajasFueraDeRango = 0;
                double pesoEmpacado = 0;

                for (int i = 1; i <= cajasJumbo; i++) {
                    System.out.print("Peso de caja Jumbo " + i + " (kg): ");
                    double peso = Double.parseDouble(entrada.nextLine().replace(',', '.'));
                    if (peso >= 8.2 && peso <= 8.364) {
                        pesoEmpacado = pesoEmpacado + peso;
                    } else {
                        cajasFueraDeRango++;
                    }
                }

                for (int i = 1; i <= cajasExtraLarge; i++) {
                    System.out.print("Peso de caja Extra Large " + i + " (kg): ");
                    double peso = Double.parseDouble(entrada.nextLine().replace(',', '.'));
                    if (peso >= 8.2 && peso <= 8.364) {
                        pesoEmpacado = pesoEmpacado + peso;
                    } else {
                        cajasFueraDeRango++;
                    }
                }

                for (int i = 1; i <= cajasLarge; i++) {
                    System.out.print("Peso de caja Large " + i + " (kg): ");
                    double peso = Double.parseDouble(entrada.nextLine().replace(',', '.'));
                    if (peso >= 8.2 && peso <= 8.364) {
                        pesoEmpacado = pesoEmpacado + peso;
                    } else {
                        cajasFueraDeRango++;
                    }
                }

                if (cajasFueraDeRango > 0) {
                    cargasObservadas++;
                    System.out.println("Estado: OBSERVADA. Hay cajas fuera del rango de 8.2 a 8.364 kg.");
                    System.out.println("Cajas revisadas: " + totalCajas);
                    System.out.println("Cajas fuera de rango: " + cajasFueraDeRango);
                } else if (totalCajas == 0) {
                    cargasObservadas++;
                    System.out.println("Estado: OBSERVADA. No se ingresaron cajas para empacar.");
                } else {
                    cargasAprobadas++;

                    int palletsJumbo = cajasJumbo / 114;
                    int palletsExtraLarge = cajasExtraLarge / 114;
                    int palletsLarge = cajasLarge / 114;

                    int remanentesJumbo = cajasJumbo % 114;
                    int remanentesExtraLarge = cajasExtraLarge % 114;
                    int remanentesLarge = cajasLarge % 114;
                    int totalRemanentes = remanentesJumbo + remanentesExtraLarge + remanentesLarge;
                    int palletsMixtos = totalRemanentes / 114;
                    int puchos = totalRemanentes % 114;

                    System.out.println("Estado: APROBADA PARA DESPACHO.");
                    System.out.println("Camion: " + camion + " | Guia: " + guia + " | Fundo: " + fundo);
                    System.out.println("Variedad: " + varietal + " | CLP: " + clp + " | MTD: " + mtd);
                    System.out.println("Cajas revisadas: " + totalCajas);
                    System.out.println("Peso empacado conforme: " + pesoEmpacado + " kg");

                    for (int i = 1; i <= palletsJumbo; i++) {
                        correlativoPPA++;
                        System.out.println("Etiqueta PPA-" + correlativoPPA + " | Calibre Jumbo | CLP " + clp);
                    }
                    for (int i = 1; i <= palletsExtraLarge; i++) {
                        correlativoPPA++;
                        System.out.println("Etiqueta PPA-" + correlativoPPA + " | Calibre Extra Large | CLP " + clp);
                    }
                    for (int i = 1; i <= palletsLarge; i++) {
                        correlativoPPA++;
                        System.out.println("Etiqueta PPA-" + correlativoPPA + " | Calibre Large | CLP " + clp);
                    }
                    for (int i = 1; i <= palletsMixtos; i++) {
                        correlativoPMA++;
                        System.out.println("Etiqueta PMA-" + correlativoPMA + " | Pallet mixto | CLP " + clp);
                    }

                    System.out.println("Pallets homogeneos (PPA): "
                            + (palletsJumbo + palletsExtraLarge + palletsLarge));
                    System.out.println("Pallets mixtos (PMA): " + palletsMixtos);
                    System.out.println("Cajas remanentes (puchos): " + puchos);
                    System.out.println("Peso bruto recibido: " + pesoBruto + " kg");
                }
            } else {
                cargasObservadas++;
                System.out.println("Estado: CUARENTENA/RECHAZO. No pasa al empaque.");
                if (!clpVigente || clp.isEmpty()) {
                    System.out.println("Revise el CLP: falta el codigo o no esta vigente.");
                }
                if (!mtdConforme) {
                    System.out.println("Revise el MTD y el origen. Arequipa debe ser menor que 0.3 e Ica menor que 0.1.");
                }
                if (!lmrConforme) {
                    System.out.println("Revise el resultado de LMR/inocuidad.");
                }
            }

            System.out.print("\nDesea registrar otra carga? (S/N): ");
            respuesta = entrada.nextLine();
        }

        System.out.println("\n===== RESUMEN DE ESTA EJECUCION =====");
        System.out.println("Cargas registradas: " + cargasRegistradas);
        System.out.println("Cargas aprobadas: " + cargasAprobadas);
        System.out.println("Cargas observadas/rechazadas: " + cargasObservadas);
        System.out.println("Peso bruto total recibido: " + pesoBrutoTotal + " kg");
        System.out.println("El resumen solo permanece mientras el programa esta abierto.");
        entrada.close();
    }
}
