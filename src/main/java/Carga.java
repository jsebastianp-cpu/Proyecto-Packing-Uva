import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Datos capturados para una carga de uva recibida en el packing. */
public class Carga {
    private final LocalDateTime fechaHoraIngreso;
    private final String camion;
    private final String guia;
    private final String fundo;
    private final String varietal;
    private final String clp;
    private final String origen;
    private final boolean clpVigente;
    private final double mtd;
    private final boolean lmrConforme;
    private final double pesoBrutoKg;
    private final Map<String, Integer> cajasPorCalibre;
    private final Map<String, List<Double>> pesosPorCalibre;

    public Carga(LocalDateTime fechaHoraIngreso, String camion, String guia,
                 String fundo, String varietal, String clp, String origen,
                 boolean clpVigente, double mtd, boolean lmrConforme,
                 double pesoBrutoKg, Map<String, Integer> cajasPorCalibre,
                 Map<String, List<Double>> pesosPorCalibre) {
        this.fechaHoraIngreso = fechaHoraIngreso;
        this.camion = camion;
        this.guia = guia;
        this.fundo = fundo;
        this.varietal = varietal;
        this.clp = clp;
        this.origen = origen;
        this.clpVigente = clpVigente;
        this.mtd = mtd;
        this.lmrConforme = lmrConforme;
        this.pesoBrutoKg = pesoBrutoKg;
        this.cajasPorCalibre = new LinkedHashMap<>(cajasPorCalibre);
        this.pesosPorCalibre = new LinkedHashMap<>();
        pesosPorCalibre.forEach((calibre, pesos) ->
                this.pesosPorCalibre.put(calibre, new ArrayList<>(pesos)));
    }

    public LocalDateTime getFechaHoraIngreso() { return fechaHoraIngreso; }
    public String getCamion() { return camion; }
    public String getGuia() { return guia; }
    public String getFundo() { return fundo; }
    public String getVarietal() { return varietal; }
    public String getClp() { return clp; }
    public String getOrigen() { return origen; }
    public boolean isClpVigente() { return clpVigente; }
    public double getMtd() { return mtd; }
    public boolean isLmrConforme() { return lmrConforme; }
    public double getPesoBrutoKg() { return pesoBrutoKg; }
    public Map<String, Integer> getCajasPorCalibre() {
        return new LinkedHashMap<>(cajasPorCalibre);
    }
    public Map<String, List<Double>> getPesosPorCalibre() {
        Map<String, List<Double>> copia = new LinkedHashMap<>();
        pesosPorCalibre.forEach((calibre, pesos) -> copia.put(calibre, new ArrayList<>(pesos)));
        return copia;
    }
}
